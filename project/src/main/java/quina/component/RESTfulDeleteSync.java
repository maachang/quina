package quina.component;

import quina.http.HttpException;
import quina.http.Method;
import quina.http.Params;
import quina.http.Request;
import quina.http.Response;

/**
 * [同期]RESTfulzメソッドDelete専用のComponent.
 */
public interface RESTfulDeleteSync extends Component {
	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	@Override
	default ComponentType getType() {
		return ComponentType.RESTfulDelete;
	}

	/**
	 * コンポーネント実行処理.
	 * @param method HTTPメソッドが設定されます.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 */
	@Override
	default void call(Method method, Request req, Response res) {
		if(method != Method.DELETE) {
			throw new HttpException(405,
				"The specified method: " + method + " cannot be used for this URL.");
		}
		res.sendJSON(delete(req, res, req.getParams()));
	}

	/**
	 * DELETEメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 * @param params パラメータが設定されます.
	 * @return Object 返却するRESTfulオブジェクトを設定します.
	 */
	public Object delete(Request req, Response res, Params params);
}
