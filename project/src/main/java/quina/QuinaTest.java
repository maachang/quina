package quina;

import quina.component.Component;
import quina.component.FileComponent;
import quina.component.RESTfulGetSync;
import quina.json.ResultJson;
import quina.logger.LogDefineElement;
import quina.logger.LogFactory;
import quina.promise.Promise;
import quina.validate.Validation;

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
		.route("/", (RESTfulGetSync)(req, res, params) -> {
				return new ResultJson("hello", "world");
			})

		// http://127.0.0.1:3333/hoge/moge/100/a/xyz/
		.route("/hoge/moge/${id}/a/${name}/",
			new Validation(
				"id", "number", ">= 10"
				,"name", "string", "default 'mo_|_ge'"
			),
			(RESTfulGetSync)(req, res, params) -> {
				return new ResultJson("params", params);
			})

		// http://127.0.0.1:3333/redirect
		.route("/redirect", (Component)(method, req, res) -> {
			res.redirect("https://www.yahoo.co.jp");
		})

		// http://127.0.0.1:3333/forward
		.route("/forward", (Component)(method, req, res) -> {
			res.forward("/hoge/moge/5/a/zzz/");
		})

		// http://127.0.0.1:3333/promise
		.route("/promise", (Component)(method, req, res) -> {
			// promiseテスト.
			System.out.println("0: thread: " + Thread.currentThread().getId());
			new Promise("hoge")
				.then((action, value) -> {
					//System.out.println("1: thread: " + Thread.currentThread().getId());
					action.resolve(value + " moge");
				})
				.then((action, value) -> {
					//System.out.println("2: thread: " + Thread.currentThread().getId());
					action.getResponse().setContentType("text/html");
					//throw new Exception("error");
					action.resolve(value);
				})
				.error((action, error) -> {
					//System.out.println("3: thread: " + Thread.currentThread().getId());
					//System.out.println("3: error: " + error);
					action.sendError(error);
				})
				.then((action, value) -> {
					//System.out.println("4: thread: " + Thread.currentThread().getId());
					//throw new Exception("error");
					action.send("success: " + value);
				})
				.start(req, res);
		})

		// http://127.0.0.1:3333/public/*
		.route("/public/*", new FileComponent("${HOME}/project/test/quinaTest/"));

		// quinaを開始して、終了まで待機する.
		quina.start().waitToExit();

		System.out.println("## exit QuinaTest.");
	}
}
