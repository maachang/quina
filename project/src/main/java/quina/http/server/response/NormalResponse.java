package quina.http.server.response;

import quina.component.ComponentType;
import quina.http.Response;
import quina.http.furnishing.BaseSendResponse;
import quina.http.furnishing.EmptySendResponse;
import quina.http.furnishing.ErrorSendResponse;
import quina.http.furnishing.FileSendResponse;
import quina.http.furnishing.InputStreamSendResponse;
import quina.http.furnishing.JsonSendResponse;
import quina.http.furnishing.MemorySendResponse;

/**
 * 標準的なレスポンス.
 */
public interface NormalResponse<T> extends
	Response<T>,
	BaseSendResponse<T>,
	EmptySendResponse<T>,
	MemorySendResponse<T>,
	JsonSendResponse<T>,
	InputStreamSendResponse<T>,
	FileSendResponse<T>,
	ErrorSendResponse<T> {

	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	default ComponentType getComponentType() {
		return ComponentType.NORMAL;
	}
}
