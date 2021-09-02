package quina.test.route;

import quina.annotation.route.Any;
import quina.component.RESTfulGetSync;
import quina.http.Params;
import quina.http.Request;
import quina.http.server.response.SyncResponse;
import quina.json.ResultJson;

@Any
public class AnyJsonGet implements RESTfulGetSync {
	@Override
	public Object get(Request req, SyncResponse res, Params params) {
		return ResultJson.of("hello", "any world!!");
	}
}
