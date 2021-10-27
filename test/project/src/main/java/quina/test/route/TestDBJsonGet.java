package quina.test.route;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import quina.annotation.route.Route;
import quina.component.restful.RESTfulGetSync;
import quina.exception.QuinaException;
import quina.http.Params;
import quina.http.Request;
import quina.http.server.response.SyncResponse;
import quina.jdbc.QuinaJDBCService;
import quina.json.JsonMap;

@Route("/json/jdbc/testTable")
public class TestDBJsonGet implements RESTfulGetSync {
	@Override
	public Object get(Request req, SyncResponse res, Params params) {
		DataSource ds = QuinaJDBCService.dataSource("testMySql");
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = ds.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select id, name, grade from TestTable where id=1001");
			JsonMap ret;
			if(rs.next()) {
				ret = JsonMap.of(
					"result", "success"
					,"id", rs.getInt("id")
					,"name", rs.getString("name")
					,"grade", rs.getInt("grade")
				);
			} else {
				ret = JsonMap.of("result", "noData");
			}
			rs.close(); rs = null;
			stmt.close(); stmt = null;
			conn.close(); conn = null;
			return ret;
		} catch(Exception e) {
			e.printStackTrace();
			throw new QuinaException(e);
		} finally {
			if(rs != null) {
				try {
					rs.close();
				} catch(Exception e) {}
			}
			if(stmt != null) {
				try {
					stmt.close();
				} catch(Exception e) {}
			}
			if(conn != null) {
				try {
					conn.close();
				} catch(Exception e) {}
			}
		}
	}

}
