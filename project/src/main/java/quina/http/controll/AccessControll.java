package quina.http.controll;

import quina.http.Request;

/**
 * HTTPアクセスコントロール.
 */
public interface AccessControll {
	/**
	 * アクセスが制限されてるかチェック.
	 * @param req HttpRequestを設定します.
	 * @return boolean trueの場合アクセス可能です.
	 */
	public boolean isAccess(Request req);
}
