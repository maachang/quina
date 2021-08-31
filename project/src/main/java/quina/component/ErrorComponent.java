package quina.component;

import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.AbstractResponse;

/**
 * HTTPエラー発生時のコンポーネント.
 */
@FunctionalInterface
public interface ErrorComponent {
	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	default ComponentType getType() {
		return ComponentType.ERROR;
	}

	/**
	 * 対応HTTPメソッド定義を取得.
	 * @return int このコンポーネントが対応するHTTPメソッド定義が返却されます.
	 */
	default int getMethod() {
		return ComponentConstants.HTTP_METHOD_ALL;
	}

	/**
	 * HttpError処理を実行.
	 * @param state HTTPステータスを設定します.
	 * @param req HttpRequestを設定します.
	 * @param res HttpResponseを設定します.
	 */
	default void call(int state, Request req, Response<?> res) {
		final boolean json = ((AbstractResponse<?>)res).getComponentType().isRESTful();
		if(res.isContentType()) {
			// json返却の場合.
			if(json) {
				// JSON返却条件を設定.
				res.setContentType("application/json");
			} else {
				// HTML返却条件を設定.
				res.setContentType("text/html");
			}
		}
		call(state, json, req, res);
	}

	/**
	 * HttpError処理を実行.
	 * @param state HTTPステータスを設定します.
	 * @param req HttpRequestを設定します.
	 * @param res HttpResponseを設定します.
	 * @param e 例外を設定します.
	 */
	default void call(int state, Request req, Response<?> res, Throwable e) {
		final boolean json = ((AbstractResponse<?>)res).getComponentType().isRESTful();
		if(res.isContentType()) {
			// json返却の場合.
			if(json) {
				// JSON返却条件を設定.
				res.setContentType("application/json");
			} else {
				// HTML返却条件を設定.
				res.setContentType("text/html");
			}
		}
		call(state, json, req, res, e);
	}

	/**
	 * HttpError処理を実行.
	 * @param state HTTPステータスを設定します.
	 * @param json エラーが発生した呼び出しコンポーネントが
	 *             [RESFful]の場合は[true]が設定されます.
	 * @param req HttpRequestを設定します.
	 * @param res HttpResponseを設定します.
	 */
	default void call(int state, boolean restful, Request req, Response<?> res) {
		call(state, restful, req, res, null);
	}

	/**
	 * HttpError処理を実行.
	 * @param state HTTPステータスを設定します.
	 * @param json エラーが発生した呼び出しコンポーネントが
	 *             [RESFful]の場合は[true]が設定されます.
	 * @param req HttpRequestを設定します.
	 * @param res HttpResponseを設定します.
	 * @param e 例外を設定します.
	 */
	public void call(int state, boolean restful, Request req, Response<?> res, Throwable e);
}
