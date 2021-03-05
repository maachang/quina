package quina.component;

import quina.http.Request;
import quina.http.Response;

/**
 * HTTPエラー発生時のコンポーネント.
 */
public interface ErrorComponent {
	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	default ComponentType getType() {
		return ComponentType.ERROR;
	}

	/**
	 * HttpError処理を実行.
	 * @param state HTTPステータスを設定します.
	 * @param req HttpRequestを設定します.
	 * @param res HttpResponseを設定します.
	 * @param e 例外を設定します.
	 */
	public void call(int state, Request req, Response<?> res, Throwable e);
}
