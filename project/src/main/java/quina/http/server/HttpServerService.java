package quina.http.server;

import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.Quina;
import quina.QuinaConfig;
import quina.QuinaService;
import quina.QuinaUtil;
import quina.exception.QuinaException;
import quina.http.HttpElement;
import quina.http.MimeTypes;
import quina.net.nio.tcp.NioConstants;
import quina.net.nio.tcp.NioUtil;
import quina.net.nio.tcp.server.NioServerConstants;
import quina.net.nio.tcp.server.NioServerCore;
import quina.util.Flag;
import quina.util.collection.QuinaMap;
import quina.util.collection.TypesClass;
import quina.worker.QuinaWorkerConstants;
import quina.worker.QuinaWorkerService;
import quina.worker.timeout.TimeoutLoopElement;

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
		// 受信タイムアウト値.
		,"timeout", TypesClass.Long, NioConstants.getTimeout()
		// 受信タイムアウト監視移行時間.
		,"doubtTime", TypesClass.Long, NioConstants.getDoubtTime()
		// ４０４エラーのレスポンスタイプ.
		,"error404RESTful", TypesClass.Boolean, HttpServerConstants.isError404RESTful()
	);
	
	// Nioサーバコア.
	private NioServerCore core = null;
	
	// QuinaWorkerService.
	private QuinaWorkerService quinaWorkerService = null;
	
	// TimeoutLoopElement.
	private TimeoutLoopElement timeoutLoopElement = null;
	
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
	
	/**
	 * タイムアウトを監視する要素を監視スレッドに登録.
	 * @param element HttpElementを設定.
	 */
	protected void pushTimeoutElement(HttpElement element) {
		timeoutLoopElement.offer(element);
	}

	@Override
	public boolean loadConfig(String configDir) {
		// サービスが開始している場合はエラー.
		if(startFlag.get()) {
			throw new QuinaException(
				"The service has already started.");
		}
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
			QuinaMap<String, Object> json = QuinaUtil.loadJson(
				configDir, MIME_CONFIG_FILE);
			// jsonが取得できた場合.
			if(json != null) {
				if(MimeTypes.getInstance().setMimeTypes(json)) {
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
				HttpServerNioCall c = new HttpServerNioCall(this);
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
				// サーバーコアを設定.
				this.core = cr;
				// NioTimeoutThreadを生成.
				this.timeoutLoopElement = new TimeoutLoopElement(
					config.getLong("timeout"),
					config.getLong("doubtTime"),
					new HttpServerTimeoutHandler());
				// nioTimeoutLoopElementをQuinaLoopThreadに登録.
				Quina.get().getQuinaLoopManager().regLoopElement(timeoutLoopElement);
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
		boolean ret = true;
		if(c != null) {
			if(!c.awaitStartup(timeout)) {
				ret = false;
			}
		}
		return ret;
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
				return core.isStopThread();
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
		boolean ret = true;
		if(c != null && !c.awaitExit(timeout)) {
			ret = false;
		}
		return ret;
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
}
