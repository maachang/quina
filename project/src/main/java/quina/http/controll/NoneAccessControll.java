package quina.http.controll;

import quina.http.Request;
import quina.http.Response;

/**
 * すべてのアクセスを不許可.
 */
public class NoneAccessControll
	implements HttpControll {
	
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
	public boolean isAccess(Request req, Response<?> res) {
		return false;
	}
}
