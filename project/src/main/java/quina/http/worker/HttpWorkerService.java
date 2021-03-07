package quina.http.worker;

import quina.QuinaException;
import quina.QuinaInfo;
import quina.QuinaService;
import quina.net.nio.tcp.worker.NioWorkerPoolingManager;
import quina.net.nio.tcp.worker.NioWorkerThreadManager;
import quina.util.Flag;

/**
 * Httpワーカースレッドサービス.
 */
public class HttpWorkerService implements QuinaService {
	// ワーカースレッドマネージャ.
	private NioWorkerThreadManager manager;

	// サーバープーリングマネージャ.
	private NioWorkerPoolingManager serverPoolingManager;

	// クライアントプーリングマネージャ.
	private NioWorkerPoolingManager clientPoolingManager;

	// NioWorker定義.
	private final HttpWorkerInfo info = new HttpWorkerInfo();

	// サービス開始フラグ.
	private final Flag startFlag = new Flag(false);

	/**
	 * コンストラクタ.
	 */
	public HttpWorkerService() {
	}

	@Override
	public synchronized void readConfig(String configDir) {
		info.readConfig(configDir);
	}

	@Override
	public void check(boolean flg) {
		if(startFlag.get() == flg) {
			if(flg) {
				throw new QuinaException(this.getClass().getName() + " has already started.");
			}
			throw new QuinaException(this.getClass().getName() + " is already stopped.");
		}
	}

	@Override
	public synchronized boolean isStartService() {
		return startFlag.get();
	}

	@Override
	public synchronized void startService() {
		if(startFlag.setToGetBefore(true)) {
			throw new QuinaException(this.getClass().getName() + " has already started.");
		}
		try {
			// プーリングマネージャサイズを生成.
			this.serverPoolingManager = new NioWorkerPoolingManager(
				info.getServerPoolingManagerLength());
			this.clientPoolingManager = new NioWorkerPoolingManager(
				info.getClientPoolingManagerLength());
			// HttpWorkerHandlerを生成.
			HttpWorkerHandler handler = new HttpWorkerHandler(info.getRecvTmpBuffer(),
				serverPoolingManager, clientPoolingManager);
			// マネージャを生成して開始処理.
			this.manager = new NioWorkerThreadManager(
				info.getWorkerThreadLength(), handler);
			this.manager.startThread();
		} catch(QuinaException qe) {
			stopService();
			throw qe;
		} catch(Exception e) {
			stopService();
			throw new QuinaException(e);
		}
	}

	@Override
	public synchronized boolean isStarted() {
		if(manager != null) {
			return manager.isStartupThread();
		}
		return false;
	}

	@Override
	public synchronized boolean waitToStartup(long timeout) {
		if(manager != null) {
			return manager.waitToStartup(timeout);
		}
		return false;
	}

	@Override
	public synchronized void stopService() {
		// 停止処理.
		if(manager != null) {
			manager.stopThread();
			manager = null;
		}
		if(serverPoolingManager != null) {
			serverPoolingManager.clear();
			serverPoolingManager = null;
		}
		if(clientPoolingManager != null) {
			clientPoolingManager.clear();
			clientPoolingManager = null;
		}
		startFlag.set(false);
	}

	@Override
	public synchronized boolean isExit() {
		if(manager != null) {
			return manager.isExitThread();
		}
		return true;
	}

	@Override
	public synchronized boolean waitToExit(long timeout) {
		if(manager != null) {
			return manager.waitToExit(timeout);
		}
		return false;
	}

	@Override
	public synchronized QuinaInfo getInfo() {
		return info;
	}

	/**
	 * 稼働中のワーカースレッドマネージャを取得.
	 * @return NioWorkerThreadManager ワーカースレッドマネージャが返却されます.
	 */
	public synchronized NioWorkerThreadManager getNioWorkerThreadManager() {
		check(false);
		return manager;
	}

	/**
	 * サーバープーリングマネージャを取得.
	 * @return NioWorkerPoolingManager サーバープーリングマネージャが返却されます.
	 */
	public synchronized NioWorkerPoolingManager getServerPoolingManager() {
		return serverPoolingManager;
	}

	/**
	 * クライアントプーリングマネージャを取得.
	 * @return NioWorkerPoolingManager クライアントプーリングマネージャが返却されます.
	 */
	public synchronized NioWorkerPoolingManager getClientPoolingManager() {
		return clientPoolingManager;
	}
}
