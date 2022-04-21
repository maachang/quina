package quina.http.controll.auth;

import quina.http.Header;
import quina.http.HttpStatus;
import quina.http.Request;
import quina.http.Response;
import quina.util.Base64;

/**
 * Http認証系ユーティリティ.
 */
public class HttpAuthUtil {
	private HttpAuthUtil() {}
	
	/**
	 * デフォルトのBasic認証メッセージ.
	 */
	public static final String DEFAULT_BASIC_AUTH_MESSAGE =
		"Please set the access authentication conditions.";
	
	/**
	 * Basic認証のHttpRequestHeaderが存在するかチェック.
	 * @param req HttpRequestを設定します.
	 * @return boolean trueの場合Basic認証のヘッダが設定されています.
	 */
	public static final boolean isBasic(Request req) {
		Header header = req.getHeader();
		// Authorization: Basic <Base64>.
		String basic = header.get("Authorization");
		if(basic != null &&
			basic.indexOf("Basic ") != -1) {
			return true;
		}
		return false;
	}
	
	/**
	 * Basic認証返却を行う.
	 * @param res Basic認証を書き込むHttpResponseを設定します.
	 * @param message Basic認証で表示するメッセージを設定します.
	 */
	public static final void setBasic(Response<?> res, String message) {
		// Basic認証用のヘッダ設定.
		if(message == null || message.isEmpty()) {
			message = DEFAULT_BASIC_AUTH_MESSAGE;
		}
		// WWW-Authenticate: Basic realm="message", charset="UTF-8"
		res.getHeader().put("WWW-Authenticate",
			new StringBuilder("Basic realm=\"")
				.append(message)
				.append("\",charset=\"UTF-8\"")
				.toString());
		// HttpStatus401返却.
		res.setStatus(HttpStatus.AuthorizationRequired);
	}
	
	/**
	 * Basic認証の返却ユーザ・パスワードを取得.
	 * @param req リクエストを設定します.
	 * @return String ["user", "password"] が返却されます.
	 */
	public static final String[] getBasicUserPassword(Request req) {
		try {
			int p;
			Header header = req.getHeader();
			// Authorization: Basic <Base64>.
			String basic = header.get("Authorization");
			if(basic == null ||
				(p = (basic = basic.trim()).indexOf("Basic ")) == -1) {
				return null;
			}
			// Base64をデコード.
			// <user:password>
			basic = new String(
				Base64.decode(
					basic.substring(p + 6).trim()), "UTF8");
			if((p = basic.indexOf(":")) == -1) {
				// 不誠実なBasic認証の内容の場合.
				return null;
			}
			// Basic認証.
			return new String[] {
				basic.substring(0, p), basic.substring(p + 1)};
		} catch(Exception e) {
			return null;
		}
	}

}
