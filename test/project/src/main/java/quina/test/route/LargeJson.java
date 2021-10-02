package quina.test.route;

import quina.component.restful.RESTfulGet;
import quina.http.Params;
import quina.http.Request;
import quina.http.server.response.RESTfulResponse;
import quina.json.JsonMap;

public class LargeJson implements RESTfulGet {

	@Override
	public void get(Request req, RESTfulResponse res, Params params) {
		res.setGzip(true).sendLargeJSON(new JsonMap("hello", "world"));
	}
	

}
