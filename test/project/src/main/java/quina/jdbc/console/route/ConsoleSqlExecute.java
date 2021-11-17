package quina.jdbc.console.route;

import quina.annotation.cdi.Inject;
import quina.annotation.route.Route;
import quina.annotation.validate.Validate;
import quina.component.restful.RESTfulPostSync;
import quina.exception.QuinaException;
import quina.http.HttpException;
import quina.http.Params;
import quina.http.Request;
import quina.http.server.response.SyncResponse;
import quina.jdbc.QuinaConnection;
import quina.jdbc.QuinaDataSource;
import quina.jdbc.QuinaJDBCService;
import quina.jdbc.console.JDBCConsoleService;
import quina.jdbc.console.QuinaJDBCConsoleConstants;
import quina.jdbc.io.IoStatement;
import quina.json.JsonList;
import quina.json.JsonMap;
import quina.util.Alphabet;
import quina.util.Base64;
import quina.util.StringUtil;
import quina.util.Utf8Util;
import quina.util.collection.ObjectList;
import quina.validate.VType;

//LoginシグニチャーValidate.
@Validate(name=QuinaJDBCConsoleConstants.LOGIN_SIGNETUER_KEY,
type=VType.String,
conditions="null",
message=DataSourceList.ERROR_MESSAGE)

// 実行対象のDataSource名.
@Validate(name="dataSource",
	type=VType.String,
	conditions="null",
	message="The DataSource name to be executed is not set.")

// 実行対象のSQL(Base64).
@Validate(name="sql",
	type=VType.String,
	conditions="null",
	message="The SQL statement to be executed is not set.")

//Route.
@Route("/quina/jdbc/console/executeSql")

/**
* 指定SQLを実行.
*/
public class ConsoleSqlExecute implements RESTfulPostSync {

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
	public Object post(Request req, SyncResponse res, Params params) {
		// ログインセッションの認証.
		if(!service.isLoginToken(req, res)) {
			// 失敗した場合.
			throw new HttpException(401, ERROR_MESSAGE);
		}
		// データーソース名を取得.
		String dataSource = params.getString("dataSource");
		// SQL情報を取得.
		String sql = getSql(params);
		// 複数のSQLとして分解.
		ObjectList<String> sqlList = multipleSql(sql);
		sql = null;
		// DataSourceを取得.
		QuinaDataSource ds = QuinaJDBCService
			.getService().getDataSource(dataSource);
		
		// 処理結果受け取りList.
		JsonList resultList = new JsonList();
		// SQL実行.
		executeSqlList(resultList, ds, sqlList);
		
		// 正常終了.
		return JsonMap.of(
			"status", "success"
			,"type", "jdbcConsole"
			,"sqlList", sqlList
			,"value", resultList);
	}
	
	// Base64変換されたSQL文を取得.
	private static final String getSql(Params params) {
		try {
			String ret = Utf8Util.toString(
				Base64.decode(params.getString("sql")));
			params.remove("sql");
			return ret;
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
	
	// 対象のSQLが複数実行のSQLの場合分ける.
	private static final ObjectList<String> multipleSql(String sql) {
		ObjectList<String> ret = new ObjectList<String>();
		String one;
		int start = 0;
		int pos;
		while(true) {
			pos = StringUtil.indexOfNoCote(sql, ";", start);
			if(pos == -1) {
				one = sql.substring(start, sql.length()).trim();
				if(!one.isEmpty()) {
					ret.add(one);
				}
				break;
			}
			one = sql.substring(start, pos).trim();
			if(!one.isEmpty()) {
				ret.add(one);
			}
			start = pos + 1;
		}
		return ret;
	}
	
	// SQL群を実行.
	private static final int executeSqlList(
		JsonList out, QuinaDataSource ds, ObjectList<String> sqlList) {
		int ret = 0;
		final int len = sqlList.size();
		IoStatement ios = null;
		QuinaConnection conn = null;
		try {
			conn = ds.getConnection();
			ios = conn.ioStatement();
			for(int i = 0; i < len; i ++) {
				executeSql(out, ios, sqlList.get(i));
				ret ++;
			}
			ios.commit();
			ios.close(); ios = null;
			conn.close(); conn = null;
			return ret;
		} catch(QuinaException qe) {
			rollback(ios);
			throw qe;
		} catch(Exception e) {
			rollback(ios);
			throw new QuinaException(e);
		} finally {
			if(ios != null) {
				try {
					ios.close();
				} catch(Exception ee) {}
			}
			if(conn != null) {
				try {
					conn.close();
				} catch(Exception ee) {}
			}
		}
	}
	
	private static final void rollback(IoStatement ios) {
		if(ios != null) {
			try {
				ios.rollback();
			} catch(Exception e) {}
		}
	}
	
	// SQLの実行.
	private static final void executeSql(
		JsonList out, IoStatement ios, String sql) {
		char c;
		// executeQuery実行.
		if(Alphabet.startsWith(sql, "select") &&
			((c = sql.charAt(6)) == ' ' || c == '\t' ||
			c == '\r' || c == '\n')) {
			JsonList list = new JsonList();
			ios.sql(sql)
				.executeQuery()
				.getList(list);
			out.add(list);
		// executeUpdate実行.
		} else {
			long[] res = new long[1];
			ios.sql(sql).executeUpdate(res);
			out.add(res[0]);
		}
	}

}
