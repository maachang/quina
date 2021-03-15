package quina.promise;

import java.io.InputStream;

import quina.Quina;
import quina.QuinaException;
import quina.http.HttpStatus;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.HttpServerCall;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.Forward;
import quina.http.server.response.Redirect;
import quina.http.server.response.ResponseUtil;
import quina.util.Flag;
import quina.util.collection.ObjectList;

/**
 * PromiseAction.
 */
public class PromiseAction {
	// Promise実行ワーカーリスト.
	private ObjectList<PromiseWorkerElement> list =
		new ObjectList<PromiseWorkerElement>();

	// 初期実行パラメータ.
	private Object initParam;

	// Httpリクエスト.
	private Request request;

	// Httpレスポンス.
	private AbstractResponse<?> response;

	// 前回実行されたワーカー要素.
	private PromiseWorkerElement before;

	// 送信処理フラグ.
	private final Flag sendFlag = new Flag(false);

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

	/**
	 * 次の正常処理を実行します.
	 * @param no 対象の項番を設定します.
	 * @param value 実行引数を設定します.
	 * @return boolean [true]の場合次の実行処理が登録できました.
	 */
	protected boolean resolve(int no, Object value) {
		final int len = list.size();
		for(int i = no; i < len; i ++) {
			if((list.get(i).getCallMode() & PromiseWorkerElement.MODE_THEN) != 0) {
				before = list.get(i);
				before.setParam(value);
				Quina.get().registerWorker(before);
				return true;
			}
		}
		return false;
	}

	/**
	 * 次の異常系処理を実行します.
	 * @param no 対象の項番を設定します.
	 * @param value 実行引数を設定します.
	 * @return boolean [true]の場合次の実行処理が登録できました.
	 */
	protected boolean reject(int no, Object value) {
		final int len = list.size();
		for(int i = no; i < len; i ++) {
			if((list.get(i).getCallMode() & PromiseWorkerElement.MODE_ERROR) != 0) {
				before = list.get(i);
				before.setParam(value);
				Quina.get().registerWorker(before);
				return true;
			}
		}
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
		if(before == null) {
			resolve(0, value);
		} else {
			resolve(before.getNo() + 1, value);
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
		if(before == null) {
			res = reject(0, value);
		} else {
			res = reject(before.getNo() + 1, value);
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
		}
		this.request = req;
		this.response = (AbstractResponse<?>)res;
		resolve(initParam);
		return this;
	}

	/**
	 * HttpRequestを取得.
	 * @return Request HttpRequestが返却されます.
	 */
	public Request getRequest() {
		return request;
	}

	/**
	 * レスポンスオブジェクトを設定します.
	 * @return Response<?> HttpResponseが返却されます.
	 */
	public Response<?> getResponse() {
		return response;
	}

	/**
	 * 送信処理が呼び出されたかチェック.
	 * @return boolean trueの場合、送信処理が呼び出されました.
	 */
	protected boolean isSend() {
		return sendFlag.get();
	}

	// 送信済みかチェック.
	private final void confirmSend() {
		if(sendFlag.setToGetBefore(true)) {
			throw new QuinaException("The transmission process has already been performed.");
		}
	}

	/**
	 * 送信処理.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction send() {
		confirmSend();
		ResponseUtil.send(response);
		return this;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction send(byte[] value) {
		return send(value, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param charset 文字コードを設定します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction send(byte[] value, String charset) {
		confirmSend();
		ResponseUtil.send(response, value, charset);
		return this;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction send(InputStream value) {
		return send(value, -1L, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction send(InputStream value, long length) {
		return send(value, length, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction send(InputStream value, int length) {
		return send(value, (long)length, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @param charset 変換文字コードが設定されます.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction send(InputStream value, int length, String charset) {
		return send(value, (long)length, charset);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @param charset 文字コードを設定します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction send(InputStream value, long length, String charset) {
		confirmSend();
		ResponseUtil.send(response, value, length, charset);
		return this;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction send(String value) {
		return send(value, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction send(String value, String charset) {
		confirmSend();
		ResponseUtil.send(response, value, charset);
		return this;
	}

	/**
	 * 送信処理.
	 * @param name 送信するファイル名を設定します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction sendFile(String name) {
		return sendFile(name, null);
	}

	/**
	 * 送信処理.
	 * @param name 送信するファイル名を設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction sendFile(String name, String charset) {
		confirmSend();
		ResponseUtil.sendFile(response, name, charset);
		return this;
	}

	/**
	 * 送信処理.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction sendJSON(Object value) {
		return sendJSON(value, null);
	}

	/**
	 * 送信処理.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction sendJSON(Object value, String charset) {
		confirmSend();
		ResponseUtil.sendJSON(response, value, charset);
		return this;
	}

	/**
	 * フォワード処理.
	 * @param path フォワード先のパスを設定します.
	 */
	public void forward(String path) {
		throw new Forward(path);
	}

	/**
	 * リダイレクト処理.
	 * @param url リダイレクトURLを設定します.
	 */
	public void redirect(String url) {
		throw new Redirect(url);
	}

	/**
	 * リダイレクト処理.
	 * @param status HTTPステータスを設定します.
	 * @param url リダイレクトURLを設定します.
	 */
	public void redirect(int status, String url) {
		throw new Redirect(status, url);
	}

	/**
	 * リダイレクト処理.
	 * @param status HTTPステータスを設定します.
	 * @param url リダイレクトURLを設定します.
	 */
	public void redirect(HttpStatus status, String url) {
		throw new Redirect(status, url);
	}

	/**
	 * エラー送信.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction sendError() {
		return sendError(-1, null);
	}

	/**
	 * エラー送信.
	 * @param status Httpステータスを設定します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction sendError(int status) {
		return sendError(status, null);
	}

	/**
	 * エラー送信.
	 * @param value エラーデータを設定します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction sendError(Object value) {
		return sendError(-1, value);
	}

	/**
	 * エラー送信.
	 * @param status Httpステータスを設定します.
	 * @param value エラーデータを設定します.
	 * @return PromiseAction PromiseActionオブジェクトが返却されます.
	 */
	public PromiseAction sendError(int status, Object value) {
		// 送信済みにセット.
		sendFlag.set(true);
		// ステータスが指定されてない場合は500エラー.
		int state = status;
		if(status < 0) {
			state = 500;
		}
		// フォワード処理の場合.
		if(value != null && value instanceof Forward) {
			// フォワード処理.
			Quina.get().getHttpServerCall().sendForward(
				(Forward)value, response.getElement());
			return this;
		}
		// エラー送信ではDefaultレスポンスに変換して送信.
		response = (AbstractResponse<?>)HttpServerCall.defaultResponse(response);
		// エラーが存在しない場合.
		if(value == null) {
			// エラー返却.
			response.setStatus(state);
			HttpServerCall.sendError(request, response);
		} else if(value instanceof Throwable) {
			// リダイレクト.
			if(value instanceof Redirect) {
				HttpServerCall.sendRedirect((Redirect)value, response);
			// エラー処理.
			} else {
				HttpServerCall.sendError(request, response, (Throwable)value);
			}
		// 文字列の場合.
		} else if(value instanceof String) {
			response.setStatus(state, value.toString());
			HttpServerCall.sendError(request, response);
		// その他オブジェクトの場合.
		} else {
			response.setStatus(state);
			HttpServerCall.sendError(request, response);
		}
		return this;
	}
}
