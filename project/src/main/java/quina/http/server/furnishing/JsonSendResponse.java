package quina.http.server.furnishing;

import quina.exception.QuinaException;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.ResponseUtil;

/**
 * JSON送信実装提供.
 */
@SuppressWarnings("unchecked")
public interface JsonSendResponse<T> extends CoreSendResponse<T> {
	/**
	 * JSON専用のコンテンツタイプ.
	 */
	public static final String JSON_CONTENT_TYPE = "application/json";

	/**
	 * 送信処理.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	default T sendJSON(Object json) {
		return sendJSON(json, null);
	}

	/**
	 * 送信処理.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	default T sendJSON(Object json, String charset) {
		if(!isCallSendMethod()) {
			return (T)this;
		}
		final AbstractResponse<?> res = (AbstractResponse<?>)getResponse();
		res.setContentType(JSON_CONTENT_TYPE);
		res.startSend();
		try {
			ResponseUtil.sendJSON(res, json, charset);
		} catch(QuinaException qe) {
			res.cancelSend();
			throw qe;
		}
		return (T)this;
	}

	/**
	 * 送信処理.
	 * 大きめのJSONデータを送信する場合に利用します.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	default T sendLargeJSON(Object json) {
		return sendLargeJSON(json, null);
	}

	/**
	 * 送信処理.
	 * 大きめのJSONデータを送信する場合に利用します.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	default T sendLargeJSON(Object json, String charset) {
		if(!isCallSendMethod()) {
			return (T)this;
		}
		final AbstractResponse<?> res = (AbstractResponse<?>)getResponse();
		res.setContentType(JSON_CONTENT_TYPE);
		res.startSend();
		try {
			ResponseUtil.sendLargeJSON(res, json, charset);
		} catch(QuinaException qe) {
			res.cancelSend();
			throw qe;
		}
		return (T)this;
	}
}
