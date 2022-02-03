package quina.test.route;

import quina.component.restful.RESTfulGet;
import quina.http.Params;
import quina.http.Request;
import quina.http.server.response.RESTfulResponse;
import quina.route.annotation.Route;

@Route("/timeoutTest")
public class TimeoutRoute implements RESTfulGet {
	@Override
	public void get(Request req, RESTfulResponse res, Params params) {
		System.out.println("timeoutRoute(ms): " + System.currentTimeMillis());
	}
}
