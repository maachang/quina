package quina.http.controll.auth;

import quina.http.Params;
import quina.http.Request;
import quina.http.Response;

/**
 * パラメータに対するユーザー名、パスワードによる認証処理.
 */
public interface AuthUserParamControll
	extends AuthUserControll, AuthUserDefine {
	
	@Override
	default boolean isAccess(Request req, Response<?> res) {
		// パラメータを取得.
		final Params params = req.getParams();
		if(params == null) {
			return false;
		}
		// ユーザー名・パスワード問い合わせ.
		return isAuth(params.getString(getUserName()),
				params.getString(getPasswordName()));
	}
}
