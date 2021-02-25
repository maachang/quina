package quina.http.worker;

import quina.QuinaException;
import quina.QuinaInfo;
import quina.QuinaService;
import quina.net.nio.tcp.worker.NioWorkerThreadManager;
import quina.util.Flag;

/**
 * Httpワーカースレッドサービス.
 */
public class HttpWorkerService implements QuinaService {
	// ワーカースレッドマネージャ.
	private NioWorkerThreadManager manager;

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
			manager = new NioWorkerThreadManager(
				info.getWorkerThreadLength(), info.getWorkerThreadHandler());
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
		}
	}

	@Override
	public synchronized boolean isExit() {
		if(manager != null) {
			return manager.isExitThread();
		}
		return false;
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
}