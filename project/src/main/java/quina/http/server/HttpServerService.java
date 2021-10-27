package quina.http.server;

import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.QuinaConfig;
import quina.QuinaService;
import quina.QuinaUtil;
import quina.exception.QuinaException;
import quina.http.HttpElement;
import quina.http.MimeTypes;
import quina.net.nio.tcp.NioConstants;
import quina.net.nio.tcp.NioTimeoutThread;
import quina.net.nio.tcp.NioUtil;
import quina.net.nio.tcp.server.NioServerConstants;
import quina.net.nio.tcp.server.NioServerCore;
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
		// 受信タイムアウト値.
		,"timeout", TypesClass.Long, NioConstants.getTimeout()
	);
	
	// Nioサーバコア.
	private NioServerCore core = null;
	
	// MimeTypes.
	private final MimeTypes mimeTypes = MimeTypes.getInstance();

	// QuinaWorkerService.
	private QuinaWorkerService quinaWorkerService = null;
	
	// NioTimeoutThread.
	private NioTimeoutThread nioTimeoutThread = null;
	
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
		nioTimeoutThread.offer(element);
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
				this.nioTimeoutThread = new NioTimeoutThread(
					config.getLong("timeout"),
					new HttpServerTimeoutHandler());
				// NioTimeoutThread管理用のスレッド開始.
				nioTimeoutThread.startThread();
				// サーバスレッド開始.
				cr.startThread();
			} catch(QuinaException qe) {
				stopService();
				nioTimeoutThread.stopThread();
				if(server != null) {
					try {server.close();} catch(Exception ee) {}
				}
				throw qe;
			} catch(Exception e) {
				stopService();
				nioTimeoutThread.stopThread();
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
			if(core != null &&
				nioTimeoutThread != null) {
				return core.isStartupThread() &&
					nioTimeoutThread.isStartupThread();
			}
			return false;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean awaitStartup(long timeout) {
		NioServerCore c = null;
		NioTimeoutThread et = null;
		lock.readLock().lock();
		try {
			c = core;
			et = nioTimeoutThread;
		} finally {
			lock.readLock().unlock();
		}
		boolean ret = true;
		if(c != null) {
			if(!c.awaitStartup(timeout)) {
				ret = false;
			}
		}
		if(et != null) {
			if(!et.awaitStartup(timeout)) {
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
			if(nioTimeoutThread != null) {
				nioTimeoutThread.stopThread();
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
			if(core != null &&
				nioTimeoutThread != null) {
				return core.isStopThread() &&
					nioTimeoutThread.isStopThread();
			}
			return true;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean awaitExit(long timeout) {
		NioServerCore c = null;
		NioTimeoutThread et = null;
		lock.readLock().lock();
		try {
			c = core;
			et = nioTimeoutThread;
		} finally {
			lock.readLock().unlock();
		}
		boolean ret = true;
		if(c != null) {
			if(!c.awaitExit(timeout)) {
				ret = false;
			}
		}
		if(et != null) {
			if(!et.awaitExit(timeout)) {
				ret = false;
			}
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
