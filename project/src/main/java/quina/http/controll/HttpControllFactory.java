package quina.http.controll;

import quina.exception.QuinaException;
import quina.http.Request;
import quina.http.Response;
import quina.util.Flag;
import quina.util.collection.IndexKeyValueList;

/**
 * アクセスコントロールファクトリ.
 */
public class HttpControllFactory {
	
	// ファクトリ管理.
	private final IndexKeyValueList<String, HttpControll> list =
		new IndexKeyValueList<String, HttpControll>();
	
	// 定義完了フラグ.
	private final Flag fixed = new Flag(false);
	
	// シングルトン.
	private static final HttpControllFactory SNGL = new HttpControllFactory();
	
	/**
	 * オブジェクトを取得.
	 * @return AuthControllFactory オブジェクトが返却されます.
	 */
	public static final HttpControllFactory getInstance() {
		return SNGL;
	}
	
	/**
	 * 確定済みかチェック
	 * @return boolean true の場合、確定しています.
	 */
	public boolean isFixed() {
		return fixed.get();
	}
	
	// fixしてる場合はエラー.
	private void checkFixed() {
		if(isFixed()) {
			throw new QuinaException("It has already been confirmed.");
		}
	}
	
	// 登録名チェック.
	private void checkName(String name) {
		if(name == null || name.isEmpty()) {
			throw new QuinaException("The name has not been set.");
		}
	}
	
	/**
	 * アクセスコントロールを登録.
	 * @param name 登録名を設定します.
	 * @param controll アクセスコントロールを設定します.
	 */
	public void regster(String name, HttpControll controll) {
		checkFixed();
		checkName(name);
		if(controll == null) {
			throw new QuinaException("access controll is not set.");
		}
		list.put(name, controll);
	}
	
	/**
	 * アクセスコントロールを削除.
	 * @param name 登録名を設定します.
	 */
	public void remove(String name) {
		checkFixed();
		checkName(name);
		list.remove(name);
	}
	
	/**
	 * アクセスコントロールを取得.
	 * @param name 登録名を設定します.
	 * @return AccessControll 対象のアクセスコントロールが返却されます.
	 */
	public HttpControll get(String name) {
		checkName(name);
		return list.get(name);
	}
	
	/**
	 * 対象アクセスコントロールを認証.
	 * @param name 登録名を設定します.
	 * @param req HTTPリクエストを設定します.
	 * @param res HTTPレスポンスを設定します.
	 * @return boolean trueの場合認証されているか、認証が必要としません.
	 */
	public boolean isAccess(String name, Request req, Response<?> res) {
		final HttpControll ctrl = get(name);
		if(ctrl == null) {
			return true;
		}
		return ctrl.isAccess(req, res);
	}
	
	/**
	 * 登録アクセス数が返却されます.
	 * @return int 登録アクセス登録数が返されます.
	 */
	public int size() {
		return list.size();
	}
	
	/**
	 * 登録アクセス名を項番を指定して所得.
	 * @param no 対象の項番を設定します.
	 * @return String 対象の登録アクセス名が返却されます.
	 */
	public String getName(int no) {
		return list.keyAt(no);
	}
}
