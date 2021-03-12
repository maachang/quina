package quina.component;

import quina.http.HttpException;
import quina.http.Method;
import quina.http.Params;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.ResponseUtil;
import quina.http.server.response.SyncResponse;

/**
 * [同期]RESTfulzメソッドGet専用のComponent.
 */
public interface RESTfulGetSync extends Component {
	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	@Override
	default ComponentType getType() {
		return ComponentType.RESTfulGetSync;
	}

	/**
	 * コンポーネント実行処理.
	 * @param method HTTPメソッドが設定されます.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 */
	@Override
	default void call(Method method, Request req, Response<?> res) {
		if(method != Method.GET) {
			throw new HttpException(405,
				"The specified method: " + method + " cannot be used for this URL.");
		}
		ResponseUtil.sendJSON((SyncResponse)res,
			get(req, (SyncResponse)res, req.getParams()));
	}

	/**
	 * GETメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res SyncResponseが設定されます.
	 * @param params パラメータが設定されます.
	 * @return Object 返却するRESTfulオブジェクトを設定します.
	 */
	public abstract Object get(Request req, SyncResponse res, Params params);

}
