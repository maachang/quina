package quina.component.error;

import quina.http.Request;
import quina.http.server.response.AnyResponse;

/**
 * Cdi(Contexts and Dependency Injection)向け
 * エラーコンポーネント.
 * 
 * ＠ErrorRoute アノテーションの利用を想定した
 * エラーコンポーネント.
 * 
 * Json形式のエラーの場合は jsonCallメソッドが呼び出され
 * Json形式以外のエラーの場合は、callメソッドが呼び出されます.
 */
public interface ErrorCdiComponent
	extends ErrorComponent {
	
	@Override
	default void call(int state, boolean restful, Request req,
		AnyResponse res, Throwable e) {
		// RESTful返却.
		if(restful) {
			jsonCall(state, req, res, e);
		// RESTful以外の返却.
		} else {
			call(state, req, res, e);
		}
	}
	
	/**
	 * Json形式のHttpError処理を実行.
	 * @param state HTTPステータスを設定します.
	 * @param req HttpRequestを設定します.
	 * @param res AnyResponseを設定します.
	 * @param e 例外を設定します.
	 */
	public void jsonCall(int state, Request req, AnyResponse res, Throwable e);

	/**
	 * Json形式以外のHttpError処理を実行.
	 * @param state HTTPステータスを設定します.
	 * @param req HttpRequestを設定します.
	 * @param res AnyResponseを設定します.
	 * @param e 例外を設定します.
	 */
	public void call(int state, Request req, AnyResponse res, Throwable e);
}
