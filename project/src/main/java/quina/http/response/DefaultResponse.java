package quina.http.response;

import java.io.InputStream;

import quina.http.HttpElement;
import quina.http.MimeTypes;

/**
 * 標準的なレスポンス.
 */
public class DefaultResponse extends AbstractResponse<DefaultResponse> {
	/**
	 * コンストラクタ.
	 * @param element Http要素を設定します.
	 * @param mimeTypes MimeType群を設定します.
	 */
	public DefaultResponse(HttpElement element, MimeTypes mimeTypes) {
		this.element = element;
		this.mimeTypes = mimeTypes;
	}

	/**
	 * 送信処理.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public DefaultResponse send() {
		ResponseUtil.send(this);
		return this;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public DefaultResponse send(byte[] value) {
		return send(value, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param charset 文字コードを設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public DefaultResponse send(byte[] value, String charset) {
		ResponseUtil.send(this, value, charset);
		return this;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public DefaultResponse send(InputStream value) {
		return send(value, -1L, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public DefaultResponse send(InputStream value, long length) {
		return send(value, length, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public DefaultResponse send(InputStream value, int length) {
		return send(value, (long)length, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @param charset 変換文字コードが設定されます.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public DefaultResponse send(InputStream value, int length, String charset) {
		return send(value, (long)length, charset);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @param charset 文字コードを設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public DefaultResponse send(InputStream value, long length, String charset) {
		ResponseUtil.send(this, value, length, charset);
		return this;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public DefaultResponse send(String value) {
		return send(value, null);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public DefaultResponse send(String value, String charset) {
		ResponseUtil.send(this, value, charset);
		return this;
	}

	/**
	 * 送信処理.
	 * @param name 送信するファイル名を設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public DefaultResponse sendFile(String name) {
		return sendFile(name, null);
	}

	/**
	 * 送信処理.
	 * @param name 送信するファイル名を設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public DefaultResponse sendFile(String name, String charset) {
		ResponseUtil.sendFile(this, name, charset);
		return this;
	}

	/**
	 * 送信処理.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public DefaultResponse sendJSON(Object value) {
		return sendJSON(value, null);
	}

	/**
	 * 送信処理.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return DefaultResponse Responseオブジェクトが返却されます.
	 */
	public DefaultResponse sendJSON(Object value, String charset) {
		ResponseUtil.sendJSON(this, value, charset);
		return this;
	}
}
