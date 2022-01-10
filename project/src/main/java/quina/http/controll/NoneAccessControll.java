package quina.http.controll;

import quina.http.Request;
import quina.http.server.HttpServerRequest;

/**
 * すべてのアクセスを不許可.
 */
public class NoneAccessControll
	implements AccessControll {
	
	// シングルトン.
	private static final NoneAccessControll SNGL =
		new NoneAccessControll();
	
	/**
	 * オブジェクトを取得.
	 * @return NoneAccessControll オブジェクトが返却されます.
	 */
	public static final NoneAccessControll getInstance() {
		return SNGL;
	}
	
	// コンストラクタ.
	protected NoneAccessControll() {}
	
	@Override
	public boolean isAccess(Request req) {
		if(req instanceof HttpServerRequest) {
			return false;
		}
		return false;
	}
}
