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

// ユーザ名Validate.
@Validate(name="user",
	type=VType.String,
	conditions="null",
	message=ConsoleLogin.ERROR_MESSAGE)

// パスワードValidate.
@Validate(name="password",
	type=VType.String)

// [header]LoginシグニチャーValidate.
@Validate(name=QuinaJDBCConsoleConstants.LOGIN_SIGNETUER_KEY,
	type=VType.String,
	conditions="null",
	message=ConsoleLogin.ERROR_MESSAGE)

// Route.
@Route("/quina/jdbc/console/{user}/{password}/login")

/**
 * コンソールログイン処理.
 */
public class ConsoleLogin implements RESTfulGetSync {
	// エラーメッセージ.
	protected static final String ERROR_MESSAGE =
		"Failed to login to the jdbc console.";
	
	// JDBCコンソールサービス.
	@Inject
	private JDBCConsoleService service;
	
	/**
	 * ログイン認証.
	 * @param req リクエストオブジェクトが設定されます.
	 * @param res レスポンスオブジェクトが設定されます.
	 * @param params パラメータが設定されます.
	 * @return Object レスポンス返却する情報を返却します.
	 */
	@Override
	public Object get(Request req, SyncResponse res, Params params) {
		// IPアクセス制御.
		service.checkAccessControll(req);
		// 指定ユーザー＋パスワードでのログイン認証に失敗.
		if(!service.isAuthLogin(
			params.getString("user"), params.getString("password"))) {
			// 認証失敗.
			throw new HttpException(401, ERROR_MESSAGE);
		}
		// ログイン認証に成功した場合返却用のログイン認証コードを生成.
		String signeture = params.getString(
			QuinaJDBCConsoleConstants.LOGIN_SIGNETUER_PARAM);
		// 新しいログイン認証トークンを生成.
		String authToken = service.createLoginToken(signeture);
		// ログイン認証トークンをレスポンスHeaderに設定.
		res.getHeader().put(
			QuinaJDBCConsoleConstants.LOGIN_AUTH_TOKEN, authToken);
		// 処理結果を返却.
		return JsonMap.of(
			"status", "success"
			,"type", "jdbcConsole");
	}
}
