package quina.http.server;

import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.QuinaConfig;
import quina.QuinaService;
import quina.QuinaUtil;
import quina.exception.QuinaException;
import quina.http.HttpCustomAnalysisParams;
import quina.http.MimeTypes;
import quina.http.worker.HttpWorkerService;
import quina.net.nio.tcp.NioConstants;
import quina.net.nio.tcp.NioUtil;
import quina.net.nio.tcp.server.NioServerConstants;
import quina.net.nio.tcp.server.NioServerCore;
import quina.util.AtomicObject;
import quina.util.Flag;
import quina.util.collection.IndexMap;
import quina.util.collection.TypesClass;

/**
 * HttpServerサービス.
 */
public class HttpServerService implements QuinaService {
	// mimeConfigファイル名.
	private static final String MIME_CONFIG_FILE = "mime";
	
	// HttpServerコンフィグ定義.
	private QuinaConfig config = new QuinaConfig(
		// コンフィグ名.
		"httpServer"
		// ByteBufferサイズ.
		,"byteBufferLength", TypesClass.Integer, NioConstants.getByteBufferLength()
		// Socket送信バッファ長.
		,"sendBuffer", TypesClass.Integer, NioServerConstants.getSendBuffer()
		// Socket受信バッファ長
		,"recvBuffer", TypesClass.Integer, NioServerConstants.getRecvBuffer()
		// KeepAlive
		,"keepAlive", TypesClass.Boolean, NioServerConstants.isKeepAlive()
		// TcpNoDeley.
		,"tcpNoDeley", TypesClass.Boolean, NioServerConstants.isTcpNoDeley()
		// サーバーソケットBindポート.
		,"bindPort", TypesClass.Integer, HttpServerConstants.getBindServerSocketPort()
		// サーバーソケットBindアドレス.
		,"bindAddress", TypesClass.String, null
		// サーバーソケット最大接続数.
		,"backLog", TypesClass.Integer, NioServerConstants.getBacklog()
		// サーバーソケット受信バッファ長.
		,"serverRecvBuffer", TypesClass.Integer, NioServerConstants.getRecvBuffer()
	);
	
	// Nioサーバコア.
	private NioServerCore core = null;
	
	// HttpServerCall.
	private final AtomicObject<HttpServerCall> httpServerCall =
		new AtomicObject<HttpServerCall>();
	
	// カスタムなPostBody解析.
	private final AtomicObject<HttpCustomAnalysisParams> custom =
		new AtomicObject<HttpCustomAnalysisParams>();
	
	// MimeTypes.
	private final MimeTypes mimeTypes = MimeTypes.getInstance();

	// HttpWorkerService.
	private HttpWorkerService httpWorkerService = null;

	// サービス開始フラグ.
	private final Flag startFlag = new Flag(false);
	
	// Read-Writeロックオブジェクト.
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * コンストラクタ.
	 * @param httpWorkerService HttpWorkerServceを設定します.
	 */
	public HttpServerService(HttpWorkerService httpWorkerService) {
		this.httpWorkerService = httpWorkerService;
	}

	@Override
	public void loadConfig(String configDir) {
		lock.writeLock().lock();
		try {
			// コンフィグ情報を読み込む.
			config.loadConfig(configDir);
			// mimeTypeのコンフィグ読み込み.
			IndexMap<String, Object> json = QuinaUtil.loadJson(
				configDir, MIME_CONFIG_FILE);
			// jsonが取得できた場合.
			if(json != null) {
				mimeTypes.setMimeTypes(json);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public boolean isStartService() {
		return startFlag.get();
	}

	@Override
	public void startService() {
		// 一度起動している場合はエラー.
		if(startFlag.setToGetBefore(true)) {
			throw new QuinaException(this.getClass().getName() +
				" service has already started.");
		}
		lock.writeLock().lock();
		try {
			// HttpWorkerServiceが開始していない場合はエラー.
			if(!httpWorkerService.isStarted()) {
				throw new QuinaException("HttpWorkerService is not started.");
			}
			ServerSocketChannel server = null;
			try {
				// サーバーコール生成.
				HttpServerCall c = new HttpServerCall(getCustom(), getMimeTypes());
				// サーバーソケット作成.
				server = NioUtil.createServerSocketChannel(
					config.getString("bindAddress"), config.getInt("bindPort"),
					config.getInt("backLog"), config.getInt("serverRecvBuffer"));
				// サーバーコア生成.
				NioServerCore cr = new NioServerCore(config.getInt("byteBufferLength"),
					config.getInt("sendBuffer"),
					config.getInt("recvBuffer"), config.getBoolean("keepAlive"),
					config.getBool("tcpNoDeley"), server, c,
					httpWorkerService.getNioWorkerThreadManager());
				// サーバーコールを設定.
				this.httpServerCall.set(c);
				// サーバーコアを設定.
				this.core = cr;
				// サーバスレッド開始.
				cr.startThread();
			} catch(QuinaException qe) {
				stopService();
				if(server != null) {
					try {server.close();} catch(Exception ee) {}
				}
				throw qe;
			} catch(Exception e) {
				stopService();
				if(server != null) {
					try {server.close();} catch(Exception ee) {}
				}
				throw new QuinaException(e);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public boolean isStarted() {
		lock.readLock().lock();
		try {
			if(core != null) {
				return core.isStartupThread();
			}
			return false;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean awaitStartup(long timeout) {
		NioServerCore c = null;
		lock.readLock().lock();
		try {
			c = core;
		} finally {
			lock.readLock().unlock();
		}
		if(c != null) {
			return c.awaitStartup(timeout);
		}
		return true;
	}

	@Override
	public void stopService() {
		lock.writeLock().lock();
		try {
			// 停止処理.
			if(core != null) {
				core.stopThread();
			}
		} finally {
			lock.writeLock().unlock();
		}
		startFlag.set(false);
	}

	@Override
	public boolean isExit() {
		lock.readLock().lock();
		try {
			if(core != null) {
				return core.isExitThread();
			}
			return true;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean awaitExit(long timeout) {
		NioServerCore c = null;
		lock.readLock().lock();
		try {
			c = core;
		} finally {
			lock.readLock().unlock();
		}
		if(c != null) {
			if(c.awaitExit(timeout)) {
				lock.writeLock().lock();
				try {
					core = null;
				} finally {
					lock.writeLock().unlock();
				}
				return true;
			}
		}
		return true;
	}

	@Override
	public QuinaConfig getConfig() {
		lock.readLock().lock();
		try {
			return config;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * MimeTypeを取得.
	 * @return
	 */
	public MimeTypes getMimeTypes() {
		lock.readLock().lock();
		try {
			return mimeTypes;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * HttpServerCallを取得.
	 * @return
	 */
	public HttpServerCall getHttpServerCall() {
		return httpServerCall.get();
	}
	
	/**
	 * Httpリクエストのパラメータ解析カスアム処理を設定.
	 * @param custom
	 */
	public void setCustom(
		HttpCustomAnalysisParams custom) {
		lock.writeLock().lock();
		try {
			this.custom.set(custom);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * Httpリクエストのパラメータ解析カスタム処理を取得.
	 * @return
	 */
	public HttpCustomAnalysisParams getCustom() {
		lock.readLock().lock();
		try {
			return custom.get();
		} finally {
			lock.readLock().unlock();
		}
	}
}
