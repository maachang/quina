package quina.http.server.furnishing;

import quina.http.server.HttpServerUtil;
import quina.http.server.response.AbstractResponse;

/**
 * エラー送信実装提供.
 */
@SuppressWarnings("unchecked")
public interface ErrorSendResponse<T> extends CoreSendResponse<T> {

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
		AbstractResponse<?> res = (AbstractResponse<?>)getResponse();
		// 送信済みにセット.
		res.startSend(false);
		try {
			// ステータスが指定されてない場合は500エラー.
			int state = status;
			if(status < 0) {
				state = 500;
			}
			// エラー送信ではDefaultレスポンスに変換して送信.
			res = (AbstractResponse<?>)HttpServerUtil.defaultResponse(res);
			// エラーが存在しない場合.
			if(value == null) {
				// エラー返却.
				res.setStatus(state);
				HttpServerUtil.sendError(getRequest(), res);
			} else if(value instanceof Throwable) {
				// エラー処理.
				HttpServerUtil.sendError(getRequest(), res, (Throwable)value);
			// 文字列の場合.
			} else if(value instanceof String) {
				res.setStatus(state, value.toString());
				HttpServerUtil.sendError(getRequest(), res);
			// その他オブジェクトの場合.
			} else {
				res.setStatus(state);
				HttpServerUtil.sendError(getRequest(), res);
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
