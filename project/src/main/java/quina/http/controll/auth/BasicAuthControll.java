package quina.http.controll.auth;

import quina.http.Request;
import quina.http.Response;

/**
 * Basic認証用コントロール.
 */
public interface BasicAuthControll
	extends AuthUserControll {
	
	/**
	 * Basic認証用のメッセージを定義.
	 * @return String Basic認証用のメッセージを設定します.
	 */
	default String getMessage() {
		return null;
	}
	
	@Override
	default boolean isAccess(Request req, Response<?> res) {
		// basic認証内容が設定されている場合.
		// responseに認証結果が設定されている可能性がある.
		if(HttpAuthUtil.isBasic(req)) {
			// ユーザー名・パスワードを取得.
			String[] userPass = HttpAuthUtil
				.getBasicUserPassword(req);
			if(userPass == null) {
				return false;
			}
			// ユーザー名・パスワード問い合わせ.
			return isAuth(userPass[0], userPass[1]);
		}
		// レスポンスにBasic認証の問い合わせを設定.
		HttpAuthUtil.setBasic(res, getMessage());
		return true;
	}
}
