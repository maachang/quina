package quina;

import quina.component.FileComponent;
import quina.component.NormalComponent;
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
		.route("/redirect", (NormalComponent)(req, res) -> {
			res.redirect("https://www.yahoo.co.jp");
		})

		// http://127.0.0.1:3333/forward
		.route("/forward", (NormalComponent)(req, res) -> {
			res.forward("/hoge/moge/5/a/zzz/");
		})

		// http://127.0.0.1:3333/promise
		.route("/promise", (NormalComponent)(req, res) -> {
			// promiseテスト.
			Promise p = new Promise((action) -> {
				action.resolve("abc");
			})
			.then((action, value) -> {
				action.resolve(value + " moge");
			})
			.then((action, value) -> {
				action.getResponse().setContentType("text/html");
				action.resolve(value);
			})
			.error((action, error) -> {
				action.sendError(error);
			})
			.then((action, value) -> {
				value = value + " xxx";
				action.resolve(value);
			})
			.start(req, res);
			// 処理結果を同期取得.
			Object o = p.waitTo();
			res.send("" + o);
		})

		// http://127.0.0.1:3333/promise2
		.route("/promise2", (NormalComponent)(req, res) -> {
			// Promise.allのテスト.
			Promise a = new Promise((action) -> {
				action.resolve("hoge");
			});

			Promise b = new Promise((action) -> {
				action.reject("moge");
			});

			Promise c = new Promise((action) -> {
				action.resolve("abc");
			});

			// allで処理を行う.
			Promise.all(a, b, c)
			.then((action, value) -> {
				Object[] lst = (Object[])value;
				int len = lst.length;
				StringBuilder buf = new StringBuilder("success: ");
				for(int i = 0; i < len; i ++) {
					if(i != 0) {
						buf.append(", ");
					}
					buf.append("[").append(i+1).append("] ").append(lst[i]);
				}
				action.getResponse().setContentType("text/html");
				res.send(buf.toString());
			})
			.error((action, error) -> {
				System.out.println("error");
				action.getResponse().setContentType("text/html");
			})
			.start(req, res).waitTo();
		})

		// http://127.0.0.1:3333/public/*
		.route("/public/*", new FileComponent("${HOME}/project/test/quinaTest/"));

		// quinaを開始して、終了まで待機する.
		quina.start().waitTo();

		System.out.println("## exit QuinaTest.");
	}
}
