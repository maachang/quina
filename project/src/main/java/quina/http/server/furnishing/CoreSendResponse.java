package quina.http.server.furnishing;

import quina.http.Request;

/**
 * コア送信実装提供..
 */
public interface CoreSendResponse<T> {
	/**
	 * HttpRequestを取得.
	 * @return Request HttpRequestが返却されます.
	 */
	public Request getRequest();

	/**
	 * 送信処理系メソッド[send(..)]が実行可能かチェック.
	 * @return boolean trueの場合、送信処理系メソッド[send(..)]の実行は可能です.
	 */
	public boolean isCallSendMethod();

	/**
	 * 送信処理が呼び出されたかチェック.
	 * @return boolean trueの場合、送信処理が呼び出されました.
	 */
	public boolean isSend();

}
