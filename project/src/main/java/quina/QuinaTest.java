package quina;

import quina.component.RESTfulGet;
import quina.util.collection.BinarySearchMap;

/**
 * QuinaTest.
 */
public class QuinaTest {
	/**
	 * テストメイン.
	 * @param args
	 * @throws Exception
	 */
	public static final void main(String[] args) throws Exception {
		QuinaTest quinaTest = new QuinaTest(args);
		quinaTest.startTest();
	}

	// 外部変数.
	protected String[] args = null;

	// コンストラクタ.
	private QuinaTest(String[] args) {
		this.args = args;
	}

	// テスト開始.
	public void startTest() {
		final Quina quina = Quina.get();

		// JSON送信.
		quina.getRouter().route("/", (RESTfulGet)(req, res, params) -> {
			res.sendJSON(new BinarySearchMap<String, Object>("hello", "world"));
		});
		// quinaを開始して、終了まで待機する.
		quina.start().waitToExit();
	}
}
