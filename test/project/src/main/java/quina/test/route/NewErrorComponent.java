package quina.test.route;

import quina.annotation.route.ErrorRoute;
import quina.component.ErrorComponent;
import quina.http.Request;
import quina.http.server.response.NormalResponse;
import quina.json.ResultJson;

@ErrorRoute
public class NewErrorComponent implements ErrorComponent {

	@Override
	public void jsonCall(int state, Request req, NormalResponse res, Throwable e) {
		res.sendJSON(ResultJson.of("error", e.getMessage()));
	}

	@Override
	public void call(int state, Request req, NormalResponse res, Throwable e) {
		res.send("error: " + e.getMessage());
	}

}
