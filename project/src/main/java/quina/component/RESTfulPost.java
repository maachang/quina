package quina.component;

import quina.http.HttpException;
import quina.http.Method;
import quina.http.Params;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.HttpServerUtil;
import quina.http.server.response.RESTfulResponse;

/**
 * RESTfulメソッドPost専用のComponent.
 */
@FunctionalInterface
public interface RESTfulPost extends Component {
	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	@Override
	default ComponentType getType() {
		return ComponentType.RESTfulPost;
	}

	/**
	 * 対応HTTPメソッド定義を取得.
	 * @return int このコンポーネントが対応するHTTPメソッド定義が返却されます.
	 */
	@Override
	default int getMethod() {
		return ComponentConstants.HTTP_METHOD_POST;
	}

	/**
	 * コンポーネント実行処理.
	 * @param method HTTPメソッドが設定されます.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 */
	@Override
	default void call(Method method, Request req, Response<?> res) {
		if(method != Method.POST) {
			throw new HttpException(405,
				"The specified method: " + method + " cannot be used for this URL.");
		}
		// ResponseがSyncResponseでない場合は変換.
		if(!(res instanceof RESTfulResponse)) {
			res = HttpServerUtil.RESTfulResponse(res);
		}
		post(req, (RESTfulResponse)res, req.getParams());

	}

	/**
	 * POSTメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res RESTfulResponseが設定されます.
	 * @param params パラメータが設定されます.
	 */
	public void post(Request req, RESTfulResponse res, Params params);
}
