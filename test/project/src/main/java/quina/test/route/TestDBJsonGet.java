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
import quina.jdbc.io.DbResult;
import quina.jdbc.io.IoStatement;
import quina.json.JsonMap;

@Route("/json/jdbc/testTable")
public class TestDBJsonGet implements RESTfulGetSync {
	@Override
	public Object get(Request req, SyncResponse res, Params params) {
		QuinaDataSource ds = QuinaJDBCService.dataSource("testMySql");
		try(QuinaConnection conn = ds.getConnection()) {
			IoStatement io = conn.ioStatement();
			io.sql("select id, name, grade from TestTable").
				sql("where id=?").
				params(1001);
			DbResult rs = io.executeQuery();
			return rs.hasNext() ? JsonMap.of(rs.next()) : JsonMap.of();
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}

}
