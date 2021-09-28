package quina.component;

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
	public ComponentType getType();

	/**
	 * 対応HTTPメソッド定義を取得.
	 * @return int このコンポーネントが対応するHTTPメソッド定義が返却されます.
	 */
	public int getMethod();

	/**
	 * コンポーネント実行処理.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 */
	public void call(Request req, Response<?> res);
}
