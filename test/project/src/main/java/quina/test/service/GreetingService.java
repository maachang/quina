package quina.test.service;

import quina.annotation.cdi.Inject;
import quina.annotation.cdi.ServiceScoped;

@ServiceScoped
public class GreetingService {
	@Inject
	private UppercaseService uppercaseService;
	
	public String greeting(String message) {
		return "hello " + uppercaseService.upperCase(message);
	}
}
