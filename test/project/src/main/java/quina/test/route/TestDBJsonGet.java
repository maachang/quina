package quina.test.route;

import quina.annotation.route.Route;
import quina.component.restful.RESTfulGetSync;
import quina.exception.QuinaException;
import quina.http.Params;
import quina.http.Request;
import quina.http.server.response.SyncResponse;
import quina.jdbc.QuinaConnection;
import quina.jdbc.QuinaDataSource;
import quina.jdbc.QuinaJDBCService;
import quina.jdbc.io.IoStatement;
import quina.json.JsonList;

@Route("/json/jdbc/testTable")
public class TestDBJsonGet implements RESTfulGetSync {
	@Override
	public Object get(Request req, SyncResponse res, Params params) {
		IoStatement io;
		QuinaDataSource ds = QuinaJDBCService.dataSource("testMySql");
		try(QuinaConnection conn = ds.getConnection()) {
			JsonList ret = new JsonList();
			
			// id=1001を取得.
			io = conn.ioStatement();
			io.sql("select id, name, grade from TestTable")
				.sql("where id=?")
				.params(1001)
				.executeQuery()
				.getRows(ret);
			
			// id=2001を取得.
			io.sql("select id, name, grade from TestTable")
				.sql("where id=?")
				.params(2001)
				.executeQuery()
				.getRows(ret);
			
			return ret;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}

}