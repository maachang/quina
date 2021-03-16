package quina.promise;

import quina.Quina;
import quina.QuinaException;
import quina.http.AbstractHttpSendFurnishing;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.AbstractResponse;
import quina.net.nio.tcp.Wait;
import quina.util.AtomicObject;
import quina.util.Flag;
import quina.util.collection.ObjectList;

/**
 * PromiseAction.
 *
 * Promiseの中核処理.
 */
public class PromiseAction extends AbstractHttpSendFurnishing<PromiseAction> {
	// Promise実行ワーカーリスト.
	private ObjectList<PromiseWorkerElement> list =
		new ObjectList<PromiseWorkerElement>();

	// 初期実行パラメータ.
	private Object initParam;

	// 前回実行されたワーカー要素.
	private final AtomicObject<PromiseWorkerElement> before =
		new AtomicObject<PromiseWorkerElement>();

	// promiseステータス.
	private final AtomicObject<PromiseStatus> status =
		new AtomicObject<PromiseStatus>(PromiseStatus.None);

	// finallyToで返却される情報.
	private final AtomicObject<Object> lastValue =
		new AtomicObject<Object>(null);

	// promise終了フラグ.
	private final Flag exitPromiseFlag = new Flag(false);

	// waitオブジェクト.
	private final Wait waitObject = new Wait();

	/**
	 * コンストラクタ.
	 * @param param 初期実行パラメータを設定します.
	 */
	protected PromiseAction(Object param) {
		this.initParam = param;
	}

	/**
	 * 正常系実行処理を設定します.
	 * resolveが呼び出された時に呼び出されます.
	 * @param call 実行処理を設定します.
	 */
	protected void then(PromiseCall call) {
		list.add(new PromiseWorkerElement(this, list.size(),
			PromiseWorkerElement.MODE_THEN, call));
	}

	/**
	 * 異常系実行処理を設定します.
	 * rejectが呼び出された時に呼び出します.
	 * @param call 実行処理を設定します.
	 */
	protected void error(PromiseCall call) {
		list.add(new PromiseWorkerElement(this, list.size(),
			PromiseWorkerElement.MODE_ERROR, call));
	}

	/**
	 * 正常系、異常系に関係なく呼び出されます.
	 * @param call 実行処理を設定します.
	 */
	protected void allways(PromiseCall call) {
		list.add(new PromiseWorkerElement(this, list.size(),
			PromiseWorkerElement.MODE_ALLWAYS, call));
	}

	// PromiseActionかReponse経由で送信処理済みの場合.
	protected boolean isActionOrResponseSend() {
		// この処理ではexitPromiseは呼び出さない.
		return isSend() || response.isSend();
	}

	// 既に送信済みの場合、Promiseの終了を行う.
	protected boolean isExitSend() {
		try {
			if(isActionOrResponseSend()) {
				// 今回が最後になるので、最終処理判断条件を設定.
				exitPromise();
				return true;
			}
		} catch(Exception e) {
		}
		return exitPromiseFlag.get();
	}

	// promiseの終了処理.
	protected void exitPromise() {
		if(!exitPromiseFlag.setToGetBefore(true)) {
			setLastStatus();
			waitObject.signal();
		}
	}

	// 最終非同期処理判断が正常結果か異常結果か判別.
	protected void setLastStatus() {
		PromiseWorkerElement em = before.get();
		if(em != null) {
			if((em.getCallMode() & PromiseWorkerElement.MODE_THEN) != 0) {
				// 正常.
				status.set(PromiseStatus.Fulfilled);
			} else {
				// 異常.
				status.set(PromiseStatus.Rejected);
			}
		}
	}

	/**
	 * 次の正常処理を実行します.
	 * @param no 対象の項番を設定します.
	 * @param value 実行引数を設定します.
	 * @return boolean [true]の場合次の実行処理が登録できました.
	 */
	protected boolean resolve(int no, Object value) {
		// 送信処理が行われている場合.
		if(!isExitPromise() && isActionOrResponseSend()) {
			// 送信済みの場合はlastValueに設定して終了処理.
			lastValue.set(value);
			exitPromise();
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
				Quina.get().registerWorker(em);
				return true;
			}
		}
		// promiseの終了の場合はlastValueに設定して終了処理
		lastValue.set(value);
		exitPromise();
		return false;
	}

	/**
	 * 次の異常系処理を実行します.
	 * @param no 対象の項番を設定します.
	 * @param value 実行引数を設定します.
	 * @return boolean [true]の場合次の実行処理が登録できました.
	 */
	protected boolean reject(int no, Object value) {
		// 送信処理が行われている場合.
		if(!isExitPromise() && isActionOrResponseSend()) {
			// 送信済みの場合はlastValueに設定して終了処理
			lastValue.set(value);
			exitPromise();
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
				Quina.get().registerWorker(em);
				return true;
			}
		}
		// promiseの終了の場合はlastValueに設定して終了処理.
		lastValue.set(value);
		exitPromise();
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
		// 最終処理の場合.
		if(!res) {
			sendError(value);
		}
		return this;
	}

	/**
	 * Promiseを開始.
	 * @param req HttpRequestを設定します.
	 * @param res HttpResponseを設定します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction start(Request req, Response<?> res) {
		// リクエスト、レスポンスが設定されていない場合.
		if(req == null || res == null) {
			throw new QuinaException("Request and response are not set.");
		// 同期コンポーネントの場合は、Promiseは利用出来ない.
		//} else if(((AbstractResponse<?>)res).getComponentType().isSync()) {
		//	throw new QuinaException(
		//		"Promises are not available when the component is in sync mode.");
		}
		this.request = req;
		this.response = (AbstractResponse<?>)res;
		this.status.set(PromiseStatus.Pending);
		Object o = initParam;
		initParam = null;
		resolve(o);
		return this;
	}

	/**
	 * 処理終了まで待機する.
	 * @return Object 処理結果が返却されます.
	 */
	protected Object waitTo() {
		waitObject.await();
		return lastValue.put(null);
	}

	/**
	 * Promiseが終了しているかチェック.
	 * @return boolean trueの場合は終了しています.
	 */
	protected boolean isExitPromise() {
		return exitPromiseFlag.get();
	}

	/**
	 * 現在のPromiseステータスを取得.
	 * @return PromiseStatus Promiseステータスが返却されます.
	 */
	protected PromiseStatus getPromiseStatus() {
		return this.status.get();
	}
}
