package quina.component;

import quina.http.Method;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.NormalResponse;

/**
 * ノーマルコンポーネント.
 */
public interface NormalComponent extends Component {
	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	@Override
	default ComponentType getType() {
		return ComponentType.NORMAL;
	}

	/**
	 * コンポーネント実行処理.
	 * @param method HTTPメソッドが設定されます.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 */
	@Override
	default void call(Method method, Request req, Response<?> res) {
		call(req, (NormalResponse)res);
	}

	/**
	 * コール実行.
	 * @param req HttpRequestが設定されます.
	 * @param res NormalResponseが設定されます.
	 */
	public void call(Request req, NormalResponse res);
}
