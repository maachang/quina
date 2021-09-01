package quina.test.route;

import quina.component.RESTfulGet;
import quina.http.Params;
import quina.http.Request;
import quina.http.server.response.RESTfulResponse;
import quina.json.ResultJson;

public class LargeJson implements RESTfulGet {

	@Override
	public void get(Request req, RESTfulResponse res, Params params) {
		res.setGzip(true).sendLargeJSON(new ResultJson("hello", "world"));
	}
	

}
