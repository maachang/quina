package quina.http.furnishing;

import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.AbstractResponse;

/**
 * コア送信実装提供.
 */
public interface CoreSendResponse<T> {
	/**
	 * HttpRequestを取得.
	 * @return Request HttpRequestが返却されます.
	 */
	public Request getRequest();

	/**
	 * レスポンスオブジェクトを設定します.
	 * @return Response<?> HttpResponseが返却されます.
	 */
	public Response<?> getResponse();

	/**
	 * 送信処理系メソッド[send(..)]が実行可能かチェック.
	 * @return boolean trueの場合、送信処理系メソッド[send(..)]の実行は可能です.
	 */
	public boolean isCallSendMethod();

	/**
	 * 送信処理が呼び出されたかチェック.
	 * @return boolean trueの場合、送信処理が呼び出されました.
	 */
	default boolean isSend() {
		if(isCallSendMethod()) {
			return ((AbstractResponse<?>)getResponse()).isSend();
		}
		return false;
	}
}
