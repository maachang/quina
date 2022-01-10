package quina.http.controll.auth;

import quina.http.Request;
import quina.http.Response;
import quina.http.controll.AccessControll;

/**
 * 認証用アクセスコントロール.
 */
public interface AuthControll
	extends AccessControll {
	
	// 認証処理の呼び出し順.
	// isAuthResponseを呼び出す.
	//   <isAuthResponse返却がtrueの場合>
	//     appendResponseを呼び出して、空のレスポンス返却を行う.
	//   <isAuthResponse返却がfalseの場合>
	//     isAccessを呼び出す.
	
	/**
	 * 認証用レスポンスが必要かチェック.
	 * @param req HTTPリクエストを設定します.
	 * @return boolean trueの場合認証用レスポンス返却が必要です.
	 */
	default boolean isAuthResponse(Request req) {
		// 認証用レスポンスは不要.
		return false;
	}
	
	/**
	 * 認証処理前に返却するレスポンス設定を行う場合に利用します.
	 * @param res Responseオブジェクトを設定します.
	 * @return boolean 認証用レスポンス返却が必要な場合trueが返却されます.
	 */
	default boolean appendResponse(Response<?> res) {
		// 認証用レスポンスなし.
		return false;
	}
}
