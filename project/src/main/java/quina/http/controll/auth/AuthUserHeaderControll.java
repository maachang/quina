package quina.http.controll.auth;

import quina.http.Header;
import quina.http.Request;
import quina.http.Response;

/**
 * Headerに対するユーザー名、パスワードによる認証処理.
 */
public interface AuthUserHeaderControll
	extends AuthUserControll, AuthUserDefine {
	
	@Override
	default boolean isAccess(Request req, Response<?> res) {
		// パラメータを取得.
		final Header header = req.getHeader();
		if(header == null) {
			return false;
		}
		// ユーザー名・パスワード問い合わせ.
		return isAuth(header.getString(getUserName()),
				header.getString(getPasswordName()));
	}
}
