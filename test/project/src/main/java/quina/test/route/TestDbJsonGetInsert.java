package quina.test.route;

import quina.annotation.route.Route;
import quina.component.restful.RESTfulGetSync;
import quina.http.Params;
import quina.http.Request;
import quina.http.server.response.SyncResponse;
import quina.jdbc.QuinaConnection;
import quina.jdbc.QuinaDataSource;
import quina.jdbc.QuinaJDBCService;
import quina.jdbc.io.IoStatement;
import quina.json.JsonMap;

@Route("/json/jdbc/{id}/{name}/{grade}/testTableInsert")
public class TestDbJsonGetInsert implements RESTfulGetSync {
	@Override
	public Object get(Request req, SyncResponse res, Params params) {
		IoStatement io = null;
		QuinaDataSource ds = QuinaJDBCService.dataSource("testMySql");
		try(QuinaConnection conn = ds.getConnection()) {
			io = conn.ioStatement();
			io.insert("TestTable", params)
				.commit();
			return new JsonMap("state", "successInsert");
		} catch(Exception e) {
			try(QuinaConnection conn = ds.getConnection()) {
				io = conn.ioStatement();
				io.updateSQL("TestTable", params)
					.sql("where id=?")
					.params(params.getInt("id"))
					.executeUpdate()
					.commit();
				return new JsonMap("state", "successUpdate");
			} catch(Exception ee) {
				e.printStackTrace();
				return new JsonMap("state", "error");
			}
		}
	}

}
