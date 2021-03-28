package quina.http.server.response;

import quina.component.ComponentType;
import quina.http.Response;
import quina.http.furnishing.BaseSendResponse;

/**
 * 同期用のレスポンス.
 */
public interface SyncResponse extends
	Response<SyncResponse>,
	BaseSendResponse<SyncResponse> {
	/**
	 * 送信なしを示すオブジェクト.
	 */
	public static final Object NOSEND = new Object();

	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	default ComponentType getComponentType() {
		return ComponentType.Sync;
	}
}
