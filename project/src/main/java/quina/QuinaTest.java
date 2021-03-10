package quina;

import quina.component.FileComponent;
import quina.component.RESTfulGetSync;
import quina.http.Params;
import quina.http.Request;
import quina.http.response.SyncResponse;
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

		// ルーターを取得.
		quina.getRouter()

		// http://127.0.0.1:3333/
		.route("/", new RESTfulGetSync() {
			@Override
			public Object get(Request req, SyncResponse res, Params params) {
				return new BinarySearchMap<String, Object>("hello", "world");
			}
		})

		// http://127.0.0.1:3333/hoge/moge/100/a/xyz/
		.route("/hoge/moge/${id}/a/${name}/", new RESTfulGetSync() {
			public Object get(Request req, SyncResponse res, Params params) {
				return new BinarySearchMap<String, Object>("params", params);
			}
		}.createValidation(
			"id", "number", "range 10 20"
			,"name", "string", "not null"
		))

		// http://127.0.0.1:3333/public/*
		.route("/public/*", new FileComponent("${HOME}/project/test/quinaTest/"));

		// quinaを開始して、終了まで待機する.
		quina.start().waitToExit();

		System.out.println("## exit QuinaTest.");
	}
}
