package quina.http.controll;

import quina.http.Request;

/**
 * すべてのアクセスを不許可.
 */
public class NoneAccessControll
	implements AccessControll {
	@Override
	public boolean isAccess(Request req) {
		return false;
	}
}
