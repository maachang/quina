package quina.component;

import quina.http.Method;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.NormalResponse;
import quina.http.server.response.NormalResponseImpl;

/**
 * Anyコンポーネント.
 */
@FunctionalInterface
public interface AnyComponent extends Component {
	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	@Override
	default ComponentType getType() {
		return ComponentType.NORMAL;
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
	 * @param method HTTPメソッドが設定されます.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 */
	@Override
	default void call(Method method, Request req, Response<?> res) {
		// ResponseがNormalResponseでない場合は変換.
		if(!(res instanceof NormalResponse)) {
			res = new NormalResponseImpl(res);
		}
		call(req, (NormalResponse)res);
	}

	/**
	 * コール実行.
	 * @param req HttpRequestが設定されます.
	 * @param res NormalResponseが設定されます.
	 */
	public void call(Request req, NormalResponse res);
}
