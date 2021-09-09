package quina.test.service;

import quina.annotation.cdi.ServiceScoped;

@ServiceScoped
public class UppercaseService {
	public String upperCase(String str) {
		if(str == null || str.isEmpty()) {
			return "";
		}
		return str.toUpperCase();
	}
}
