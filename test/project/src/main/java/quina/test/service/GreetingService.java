package quina.test.service;

import quina.annotation.cdi.ServiceScoped;

@ServiceScoped
public class GreetingService {
	public String greeting(String message) {
		return "hello " + message ;
	}
}
