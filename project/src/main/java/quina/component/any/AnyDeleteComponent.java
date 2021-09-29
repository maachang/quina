package quina.component.any;

import quina.component.Component;
import quina.component.ComponentConstants;
import quina.component.ComponentType;
import quina.http.Method;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.AnyResponse;
import quina.http.server.response.AnyResponseImpl;

/**
 * メソッドDelete専用のComponent.
 */
@FunctionalInterface
public interface AnyDeleteComponent extends Component {
	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	@Override
	default ComponentType getType() {
		return ComponentType.AnyDelete;
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
		// ResponseがAnyResponseでない場合は変換.
		if(!(res instanceof AnyResponse)) {
			res = new AnyResponseImpl(res);
		}
		delete(req, (AnyResponse)res);
	}

	/**
	 * DELETEメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res RESTfulResponseが設定されます.
	 */
	public void delete(Request req, AnyResponse res);
}