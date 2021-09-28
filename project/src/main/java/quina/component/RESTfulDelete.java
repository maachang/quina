package quina.component;

import quina.http.Method;
import quina.http.Params;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.RESTfulResponse;
import quina.http.server.response.RESTfulResponseImpl;

/**
 * RESTfulメソッドDelete専用のComponent.
 */
@FunctionalInterface
public interface RESTfulDelete extends Component {
	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	@Override
	default ComponentType getType() {
		return ComponentType.RESTfulDelete;
	}

	/**
	 * 対応HTTPメソッド定義を取得.
	 * @return int このコンポーネントが対応するHTTPメソッド定義が返却されます.
	 */
	@Override
	default int getMethod() {
		return ComponentConstants.HTTP_METHOD_DELETE;
	}

	/**
	 * コンポーネント実行処理.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 */
	@Override
	default void call(Request req, Response<?> res) {
		if(req.getMethod() != Method.DELETE) {
			ComponentConstants.httpError405(req);
		}
		// ResponseがSyncResponseでない場合は変換.
		if(!(res instanceof RESTfulResponse)) {
			res = new RESTfulResponseImpl(res);
		}
		delete(req, (RESTfulResponse)res, req.getParams());
	}

	/**
	 * DELETEメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res RESTfulResponseが設定されます.
	 * @param params パラメータが設定されます.
	 */
	public void delete(Request req, RESTfulResponse res, Params params);
}
