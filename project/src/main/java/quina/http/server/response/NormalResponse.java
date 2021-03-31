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
public interface NormalResponse extends
	Response<NormalResponse>,
	BaseSendResponse<NormalResponse>,
	EmptySendResponse<NormalResponse>,
	MemorySendResponse<NormalResponse>,
	JsonSendResponse<NormalResponse>,
	InputStreamSendResponse<NormalResponse>,
	FileSendResponse<NormalResponse>,
	ErrorSendResponse<NormalResponse> {

	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	default ComponentType getComponentType() {
		return ComponentType.NORMAL;
	}
}
