package quina;

import quina.component.EtagManagerInfo;
import quina.exception.QuinaException;
import quina.http.server.HttpServerCall;
import quina.http.server.HttpServerInfo;
import quina.http.server.HttpServerService;
import quina.http.worker.HttpWorkerInfo;
import quina.http.worker.HttpWorkerService;
import quina.logger.LogFactory;
import quina.net.nio.tcp.NioUtil;
import quina.net.nio.tcp.worker.NioWorkerThreadManager;
import quina.net.nio.tcp.worker.WorkerElement;
import quina.promise.PromiseWorkerManager;
import quina.shutdown.ShutdownCall;
import quina.shutdown.ShutdownConstants;
import quina.shutdown.ShutdownManager;
import quina.shutdown.ShutdownManagerInfo;
import quina.util.Args;
import quina.util.AtomicObject;
import quina.util.Env;
import quina.util.FileUtil;
import quina.util.collection.IndexMap;
import quina.util.collection.ObjectList;

/**
 * Quina.
 */
public class Quina {
	// シングルトン.
	private static final Quina SNGL = new Quina();

	// 基本的なQuinaShutdownToken.
	private static final String DEFAULT_TOKEN = "@aniuq";

	// コンフィグディレクトリ.
	private String configDir = null;

	// ルータオブジェクト.
	private final Router router;

	// 基本サービス: HttpServerService.
	// Httpサーバー関連のサービスを管理します.
	private final HttpServerService httpServerService;

	// 基本サービス: HttpWorkerService.
	// Httpワーカースレッド関連を管理します.
	private HttpWorkerService httpWorkerService;

	// Quinaサービス管理.
	private QuinaServiceManager quinaServiceManager;

	// シャットダウンマネージャー.
	private ShutdownManager shutdownManager;

	// 実行引数管理.
	private Args args;

	// 実行中のワーカースレッドマネージャ.
	private final AtomicObject<NioWorkerThreadManager> workerManager =
		new AtomicObject<NioWorkerThreadManager>();

	// コンストラクタ.
	private Quina() {
		// ネットワーク初期処理.
		NioUtil.initNet();
		// シャットダウンデフォルトトークンを設定.
		ShutdownConstants.setDefaultToken(DEFAULT_TOKEN);
		// ルーターオブジェクト生成.
		this.router = new Router();
		// シャットダウンマネージャを生成し、quinaシャットダウンコールをセット.
		this.shutdownManager = new ShutdownManager();
		this.shutdownManager.getInfo().register(new QuinaShutdownCall());
		// quinaServiceを管理するマネージャを生成.
		this.quinaServiceManager = new QuinaServiceManager();
		// HTTP関連サービスを生成.
		this.httpWorkerService = new HttpWorkerService();
		this.httpServerService = new HttpServerService(this.httpWorkerService);
	}

	/**
	 * quinaを取得.
	 * @return Quina quinaが返却されます.
	 */
	public static final Quina get() {
		return SNGL;
	}

	/**
	 * routerを取得.
	 * @return Router ルータが返却されます.
	 */
	public static final Router router() {
		return SNGL.getRouter();
	}

	/**
	 * ルータを取得.
	 * @return Router ルータが返却されます.
	 */
	public Router getRouter() {
		return router;
	}

	/**
	 * 既にサービスが稼働/停止している場合はエラー返却.
	 * @param flg [true]の場合、開始中 [false]の場合、停止中の場合、
	 *            エラーが発生します.
	 */
	protected void check(boolean flg) {
		// 基本サービスのチェック.
		httpWorkerService.check(flg);
		httpServerService.check(flg);
		// 登録サービスのチェック.
		final int len = quinaServiceManager.size();
		for(int i = 0; i < len; i ++) {
			quinaServiceManager.get(i).check(flg);
		}
	}

	/**
	 * コンフィグディレクトリを設定.
	 * @param configDir コンフィグディレクトリを設定します.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina setConfigDirectory(String configDir) {
		check(true);
		try {
			if(configDir == null || configDir.isEmpty()) {
				this.configDir = null;
			} else {
				configDir = FileUtil.getFullPath(Env.path(configDir));
				if(!configDir.endsWith("/")) {
					configDir = configDir + "/";
				}
				this.configDir = configDir;
			}
		} catch(Exception e) {
			throw new QuinaException(e);
		}
		return this;
	}

	/**
	 * コンフィグディレクトリを取得.
	 * @return String 設定されているコンフィグディレクトリが返却されます.
	 */
	public String getConfigDirectory() {
		return configDir;
	}

	/**
	 * コンフィグ情報を読み込む.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina loadConfig() {
		return loadConfig(null);
	}

	/**
	 * コンフィグ情報を読み込む.
	 * @param configDir コンフィグディレクトリを設定します.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina loadConfig(String configDir) {
		check(true);
		// コンフィグディレクトリを設定.
		setConfigDirectory(configDir);
		configDir = getConfigDirectory();
		// ログのコンフィグ定義.
		loadLogConfig(configDir);
		// Promiseワーカーのコンフィグ設定.
		PromiseWorkerManager.getInstance().getConfig().loadConfig(configDir);
		// シャットダウンマネージャのコンフィグ定義.
		loadShutdownManagerConfig(configDir);
		// Etagマネージャのコンフィグ定義.
		loadEtagManagerConfig(configDir);
		// 標準コンポーネントのコンフィグ情報を読み込む.
		httpWorkerService.readConfig(configDir);
		httpServerService.readConfig(configDir);
		// 登録サービスのコンフィグ情報を読み込む.
		final int len = quinaServiceManager.size();
		for(int i = 0; i < len; i ++) {
			quinaServiceManager.get(i).readConfig(this.configDir);
		}
		return this;
	}

	// ログのコンフィグ定義.
	private static final void loadLogConfig(String configDir) {
		final LogFactory logFactory = LogFactory.getInstance();
		// LogFactoryが既にコンフィグ設定されている場合.
		if(logFactory.isConfig()) {
			return;
		}
		// log.jsonのコンフィグファイルを取得.
		IndexMap<String, Object> json = QuinaUtil.loadJson(configDir, "log");
		if(json == null) {
			return;
		}
		// ログコンフィグをセット.
		logFactory.config(json);
	}

	// シャットダウンマネージャのコンフィグ条件を設定.
	private final void loadShutdownManagerConfig(String configDir) {
		final ShutdownManagerInfo info = shutdownManager.getInfo();
		// shutdownManagerのコンフィグが既に設定されている場合.
		if(info.isConfig()) {
			return;
		}
		// shutdown.jsonのコンフィグファイルを取得.
		IndexMap<String, Object> json = QuinaUtil.loadJson(configDir, "shutdown");
		if(json == null) {
			return;
		}
		// shutdownManagerのコンフィグ条件をセット.
		info.config(json);
	}

	// Etagマネージャのコンフィグ条件を設定.
	private final void loadEtagManagerConfig(String configDir) {
		final EtagManagerInfo info = router.getEtagManagerInfo();
		if(info.isDone()) {
			return;
		}
		// etag.jsonのコンフィグファイルを取得.
		IndexMap<String, Object> json = QuinaUtil.loadJson(configDir, "etag");
		if(json == null) {
			return;
		}
		// etagManagerのコンフィグ条件をセット.
		info.config(json);
	}

	/**
	 * シャットダウンマネージャ情報を取得.
	 * @return ShutdownManagerInfo シャットダウンマネージャ情報が返却されます.
	 */
	public ShutdownManagerInfo getShutdownManagerInfo() {
		return shutdownManager.getInfo();
	}

	/**
	 * Etag管理定義情報を取得.
	 * @return EtagManagerInfo Etag管理定義情報が返却されます.
	 */
	public EtagManagerInfo getEtagManagerInfo() {
		return router.getEtagManagerInfo();
	}

	/**
	 * HttpWorkerInfoを取得.
	 * @return HttpWorkerInfo HttpWorkerInfoが返却されます.
	 */
	public HttpWorkerInfo getHttpWorkerInfo() {
		return (HttpWorkerInfo)httpWorkerService.getInfo();
	}

	/**
	 * HttpServerInfoを取得.
	 * @return HttpServerInfo HttpServerInfoが返却されます.
	 */
	public HttpServerInfo getHttpServerInfo() {
		return (HttpServerInfo)httpServerService.getInfo();
	}

	/**
	 * 全てのQuinaサービスを開始処理.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina start() {
		check(true);
		try {
			// AutoRouter読み込みを実行.
			router.autoRoute();
			// Etag管理情報を取得.
			final EtagManagerInfo etagManagerInfo =
				router.getEtagManagerInfo();
			// Etag管理情報が確定されていない場合は確定する.
			if(!etagManagerInfo.isDone()) {
				etagManagerInfo.done();
			}
			// 登録されたQuinaServiceを起動.
			QuinaService qs;
			final int len = quinaServiceManager.size();
			for(int i = 0; i < len; i ++) {
				(qs = quinaServiceManager.get(i)).startService();
				qs.awaitStartup();
			}
			// 基本サービスを起動.
			// ワーカー起動で、最後にサーバー起動.
			httpWorkerService.startService();
			httpWorkerService.awaitStartup();
			httpServerService.startService();
			httpServerService.awaitStartup();
			// ワーカースレッドマネージャを取得.
			workerManager.set(
				httpWorkerService.getNioWorkerThreadManager());
			return this;
		} catch(QuinaException qe) {
			try {
				stop();
			} catch(Exception e) {}
			throw qe;
		}
	}

	/**
	 * Quinaサービス開始処理[start()]が呼ばれたかチェック.
	 * @return boolean [true]の場合開始しています.
	 */
	public boolean isStart() {
		// 基本サービスの開始処理[start()]が呼び出された場合.
		if(httpServerService.isStartService() &&
			httpWorkerService.isStartService()) {
			// 登録サービスの開始処理[start()]が呼び出されたかチェック.
			final int len = quinaServiceManager.size();
			for(int i = 0; i < len; i ++) {
				if(!quinaServiceManager.get(i).isStartService()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
 	 * 全てのQuinaサービスが起動済みかチェック.
	 * @return boolean trueの場合、全てのQuinaサービスが起動しています.
	 */
	public boolean isStarted() {
		// 基本サービスが起動している場合.
		if(httpServerService.isStarted() &&
			httpWorkerService.isStarted()) {
			// 登録サービスが起動しているかチェック.
			final int len = quinaServiceManager.size();
			for(int i = 0; i < len; i ++) {
				if(!quinaServiceManager.get(i).isStarted()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 全てのQuinaサービスが起動済みになるまで待機.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina awaitStarted() {
		// 全てのQuinaサービスが起動済みになるまで待機.
		while(!isExit()) {
			QuinaUtil.sleep(50L);
		}
		return this;
	}

	/**
	 * 全てのQuinaサービスを終了開始処理.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina stop() {
		// 基本サービスを停止.
		// 最初にサーバ停止で、次にワーカー停止.
		httpServerService.stopService();
		httpServerService.awaitExit();
		httpWorkerService.stopService();
		httpWorkerService.awaitExit();
		// 登録されたサービスを後ろから停止.
		QuinaService qs;
		final int len = quinaServiceManager.size();
		for(int i = len - 1; i >= 0; i ++) {
			(qs = quinaServiceManager.get(i)).stopService();
			qs.awaitExit();
		}
		return this;
	}

	/**
	 * 全てのQuinaのサービスが終了完了したかチェック.
	 * @return boolean trueの場合、全てのサービスが終了完了しています.
	 */
	public boolean isExit() {
		// 登録サービスの停止チェック.
		final int len = quinaServiceManager.size();
		for(int i = 0; i < len; i ++) {
			if(!quinaServiceManager.get(i).isExit()) {
				return false;
			}
		}
		// 基本サービスの停止チェック.
		return httpWorkerService.isExit() &&
			httpServerService.isExit();
	}

	/**
	 * 全てのQuinaサービスが停止完了するまで待機.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina await() {
		// シャットダウンマネージャが開始されていない場合.
		if(!shutdownManager.getInfo().isStart()) {
			// シャットダウンマネージャを開始.
			shutdownManager.startShutdown();
			LogFactory.getInstance().get()
				.info("### start ShutdownManager");
		}
		// すべてのサービスが終了するまで待機.
		while(!isExit()) {
			QuinaUtil.sleep(50L);
		}
		return this;
	}

	/**
	 * コマンドライン引数を設定.
	 * @param args main(String[] args) で渡される args 変数をセットします.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina setArgs(String[] args) {
		if(args == null) {
			this.args = new Args();
		} else {
			this.args = new Args(args);
		}
		return this;
	}

	/**
	 * コマンドライン引数管理オブジェクトを取得.
	 * @return Args コマンドライン引数管理オブジェクトが返却されます.
	 */
	public Args getArgs() {
		return args;
	}

	/**
	 * ワーカー要素を登録します.
	 * @param em Worker要素を設定します.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina registerWorker(WorkerElement em) {
		final NioWorkerThreadManager man = workerManager.get();
		// 開始していない場合.
		if(man == null) {
			throw new QuinaException("Quina has not started.");
		}
		try {
			man.push(em);
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
		return this;
	}

	/**
	 * ワーカースレッドが停止状態かチェック.
	 * @return boolean trueの場合停止しています.
	 */
	public boolean isStopWorker() {
		final NioWorkerThreadManager man = workerManager.get();
		// 開始していない場合.
		if(man == null) {
			return true;
		}
		return man.isStopCall();
	}

	/**
	 * 登録されているHttpServerCallを取得.
	 * @return HttpServerCall HttpServerCallが返却されます.
	 */
	public HttpServerCall getHttpServerCall() {
		return httpServerService.getHttpServerCall();
	}

	/**
	 * 1つのQuinaService要素.
	 */
	protected static final class QuinaServiceEntry {
		private String name;
		private QuinaService service;

		/**
		 * コンストラクタ.
		 * @param name サービス登録名を設定します.
		 * @param service 登録サービスを設定します.
		 */
		protected QuinaServiceEntry(String name, QuinaService service) {
			this.name = name;
			this.service = service;
		}

		/**
		 * サービス登録名を取得.
		 * @return String サービス登録名が返却されます.
		 */
		public String getName() {
			return name;
		}

		/**
		 * QuinaServiceを取得.
		 * @return QuinaService QuinaServiceが返却されます.
		 */
		public QuinaService getService() {
			return service;
		}

		/**
		 * QuinaServiceを設定.
		 * @param newService 新しいQuinaServiceを設定します.
		 * @return QuinaService 前回登録されていたQuinaServiceが返却されます.
		 */
		protected QuinaService setService(QuinaService newService) {
			QuinaService ret = service;
			service = newService;
			return ret;
		}
	}

	/**
	 * QuinaService管理オブジェクト.
	 */
	protected static final class QuinaServiceManager {
		private final ObjectList<QuinaServiceEntry> list =
			new ObjectList<QuinaServiceEntry>();

		/**
		 * コンストラクタ.
		 */
		public QuinaServiceManager() {}

		// 検索.
		private static final int search(
			ObjectList<QuinaServiceEntry> list, String name) {
			final int len = list.size();
			for(int i = 0; i < len; i ++) {
				if(name.equals(list.get(i).getName())) {
					return i;
				}
			}
			return -1;
		}

		/**
		 * データセット.
		 * @param name サービス登録名を設定します.
		 * @param service 登録サービスを設定します.
		 * @return QuinaService 前回登録されていたサービスが返却されます.
		 */
		public QuinaService put(String name, QuinaService service) {
			if(name == null || service == null) {
				return null;
			}
			final int p = search(list, name);
			if(p == -1) {
				list.add(new QuinaServiceEntry(name, service));
				return null;
			}
			// 一番最後に再設定して返却.
			QuinaServiceEntry e = list.remove(p);
			list.add(e);
			return e.setService(service);
		}

		/**
		 * 登録名を指定して取得.
		 * @param name 取得したい登録名を設定します.
		 * @return QuinaService 対象のサービスが返却されます.
		 */
		public QuinaService get(String name) {
			if(name != null) {
				final int p = search(list, name);
				if(p != -1) {
					return list.get(p).getService();
				}
			}
			return null;
		}

		/**
		 * 登録名を指定して登録項番を取得.
		 * @param name 取得したい登録名を設定します.
		 * @return int 登録項番が返却されます.
		 */
		public int getNo(String name) {
			if(name != null) {
				final int p = search(list, name);
				if(p != -1) {
					return p;
				}
			}
			return -1;
		}

		/**
		 * 項番を設定して取得.
		 * @param no 対象の項番を設定します.
		 * @return QuinaService 対象のサービスが返却されます.
		 */
		public QuinaService get(int no) {
			if(no >= 0 && no < list.size()) {
				return list.get(no).getService();
			}
			return null;
		}

		/**
		 * 登録名を指定して削除.
		 * @param name 削除対象の登録名を設定します.
		 * @return QuinaService 削除されたサービスが返却されます.
		 */
		public QuinaService remove(String name) {
			if(name != null) {
				final int p = search(list, name);
				if(p != -1) {
					return list.remove(p).getService();
				}
			}
			return null;
		}

		/**
		 * 登録数を取得.
		 * @return int サービスの登録数が返却されます.
		 */
		public int size() {
			return list.size();
		}

		/**
		 * 項番を指定して登録名を取得.
		 * @param no 対象の項番を設定します.
		 * @return String 登録名が返却されます.
		 */
		public String nameAt(int no) {
			if(no >= 0 && no < list.size()) {
				return list.get(no).getName();
			}
			return null;
		}
	}

	// QuinaShutdownCall.
	private static final class QuinaShutdownCall
		extends ShutdownCall {
		@Override
		public void call() {
			// Quinaの停止を実施.
			LogFactory.getInstance().get()
				.info("* * A shutdown hook has been detected * *");
			Quina.get().stop();
		}
	}
}
