package quina.component.restful;

import quina.component.Component;
import quina.component.ComponentConstants;
import quina.component.ComponentType;
import quina.http.HttpException;
import quina.http.Params;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.RESTfulResponse;
import quina.http.server.response.RESTfulResponseImpl;

/**
 * RESTfulのComponent.
 */
public interface RESTful extends Component {
	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	@Override
	default ComponentType getType() {
		return ComponentType.RESTful;
	}

	/**
	 * 対応HTTPメソッド定義を取得.
	 * @return int このコンポーネントが対応するHTTPメソッド定義が返却されます.
	 */
	@Override
	default int getMethod() {
		return ComponentConstants.HTTP_METHOD_ALL;
	}

	/**
	 * コンポーネント実行処理.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 * @param params パラメータが設定されます.
	 */
	@Override
	default void call(Request req, Response<?> res) {
		// ResponseがSyncResponseでない場合は変換.
		if(!(res instanceof RESTfulResponse)) {
			res = new RESTfulResponseImpl(res);
		}
		final RESTfulResponse rres = (RESTfulResponse)res;
		switch(req.getMethod()) {
		case GET: get(req, rres, req.getParams()); break;
		case POST: post(req, rres, req.getParams()); break;
		case DELETE: delete(req, rres, req.getParams()); break;
		case PUT: put(req, rres, req.getParams()); break;
		case PATCH: patch(req, rres, req.getParams()); break;
		default: ComponentConstants.httpError405(req);
		}
	}

	/**
	 * GETメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res RESTfulResponseが設定されます.
	 * @param params パラメータが設定されます.
	 */
	default void get(Request req, RESTfulResponse res, Params params) {
		throw new HttpException(405,
			"The specified method: GET cannot be used for this URL.");
	}

	/**
	 * POSTメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res RESTfulResponseが設定されます.
	 * @param params パラメータが設定されます.
	 */
	default void post(Request req, RESTfulResponse res, Params params) {
		throw new HttpException(405,
			"The specified method: POST cannot be used for this URL.");
	}

	/**
	 * DELETEメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res RESTfulResponseが設定されます.
	 * @param params パラメータが設定されます.
	 */
	default void delete(Request req, RESTfulResponse res, Params params) {
		throw new HttpException(405,
			"The specified method: DELETE cannot be used for this URL.");
	}

	/**
	 * PUTメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res RESTfulResponseが設定されます.
	 * @param params パラメータが設定されます.
	 */
	default void put(Request req, RESTfulResponse res, Params params) {
		throw new HttpException(405,
			"The specified method: PUT cannot be used for this URL.");
	}

	/**
	 * PATCHメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res RESTfulResponseが設定されます.
	 * @param params パラメータが設定されます.
	 */
	default void patch(Request req, RESTfulResponse res, Params params) {
		throw new HttpException(405,
			"The specified method: PATCH cannot be used for this URL.");
	}
}
