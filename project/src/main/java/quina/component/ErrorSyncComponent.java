package quina.component;

import quina.http.Request;
import quina.http.Response;
import quina.http.server.HttpServerUtil;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.NormalResponse;
import quina.http.server.response.ResponseUtil;
import quina.http.server.response.SyncResponse;

/**
 * エラーコンポーネント.
 * 
 * ラムダ実装で、直接Routerにセットする場合は
 * こちらを利用します.
 */
@FunctionalInterface
public interface ErrorSyncComponent extends ErrorComponent {
	/**
	 * 送信なしを示すオブジェクト.
	 */
	public static final Object NOSEND = SyncResponse.NOSEND;
	
	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
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
			Object result = call(state, true, req, (SyncResponse)res, e);
			// Json返却.
			ResponseUtil.sendJSON((AbstractResponse<?>)res, result);
		// RESTful以外の返却.
		} else {
			if(res.isContentType()) {
				// HTML返却条件を設定.
				res.setContentType("text/html");
			}
			Object result = call(state, false, req, (SyncResponse)res, e);
			// 同期返却.
			ResponseUtil.sendSync((AbstractResponse<?>)res, result);
		}
	}

	/**
	 * HttpError処理を実行.
	 * @param state HTTPステータスを設定します.
	 * @param json エラーが発生した呼び出しコンポーネントが
	 *             [RESFful]の場合は[true]が設定されます.
	 * @param req HttpRequestを設定します.
	 * @param res SyncResponseを設定します.
	 * @return Object 返却するオブジェクトを設定します.
	 * @param e 例外を設定します.
	 */
	public Object call(int state, boolean restful, Request req,
		SyncResponse res, Throwable e);
	
}
