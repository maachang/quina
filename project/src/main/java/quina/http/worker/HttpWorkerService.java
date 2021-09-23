package quina.http.worker;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.QuinaConfig;
import quina.QuinaService;
import quina.exception.QuinaException;
import quina.net.nio.tcp.NioConstants;
import quina.net.nio.tcp.worker.NioWorkerConstants;
import quina.net.nio.tcp.worker.NioWorkerThreadManager;
import quina.util.Flag;
import quina.util.collection.TypesClass;

/**
 * Httpワーカースレッドサービス.
 */
public class HttpWorkerService implements QuinaService {
	// ワーカースレッドマネージャ.
	private NioWorkerThreadManager manager;

	// HttpWorkerコンフィグ定義.
	private final QuinaConfig config = new QuinaConfig(
		// コンフィグ名.
		"httpWorker"
		// ワーカースレッド管理サイズ.
		,"workerThreadLength", TypesClass.Integer,
			NioWorkerConstants.getWorkerThreadLength()
		
		// 受信テンポラリバッファサイズ.
		,"recvTmpBuffer", TypesClass.Integer,
			NioConstants.getByteBufferLength()
	);

	// サービス開始フラグ.
	private final Flag startFlag = new Flag(false);
	
	// Read-Writeロックオブジェクト.
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * コンストラクタ.
	 */
	public HttpWorkerService() {
	}

	@Override
	public boolean loadConfig(String configDir) {
		lock.writeLock().lock();
		try {
			return config.loadConfig(configDir);
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
		// 既に開始してる場合はエラー.
		if(startFlag.setToGetBefore(true)) {
			throw new QuinaException(this.getClass().getName() +
				" service has already started.");
		}
		lock.writeLock().lock();
		try {
			// HttpWorkerHandlerを生成.
			HttpWorkerHandler handler = new HttpWorkerHandler(
				config.getInt("recvTmpBuffer"));
			// マネージャを生成して開始処理.
			this.manager = new NioWorkerThreadManager(
				config.getInt("workerThreadLength"), handler);
			this.manager.startThread();
		} catch(QuinaException qe) {
			stopService();
			throw qe;
		} catch(Exception e) {
			stopService();
			throw new QuinaException(e);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public boolean isStarted() {
		lock.readLock().lock();
		try {
			if(manager != null) {
				return manager.isStartupThread();
			}
			return false;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean awaitStartup(long timeout) {
		NioWorkerThreadManager m = null;
		lock.readLock().lock();
		try {
			m = manager;
		} finally {
			lock.readLock().unlock();
		}
		if(m != null) {
			return m.awaitStartup(timeout);
		}
		return false;
	}

	@Override
	public void stopService() {
		lock.writeLock().lock();
		try {
			// 停止処理.
			if(manager != null) {
				manager.stopThread();
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
			if(manager != null) {
				return manager.isExitThread();
			}
			return true;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean awaitExit(long timeout) {
		NioWorkerThreadManager m = null;
		lock.readLock().lock();
		try {
			m = manager;
		} finally {
			lock.readLock().unlock();
		}
		if(m != null) {
			if(m.awaitExit(timeout)) {
				lock.writeLock().lock();
				try {
					manager = null;
				} finally {
					lock.writeLock().unlock();
				}
				return true;
			}
		}
		return false;
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
	 * 稼働中のワーカースレッドマネージャを取得.
	 * @return NioWorkerThreadManager ワーカースレッドマネージャが返却されます.
	 */
	public NioWorkerThreadManager getNioWorkerThreadManager() {
		checkService(false);
		lock.readLock().lock();
		try {
			return manager;
		} finally {
			lock.readLock().unlock();
		}
	}
}
