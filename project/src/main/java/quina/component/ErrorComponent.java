package quina.component;

import quina.http.Request;
import quina.http.server.response.NormalResponse;

/**
 * エラーコンポーネント.
 * 
 * Json形式のエラーの場合は jsonCallメソッドが呼び出され
 * Json形式以外のエラーの場合は、callメソッドが呼び出されます.
 */
public interface ErrorComponent
	extends ErrorAttributeComponent {
	
	@Override
	default void call(int state, boolean restful, Request req,
		NormalResponse res, Throwable e) {
		if(restful) {
			jsonCall(state, req, res, e);
		} else {
			call(state, req, res, e);
		}
	}
	
	/**
	 * Json形式のHttpError処理を実行.
	 * @param state HTTPステータスを設定します.
	 * @param req HttpRequestを設定します.
	 * @param res NormalResponseを設定します.
	 * @param e 例外を設定します.
	 */
	public void jsonCall(int state, Request req, NormalResponse res, Throwable e);

	/**
	 * Json形式以外のHttpError処理を実行.
	 * @param state HTTPステータスを設定します.
	 * @param req HttpRequestを設定します.
	 * @param res NormalResponseを設定します.
	 * @param e 例外を設定します.
	 */
	public void call(int state, Request req, NormalResponse res, Throwable e);
}
