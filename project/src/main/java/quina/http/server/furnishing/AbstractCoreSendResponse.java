package quina.http.server.furnishing;

import quina.http.Response;
import quina.http.server.response.AbstractResponse;

/**
 * コア送信実装提供.
 */
public interface AbstractCoreSendResponse<T>
	extends CoreSendResponse<T> {
	/**
	 * レスポンスオブジェクトを設定します.
	 * @return Response<?> HttpResponseが返却されます.
	 */
	public Response<?> _getResponse();

	/**
	 * 送信処理が呼び出されたかチェック.
	 * @return boolean trueの場合、送信処理が呼び出されました.
	 */
	@Override
	default boolean isSend() {
		if(isCallSendMethod()) {
			return ((AbstractResponse<?>)_getResponse())
				.isSend();
		}
		return false;
	}
}
