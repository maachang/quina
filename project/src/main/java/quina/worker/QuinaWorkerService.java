package quina.worker;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.QuinaConfig;
import quina.QuinaService;
import quina.exception.QuinaException;
import quina.util.Flag;
import quina.util.collection.ObjectList;
import quina.util.collection.TypesClass;

/**
 * QuinaWorkerサービス.
 */
public class QuinaWorkerService
	implements QuinaService {
	// Quinaワーカーハンドラ.
	private QuinaWorkerHandler handle;
	// Quinaワーカー実行用要素群.
	private ObjectList<QuinaWorkerCallHandler> callHandles;
	// Quinaワーカーマネージャ.
	private QuinaWorkerManager manager;

	// コンフィグ定義.
	private final QuinaConfig config = new QuinaConfig(
		// コンフィグ名.
		"quinaWorker"
		// ワーカースレッド管理サイズ.
		,"workerLength", TypesClass.Integer,
			QuinaWorkerConstants.getWorkerLength()
	);

	// サービス開始フラグ.
	private final Flag startFlag = new Flag(false);
	
	// Read-Writeロックオブジェクト.
	private final ReentrantReadWriteLock lock =
		new ReentrantReadWriteLock();

	/**
	 * コンストラクタ.
	 */
	public QuinaWorkerService() {
	}
	
	/**
	 * ワーカーハンドラーを設定.
	 * @param handle 対象のワーカーハンドラーを設定します.
	 * @return QuinaWorkerService このオブジェクトが返却されます.
	 */
	public QuinaWorkerService setHandler(
		QuinaWorkerHandler handle) {
		lock.writeLock().lock();
		try {
			this.handle = handle;
		} finally {
			lock.writeLock().unlock();
		}
		return this;
	}
	
	/**
	 * 設定されているワーカーハンドラを取得.
	 * @return QuinaWorkerHandler ワーカーハンドラーが
	 *                            返却されます.
	 */
	public QuinaWorkerHandler getHandler() {
		lock.readLock().lock();
		try {
			return handle;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * ワーカーハンドラーが設定されてるか取得.
	 * @return boolean trueの場合設定されています.
	 */
	public boolean isHandler() {
		lock.readLock().lock();
		try {
			return handle != null;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * QuinaWorkerCallに対するハンドルを追加.
	 * @param handle 対象のQuinaWorkerCallHandlerを設定します.
	 * @return QuinaWorkerService このオブジェクトが返却されます.
	 */
	public QuinaWorkerService addCallHandle(
		QuinaWorkerCallHandler handle) {
		if(handle == null) {
			throw new QuinaException("The specified argument is null.");
		} else if(handle.targetId() == null) {
			throw new QuinaException(
				"The target call for the target handle " +
					handle.getClass().getName() + " is null.");
		}
		lock.writeLock().lock();
		try {
			if(callHandles == null) {
				callHandles =
					new ObjectList<QuinaWorkerCallHandler>();
			}
			callHandles.add(handle);
		} finally {
			lock.writeLock().unlock();
		}
		return this;
	}
	
	/**
	 * 登録されているQuinaWorkerCallHandler数を取得.
	 * @return int 登録されている数が返却されます.
	 */
	public int getCallHandleSize() {
		lock.readLock().lock();
		try {
			return callHandles == null ?
				0 : callHandles.size();
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * 番号を指定して登録されているQuinaWorkerCallHandlerを取得.
	 * @param no 対象の項番を設定します.
	 * @return QuinaWorkerCallHandler QuinaWorkerCallHandlerが
	 *                                返却されます.
	 */
	public QuinaWorkerCallHandler getCallHandle(int no) {
		lock.readLock().lock();
		try {
			if(callHandles == null || no < 0 ||
				no > callHandles.size()) {
				return null;
			}
			return callHandles.get(no);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * QuinaWorkerCallのターゲットIDを指定して
	 * QuinaWorkerCallHandlerを取得します.
	 * @param id QuinaWorkerCallのターゲットIDを指定します.
	 * @return QuinaWorkerCallHandler QuinaWorkerCallHandlerが
	 *                                返却されます.
	 */
	public QuinaWorkerCallHandler getCallHandleByTargetId(
		int id) {
		lock.readLock().lock();
		try {
			final int len = callHandles == null ?
				0 : callHandles.size();
			for(int i = 0; i < len; i ++) {
				if(callHandles.get(i).targetId() == id) {
					return callHandles.get(i);
				}
			}
		} finally {
			lock.readLock().unlock();
		}
		return null;
	}
	
	/**
	 * 対象のQuinaWorkerCallHandlerが登録されてるかチェック.
	 * @param handle チェック対象のハンドルを設定します.
	 * @return boolean trueの場合、存在します.
	 */
	public boolean isCallHandle(
		QuinaWorkerCallHandler handle) {
		if(handle == null) {
			throw new QuinaException(
				"The specified argument is null.");
		} else if(handle.targetId() == null) {
			throw new QuinaException(
				"The target element for the target handle " +
					handle.getClass().getName() + " is null.");
		}
		lock.readLock().lock();
		try {
			if(callHandles == null) {
				return false;
			}
			final int len = callHandles.size();
			for(int i = 0; i < len; i ++) {
				if(callHandles.get(i).targetId().equals(
					handle.targetId())) {
					return true;
				}
			}
		} finally {
			lock.readLock().unlock();
		}
		return false;
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
		// ワーカーハンドラが設定されていない場合.
		if(!isHandler()) {
			throw new QuinaException(
				"QuinaWorkerHandle is not set.");
		}
		// 既に開始してる場合はエラー.
		if(startFlag.setToGetBefore(true)) {
			throw new QuinaException(
				this.getClass().getName() +
				" service has already started.");
		}
		lock.writeLock().lock();
		try {
			// マネージャを生成して開始処理.
			this.manager = new QuinaWorkerManager(
				config.getInt("workerLength"), handle,
				toArrayCallHandle());
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

	// callHandles群を配列に変換.
	private final QuinaWorkerCallHandler[] toArrayCallHandle() {
		final int len = callHandles.size();
		QuinaWorkerCallHandler[] ret =
			new QuinaWorkerCallHandler[len];
		for(int i = 0; i < len; i ++) {
			ret[i] = callHandles.get(i);
		}
		return ret;
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
		QuinaWorkerManager m = null;
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
		QuinaWorkerManager m = null;
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
	 * ワーカー要素の追加.
	 * @param em Quinaワーカー要素を設定します.
	 */
	public void push(QuinaWorkerCall em) {
		// サービスが開始してない場合エラー.
		if(!isStartService()) {
			throw new QuinaException(
				"The service has not started.");
		}
		manager.push(em);
	}
}
