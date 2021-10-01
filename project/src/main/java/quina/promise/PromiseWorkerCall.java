package quina.promise;

import quina.util.AtomicObject;
import quina.worker.QuinaWorkerCall;
import quina.worker.QuinaWorkerConstants;

/**
 * Promiseワーカー要素.
 */
final class PromiseWorkerCall extends QuinaWorkerCall {
	// then()呼び出し.
	protected static final int MODE_THEN = 0x01;
	// error()呼び出し.
	protected static final int MODE_ERROR = 0x02;
	// allways()呼び出し.
	protected static final int MODE_ANY = 0x03;
	
	/**
	 * 呼び出しモード.
	 * [11の場合はthen()呼び出し.
	 * [2]の場合はerror()呼び出し.
	 * [3]の場合はallways()呼び出し.
	 */
	private int callMode;

	/**
	 * このPrimiseワーカの呼び出し順位の項番.
	 */
	private int no;

	/**
	 * PromiseAction情報.
	 */
	private PromiseActionImpl action;

	/**
	 * 実行情報.
	 */
	private PromiseCall call;

	/**
	 * 実行パラメータ.
	 */
	private final AtomicObject<Object> param =
		new AtomicObject<Object>();

	/**
	 * コンストラクタ.
	 * @param action PromiseAction情報を設定します.
	 * @param no このPrimiseワーカの呼び出し順位の項番が
	 *           設定されます.
	 * @param callMode [0]の場合はthen()呼び出し.
	 *                 [1]の場合はerror()呼び出し.
	 *                 [2]の場合はallway()呼び出し.
	 * @param call 実行対象のコールオブジェクトを設定します.
	 */
	protected PromiseWorkerCall(
		PromiseActionImpl action, int no, int callMode,
		PromiseCall call) {
		this.action = action;
		this.no = no;
		this.callMode = callMode;
		this.call = call;
	}

	/**
	 * 特録項番を取得.
	 * @return int 登録項番が返却されます.
	 */
	public int getNo() {
		return no;
	}

	/**
	 * コールモードを取得.
	 * @return int コールモードが返却されます.
	 */
	public int getCallMode() {
		return callMode;
	}

	/**
	 * 実行パラメータを設定.
	 * @param param 実行パラメータを設定します.
	 */
	public void setParam(Object param) {
		this.param.set(param);
	}
	
	/**
	 * ワーカー要素用のユニークIDを取得.
	 * @return int ユニークIDを取得します.
	 */
	@Override
	public int getId() {
		return QuinaWorkerConstants.PROMISE_WORKER_CALL_ID;
	}

	/**
	 * 要素を破棄.
	 * @param no 対象のスレッドNoを設定します.
	 */
	@Override
	public void destroy(int no) {
		
	}

	/**
	 * 要素が既に破棄されているかチェック.
	 * @param no 対象のスレッドNoを設定します.
	 * @return boolean [true]の場合は既に破棄されています.
	 */
	@Override
	public boolean isDestroy(int no) {
		return false;
	}

	/**
	 * 対象要素の実行時の呼び出し.
	 * @param no 対象のスレッドNoを設定します.
	 * @return boolean falseの場合実行処理は失敗しました.
	 */
	@Override
	public boolean executeCall(int no) {
		// 実行パラメータを取得.
		final Object v = param.get();
		// 取得した実行パラメータは削除.
		param.set(null);
		// 実行処理.
		try {
			// コール実行前のresolveやreject
			// 呼び出し累計回数を取得.
			final long count = action.getResolveRejectCount();
			call.call(action, v);
			// resolveやrejectが呼ばれてない場合.
			if(count == action.getResolveRejectCount()) {
				// 最後に呼ばれた処理がerrorの場合はreject実行.
				if(action.getBeforeCallMode() == MODE_ERROR ||
					v instanceof Throwable) {
					action.reject(v);
				// それ以外は空のresolve実行.
				} else {
					action.resolve();
				}
			}
		} catch(Exception e) {
			// エラーの場合リジェクト.
			action.reject(e);
		}
		return true;
	}
}
