package quina.test.route;

import quina.component.restful.RESTfulGetSync;
import quina.http.Params;
import quina.http.Request;
import quina.http.server.response.SyncResponse;
import quina.json.JsonMap;
import quina.route.annotation.AnyRoute;

@AnyRoute
public class AnyJsonGet implements RESTfulGetSync {
	@Override
	public Object get(Request req, SyncResponse res, Params params) {
		return JsonMap.of("hello", "any world!!");
	}
}
