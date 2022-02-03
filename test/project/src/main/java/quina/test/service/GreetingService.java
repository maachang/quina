package quina.test.service;

import quina.compile.cdi.annotation.Inject;
import quina.compile.cdi.annotation.ServiceScoped;

@ServiceScoped
public class GreetingService {
	@Inject
	private UppercaseService uppercaseService;
	
	public String greeting(String message) {
		return "hello " + uppercaseService.upperCase(message);
	}
}
