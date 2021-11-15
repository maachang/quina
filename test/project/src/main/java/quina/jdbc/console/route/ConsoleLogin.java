package quina.jdbc.console.route;

import quina.annotation.cdi.Inject;
import quina.annotation.route.Route;
import quina.annotation.validate.Validate;
import quina.component.restful.RESTfulGetSync;
import quina.http.HttpException;
import quina.http.Params;
import quina.http.Request;
import quina.http.server.response.SyncResponse;
import quina.jdbc.console.JDBCConsoleService;
import quina.jdbc.console.QuinaJDBCConsoleConstants;
import quina.json.JsonMap;
import quina.validate.VType;

/**
 * コンソールログイン.
 */
@Validate(name="user", type=VType.String, conditions="null", message=ConsoleLogin.LOGIN_ERROR_MESSAGE)
@Validate(name="password", type=VType.String)
@Validate(name=QuinaJDBCConsoleConstants.LOGIN_SIGNETUER_KEY,
	type=VType.String, conditions="null", message=ConsoleLogin.LOGIN_ERROR_MESSAGE)

@Route("/quina/jdbc/console/{user}/{password}/login")
public class ConsoleLogin implements RESTfulGetSync {
	// ログインエラーメッセージ.
	protected static final String LOGIN_ERROR_MESSAGE =
		"Failed to login to the jdbc console.";
	
	// JDBCコンソールサービス.
	@Inject
	private JDBCConsoleService service;
	
	@Override
	public Object get(Request req, SyncResponse res, Params params) {
		// 指定ユーザー＋パスワードでのログイン認証に失敗.
		if(!service.isAuthLogin(
			params.getString("user"), params.getString("password"))) {
			// 認証失敗.
			throw new HttpException(401, LOGIN_ERROR_MESSAGE);
		}
		// ログイン認証に成功した場合返却用のログイン認証コードを生成.
		String signeture = req.getHeader().getString(QuinaJDBCConsoleConstants.LOGIN_SIGNETUER_KEY);
		return JsonMap.of("status", "success", "authCode", service.createLoginValue(signeture));
	}
}
