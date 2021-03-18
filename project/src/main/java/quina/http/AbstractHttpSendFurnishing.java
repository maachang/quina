package quina.http;

import java.io.InputStream;

import quina.QuinaException;
import quina.http.server.HttpServerUtil;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.ResponseUtil;

/**
 * Http送信実装提供.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractHttpSendFurnishing<T> {
	// HttpRequest.
	protected Request request;

	// HttpResponse.
	protected AbstractResponse<?> response;

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
	public boolean isSend() {
		return response.isSend();
	}

	// 送信済みかチェック.
	protected final void confirmSend() {
		response.startSend();
	}

	// 送信処理フラグをキャンセル.
	protected final void cancelSend() {
		response.cancelSend();
	}

	/**
	 * 送信処理.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	public T send() {
		confirmSend();
		try {
			ResponseUtil.send(response);
		} catch(QuinaException qe) {
			cancelSend();
			throw qe;
		}
		return (T)this;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	public T send(byte[] value) {
		return send(value, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param charset 文字コードを設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	public T send(byte[] value, String charset) {
		confirmSend();
		try {
			ResponseUtil.send(response, value, charset);
		} catch(QuinaException qe) {
			cancelSend();
			throw qe;
		}
		return (T)this;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	public T send(InputStream value) {
		return send(value, -1L, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	public T send(InputStream value, long length) {
		return send(value, length, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	public T send(InputStream value, int length) {
		return send(value, (long)length, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @param charset 変換文字コードが設定されます.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	public T send(InputStream value, int length, String charset) {
		return send(value, (long)length, charset);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @param charset 文字コードを設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	public T send(InputStream value, long length, String charset) {
		confirmSend();
		try {
			ResponseUtil.send(response, value, length, charset);
		} catch(QuinaException qe) {
			cancelSend();
			throw qe;
		}
		return (T)this;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	public T send(String value) {
		return send(value, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	public T send(String value, String charset) {
		confirmSend();
		try {
			ResponseUtil.send(response, value, charset);
		} catch(QuinaException qe) {
			cancelSend();
			throw qe;
		}
		return (T)this;
	}

	/**
	 * 送信処理.
	 * @param name 送信するファイル名を設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	public T sendFile(String name) {
		return sendFile(name, null);
	}

	/**
	 * 送信処理.
	 * @param name 送信するファイル名を設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	public T sendFile(String name, String charset) {
		confirmSend();
		try {
			ResponseUtil.sendFile(response, name, charset);
		} catch(QuinaException qe) {
			cancelSend();
			throw qe;
		}
		return (T)this;
	}

	/**
	 * 送信処理.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	public T sendJSON(Object value) {
		return sendJSON(value, null);
	}

	/**
	 * 送信処理.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	public T sendJSON(Object value, String charset) {
		confirmSend();
		try {
			ResponseUtil.sendJSON(response, value, charset);
		} catch(QuinaException qe) {
			cancelSend();
			throw qe;
		}
		return (T)this;
	}

	/**
	 * フォワード処理.
	 * @param path フォワード先のパスを設定します.
	 */
	public void forward(String path) {
		ResponseUtil.forward(response.getElement(), path);
	}

	/**
	 * リダイレクト処理.
	 * @param url リダイレクトURLを設定します.
	 */
	public void redirect(String url) {
		confirmSend();
		try {
			ResponseUtil.redirect(response, url);
		} catch(QuinaException qe) {
			cancelSend();
			throw qe;
		}
	}

	/**
	 * リダイレクト処理.
	 * @param status HTTPステータスを設定します.
	 * @param url リダイレクトURLを設定します.
	 */
	public void redirect(int status, String url) {
		confirmSend();
		try {
			ResponseUtil.redirect(response, status, url);
		} catch(QuinaException qe) {
			cancelSend();
			throw qe;
		}
	}

	/**
	 * リダイレクト処理.
	 * @param status HTTPステータスを設定します.
	 * @param url リダイレクトURLを設定します.
	 */
	public void redirect(HttpStatus status, String url) {
		confirmSend();
		try {
			ResponseUtil.redirect(response, status, url);
		} catch(QuinaException qe) {
			cancelSend();
			throw qe;
		}
	}

	/**
	 * エラー送信.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	public T sendError() {
		return sendError(-1, null);
	}

	/**
	 * エラー送信.
	 * @param status Httpステータスを設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	public T sendError(int status) {
		return sendError(status, null);
	}

	/**
	 * エラー送信.
	 * @param value エラーデータを設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	public T sendError(Object value) {
		return sendError(-1, value);
	}

	/**
	 * エラー送信.
	 * @param status Httpステータスを設定します.
	 * @param value エラーデータを設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	public T sendError(int status, Object value) {
		// 送信済みにセット.
		response.startSend(false);
		try {
			// ステータスが指定されてない場合は500エラー.
			int state = status;
			if(status < 0) {
				state = 500;
			}
			// エラー送信ではDefaultレスポンスに変換して送信.
			response = (AbstractResponse<?>)HttpServerUtil.defaultResponse(response);
			// エラーが存在しない場合.
			if(value == null) {
				// エラー返却.
				response.setStatus(state);
				HttpServerUtil.sendError(request, response);
			} else if(value instanceof Throwable) {
				// エラー処理.
				HttpServerUtil.sendError(request, response, (Throwable)value);
			// 文字列の場合.
			} else if(value instanceof String) {
				response.setStatus(state, value.toString());
				HttpServerUtil.sendError(request, response);
			// その他オブジェクトの場合.
			} else {
				response.setStatus(state);
				HttpServerUtil.sendError(request, response);
			}
		} catch(Exception e) {
			// HttpElementをクローズ.
			try {
				response.getElement().close();
			} catch(Exception ee) {}
		}
		return (T)this;
	}
}
