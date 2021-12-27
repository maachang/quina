package quina.http.controll;

import quina.http.Request;

/**
 * 常にアクセスを許可.
 */
public class AnyAccessControll
	implements AccessControll {
	@Override
	public boolean isAccess(Request req) {
		return true;
	}

}
