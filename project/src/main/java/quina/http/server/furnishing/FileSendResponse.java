package quina.http.server.furnishing;

import quina.exception.QuinaException;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.ResponseUtil;

/**
 * ファイル送信実装提供.
 */
@SuppressWarnings("unchecked")
public interface FileSendResponse<T> extends AbstractCoreSendResponse<T> {
	/**
	 * 送信処理.
	 * @param name 送信するファイル名を設定します.
	 * @return T オブジェクトが返却されます.
	 */
	default T sendFile(String name) {
		return sendFile(name, null);
	}

	/**
	 * 送信処理.
	 * @param name 送信するファイル名を設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 * @return T オブジェクトが返却されます.
	 */
	default T sendFile(String name, String charset) {
		if(!isCallSendMethod()) {
			return (T)this;
		}
		final AbstractResponse<?> res =
			(AbstractResponse<?>)_getResponse();
		res.startSend();
		try {
			ResponseUtil.sendFile(res, name, charset);
		} catch(QuinaException qe) {
			res.cancelSend();
			throw qe;
		}
		return (T)this;
	}

}
