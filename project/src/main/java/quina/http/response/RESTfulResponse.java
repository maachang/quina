package quina.http.response;

import quina.component.ComponentType;
import quina.http.HttpElement;
import quina.http.MimeTypes;
import quina.http.Response;

/**
 * RESTful用のレスポンス.
 */
public class RESTfulResponse extends AbstractResponse<RESTfulResponse> {
	/***
	 * コンストラクタ.
	 * @param res レスポンスオブジェクトを設定します.
	 */
	@SuppressWarnings("rawtypes")
	public RESTfulResponse(Response<?> res) {
		AbstractResponse r = (AbstractResponse)res;
		this.element = r.element;
		this.mimeTypes = r.mimeTypes;
	}

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
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	public ComponentType getComponentType() {
		return ComponentType.RESTful;
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
