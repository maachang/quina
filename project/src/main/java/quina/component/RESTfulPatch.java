package quina.component;

import quina.http.HttpException;
import quina.http.Method;
import quina.http.Params;
import quina.http.Request;
import quina.http.Response;

/**
 * RESTfulzメソッドPatch専用のComponent.
 */
public interface RESTfulPatch extends Component {
	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	@Override
	default ComponentType getType() {
		return ComponentType.RESTfulPatch;
	}

	/**
	 * コンポーネント実行処理.
	 * @param method HTTPメソッドが設定されます.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 */
	@Override
	default void call(Method method, Request req, Response res) {
		if(method != Method.PATCH) {
			throw new HttpException(405,
				"The specified method: " + method + " cannot be used for this URL.");
		}
		patch(req, res, req.getParams());

	}

	/**
	 * PATCHメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 * @param params パラメータが設定されます.
	 */
	public void patch(Request req, Response res, Params params);
}
