package quina.http.furnishing;

import quina.QuinaException;
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
	default T sendJSON(Object value) {
		return sendJSON(value, null);
	}

	/**
	 * 送信処理.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	default T sendJSON(Object value, String charset) {
		if(!isCallSendMethod()) {
			return (T)this;
		}
		final AbstractResponse<?> res = (AbstractResponse<?>)getResponse();
		res.setContentType(JSON_CONTENT_TYPE);
		res.startSend();
		try {
			ResponseUtil.sendJSON(res, value, charset);
		} catch(QuinaException qe) {
			res.cancelSend();
			throw qe;
		}
		return (T)this;
	}
}
