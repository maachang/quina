package quina.test.route;

import quina.annotation.route.Route;
import quina.component.RESTfulGetSync;
import quina.http.Params;
import quina.http.Request;
import quina.http.server.response.SyncResponse;
import quina.json.ResultJson;

@Route("/abc/json")
public class JsonGet implements RESTfulGetSync {

	@Override
	public Object get(Request req, SyncResponse res, Params params) {
		return ResultJson.of("hoge", 100 , "moge", "abc");
	}

}
