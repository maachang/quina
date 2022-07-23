package quina.test;

import quina.Quina;
import quina.component.any.AnyComponent;
import quina.component.restful.RESTfulGet;
import quina.component.restful.RESTfulGetSync;
import quina.json.JsonMap;
import quina.logger.LogDefineElement;
import quina.logger.LogFactory;
import quina.promise.Promise;
import quina.validate.VType;
import quina.validate.Validation;

/**
 * Routerにラムダでコンポーネント登録でのQuinaテスト.
 */
public class QuinaTestLmd {

	/**
	 * テストメイン.
	 * @param args
	 * @throws Exception
	 */
	public static final void main(String[] args) throws Exception {
		// Quina初期処理.
		Quina.get().initialize(QuinaTestLmd.class, args);
		// ログの定義を直接セット.
		LogFactory.getInstance().register(
			new LogDefineElement().setDirectory(".log/"));

		// テストプログラムの実行.
		QuinaTestLmd quinaTest = new QuinaTestLmd(args);

		// テスト開始.
		quinaTest.startTest();
	}

	// 外部変数.
	protected String[] args = null;

	// コンストラクタ.
	private QuinaTestLmd(String[] args) {
		this.args = args;
	}

	// テスト開始.
	public void startTest() {
		final Quina quina = Quina.get();
		
		// ルーターを取得.
		quina.getRouter()

		// http://127.0.0.1:3333/
		.route("/", (RESTfulGetSync)(req, res, params) -> {
			return new JsonMap("hello", "world");
		})

		// http://127.0.0.1:3333/largeJson
		.route("/largeJson", (RESTfulGet)(req, res, params) -> {
			res.setGzip(true).sendLargeJSON(new JsonMap("hello", "world"));
		})

		// http://127.0.0.1:3333/hoge/moge/100/a/xyz/
		.route("/hoge/moge/${id}/a/${name}/",
			Validation.of(
				"id", VType.Number, ">= 10"
				,"name", VType.String, "default 'mo_|_ge'"
			),
			(RESTfulGetSync)(req, res, params) -> {
				return new JsonMap("params", params);
		})
		
		// http://127.0.0.1:3333/redirect
		.route("/redirect", (AnyComponent)(req, res) -> {
			res.redirect("https://www.yahoo.co.jp");
		})

		// http://127.0.0.1:3333/forward
		.route("/forward", (AnyComponent)(req, res) -> {
			res.forward("/hoge/moge/5/a/zzz/");
		})

		// http://127.0.0.1:3333/promise
		.route("/promise", (AnyComponent)(req, res) -> {
			// promiseテスト.
			Promise p = new Promise((action) -> {
				action.resolve("abc");
			})
			.then((action, value) -> {
				action.resolve(value + " moge");
			})
			.then((action, value) -> {
				action.resolve(value);
			})
			.error((action, error) -> {
				res.setContentType("text/html");
				res.sendError(error);
			})
			.then((action, value) -> {
				value = value + " xxx";
				res.setContentType("text/html");
				res.send("" + value);
				
				//action.resolve(value);
			});
			p.start();
			/**
			Object o = p.start().await();
			res.setContentType("text/html");
			res.send("" + o);
			**/
		})

		// http://127.0.0.1:3333/promiseAll
		.route("/promiseAll", (AnyComponent)(req, res) -> {
			// Promise.allのテスト.
			Promise a = new Promise((action) -> {
				action.resolve("hoge");
			});

			Promise b = new Promise((action) -> {
				//action.reject("moge");
				action.resolve("moge");
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
				res.setContentType("text/html");
				res.send(buf.toString());
			})
			.error((action, error) -> {
				res.setContentType("text/html");
				res.sendError(error);
			})
			.start();
		})

		// http://127.0.0.1:3333/public/*
		//.route("/public/*", new FileComponent("${HOME}/project/test/quinaTest/"));
		;

		// quinaを開始して、終了まで待機する.
		quina.start().await();

		System.out.println("## exit QuinaTest.");
	}
}
