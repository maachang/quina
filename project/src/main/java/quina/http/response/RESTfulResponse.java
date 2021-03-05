package quina.http.response;

import quina.http.HttpElement;
import quina.http.MimeTypes;

/**
 * RESTful用のレスポンス.
 */
public class RESTfulResponse extends AbstractResponse<RESTfulResponse> {
	/**
	 * コンストラクタ.
	 * @param element Http要素を設定します.
	 * @param mimeTypes MimeType群を設定します.
	 */
	public RESTfulResponse(HttpElement element, MimeTypes mimeTypes) {
		this.element = element;
		this.mimeTypes = mimeTypes;
	}

	/**
	 * 空の情報を送信処理.
	 * @return RESTfulResponse Responseオブジェクトが返却されます.
	 */
	public RESTfulResponse send() {
		ResponseUtil.send(this);
		return this;
	}

	/**
	 * 送信処理.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return RESTfulResponse Responseオブジェクトが返却されます.
	 */
	public RESTfulResponse send(Object value) {
		return send(value, null);
	}

	/**
	 * 送信処理.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return RESTfulResponse Responseオブジェクトが返却されます.
	 */
	public RESTfulResponse send(Object value, String charset) {
		ResponseUtil.sendJSON(this, value, charset);
		return this;
	}
}
