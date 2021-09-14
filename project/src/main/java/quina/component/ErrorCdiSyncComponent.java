package quina.component;

import quina.http.Request;
import quina.http.Response;
import quina.http.server.HttpServerUtil;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.NormalResponse;
import quina.http.server.response.ResponseUtil;
import quina.http.server.response.SyncResponse;

/**
 * [同期]Cdi(Contexts and Dependency Injection)向け
 * エラーコンポーネント.
 * 
 * ＠ErrorRoute アノテーションの利用を想定した
 * エラーコンポーネント.

 * 
 * Json形式のエラーの場合は jsonCallメソッドが呼び出され
 * Json形式以外のエラーの場合は、callメソッドが呼び出されます.
 */
public interface ErrorCdiSyncComponent
	extends ErrorComponent {
	/**
	 * 送信なしを示すオブジェクト.
	 */
	public static final Object NOSEND = SyncResponse.NOSEND;
	
	@Override
	default ComponentType getType() {
		return ComponentType.ErrorSync;
	}
	
	@Override
	default void call(int state, boolean restful, Request req,
		NormalResponse res, Throwable e) {
		call(state, req, (Response<?>)res, e);
	}
	
	@Override
	default void call(int state, Request req, Response<?> res, Throwable e) {
		final boolean restful = ((AbstractResponse<?>)res)
			.getComponentType().isRESTful();
		// ResponseがNormalResponseでない場合は変換.
		if(!(res instanceof SyncResponse)) {
			res = HttpServerUtil.syncResponse(res);
		}
		// RESTful返却.
		if(restful) {
			if(res.isContentType()) {
				// JSON返却条件を設定.
				res.setContentType("application/json");
			}
			Object result = jsonCall(state, req, (SyncResponse)res, e);
			// Json返却.
			ResponseUtil.sendJSON((AbstractResponse<?>)res, result);
		// RESTful以外の返却.
		} else {
			if(res.isContentType()) {
				// HTML返却条件を設定.
				res.setContentType("text/html");
			}
			Object result = call(state, req, (SyncResponse)res, e);
			// 同期返却.
			ResponseUtil.sendSync((AbstractResponse<?>)res, result);
		}
	}
	
	/**
	 * Json形式のHttpError処理を実行.
	 * @param state HTTPステータスを設定します.
	 * @param req HttpRequestを設定します.
	 * @param res SyncResponseを設定します.
	 * @param e 例外を設定します.
	 * @return Object 返却するオブジェクトを設定します.
	 */
	public Object jsonCall(int state, Request req, SyncResponse res, Throwable e);

	/**
	 * Json形式以外のHttpError処理を実行.
	 * @param state HTTPステータスを設定します.
	 * @param req HttpRequestを設定します.
	 * @param res NormalResponseを設定します.
	 * @param e 例外を設定します.
	 * @return Object 返却するオブジェクトを設定します.
	 */
	public Object call(int state, Request req, SyncResponse res, Throwable e);
}
