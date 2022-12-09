package quina.http.controll.auth;

import quina.http.controll.HttpControll;

/**
 * ユーザー名・パスワード認証用アクセスコントロール.
 */
public interface AuthUserControll
	extends HttpControll {
	
	/**
	 * ユーザー名,パスワード認証を行います.
	 * @param user ユーザー名が設定されます.
	 * @param password パスワードが設定されます.
	 * @return boolean trueの場合、認証が成功しました.
	 */
	public boolean isAuth(String user, String password);

}
