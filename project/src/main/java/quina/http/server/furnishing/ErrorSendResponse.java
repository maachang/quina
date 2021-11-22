package quina.http.server.furnishing;

import quina.http.server.HttpServerCore;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.AnyResponseImpl;

/**
 * エラー送信実装提供.
 */
@SuppressWarnings("unchecked")
public interface ErrorSendResponse<T>
	extends AbstractCoreSendResponse<T> {

	/**
	 * エラー送信.
	 * @return T オブジェクトが返却されます.
	 */
	default T sendError() {
		return sendError(-1, null);
	}

	/**
	 * エラー送信.
	 * @param status Httpステータスを設定します.
	 * @return T オブジェクトが返却されます.
	 */
	default T sendError(int status) {
		return sendError(status, null);
	}

	/**
	 * エラー送信.
	 * @param value エラーデータを設定します.
	 * @return T オブジェクトが返却されます.
	 */
	default T sendError(Object value) {
		return sendError(-1, value);
	}

	/**
	 * エラー送信.
	 * @param status Httpステータスを設定します.
	 * @param value エラーデータを設定します.
	 * @return T オブジェクトが返却されます.
	 */
	default T sendError(int status, Object value) {
		if(!isCallSendMethod()) {
			return (T)this;
		}
		AbstractResponse<?> res =
			(AbstractResponse<?>)_getResponse();
		// 送信済みにセット.
		res.startSend(false);
		try {
			// ステータスが指定されてない場合は500エラー.
			int state = status;
			if(status < 0) {
				state = 500;
			}
			// エラー送信ではAnyレスポンスに変換して送信.
			res = (AbstractResponse<?>)new AnyResponseImpl(res);
			// エラーが存在しない場合.
			if(value == null) {
				// エラー返却.
				res.setStatus(state);
				HttpServerCore.sendError(getRequest(), res);
			} else if(value instanceof Throwable) {
				// エラー処理.
				HttpServerCore.sendError(getRequest(), res, (Throwable)value);
			// 文字列の場合.
			} else if(value instanceof String) {
				res.setStatus(state, value.toString());
				HttpServerCore.sendError(getRequest(), res);
			// その他オブジェクトの場合.
			} else {
				res.setStatus(state);
				HttpServerCore.sendError(getRequest(), res);
			}
		} catch(Exception e) {
			// HttpElementをクローズ.
			try {
				res.getElement().close();
			} catch(Exception ee) {}
		}
		return (T)this;
	}
}
