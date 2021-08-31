package quina.http.server.furnishing;

import quina.exception.QuinaException;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.ResponseUtil;

/**
 * メモリ送信実装提供.
 */
@SuppressWarnings("unchecked")
public interface MemorySendResponse<T> extends CoreSendResponse<T> {

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	default T send(byte[] value) {
		return send(value, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param charset 文字コードを設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	default T send(byte[] value, String charset) {
		if(!isCallSendMethod()) {
			return (T)this;
		}
		final AbstractResponse<?> res = (AbstractResponse<?>)getResponse();
		res.startSend();
		try {
			ResponseUtil.send(res, value, charset);
		} catch(QuinaException qe) {
			res.cancelSend();
			throw qe;
		}
		return (T)this;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	default T send(String value) {
		return send(value, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 * @return T PromiseActionオブジェクトが返却されます.
	 */
	default T send(String value, String charset) {
		if(!isCallSendMethod()) {
			return (T)this;
		}
		final AbstractResponse<?> res = (AbstractResponse<?>)getResponse();
		res.startSend();
		try {
			ResponseUtil.send(res, value, charset);
		} catch(QuinaException qe) {
			res.cancelSend();
			throw qe;
		}
		return (T)this;
	}
}
