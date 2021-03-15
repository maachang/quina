package quina.promise;

import quina.http.Request;
import quina.http.Response;

/**
 * Quina専用のPromise処理.
 *
 * ベースはjavascriptのPromiseを模倣した形での実装ですが、最低限の実装です.
 * また基本的には、この処理はQuina.get().start() を呼び出さないと利用出来ません.
 *
 * 利用方法としてはQuinaでのComponentオブジェクト実行内で利用が想定されています.
 *
 * 使い方はこんな感じです.
 * Promise promise = new Promise("hoge");
 * promise.then((action, value) -> {
 *   action.resolve(value + " moge");
 * });
 * promise.then((action, value) -> {
 *   action.getResponse().setContentType("text/html");
 *   action.send(value);
 * });
 * promise.exception((action, error) -> {
 *   action.sendError(error);
 * });
 *
 * 基本的にはjsで問題となるコールバック地獄を回避するための
 * "最低限"のPromise実装を提供します.
 */
public class Promise {
	// promiseアクション.
	private PromiseAction action;

	/**
	 * コンストラクタ.
	 */
	public Promise() {
		this(null);
	}

	/**
	 * コンストラクタ.
	 * @param param パラメータを設定します.
	 */
	public Promise(Object param) {
		action = new PromiseAction(param);
	}

	/**
	 * 正常系実行処理を設定します.
	 * resolveが呼び出された時に呼び出されます.
	 * @param call 実行処理を設定します.
	 * @return Promise Promiseオブジェクトが返却されます.
	 */
	public Promise then(PromiseCall call) {
		action.then(call);
		return this;
	}

	/**
	 * 異常系実行処理を設定します.
	 * rejectが呼び出された時に呼び出します.
	 * @param call 実行処理を設定します.
	 * @return Promise Promiseオブジェクトが返却されます.
	 */
	public Promise error(PromiseCall call) {
		action.error(call);
		return this;
	}

	/**
	 * 正常系、異常系に関係なく呼び出されます.
	 * @param call 実行処理を設定します.
	 * @return Promise Promiseオブジェクトが返却されます.
	 */
	public Promise allways(PromiseCall call) {
		action.allways(call);
		return this;
	}

	/**
	 * Promiseを開始.
	 * @param req HttpRequestを設定します.
	 * @param res HttpResponseを設定します.
	 * @return Promise Promiseオブジェクトが返却されます.
	 */
	public Promise start(Request req, Response<?> res) {
		action.start(req, res);
		return this;
	}
}
