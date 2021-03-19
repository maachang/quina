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
			//System.out.println("0: thread: " + Thread.currentThread().getId());
			Promise p = new Promise((action) -> {
				//System.out.println("init: " + action.getStatus());
				action.resolve("abc");
			})
				.then((action, value) -> {
					//System.out.println("1): " + action.getStatus());
					//System.out.println("1: thread: " + Thread.currentThread().getId());
					action.resolve(value + " moge");
				})
				.then((action, value) -> {
					//System.out.println("2): " + action.getStatus());
					//System.out.println("2: thread: " + Thread.currentThread().getId());
					action.getResponse().setContentType("text/html");
					//action.send("" + value);
					//throw new Exception("error");
					action.resolve(value);
					//action.end(value);
				})
				.error((action, error) -> {
					//System.out.println("3): " + action.getStatus());
					//System.out.println("3: thread: " + Thread.currentThread().getId());
					//System.out.println("3: error: " + error);
					action.sendError(error);
				})
				.then((action, value) -> {
					//System.out.println("4): " + action.getStatus());
					//System.out.println("4: thread: " + Thread.currentThread().getId());
					//throw new Exception("error");
					//action.send("success: " + value);
					value = value + " xxx";
					action.resolve(value);
				})
				.start(req, res);
				Object o = p.waitTo();
				//System.out.println("o: " + o);
				res.send("" + o);
		})

		// http://127.0.0.1:3333/promise2
		.route("/promise2", (NormalComponent)(req, res) -> {
			Promise a = new Promise((action) -> {
				action.resolve("hoge");
			});

			Promise b = new Promise((action) -> {
				action.reject("moge");
			});

			Promise c = new Promise((action) -> {
				action.resolve("abc");
			});

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
				//action.resolve();
			})
			.error((action, error) -> {
				action.getResponse().setContentType("text/html");
				res.send("error: " + error);
				//action.reject(null);
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
