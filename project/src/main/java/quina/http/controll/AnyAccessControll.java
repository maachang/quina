package quina.http.controll;

import quina.http.Request;
import quina.http.server.HttpServerRequest;

/**
 * 常にアクセスを許可.
 */
public class AnyAccessControll
	implements AccessControll {
	
	// シングルトン.
	private static final AnyAccessControll SNGL =
		new AnyAccessControll();
	
	/**
	 * オブジェクトを取得.
	 * @return AnyAccessControll オブジェクトが返却されます.
	 */
	public static final AnyAccessControll getInstance() {
		return SNGL;
	}
	
	// コンストラクタ.
	protected AnyAccessControll() {}
	
	@Override
	public boolean isAccess(Request req) {
		if(req instanceof HttpServerRequest) {
			return true;
		}
		return true;
	}
}
