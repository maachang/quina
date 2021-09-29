package quina.component.any;

import quina.component.Component;
import quina.component.ComponentConstants;
import quina.component.ComponentType;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.AnyResponse;
import quina.http.server.response.AnyResponseImpl;

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
		return ComponentType.Any;
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
	 */
	@Override
	default void call(Request req, Response<?> res) {
		// ResponseがAnyResponseでない場合は変換.
		if(!(res instanceof AnyResponse)) {
			res = new AnyResponseImpl(res);
		}
		call(req, (AnyResponse)res);
	}

	/**
	 * コール実行.
	 * @param req HttpRequestが設定されます.
	 * @param res AnyResponseが設定されます.
	 */
	public void call(Request req, AnyResponse res);
}
