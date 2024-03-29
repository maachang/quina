package quina.http.server.furnishing;

import quina.exception.QuinaException;
import quina.http.HttpStatus;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.ResponseUtil;

/**
 * [Abstract]基本送信実装提供.
 */
@SuppressWarnings("unchecked")
public interface AbstractBaseSendResponse<T>
	extends BaseSendResponse<T>, 
		AbstractCoreSendResponse<T> {
	
	/**
	 * フォワード処理.
	 * @param path フォワード先のパスを設定します.
	 * @return T オブジェクトが返却されます.
	 */
	@Override
	default T forward(String path) {
		if(!isCallSendMethod()) {
			return (T)this;
		}
		ResponseUtil.forward(
			((AbstractResponse<?>)_getResponse())
				.getElement(), path);
		return (T)this;
	}

	/**
	 * リダイレクト処理.
	 * @param url リダイレクトURLを設定します.
	 * @return T オブジェクトが返却されます.
	 */
	@Override
	default T redirect(String url) {
		if(!isCallSendMethod()) {
			return (T)this;
		}
		final AbstractResponse<?> res =
			(AbstractResponse<?>)_getResponse();
		res.startSend();
		try {
			ResponseUtil.redirect(res, url);
		} catch(QuinaException qe) {
			res.cancelSend();
			throw qe;
		}
		return (T)this;
	}

	/**
	 * リダイレクト処理.
	 * @param status HTTPステータスを設定します.
	 * @param url リダイレクトURLを設定します.
	 * @return T オブジェクトが返却されます.
	 */
	@Override
	default T redirect(int status, String url) {
		if(!isCallSendMethod()) {
			return (T)this;
		}
		final AbstractResponse<?> res =
			(AbstractResponse<?>)_getResponse();
		res.startSend();
		try {
			ResponseUtil.redirect(res, status, url);
		} catch(QuinaException qe) {
			res.cancelSend();
			throw qe;
		}
		return (T)this;
	}

	/**
	 * リダイレクト処理.
	 * @param status HTTPステータスを設定します.
	 * @param url リダイレクトURLを設定します.
	 * @return T オブジェクトが返却されます.
	 */
	@Override
	default T redirect(HttpStatus status, String url) {
		if(!isCallSendMethod()) {
			return (T)this;
		}
		final AbstractResponse<?> res =
			(AbstractResponse<?>)_getResponse();
		res.startSend();
		try {
			ResponseUtil.redirect(res, status, url);
		} catch(QuinaException qe) {
			res.cancelSend();
			throw qe;
		}
		return (T)this;
	}
}
