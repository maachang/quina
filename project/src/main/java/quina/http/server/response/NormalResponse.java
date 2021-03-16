package quina.http.server.response;

import java.io.InputStream;

import quina.QuinaException;
import quina.component.ComponentType;
import quina.http.HttpElement;
import quina.http.MimeTypes;
import quina.http.Response;

/**
 * 標準的なレスポンス.
 */
public class NormalResponse extends AbstractResponse<NormalResponse> {

	/***
	 * コンストラクタ.
	 * @param res レスポンスオブジェクトを設定します.
	 */
	@SuppressWarnings("rawtypes")
	public NormalResponse(Response<?> res) {
		AbstractResponse r = (AbstractResponse)res;
		this.element = r.element;
		this.mimeTypes = r.mimeTypes;
	}

	/**
	 * コンストラクタ.
	 * @param element Http要素を設定します.
	 * @param mimeTypes MimeType群を設定します.
	 */
	public NormalResponse(HttpElement element, MimeTypes mimeTypes) {
		this.element = element;
		this.mimeTypes = mimeTypes;
	}

	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	public ComponentType getComponentType() {
		return ComponentType.NORMAL;
	}

	/**
	 * 送信処理.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public NormalResponse send() {
		startSend();
		try {
			ResponseUtil.send(this);
		} catch(QuinaException qe) {
			cancelSend();
			throw qe;
		}
		return this;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public NormalResponse send(byte[] value) {
		return send(value, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param charset 文字コードを設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public NormalResponse send(byte[] value, String charset) {
		startSend();
		try {
			ResponseUtil.send(this, value, charset);
		} catch(QuinaException qe) {
			cancelSend();
			throw qe;
		}
		return this;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public NormalResponse send(InputStream value) {
		return send(value, -1L, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public NormalResponse send(InputStream value, long length) {
		return send(value, length, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public NormalResponse send(InputStream value, int length) {
		return send(value, (long)length, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @param charset 変換文字コードが設定されます.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public NormalResponse send(InputStream value, int length, String charset) {
		return send(value, (long)length, charset);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @param charset 文字コードを設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public NormalResponse send(InputStream value, long length, String charset) {
		startSend();
		try {
			ResponseUtil.send(this, value, length, charset);
		} catch(QuinaException qe) {
			cancelSend();
			throw qe;
		}
		return this;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public NormalResponse send(String value) {
		return send(value, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public NormalResponse send(String value, String charset) {
		startSend();
		try {
			ResponseUtil.send(this, value, charset);
		} catch(QuinaException qe) {
			cancelSend();
			throw qe;
		}
		return this;
	}

	/**
	 * 送信処理.
	 * @param name 送信するファイル名を設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public NormalResponse sendFile(String name) {
		return sendFile(name, null);
	}

	/**
	 * 送信処理.
	 * @param name 送信するファイル名を設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public NormalResponse sendFile(String name, String charset) {
		startSend();
		try {
			ResponseUtil.sendFile(this, name, charset);
		} catch(QuinaException qe) {
			cancelSend();
			throw qe;
		}
		return this;
	}

	/**
	 * 送信処理.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public NormalResponse sendJSON(Object value) {
		return sendJSON(value, null);
	}

	/**
	 * 送信処理.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public NormalResponse sendJSON(Object value, String charset) {
		startSend();
		try {
			ResponseUtil.sendJSON(this, value, charset);
		} catch(QuinaException qe) {
			cancelSend();
			throw qe;
		}
		return this;
	}

	/**
	 * 新しいデフォルトのResponseを取得.
	 * @param response レスポンスを設定します.
	 * @return Response<?> 新しいレスポンスが返却されます.
	 */
	public static final Response<?> newResponse(Response<?> response) {
		final AbstractResponse<?> res = (AbstractResponse<?>)response;
		final HttpElement em = res.getElement();
		response = new NormalResponse(em, res.getMimeTypes());
		em.setResponse(response);
		return response;
	}
}
