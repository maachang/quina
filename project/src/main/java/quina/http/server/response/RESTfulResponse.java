package quina.http.server.response;

import quina.component.ComponentType;
import quina.http.HttpElement;
import quina.http.MimeTypes;
import quina.http.Response;
import quina.http.furnishing.EmptySendResponse;
import quina.http.furnishing.ErrorSendResponse;
import quina.http.furnishing.JsonSendResponse;

/**
 * RESTful用のレスポンス.
 */
public class RESTfulResponse extends AbstractResponse<RESTfulResponse>
	implements EmptySendResponse<RESTfulResponse>,
		JsonSendResponse<RESTfulResponse>,
		ErrorSendResponse<RESTfulResponse> {

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
	 * 新しいデフォルトのResponseを取得.
	 * @param response レスポンスを設定します.
	 * @return Response<?> 新しいレスポンスが返却されます.
	 */
	public static final Response<?> newResponse(Response<?> response) {
		final AbstractResponse<?> res = (AbstractResponse<?>)response;
		final HttpElement em = res.getElement();
		response = new RESTfulResponse(em, res.getMimeTypes());
		em.setResponse(response);
		return response;
	}

	/**
	 * 送信処理.
	 * @return RESTfulResponse オブジェクトが返却されます.
	 */
	public RESTfulResponse send() {
		setContentType(JsonSendResponse.JSON_CONTENT_TYPE);
		return EmptySendResponse.super.send();
	}
}
