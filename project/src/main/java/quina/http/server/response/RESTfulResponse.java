package quina.http.server.response;

import quina.component.ComponentType;
import quina.http.Response;
import quina.http.server.furnishing.BaseSendResponse;
import quina.http.server.furnishing.EmptySendResponse;
import quina.http.server.furnishing.ErrorSendResponse;
import quina.http.server.furnishing.JsonSendResponse;

/**
 * RESTful用のレスポンス.
 */
public interface RESTfulResponse extends
	Response<RESTfulResponse>,
	BaseSendResponse<RESTfulResponse>,
	EmptySendResponse<RESTfulResponse>,
	JsonSendResponse<RESTfulResponse>,
	ErrorSendResponse<RESTfulResponse> {

	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	default ComponentType getComponentType() {
		return ComponentType.RESTful;
	}

	/**
	 * 送信処理.
	 * @return T オブジェクトが返却されます.
	 */
	default RESTfulResponse send() {
		setContentType(JsonSendResponse.JSON_CONTENT_TYPE);
		return EmptySendResponse.super.send();
	}
}
