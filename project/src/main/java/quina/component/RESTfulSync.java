package quina.component;

import quina.http.HttpException;
import quina.http.Method;
import quina.http.Params;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.ResponseUtil;
import quina.http.server.response.SyncResponse;

/**
 * [同期]RESTfulのComponent.
 */
public interface RESTfulSync extends Component {
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
	default void call(Method method, Request req, Response<?> res) {
		final Object o;
		final SyncResponse sres = (SyncResponse)res;
		switch(method) {
		case GET: o = get(req, sres, req.getParams()); break;
		case POST: o = post(req, sres, req.getParams()); break;
		case DELETE: o = delete(req, sres, req.getParams()); break;
		case PUT: o = put(req, sres, req.getParams()); break;
		case PATCH: o = patch(req, sres, req.getParams()); break;
		default: throw new HttpException(405, "Unsupported HTTP method: " + method.getName());
		}
		// 送信なしを示す場合.
		if(NOSEND == o) {
			return;
		// 返却内容が存在する場合.
		} else {
			ResponseUtil.sendJSON((AbstractResponse<?>)res, o);
		}
	}

	/**
	 * GETメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res SyncResponseが設定されます.
	 * @param params パラメータが設定されます.
	 * @return Object 返却するRESTfulオブジェクトを設定します.
	 */
	default Object get(Request req, SyncResponse res, Params params) {
		throw new HttpException(405,
			"The specified method: GET cannot be used for this URL.");
	}

	/**
	 * POSTメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res SyncResponseが設定されます.
	 * @param params パラメータが設定されます.
	 * @return Object 返却するRESTfulオブジェクトを設定します.
	 */
	default Object post(Request req, SyncResponse res, Params params) {
		throw new HttpException(405,
			"The specified method: POST cannot be used for this URL.");
	}

	/**
	 * DELETEメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res SyncResponseが設定されます.
	 * @param params パラメータが設定されます.
	 * @return Object 返却するRESTfulオブジェクトを設定します.
	 */
	default Object delete(Request req, SyncResponse res, Params params) {
		throw new HttpException(405,
			"The specified method: DELETE cannot be used for this URL.");
	}

	/**
	 * PUTメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res SyncResponseが設定されます.
	 * @param params パラメータが設定されます.
	 * @return Object 返却するRESTfulオブジェクトを設定します.
	 */
	default Object put(Request req, SyncResponse res, Params params) {
		throw new HttpException(405,
			"The specified method: PUT cannot be used for this URL.");
	}

	/**
	 * PATCHメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res SyncResponseが設定されます.
	 * @param params パラメータが設定されます.
	 * @return Object 返却するRESTfulオブジェクトを設定します.
	 */
	default Object patch(Request req, SyncResponse res, Params params) {
		throw new HttpException(405,
			"The specified method: PATCH cannot be used for this URL.");
	}
}
