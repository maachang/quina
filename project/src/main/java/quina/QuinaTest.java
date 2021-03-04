package quina;

import quina.component.RESTfulGet;
import quina.logger.LogDefineElement;
import quina.logger.LogFactory;
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
		// ログの定義を直接セット.
		LogFactory.getInstance().register(
			new LogDefineElement().setDirectory("${HOME}/project/test/log/"));

		// テストプログラムの実行.
		QuinaTest quinaTest = new QuinaTest(args);

		// テスト開始.
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
