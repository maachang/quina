package quina.test.route;

import quina.component.restful.RESTfulGetSync;
import quina.exception.QuinaException;
import quina.http.Params;
import quina.http.Request;
import quina.http.server.response.SyncResponse;
import quina.jdbc.QuinaConnection;
import quina.jdbc.QuinaDataSource;
import quina.jdbc.QuinaJDBCService;
import quina.jdbc.io.QueryStatement;
import quina.json.JsonList;
import quina.route.annotation.Route;

@Route("/json/jdbc/testTable")
public class TestDBJsonGet implements RESTfulGetSync {
	@Override
	public Object get(Request req, SyncResponse res, Params params) {
		QueryStatement qe;
		QuinaDataSource ds = QuinaJDBCService.dataSource("testdb");
		try(QuinaConnection conn = ds.getConnection()) {
			JsonList ret = new JsonList();
			
			// id=1001を取得.
			qe = conn.queryStatement();
			//qe.sql("select id, name, grade from TestTable")
			//qe.selectSQL("TestTable", "id", "name", "grade")
			//	.sql("where id=?")
			//	.params(1001)
			//	.executeQuery()
			//	.getList(ret);
			qe.selectRow(ret, "TestTable",
				"id", 1001);
			
			// id=2001を取得.
			//qe.sql("select id, name, grade from TestTable")
			//qe.selectSQL("TestTable", "id", "name", "grade")
			//	.sql("where id=?")
			//	.params(2001)
			//	.executeQuery()
			//	.getList(ret);
			qe.selectRow(ret, "TestTable",
				"id", 2001);
			
			return ret;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}

}
