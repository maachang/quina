package quina.jdbc.console.route;

import java.util.ArrayList;
import java.util.List;

import quina.compile.cdi.annotation.Inject;
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
import quina.route.annotation.Route;
import quina.util.Alphabet;
import quina.util.Base64;
import quina.util.StringUtil;
import quina.validate.VType;
import quina.validate.annotation.Validate;

//LoginシグニチャーValidate.
@Validate(name=QuinaJDBCConsoleConstants.LOGIN_SIGNETUER_KEY,
type=VType.String,
conditions="null",
message=DataSourceList.ERROR_MESSAGE)

// 実行対象のDataSource名をValidate.
@Validate(name="dataSource",
	type=VType.String,
	conditions="null",
	message="The DataSource name to be executed is not set.")

// 実行対象のSQL(Base64)をValidate.
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
		// IPアクセス制御.
		service.checkAccessControll(req, res);
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
		List<String> sqlList = multipleSql(sql);
		
		sql = null;
		// DataSourceを取得.
		QuinaDataSource ds = QuinaJDBCService
			.getService().getDataSource(dataSource);
		
		// 処理結果受け取りList.
		JsonList resultList = new JsonList();
		// SQL実行.
		executeSqlList(resultList, ds, sqlList);
		
		// SQLListをbase64変換.
		sqlListByBase64(sqlList);
		
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
			String ret = new String(
				Base64.decode(params.getString("sql")), "UTF8");
			params.remove("sql");
			return ret;
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
	
	// 対象のSQLが複数実行のSQLの場合分ける.
	private static final List<String> multipleSql(String sql) {
		List<String> ret = new ArrayList<String>();
		String one;
		int start = 0;
		int pos;
		while(true) {
			pos = StringUtil.indexOfNoQuate(sql, ";", start);
			if(pos == -1) {
				one = sql.substring(start, sql.length()).trim();
				if(!one.isEmpty()) {
					ret.add(trimSql(one));
				}
				break;
			}
			one = sql.substring(start, pos).trim();
			if(!one.isEmpty()) {
				ret.add(trimSql(one));
			}
			start = pos + 1;
		}
		return ret;
	}
	
	// １つのSQLの改行等をクリア.
	private static final String trimSql(String sql) {
		char c;
		int quote = -1;
		boolean beforeYen = false;
		StringBuilder buf = new StringBuilder();
		final int len = sql.length();
		for(int i = 0; i < len; i ++) {
			c = sql.charAt(i);
			if(quote != -1) {
				if(!beforeYen && c == quote) {
					buf.append("\'");
					quote = -1;
				} else {
					buf.append(c);
				}
			} else if(c =='\r') {
				buf.append("");
			} else if(!beforeYen && (c =='\"' || c == '\'')) {
				quote = c;
				buf.append("\'");
			} else if(c == '\t' || c == '\n') {
				buf.append(" ");
			} else {
				buf.append(c);
			}
			if(c == '\\') {
				beforeYen = true;
			} else {
				beforeYen = false;
			}
		}
		return buf.toString();
	}
	
	// SQLListをBase64変換.
	private static final void sqlListByBase64(List<String> sqlList) {
		try {
			String sql;
			final int len = sqlList.size();
			for(int i = 0; i < len; i ++) {
				sql = sqlList.get(i);
				sqlList.set(i, Base64.encode(sql.getBytes("UTF8")));
			}
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
	
	// SQL群を実行.
	private final int executeSqlList(
		JsonList out, QuinaDataSource ds, List<String> sqlList) {
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
	
	// executeQueryの実行が必要なSQL文.
	private static final Object[] executeQueryToSqlHeaderList =
		new Object[] {
		"select", 6, "show", 4
	};
	
	// query実行のSQLの場合.
	private static final boolean isExecuteQueryBySql(String sql) {
		char c;
		String code;
		int pos;
		Object[] list = executeQueryToSqlHeaderList;
		final int len = list.length;
		for(int i = 0; i < len; i += 2) {
			code = (String)list[i];
			pos = (Integer)list[i + 1];
			if(Alphabet.startsWith(sql, code) &&
				((c = sql.charAt(pos)) == ' ' || c == '\t' ||
				c == '\r' || c == '\n')) {
				return true;
			}
		}
		return false;
	}
	
	// 1つのSQLの実行.
	private final void executeSql(
		JsonList out, IoStatement ios, String sql) {
		// executeQuery実行.
		if(isExecuteQueryBySql(sql)) {
			JsonList list = new JsonList();
			// 最大件数を設定して取得.
			ios.sql(sql)
				.executeQuery()
				.getList(list, service.getResultQuerySize());
			out.add(list);
		// executeUpdate実行.
		} else {
			long[] res = new long[1];
			ios.sql(sql).executeUpdate(res);
			out.add(res[0]);
		}
	}
	
	// ロールバック.
	private static final void rollback(IoStatement ios) {
		if(ios != null) {
			try {
				ios.rollback();
			} catch(Exception e) {}
		}
	}
}
