package quina.component;

import quina.http.HttpException;
import quina.http.Method;
import quina.http.Params;
import quina.http.Request;
import quina.http.Response;
import quina.http.response.RESTfulResponse;

/**
 * RESTfulのComponent.
 */
public abstract class RESTful extends AbstractValidationComponent<RESTful>
	implements Component {
	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	@Override
	public ComponentType getType() {
		return ComponentType.RESTful;
	}

	/**
	 * コンポーネント実行処理.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 * @param params パラメータが設定されます.
	 */
	@Override
	public void call(Method method, Request req, Response<?> res) {
		final RESTfulResponse rres = (RESTfulResponse)res;
		switch(method) {
		case GET: get(req, rres, execute(req)); break;
		case POST: post(req, rres, execute(req)); break;
		case DELETE: delete(req, rres, execute(req)); break;
		case PUT: put(req, rres, execute(req)); break;
		case PATCH: patch(req, rres, execute(req)); break;
		default: throw new HttpException(405, "Unsupported HTTP method: " + method.getName());
		}
	}

	/**
	 * GETメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res RESTfulResponseが設定されます.
	 * @param params パラメータが設定されます.
	 */
	public void get(Request req, RESTfulResponse res, Params params) {
		throw new HttpException(405,
			"The specified method: GET cannot be used for this URL.");
	}

	/**
	 * POSTメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res RESTfulResponseが設定されます.
	 * @param params パラメータが設定されます.
	 */
	public void post(Request req, RESTfulResponse res, Params params) {
		throw new HttpException(405,
			"The specified method: POST cannot be used for this URL.");
	}

	/**
	 * DELETEメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res RESTfulResponseが設定されます.
	 * @param params パラメータが設定されます.
	 */
	public void delete(Request req, RESTfulResponse res, Params params) {
		throw new HttpException(405,
			"The specified method: DELETE cannot be used for this URL.");
	}

	/**
	 * PUTメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res RESTfulResponseが設定されます.
	 * @param params パラメータが設定されます.
	 */
	public void put(Request req, RESTfulResponse res, Params params) {
		throw new HttpException(405,
			"The specified method: PUT cannot be used for this URL.");
	}

	/**
	 * PATCHメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res RESTfulResponseが設定されます.
	 * @param params パラメータが設定されます.
	 */
	public void patch(Request req, RESTfulResponse res, Params params) {
		throw new HttpException(405,
			"The specified method: PATCH cannot be used for this URL.");
	}
}
