package quina.http.server;

import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.QuinaConfig;
import quina.QuinaService;
import quina.QuinaUtil;
import quina.exception.QuinaException;
import quina.http.MimeTypes;
import quina.net.nio.tcp.NioConstants;
import quina.net.nio.tcp.NioUtil;
import quina.net.nio.tcp.server.NioServerConstants;
import quina.net.nio.tcp.server.NioServerCore;
import quina.util.AtomicObject;
import quina.util.Flag;
import quina.util.collection.IndexMap;
import quina.util.collection.TypesClass;
import quina.worker.QuinaWorkerConstants;
import quina.worker.QuinaWorkerService;

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
		// 受信テンポラリバッファサイズ.
		,"recvTmpBuffer", TypesClass.Integer, NioConstants.getByteBufferLength()
	);
	
	// Nioサーバコア.
	private NioServerCore core = null;
	
	// HttpServerCall.
	private final AtomicObject<HttpServerNioCall> httpServerCall =
		new AtomicObject<HttpServerNioCall>();
	
	// MimeTypes.
	private final MimeTypes mimeTypes = MimeTypes.getInstance();

	// QuinaWorkerService.
	private QuinaWorkerService quinaWorkerService = null;

	// サービス開始フラグ.
	private final Flag startFlag = new Flag(false);
	
	// Read-Writeロックオブジェクト.
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * コンストラクタ.
	 * @param QuinaWorkerService QuinaWorkerServiceを設定します.
	 */
	public HttpServerService(QuinaWorkerService quinaWorkerService) {
		this.quinaWorkerService = quinaWorkerService;
	}

	@Override
	public boolean loadConfig(String configDir) {
		// HttpServerWorkerCallHandlerを取得.
		HttpServerWorkerCallHandler hnd =
			(HttpServerWorkerCallHandler)quinaWorkerService
				.getCallHandleByTargetId(
					QuinaWorkerConstants.HTTP_SERVER_WORKER_CALL_ID);
		// 対象ハンドルが存在しない場合.
		if(hnd == null) {
			throw new QuinaException(
				"HttpServerWorkerCallHandler is not set in " +
				"QuinaWorkerService.");
		}
		boolean ret = false;
		lock.writeLock().lock();
		try {
			// コンフィグ情報を読み込む.
			ret = config.loadConfig(configDir);
			// mimeTypeのコンフィグ読み込み.
			IndexMap<String, Object> json = QuinaUtil.loadJson(
				configDir, MIME_CONFIG_FILE);
			// jsonが取得できた場合.
			if(json != null) {
				if(mimeTypes.setMimeTypes(json)) {
					ret = true;
				}
			}
			// 対象ハンドルにコンフィグのテンポラリバイナリサイズを
			// 登録する.
			hnd.setTmpBinaryLength(config.getInt("recvTmpBuffer"));
		} finally {
			lock.writeLock().unlock();
		}
		return ret;
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
			// QuinaWorkerServiceが開始していない場合はエラー.
			if(!quinaWorkerService.isStarted()) {
				throw new QuinaException("HttpWorkerService is not started.");
			}
			ServerSocketChannel server = null;
			try {
				// サーバーコール生成.
				HttpServerNioCall c = new HttpServerNioCall();
				// サーバーソケット作成.
				server = NioUtil.createServerSocketChannel(
					config.getString("bindAddress"), config.getInt("bindPort"),
					config.getInt("backLog"), config.getInt("serverRecvBuffer"));
				// サーバーコア生成.
				NioServerCore cr = new NioServerCore(config.getInt("byteBufferLength"),
					config.getInt("sendBuffer"),
					config.getInt("recvBuffer"), config.getBoolean("keepAlive"),
					config.getBool("tcpNoDeley"), server, c,
					quinaWorkerService);
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
}
