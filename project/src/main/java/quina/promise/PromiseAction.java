package quina.promise;

import quina.Quina;
import quina.QuinaException;
import quina.http.AbstractHttpSendFurnishing;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.AbstractResponse;
import quina.net.nio.tcp.Wait;
import quina.util.AtomicNumber64;
import quina.util.AtomicObject;
import quina.util.Flag;
import quina.util.collection.ObjectList;

/**
 * Promiseアクション.
 */
public class PromiseAction
	extends AbstractHttpSendFurnishing<PromiseAction> {
	// Promise実行ワーカーリスト.
	private ObjectList<PromiseWorkerElement> list =
		new ObjectList<PromiseWorkerElement>();

	// 初期実行パラメータ.
	private Object initParam;

	// 終了コール.
	private PromiseFromEndCall endCall;

	// 処理終了時にwaitObject.signalAll()呼び出しをする場合はtrue.
	private boolean waitAllFlag = false;

	// waitオブジェクト.
	private Wait waitObject = null;

	// resolveやreject呼び出し回数管理.
	private final AtomicNumber64 resolveRejectCounter =
		new AtomicNumber64(0L);

	// 前回実行されたワーカー要素.
	private final AtomicObject<PromiseWorkerElement> before =
		new AtomicObject<PromiseWorkerElement>();

	// promiseステータス.
	private final AtomicObject<PromiseStatus> status =
		new AtomicObject<PromiseStatus>(PromiseStatus.Pending);

	// waitToで返却される情報.
	private final AtomicObject<Object> lastValue =
		new AtomicObject<Object>(null);

	// waitToで返却される情報が設定されたかフラグ設定.
	private final Flag lastValueFlag = new Flag(false);

	// promise開始フラグ.
	private final Flag startPromiseFlag = new Flag(false);

	// promise終了フラグ.
	private final Flag exitPromiseFlag = new Flag(false);

	/**
	 * コンストラクタ.
	 * @param param 初期実行パラメータを設定します.
	 */
	protected PromiseAction(Object param) {
		this.initParam = param;
	}

	// Promiseが開始されていない場合はエラー処理.
	private final void checkStartPromise() {
		// Promiseが開始していない場合エラー.
		if(!startPromiseFlag.get()) {
			throw new QuinaException("Promise has not started.");
		}
	}

	// Promiseが開始されている場合はエラー.
	private final void checkNotStartPromise() {
		// Promiseが開始している場合エラー.
		if(startPromiseFlag.get()) {
			throw new QuinaException("Promise has already started.");
		}
	}

	/**
	 * 正常系実行処理を設定します.
	 * resolveが呼び出された時に呼び出されます.
	 * @param call 実行処理を設定します.
	 */
	protected void then(PromiseCall call) {
		if(call == null) {
			throw new QuinaException(
				"The call object for normal execution has not been set.");
		}
		checkNotStartPromise();
		list.add(new PromiseWorkerElement(this, list.size(),
			PromiseWorkerElement.MODE_THEN, call));
	}

	/**
	 * 異常系実行処理を設定します.
	 * rejectが呼び出された時に呼び出します.
	 * @param call 実行処理を設定します.
	 */
	protected void error(PromiseCall call) {
		if(call == null) {
			throw new QuinaException(
				"The call object for executing the abnormal system has not been set.");
		}
		checkNotStartPromise();
		list.add(new PromiseWorkerElement(this, list.size(),
			PromiseWorkerElement.MODE_ERROR, call));
	}

	/**
	 * 正常系、異常系に関係なく呼び出される処理を設定します.
	 * @param call 実行処理を設定します.
	 */
	protected void allways(PromiseCall call) {
		if(call == null) {
			throw new QuinaException("The call object for execution has not been set.");
		}
		checkNotStartPromise();
		list.add(new PromiseWorkerElement(this, list.size(),
			PromiseWorkerElement.MODE_ALLWAYS, call));
	}

	/**
	 * 最後に必ず呼び出される処理を設定します.
	 * @param call Promise終了呼び出しコールを設定します.
	 */
	protected void finalyTo(PromiseFromEndCall call) {
		if(call == null) {
			throw new QuinaException("The call object for execution has not been set.");
		}
		checkNotStartPromise();
		endCall = call;
	}

	/**
	 * Promiseが開始しているか取得.
	 * @return boolean trueの場合は開始しています.
	 */
	protected boolean isStartPromise() {
		return startPromiseFlag.get();
	}

	/**
	 * Promiseが終了しているか取得.
	 * @return boolean trueの場合は終了しています.
	 */
	protected boolean isExitPromise() {
		return exitPromiseFlag.get();
	}

	// 最終非同期ステータスをセット.
	protected void setLastStatus(PromiseStatus state) {
		// 指定されたステータスを設定.
		status.set(state);
	}

	// 最終非同期処理のステータスを抽出して設定します.
	protected boolean setLastStatusByLastPromise() {
		PromiseWorkerElement em = before.get();
		if(em != null) {
			if((em.getCallMode() & PromiseWorkerElement.MODE_THEN) != 0) {
				// success.
				status.set(PromiseStatus.Fulfilled);
				return true;
			} else {
				// reject.
				status.set(PromiseStatus.Rejected);
				return true;
			}
		}
		return false;
	}

	// promiseの実行最終処理を定義して終了.
	protected void exitPromise() {
		exitPromise(null);
	}

	// promiseの終了処理.
	protected void exitPromise(PromiseStatus state) {
		if(!exitPromiseFlag.setToGetBefore(true)) {
			// ステータスを設定.
			if(state == null) {
				setLastStatusByLastPromise();
			} else {
				setLastStatus(state);
			}
			// 最終呼び出し処理が存在する場合は実行.
			if(endCall != null) {
				try {
					endCall.call(this);
				} catch(Exception e) {
					// エラーは無視.
				}
			}
			// waitToの解除.
			if(waitAllFlag) {
				// waitAllFlagが[true]の場合はsignalAll呼び出し.
				waitObject.signalAll();
			} else {
				// waitAllFlagが[false]の場合はsignal呼び出し.
				waitObject.signal();
			}
		}
	}

	// 送信済みの場合はlastValueに設定して終了処理.
	protected void setLastValue(Object value) {
		lastValue.set(value);
		lastValueFlag.set(true);
	}

	/**
	 * 次の正常処理を実行します.
	 * @param no 対象の項番を設定します.
	 * @param value 実行引数を設定します.
	 * @return boolean [true]の場合次の実行処理が登録できました.
	 */
	protected boolean resolve(int no, Object value) {
		checkStartPromise();
		// resolveが呼び出された.
		resolveRejectCounter.inc();
		// 送信処理が行われている場合.
		if(isExitPromise()) {
			// 送信済みの場合はlastValueに設定して終了処理.
			setLastValue(value);
			// success返却.
			exitPromise(PromiseStatus.Fulfilled);
			return false;
		}
		// 利用可能なthen()追加の実行処理を取得.
		PromiseWorkerElement em;
		final int len = list.size();
		for(int i = no; i < len; i ++) {
			em = list.get(i);
			if((em.getCallMode() & PromiseWorkerElement.MODE_THEN) != 0) {
				em.setParam(value);
				before.set(em);
				// successステータス設定.
				status.set(PromiseStatus.Fulfilled);
				Quina.get().registerWorker(em);
				return true;
			}
		}
		// promiseの終了の場合はlastValueに設定して終了処理
		setLastValue(value);
		// success返却.
		exitPromise(PromiseStatus.Fulfilled);
		return false;
	}

	/**
	 * 次の異常系処理を実行します.
	 * @param no 対象の項番を設定します.
	 * @param value 実行引数を設定します.
	 * @return boolean [true]の場合次の実行処理が登録できました.
	 */
	protected boolean reject(int no, Object value) {
		checkStartPromise();
		// rejectが呼び出された.
		resolveRejectCounter.inc();
		// 送信処理が行われている場合.
		if(isExitPromise()) {
			// 送信済みの場合はlastValueに設定して終了処理
			setLastValue(value);
			// reject.
			exitPromise(PromiseStatus.Rejected);
			return false;
		}
		// 利用可能なerror()追加の実行処理を取得.
		PromiseWorkerElement em;
		final int len = list.size();
		for(int i = no; i < len; i ++) {
			em = list.get(i);
			if((em.getCallMode() & PromiseWorkerElement.MODE_ERROR) != 0) {
				em.setParam(value);
				before.set(em);
				// reject.
				status.set(PromiseStatus.Rejected);
				Quina.get().registerWorker(em);
				return true;
			}
		}
		// promiseの終了の場合はlastValueに設定して終了処理.
		setLastValue(value);
		// reject.
		exitPromise(PromiseStatus.Rejected);
		return false;
	}

	/**
	 * 次の正常処理を実行します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction resolve() {
		return resolve(null);
	}

	/**
	 * 次の正常処理を実行します.
	 * この処理を呼び出すと次のthen()やallways()を実行します.
	 * @param value 実行引数を設定します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction resolve(Object value) {
		final PromiseWorkerElement b = before.get();
		if(b == null) {
			resolve(0, value);
		} else {
			resolve(b.getNo() + 1, value);
		}
		return this;
	}

	/**
	 * 次の異常系処理を実行します.
	 * この処理を呼び出すと次のerror()やallways()を実行します.
	 * @param value 実行引数を設定します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction reject(Object value) {
		boolean res;
		final PromiseWorkerElement b = before.get();
		if(b == null) {
			res = reject(0, value);
		} else {
			res = reject(b.getNo() + 1, value);
		}
		// 最終処理の場合でデータ送信をしていない場合.
		//if(!res && !response.isSend()) {
		//	sendError(value);
		//}
		return this;
	}

	/**
	 * 処理を終わらせます.
	 * この処理を呼び出すと次のthen()やerror()やallways()で
	 * 定義された内容を無視してPromiseを終わらせます.
	 * @param value 実行引数を設定します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction exit(Object value) {
		checkStartPromise();
		// exitが呼び出された.
		resolveRejectCounter.inc();
		// promise終了の場合はlastValueに設定.
		setLastValue(value);
		// valueが例外系の場合.
		if(value instanceof Throwable) {
			// エラー返却.
			exitPromise(PromiseStatus.Rejected);
			return this;
		}
		// success返却.
		exitPromise(PromiseStatus.Fulfilled);
		return this;
	}

	/**
	 * Waitオブジェクトを設定.
	 * @param waitAllFlag [true]の場合、waitObject.signalAll()を呼び出します.
	 * @param waitObject Waitオブジェクトを設定します.
	 */
	protected void setWaitObject(boolean waitAllFlag, Wait waitObject) {
		checkNotStartPromise();
		this.waitAllFlag = waitAllFlag;
		this.waitObject = waitObject;
	}

	/**
	 * Promiseを開始.
	 * @param req HttpRequestを設定します.
	 * @param res HttpResponseを設定します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	protected PromiseAction start(boolean execFlag, Request req, Response<?> res) {
		// リクエスト、レスポンスが設定されていない場合.
		if(req == null || res == null) {
			throw new QuinaException("Request and response are not set.");
		// 既に開始済みの場合.
		} else if(startPromiseFlag.setToGetBefore(true)) {
			throw new QuinaException("Promise has already started.");
		}
		this.request = req;
		this.response = (AbstractResponse<?>)res;
		// waitオブジェクトが設定されていない場合は生成.
		if(waitObject == null) {
			this.waitObject = new Wait();
		}
		// スタート時にresolve実行させる場合.
		if(execFlag) {
			// 初期パラメータを取得してresolve実行.
			Object o = initParam;
			initParam = null;
			resolve(o);
		}
		return this;
	}

	/**
	 * 処理終了まで待機する.
	 * @return Object 処理結果が返却されます.
	 */
	protected Object waitTo() {
		// Promiseが終了していない場合.
		if(!exitPromiseFlag.get()) {
			// 待機処理を実施.
			waitObject.await();
			// 終了した時の返信パラメータをセット.
			return lastValue.put(null);
		}
		// 既にPromiseが終了した場合.
		// 終了した時の返信パラメータをセット.
		return lastValue.put(null);
	}

	/**
	 * 処理終了まで待機する.
	 * @param out 処理結果が返却されます.
	 * @param time 待機時間をミリ秒で設定します.
	 * @return boolean trueの場合、処理は終了しています.
	 */
	protected boolean waitTo(Object[] out, long time) {
		// Promiseが終了していない場合.
		if(!exitPromiseFlag.get()) {
			// 待機処理を実施.
			if(waitObject.await(time)) {
				if(out != null) {
					// 終了した時の返信パラメータをセット.
					out[0] = lastValue.put(null);
				}
			}
		// 既にPromiseが終了した場合.
		} else if(lastValueFlag.get()) {
			if(out != null) {
				// 終了した時の返信パラメータをセット.
				out[0] = lastValue.put(null);
			}
		}
		// 終了済みの場合は[true].
		return isExitPromise();
	}

	/**
	 * resolveやrejectやendが呼ばれた累計回数を取得.
	 * @return long 累計回数が返却されます.
	 */
	protected long getResolveRejectCount() {
		return resolveRejectCounter.get();
	}

	/**
	 * 直近で呼び出されたコールモードを取得.
	 * @return
	 */
	protected int getBeforeCallMode() {
		PromiseWorkerElement em = before.get();
		if(em == null) {
			return 0;
		}
		return em.getCallMode();
	}

	/**
	 * 現在のPromiseステータスを取得.
	 * @return PromiseStatus Promiseステータスが返却されます.
	 */
	public PromiseStatus getStatus() {
		return status.get();
	}
}
