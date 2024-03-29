package quina.test.route;

import quina.compile.cdi.annotation.Inject;
import quina.component.restful.RESTfulGetSync;
import quina.http.Params;
import quina.http.Request;
import quina.http.server.response.SyncResponse;
import quina.json.JsonMap;
import quina.logger.Log;
import quina.logger.annotation.LogDefine;
import quina.route.annotation.Route;
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
		return JsonMap.of(
			"geeting", service.greeting(params.getString("name")));
	}

}
