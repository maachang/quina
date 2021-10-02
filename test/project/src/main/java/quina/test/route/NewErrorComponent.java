package quina.test.route;

import quina.annotation.route.ErrorRoute;
import quina.component.error.ErrorCdiSyncComponent;
import quina.http.Request;
import quina.http.server.response.SyncResponse;
import quina.json.JsonMap;

@ErrorRoute
public class NewErrorComponent implements ErrorCdiSyncComponent {
	@Override
	public Object jsonCall(int state, Request req, SyncResponse res, Throwable e) {
		return JsonMap.of("error", e.getMessage());
	}

	@Override
	public Object call(int state, Request req, SyncResponse res, Throwable e) {
		return "error: " + e.getMessage();
	}

}
