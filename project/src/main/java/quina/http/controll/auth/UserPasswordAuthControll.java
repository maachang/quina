package quina.http.controll.auth;

import quina.http.Params;
import quina.http.Request;

/**
 * ユーザー名、パスワードによる認証処理.
 */
public interface UserPasswordAuthControll
	extends AuthControll {
	
	/**
	 * ユーザー名,パスワード認証を行います.
	 * @param user ユーザー名が設定されます.
	 * @param password パスワードが設定されます.
	 * @return boolean trueの場合、認証が成功しました.
	 */
	public boolean isAuth(String user, String password);
	
	/**
	 * ユーザー名が設定されているパラメータキー名を取得.
	 * @return String ユーザー名が設定されている
	 *                パラメータキー名が返却されます.
	 */
	public String getUserKeyName();
	
	/**
	 * パスワードが設定されているパラメータキー名を取得.
	 * @return String パスワードが設定されている
	 *                パラメータキー名が返却されます.
	 */
	public String getPasswordKeyName();
	
	@Override
	default boolean isAccess(Request req) {
		// パラメータで処理.
		return isUserPasswordAuth(req.getParams());
	}
	
	/**
	 * ユーザーパスワード認証.
	 * @param header リクエストパラメータを設定します.
	 * @return boolean trueの場合、認証は成功しました.
	 */
	default boolean isUserPasswordAuth(Params params) {
		return isAuth(params.getString(getUserKeyName()),
				params.getString(getPasswordKeyName()));
	}
}
