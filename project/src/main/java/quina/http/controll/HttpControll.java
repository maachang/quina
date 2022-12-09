package quina.http.controll;

import quina.http.Request;
import quina.http.Response;

/**
 * HTTPアクセスコントロール.
 */
public interface HttpControll {
	/**
	 * アクセスが制限されてるかチェック.
	 * @param req HttpRequestを設定します.
	 * @param res HttpResponseを設定します.
	 * @return boolean trueの場合アクセス可能です.
	 */
	public boolean isAccess(Request req, Response<?> res);
}
