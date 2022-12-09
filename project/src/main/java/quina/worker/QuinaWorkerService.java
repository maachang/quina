package quina.worker;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.QuinaConfig;
import quina.QuinaService;
import quina.compile.cdi.annotation.AnnotationCdiConstants;
import quina.exception.QuinaException;
import quina.thread.QuinaBackgroundElement;
import quina.thread.QuinaBackgroundManager;
import quina.thread.QuinaBackgroundThread;
import quina.util.Flag;
import quina.util.collection.ObjectList;
import quina.util.collection.TypesClass;

/**
 * QuinaWorkerサービス.
 */
public class QuinaWorkerService
	implements QuinaService, QuinaBackgroundManager {
	// Quinaワーカーハンドラ.
	// 初期値はBlankハンドラ.
	private QuinaWorkerHandler handle = new QuinaBlankWorkerHandler();
	// Quinaワーカー実行用要素群.
	private ObjectList<QuinaWorkerCallHandler> callHandles;
	// Quinaワーカーマネージャ.
	private QuinaWorkerManager manager;
	// QuinaLoopスレッド.
	private QuinaBackgroundThread loopThread = new QuinaBackgroundThread();

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
		wlock();
		try {
			this.handle = handle;
		} finally {
			wulock();
		}
		return this;
	}
	
	/**
	 * 設定されているワーカーハンドラを取得.
	 * @return QuinaWorkerHandler ワーカーハンドラーが
	 *                            返却されます.
	 */
	public QuinaWorkerHandler getHandler() {
		rlock();
		try {
			return handle;
		} finally {
			rulock();
		}
	}
	
	/**
	 * ワーカーハンドラーが設定されてるか取得.
	 * @return boolean trueの場合設定されています.
	 */
	public boolean isHandler() {
		rlock();
		try {
			return handle != null;
		} finally {
			rulock();
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
		wlock();
		try {
			if(callHandles == null) {
				callHandles =
					new ObjectList<QuinaWorkerCallHandler>();
			}
			callHandles.add(handle);
		} finally {
			wulock();
		}
		return this;
	}
	
	/**
	 * 登録されているQuinaWorkerCallHandler数を取得.
	 * @return int 登録されている数が返却されます.
	 */
	public int getCallHandleSize() {
		rlock();
		try {
			return callHandles == null ?
				0 : callHandles.size();
		} finally {
			rulock();
		}
	}
	
	/**
	 * 番号を指定して登録されているQuinaWorkerCallHandlerを取得.
	 * @param no 対象の項番を設定します.
	 * @return QuinaWorkerCallHandler QuinaWorkerCallHandlerが
	 *                                返却されます.
	 */
	public QuinaWorkerCallHandler getCallHandle(int no) {
		rlock();
		try {
			if(callHandles == null || no < 0 ||
				no > callHandles.size()) {
				return null;
			}
			return callHandles.get(no);
		} finally {
			rulock();
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
		rlock();
		try {
			final int len = callHandles == null ?
				0 : callHandles.size();
			for(int i = 0; i < len; i ++) {
				if(callHandles.get(i).targetId() == id) {
					return callHandles.get(i);
				}
			}
		} finally {
			rulock();
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
		rlock();
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
			rulock();
		}
		return false;
	}
	
	/**
	 * ループ実行要素の登録.
	 * @param em ループ実行用の要素を設定します.
	 */
	public void regLoopElement(QuinaBackgroundElement em) {
		wlock();
		try {
			loopThread.regLoopElement(em);
		} finally {
			wulock();
		}
	}
	
	@Override
	public boolean loadConfig(String configDir) {
		wlock();
		try {
			// 既にサービスが開始している場合はエラー.
			checkService(true);
			return config.loadConfig(configDir);
		} finally {
			wulock();
		}
	}
	
	@Override
	public boolean isStartService() {
		return startFlag.get();
	}
	
	@Override
	public void startService() {
		wlock();
		try {
			// ワーカーハンドラが設定されていない場合.
			if(!isHandler()) {
				throw new QuinaException(
					"QuinaWorkerHandle is not set.");
			}
			// 既にサービスが開始している場合はエラー.
			checkService(true);
			// マネージャを生成して開始処理.
			this.manager = new QuinaWorkerManager(
				config.getInt("workerLength"), handle,
				toArrayCallHandle());
			this.manager.startThread();
			this.loopThread.startThread();
			// サービス開始.
			startFlag.set(true);
		} catch(QuinaException qe) {
			stopService();
			throw qe;
		} catch(Exception e) {
			stopService();
			throw new QuinaException(e);
		} finally {
			wulock();
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
	public ReadWriteLock getLock() {
		return lock;
	}
	
	@Override
	public boolean isStarted() {
		rlock();
		try {
			if(manager != null && loopThread != null) {
				if(manager != null) {
					if(!manager.isStartupThread()) {
						return false;
					}
				}
				if(loopThread != null) {
					if(!loopThread.isStartupThread()) {
						return false;
					}
				}
				return true;
			}
			return false;
		} finally {
			rulock();
		}
	}

	@Override
	public boolean awaitStartup(long timeout) {
		QuinaWorkerManager m = null;
		QuinaBackgroundThread lt;
		rlock();
		try {
			m = manager;
			lt = loopThread;
		} finally {
			rulock();
		}
		if(m != null) {
			if(lt != null) {
				if(m.awaitStartup(timeout)) {
					return lt.awaitStartup(timeout);
				}
				return false;
			}
			return m.awaitStartup(timeout);
		} else if(lt != null) {
			return lt.awaitStartup(timeout);
		}
		return true;
	}

	@Override
	public void stopService() {
		wlock();
		try {
			// 開始していない or 既に停止してる場合
			if(!startFlag.get()) {
				return;
			}
			// マネージャ停止処理.
			if(manager != null) {
				manager.stopThread();
			}
			// ループ監視停止処理.
			if(loopThread != null) {
				loopThread.stopThread();
			}
			// サービス停止完了.
			startFlag.set(false);
		} finally {
			wulock();
		}
	}

	@Override
	public boolean isExit() {
		rlock();
		try {
			if(manager != null) {
				if(!manager.isExitThread()) {
					return false;
				}
			}
			if(loopThread != null) {
				if(!loopThread.isExitThread()) {
					return false;
				}
			}
			return true;
		} finally {
			rulock();
		}
	}

	@Override
	public boolean awaitExit(long timeout) {
		QuinaWorkerManager m = null;
		QuinaBackgroundThread lt;
		rlock();
		try {
			m = manager;
			lt = loopThread;
		} finally {
			rulock();
		}
		if(m != null) {
			if(lt != null) {
				if(m.awaitExit(timeout)) {
					return lt.awaitExit(timeout);
				}
				return false;
			}
			return m.awaitExit(timeout);
		} else if(lt != null) {
			return lt.awaitExit(timeout);
		}
		return true;
	}

	@Override
	public QuinaConfig getConfig() {
		rlock();
		try {
			return config;
		} finally {
			rulock();
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
		rlock();
		try {
			if(manager == null) {
				throw new QuinaException(
					"The service has not started.");
			}
			manager.push(em);
		} finally {
			rulock();
		}
	}
	
	/**
	 * QuinaLoopScopedアノテーション自動読み込み実行用クラス名.
	 */
	public static final String AUTO_READ_QUINA_LOOP_ELEMENT_CLASS = "LoadQuinaLoopElement";

	/**
	 * QuinaLoopScopedアノテーション自動読み込み実行用メソッド名.
	 */
	public static final String AUTO_READ_QUINA_LOOP_ELEMENT_METHOD = "load";
	
	/**
	 * AutoQuinaLoopElement実行.
	 * @return void このオブジェクトが返却されます.
	 */
	public void autoQuinaLoopElement() {
		java.lang.Class<?> clazz;
		java.lang.reflect.Method method;
		try {
			// AutoRoute実行用のクラスを取得.
			clazz = Class.forName(
				AnnotationCdiConstants.CDI_PACKAGE_NAME + "." +
					AUTO_READ_QUINA_LOOP_ELEMENT_CLASS);
			// 実行メソッドを取得.
			method = clazz.getMethod(AUTO_READ_QUINA_LOOP_ELEMENT_METHOD);
		} catch(Exception e) {
			// クラスローディングやメソッド読み込みに失敗した場合は処理終了.
			return;
		}
		try {
			// Methodをstatic実行.
			method.invoke(null);
		} catch(InvocationTargetException it) {
			// メソッド実行でエラーの場合はエラー返却.
			throw new QuinaException(it.getCause());
		} catch(Exception e) {
			// メソッド実行でエラーの場合はエラー返却.
			throw new QuinaException(e);
		}
		return;
	}
}
