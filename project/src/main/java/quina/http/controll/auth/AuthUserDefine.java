package quina.http.controll.auth;

/**
 * ユーザー・パスワード認証定義.
 */
public interface AuthUserDefine {
	/**
	 * ユーザー名が設定されている認証キー名を取得.
	 * @return String ユーザー名が設定されている
	 *                認証キー名が返却されます.
	 */
	public String getUserName();
	
	/**
	 * パスワードが設定されている認証キー名を取得.
	 * @return String パスワードが設定されている
	 *                認証キー名が返却されます.
	 */
	public String getPasswordName();
}
