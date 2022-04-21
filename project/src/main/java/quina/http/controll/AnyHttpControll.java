package quina.http.controll;

import quina.http.Request;
import quina.http.Response;

/**
 * 常にアクセスを許可.
 */
public class AnyHttpControll
	implements HttpControll {
	
	// シングルトン.
	private static final AnyHttpControll SNGL =
		new AnyHttpControll();
	
	/**
	 * オブジェクトを取得.
	 * @return AnyAccessControll オブジェクトが返却されます.
	 */
	public static final AnyHttpControll getInstance() {
		return SNGL;
	}
	
	// コンストラクタ.
	protected AnyHttpControll() {}
	
	
	/**
	 * 認証用レスポンスが必要かチェック.
	 * @param req HttpRequestを設定します.
	 * @param res HttpResponseを設定します.
	 * @return boolean trueの場合認証用レスポンス返却が必要です.
	 */
	@Override
	public boolean isAccess(Request req, Response<?> res) {
		return true;
	}
}
