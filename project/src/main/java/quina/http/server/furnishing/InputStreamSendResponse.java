package quina.http.server.furnishing;

import java.io.InputStream;

import quina.exception.QuinaException;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.ResponseUtil;

/**
 * InputStream関連送信実装.
 */
@SuppressWarnings("unchecked")
public interface InputStreamSendResponse<T> extends CoreSendResponse<T> {
	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return T オブジェクトが返却されます.
	 */
	default T send(InputStream value) {
		return send(value, -1L, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @return T オブジェクトが返却されます.
	 */
	default T send(InputStream value, long length) {
		return send(value, length, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @return T オブジェクトが返却されます.
	 */
	default T send(InputStream value, int length) {
		return send(value, (long)length, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @param charset 変換文字コードが設定されます.
	 * @return T オブジェクトが返却されます.
	 */
	default T send(InputStream value, int length, String charset) {
		return send(value, (long)length, charset);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @param charset 文字コードを設定します.
	 * @return T オブジェクトが返却されます.
	 */
	default T send(InputStream value, long length, String charset) {
		if(!isCallSendMethod()) {
			return (T)this;
		}
		final AbstractResponse<?> res = (AbstractResponse<?>)getResponse();
		res.startSend();
		try {
			ResponseUtil.send(res, value, length, charset);
		} catch(QuinaException qe) {
			res.cancelSend();
			throw qe;
		}
		return (T)this;
	}
}
