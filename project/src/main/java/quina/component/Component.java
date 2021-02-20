package quina.component;

import quina.http.Method;
import quina.http.Request;
import quina.http.Response;

/**
 * 実行コンポーネント.
 */
public interface Component {
	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	default ComponentType getType() {
		return ComponentType.NORMAL;
	}

	/**
	 * コンポーネント実行処理.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 */
	default void call(Request req, Response res) {
		call(req.getMethod(), req, res);
	}

	/**
	 * コンポーネント実行処理.
	 * @param method HTTPメソッドが設定されます.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 */
	public void call(Method method, Request req, Response res);
}
