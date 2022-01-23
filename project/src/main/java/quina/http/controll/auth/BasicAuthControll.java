package quina.http.controll.auth;

import quina.http.Header;
import quina.http.HttpStatus;
import quina.http.Request;
import quina.http.Response;
import quina.util.Base64;

/**
 * Basic認証用コントロール.
 */
public interface BasicAuthControll
	extends AuthControll {
	
	/**
	 * デフォルトの認証メッセージ.
	 */
	public static final String DEFAULT_AUTH_MESSAGE =
		"Please set the access authentication conditions.";
	
	/**
	 * Basic認証を行います.
	 * @param user ユーザー名が設定されます.
	 * @param password パスワードが設定されます.
	 * @return boolean trueの場合、認証が成功しました.
	 */
	public boolean isAuth(String user, String password);
	
	/**
	 * レスポンス返却される認証用メッセージを設定します.
	 * @return String メッセージが返却されます.
	 */
	default String getAuthMessage() {
		// デフォルトのメッセージを利用.
		return null;
	}
	
	@Override
	default boolean isAuthResponse(Request req) {
		if(req.getHeader().containsKey("Authorization")) {
			// リクエストにAuthorizationヘッダが存在する
			// 場合は不要.
			return false;
		}
		// リクエストにAuthorizationヘッダが存在しない
		// 場合は必要.
		return true;
	}
	
	@Override
	default boolean appendResponse(Response<?> res) {
		// Basic認証用のヘッダ設定.
		String message = getAuthMessage();
		if(message == null || message.isEmpty()) {
			message = DEFAULT_AUTH_MESSAGE;
		}
		// WWW-Authenticate: Basic realm="message", charset="UTF-8"
		res.getHeader().put("WWW-Authenticate",
			new StringBuilder("Basic realm=\"")
				.append(message)
				.append("\",charset=\"UTF-8\"")
				.toString());
		// HttpStatus401返却.
		res.setStatus(HttpStatus.AuthorizationRequired);
		return true;
	}
	
	@Override
	default boolean isAccess(Request req) {
		// ヘッダで認証処理.
		return isBasicAuth(req.getHeader());
	}
	
	/**
	 * Basic認証.
	 * @param header リクエストヘッダを設定します.
	 * @return boolean trueの場合、認証は成功しました.
	 */
	default boolean isBasicAuth(Header header) {
		try {
			int p;
			// Authorization: Basic <Base64>.
			String basic = header.get("Authorization");
			if(basic == null ||
				(p = (basic = basic.trim()).indexOf("Basic ")) == -1) {
				return false;
			}
			// Base64をデコード.
			// <user:password>
			basic = new String(
				Base64.decode(
					basic.substring(p + 6).trim()), "UTF8");
			if((p = basic.indexOf(":")) == -1) {
				// 不誠実なBasic認証の内容の場合.
				return false;
			}
			// Basic認証.
			return isAuth(
				basic.substring(0, p), basic.substring(p + 1));
		} catch(Exception e) {
			return false;
		}
	}
}
