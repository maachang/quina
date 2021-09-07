package quina.test.route;

import quina.annotation.cdi.Inject;
import quina.annotation.log.LogDefine;
import quina.annotation.route.Route;
import quina.component.RESTfulGetSync;
import quina.http.Params;
import quina.http.Request;
import quina.http.server.response.SyncResponse;
import quina.json.ResultJson;
import quina.logger.Log;
import quina.test.service.GreetingService;

@Route("/json/{name}/greeting")
public class GreetingJsonGet implements RESTfulGetSync {
	
	@LogDefine("greeting")
	private Log log;
	
	@Inject
	private GreetingService service;

	@Override
	public Object get(Request req, SyncResponse res, Params params) {
		log.info("greeting accees: " + params.getString("name"));
		return ResultJson.of(
			"geeting", service.greeting(params.getString("name")));
	}

}
