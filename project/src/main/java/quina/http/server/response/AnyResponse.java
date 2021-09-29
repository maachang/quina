package quina.http.server.response;

import quina.component.ComponentType;
import quina.http.Response;
import quina.http.server.furnishing.BaseSendResponse;
import quina.http.server.furnishing.EmptySendResponse;
import quina.http.server.furnishing.ErrorSendResponse;
import quina.http.server.furnishing.FileSendResponse;
import quina.http.server.furnishing.InputStreamSendResponse;
import quina.http.server.furnishing.JsonSendResponse;
import quina.http.server.furnishing.MemorySendResponse;

/**
 * 標準的なレスポンス.
 */
public interface AnyResponse extends
	Response<AnyResponse>,
	BaseSendResponse<AnyResponse>,
	EmptySendResponse<AnyResponse>,
	MemorySendResponse<AnyResponse>,
	JsonSendResponse<AnyResponse>,
	InputStreamSendResponse<AnyResponse>,
	FileSendResponse<AnyResponse>,
	ErrorSendResponse<AnyResponse> {

	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	default ComponentType getComponentType() {
		return ComponentType.Any;
	}
}
