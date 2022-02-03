package quina.jdbc.console.route;

import quina.component.error.ErrorCdiSyncComponent;
import quina.http.Request;
import quina.http.server.response.SyncResponse;
import quina.json.JsonMap;
import quina.route.annotation.ErrorRoute;

/**
 * JDBCConsoleエラー.
 * アクセスurlが "/quina/jdbc/console/" を先頭から含む場合に
 * 実行されます.
 */
@ErrorRoute(route="/quina/jdbc/console/")
public class ConsoleError
	implements ErrorCdiSyncComponent {
	
	/**
	 * JSON返却エラー処理.
	 */
	@Override
	public Object jsonCall(
		int state, Request req, SyncResponse res, Throwable e) {
		return JsonMap.of(
			"status", "error"
			,"httpStatus", state
			,"type", "jdbcConsole"
			,"message", res.getMessage());
	}
	
	/**
	 * 通常エラー処理.
	 */
	@Override
	public Object call(
		int state, Request req, SyncResponse res, Throwable e) {
		return "error(" + state + "): " +
			res.getMessage();
	}
}
