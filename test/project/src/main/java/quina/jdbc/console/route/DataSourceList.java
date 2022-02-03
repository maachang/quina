package quina.jdbc.console.route;

import quina.compile.cdi.annotation.Inject;
import quina.component.restful.RESTfulGetSync;
import quina.http.HttpException;
import quina.http.Params;
import quina.http.Request;
import quina.http.server.response.SyncResponse;
import quina.jdbc.QuinaJDBCService;
import quina.jdbc.console.JDBCConsoleService;
import quina.jdbc.console.QuinaJDBCConsoleConstants;
import quina.json.JsonList;
import quina.json.JsonMap;
import quina.route.annotation.Route;
import quina.validate.VType;
import quina.validate.annotation.Validate;

// LoginシグニチャーValidate.
@Validate(name=QuinaJDBCConsoleConstants.LOGIN_SIGNETUER_KEY,
type=VType.String,
conditions="null",
message=DataSourceList.ERROR_MESSAGE)

// Route.
@Route("/quina/jdbc/console/getDataSources")

/**
 * データソース一覧を取得.
 */
public class DataSourceList implements RESTfulGetSync {
	
	// エラーメッセージ.
	protected static final String ERROR_MESSAGE =
		"The login session has been disconnected.";
	
	// JDBCコンソールサービス.
	@Inject
	private JDBCConsoleService service;
	
	/**
	 * JDBCデータソース一覧が返却されます.
	 * @param req リクエストオブジェクトが設定されます.
	 * @param res レスポンスオブジェクトが設定されます.
	 * @param params パラメータが設定されます.
	 * @return Object レスポンス返却する情報を返却します.
	 */
	@Override
	public Object get(Request req, SyncResponse res, Params params) {
		// IPアクセス制御.
		service.checkAccessControll(req);
		// ログインセッションの認証.
		if(!service.isLoginToken(req, res)) {
			// 失敗した場合.
			throw new HttpException(401, ERROR_MESSAGE);
		}
		// データソース一覧を取得.
		JsonList list = new JsonList();
		QuinaJDBCService.getService().getDataSouceNames(list);
		// 正常終了.
		return JsonMap.of(
			"status", "success"
			,"type", "jdbcConsole"
			,"value", list);
	}
}
