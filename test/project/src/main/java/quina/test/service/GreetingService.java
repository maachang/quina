package quina.test.service;

import quina.annotation.cdi.Inject;
import quina.annotation.cdi.ServiceScoped;
import quina.annotation.log.LogDefine;
import quina.logger.Log;

@ServiceScoped
public class GreetingService {
	@Inject
	private UppercaseService uppercaseService;
	
	@LogDefine("greeting")
	private Log log;
	
	public String greeting(String message) {
		log.info("message = " + message);
		return "hello " + uppercaseService.upperCase(message);
	}
}
