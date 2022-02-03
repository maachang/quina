package quina.test.service;

import quina.Quina;
import quina.compile.cdi.annotation.ServiceScoped;

@ServiceScoped
public class UppercaseService {
	public String upperCase(String str) {
		System.out.println("QuinaContext: " + Quina.getHttpContext());
		if(str == null || str.isEmpty()) {
			return "";
		}
		return str.toUpperCase();
	}
}
