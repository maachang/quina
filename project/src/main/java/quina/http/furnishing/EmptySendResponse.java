package quina.http.furnishing;

import quina.QuinaException;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.ResponseUtil;

/**
 * 空送信実装提供.
 */
@SuppressWarnings("unchecked")
public interface EmptySendResponse<T> extends CoreSendResponse<T> {
	/**
	 * 送信処理.
	 * @return T オブジェクトが返却されます.
	 */
	default T send() {
		if(!isCallSendMethod()) {
			return (T)this;
		}
		final AbstractResponse<?> res = (AbstractResponse<?>)getResponse();
		res.startSend();
		try {
			ResponseUtil.send(res);
		} catch(QuinaException qe) {
			res.cancelSend();
			throw qe;
		}
		return (T)this;
	}
}
