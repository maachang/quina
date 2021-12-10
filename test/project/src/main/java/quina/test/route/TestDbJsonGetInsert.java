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
		QuinaDataSource ds = QuinaJDBCService.dataSource("testdb");
		try(QuinaConnection conn = ds.getConnection()) {
			io = conn.ioStatement();
			// １行データを注入・更新.
			io.upsert("TestTable", "id", params)
				.commit();
			return JsonMap.of("state", "success");
		} catch(Exception e) {
			e.printStackTrace();
			return JsonMap.of("state", "error");
		}
	}
}
