package quina.promise;

import java.io.IOException;

import quina.net.nio.tcp.worker.WorkerElement;
import quina.util.AtomicObject;

/**
 * Promiseワーカー要素.
 */
final class PromiseWorkerElement implements WorkerElement {
	// then()呼び出し.
	protected static final int MODE_THEN = 0x01;
	// error()呼び出し.
	protected static final int MODE_ERROR = 0x02;
	// allways()呼び出し.
	protected static final int MODE_ALLWAYS = 0x03;

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
	 * 破棄呼び出しフラグ.
	 */
	private boolean destroyFlag;

	/**
	 * PromiseAction情報.
	 */
	private PromiseAction action;

	/**
	 * 実行情報.
	 */
	private PromiseCall call;

	/**
	 * 実行パラメータ.
	 */
	private final AtomicObject<Object> param = new AtomicObject<Object>();

	/**
	 * コンストラクタ.
	 * @param action PromiseAction情報を設定します.
	 * @param no このPrimiseワーカの呼び出し順位の項番が設定されます.
	 * @param callMode [0]の場合はthen()呼び出し.
	 *                 [1]の場合はerror()呼び出し.
	 *                 [2]の場合はallway()呼び出し.
	 * @param call 実行対象のコールオブジェクトを設定します.
	 */
	protected PromiseWorkerElement(
		PromiseAction action, int no, int callMode, PromiseCall call) {
		this.action = action;
		this.no = no;
		this.callMode = callMode;
		this.destroyFlag = false;
		this.call = call;
	}

	/**
	 * クローズ処理.
	 * @exception IOException IO例外.
	 */
	@Override
	public void close() throws IOException {
		this.action = null;
		this.call = null;
		destroy();
	}

	/**
	 * オブジェクト破棄.
	 */
	@Override
	public void destroy() {
		destroyFlag = true;
	}

	/**
	 * 破棄されたかチェック.
	 * @return boolean trueの場合、破棄されています.
	 */
	@Override
	public boolean isDestroy() {
		return destroyFlag;
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
	 * ワーカースレッドでの実行処理.
	 * @param o ワーカーIDが設定されます.
	 * @return trueの場合正常に処理されました.
	 */
	@Override
	public boolean call(Object o) {
		final Object v = param.get();
		param.set(null);
		// 実行処理.
		try {
			call.call(action, v);
		} catch(Exception e) {
			// エラーの場合リジェクト.
			action.reject(e);
		}
		return true;
	}
}
