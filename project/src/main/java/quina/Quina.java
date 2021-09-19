package quina;

import quina.annotation.cdi.CdiHandle;
import quina.annotation.cdi.CdiHandleManager;
import quina.annotation.cdi.CdiReflectManager;
import quina.annotation.cdi.CdiServiceManager;
import quina.annotation.log.AnnotationLog;
import quina.annotation.quina.AnnotationQuina;
import quina.component.EtagManagerInfo;
import quina.exception.CoreException;
import quina.exception.QuinaException;
import quina.http.server.HttpServerCall;
import quina.http.server.HttpServerService;
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
import quina.util.TwoStepsFlag;
import quina.util.collection.IndexMap;

/**
 * Quina.
 */
public final class Quina {
	
	// シングルトン.
	private static final Quina SNGL = new Quina();

	// 基本的なQuinaShutdownToken.
	private static final String DEFAULT_TOKEN = "@aniuq";
	
	// 初期化実行フラグ.
	private final TwoStepsFlag initFlag = new TwoStepsFlag();

	// コンフィグディレクトリ.
	private String configDir = null;

	// ルータオブジェクト.
	private Router router;

	// 基本サービス: HttpServerService.
	// Httpサーバー関連のサービスを管理します.
	private HttpServerService httpServerService;

	// 基本サービス: HttpWorkerService.
	// Httpワーカースレッド関連を管理します.
	private HttpWorkerService httpWorkerService;

	// シャットダウンマネージャー.
	private ShutdownManager shutdownManager;

	// 実行引数管理.
	private Args args;

	// 実行中のワーカースレッドマネージャ.
	private final AtomicObject<NioWorkerThreadManager> workerManager =
		new AtomicObject<NioWorkerThreadManager>();
	
	// Quinaサービス管理.
	private final QuinaServiceManager quinaServiceManager = new
		QuinaServiceManager();
	
	// CDIサービスマネージャ.
	private final CdiServiceManager cdiManager = new CdiServiceManager();
	
	// CDIリフレクションマネージャ.
	private final CdiReflectManager cdiRefrectManager = new CdiReflectManager();
	
	// CDIアノテーションマネージャ.
	private final CdiHandleManager cdiHandleManager = new CdiHandleManager();

	// コンストラクタ.
	private Quina() {
	}

	/**
	 * Quina初期設定.
	 * この処理はQuinaを利用する場合、必ず１度呼び出す必要があります.
	 * 
	 * またこの呼出の場合、対象オブジェクトに＠CdiScopedが定義されてる
	 * 場合は内の＠Injectと＠LogDefineが定義されたフィールドに内容が反映
	 * されます.
	 * 
	 * @param mainObject Quinaを初期化するオブジェクトを設定します.
	 * @param args main()メソッドの第一引数を設定します.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina initialize(Object mainObject, String[] args) {
		return initialize(mainObject.getClass(), mainObject, args);
	}
	
	/**
	 * Quina初期設定.
	 * この処理はQuinaを利用する場合、必ず１度呼び出す必要があります.
	 * 
	 * またこの呼出の場合、対象オブジェクトに＠CdiScopedが定義されてる
	 * 場合は内の＠Injectと＠LogDefineが定義されたstaticフィールドに
	 * 内容が反映されます.
	 * 
	 * @param mainClass Quinaを初期化するクラスを設定します.
	 * @param args main()メソッドの第一引数を設定します.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	private Quina initialize(Class<?> mainClass, String[] args) {
		return initialize(mainClass, null, args);
	}

	// Quina初期設定.
	private Quina initialize(Class<?> mainClass, Object mainObject, String[] args) {
		try {
			// 初期化処理開始.
			if(!initFlag.start()) {
				// 既に実行済みの場合.
				return this;
			}
			// ネットワーク初期処理.
			NioUtil.initNet();
			
			// SystemPropertyの初期処理.
			AnnotationQuina.loadSystemProperty(mainClass);
			
			// CdiReflectManager読み込み.
			cdiRefrectManager.autoCdiReflect();
			// AutoCdiService読み込みを実行.
			cdiManager.autoCdiService();
			// AutoQuinaService読み込みを実行.
			quinaServiceManager.autoQuinaService();
			
			// CdiAnnotationScopedアノテーションを反映.
			cdiHandleManager.autoCdiHandleManager();
			// CdiHandleManagerをFix.
			cdiHandleManager.fix();
			
			// コンフィグディレクトリを取得.
			String configDir = AnnotationQuina
				.loadConfigDirectory(mainClass);
			// 最初にログ定義だけを反映.
			if(!loadLogConfig(configDir)) {
				// 外部コンフィグファイルが存在しない場合は
				// LogConfigの初期処理.
				loadLogConfigByAnnotation(mainClass);
			}
			
			// Argsを設定.
			if(args != null) {
				this.args = new Args(args);
			}
			
			// シャットダウンデフォルトトークンを設定.
			ShutdownConstants.setDefaultToken(DEFAULT_TOKEN);
			
			// ルーターオブジェクト生成.
			this.router = new Router();
			
			// シャットダウンマネージャを生成し、quinaシャットダウンコールをセット.
			this.shutdownManager = new ShutdownManager();
			this.shutdownManager.getInfo().register(new QuinaShutdownCall());
			
			// HTTP関連サービスを生成.
			this.httpWorkerService = new HttpWorkerService();
			this.httpServerService = new HttpServerService(this.httpWorkerService);
			
			// AppendMimeのAnnotationを読み込む.
			AnnotationQuina.loadAppendMimeType(mainClass);
			
			// ServiceScopedアノテーションを反映.
			updateAnnotationService();
			
			// QuinaServiceScopedアノテーションを反映.
			updateAnnotationQuinaService();
			
			// CdiScopedアノテーションを反映.
			if(mainObject != null &&
				AnnotationQuina.isCdiScoped(mainObject)) {
				cdiHandleManager.load(mainObject);
			} else if(AnnotationQuina.isCdiScoped(mainClass)) {
				cdiHandleManager.load(mainClass);
			}
			
			// コンフィグディレクトリが設定されてる場合.
			if(configDir != null && !configDir.isEmpty()) {
				// 登録してQuinaのコンフィグ情報をロードする.
				this.loadConfig(configDir);
			}
			
			// 初期化成功.
			initFlag.forcedSuccess();
			return this;
		} catch(CoreException ce) {
			// 初期化失敗.
			initFlag.failure();
			throw ce;
		} catch(Exception e) {
			// 初期化失敗.
			initFlag.failure();
			throw new QuinaException(e);
		}
	}
	
	/**
	 * initialize処理が呼び出されてない場合は例外.
	 */
	protected void checkInit() {
		// 初期化実行済みでない場合.
		if(!initFlag.isExecuted()) {
			throw new QuinaException(
				"Quina initialization process has not been executed.");
		}
	}
	
	// サービス群に対してAnnotation関連を反映.
	private final void updateAnnotationService() {
		final int len = cdiManager.size();
		for(int i = 0; i < len; i ++) {
			// アノテーションを注入.
			cdiHandleManager.load(cdiManager.getService(i));
		}
	}
	
	// QuinaService群に対してAnnotation関連を反映.
	private final void updateAnnotationQuinaService() {
		final int len = quinaServiceManager.size();
		for(int i = 0; i < len; i ++) {
			// アノテーションを注入.
			cdiHandleManager.load(
				quinaServiceManager.get(i));
		}
	}
	
	/**
	 * Quina初期設定.
	 * この処理はQuinaを利用する場合、必ず１度呼び出す必要があります.
	 * 
	 * またこの呼出の場合、対象オブジェクトに＠CdiScopedが定義されてる
	 * 場合は内の＠Injectと＠LogDefineが定義されたフィールドに内容が反映
	 * されます.
	 * 
	 * @param mainObject Quinaを初期化するオブジェクトを設定します.
	 * @param args main()メソッドの第一引数を設定します.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public static final Quina init(Object mainObject, String[] args) {
		return SNGL.initialize(mainObject, args);
	}
	
	/**
	 * Quina初期設定.
	 * この処理はQuinaを利用する場合、必ず１度呼び出す必要があります.
	 * 
	 * またこの呼出の場合、対象オブジェクトに＠CdiScopedが定義されてる
	 * 場合は内の＠Injectと＠LogDefineが定義されたstaticフィールドに
	 * 内容が反映されます.
	 * 
	 * @param mainClass Quinaを初期化するクラスを設定します.
	 * @param args main()メソッドの第一引数を設定します.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public static final Quina init(Class<?> mainClass, String[] args) {
		return SNGL.initialize(mainClass, args);
	}
	
	/**
	 * 指定オブジェクトにCDIを反映.
	 * @param o 対象のオブジェクトを設定します.
	 * @return 
	 */
	public static final Quina loadCdi(CdiHandle o) {
		if(!SNGL.cdiHandleManager.isFix()) {
			throw new QuinaException("Not completed. ");
		}
		SNGL.cdiHandleManager.load(o);
		return SNGL;
	}
	
	/**
	 * 指定オブジェクトにCDIを反映.
	 * @param c 対象のクラスを設定します.
	 * @return 
	 */
	public static final Quina loadCdi(Class<?> c) {
		if(!SNGL.cdiHandleManager.isFix()) {
			throw new QuinaException("Not completed. ");
		}
		SNGL.cdiHandleManager.load(c);
		return SNGL;
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
		checkInit();
		return router;
	}
	
	/**
	 * QuinaServiceManagerを取得.
	 * @return QuinaServiceManager QuinaServiceManagerga返却されます.
	 */
	public QuinaServiceManager getQuinaServiceManager() {
		return quinaServiceManager;
	}
	
	/**
	 * CDI（Contexts and Dependency Injection）
	 * サービスマネージャを取得.
	 * @return CidManager CDIサービスマネージャが返却されます.
	 */
	public CdiServiceManager getCdiServiceManager() {
		return cdiManager;
	}
	
	/**
	 * CDI（Contexts and Dependency Injection）
	 * リフレクションマネージャを取得.
	 * @return CdiReflectManager CDIリフレクションマネージャが返却されます.
	 */
	public CdiReflectManager getCdiReflectManager() {
		return cdiRefrectManager;
	}
	
	/**
	 * CDI（Contexts and Dependency Injection）
	 * アノテーションマネージャを取得.
	 * @return CdiHandleManager CDIアノテーションマネージャが返却されます.
	 */
	public CdiHandleManager getCdiHandleManager() {
		return cdiHandleManager;
	}
	
	/**
	 * サービスの状態チェック.
	 * @param mode [true]を指定した場合、開始中の場合、
	 *             エラーが発生します.
	 *             [false]を指定した場合、停止中の場合、
	 *             エラーが発生します.
	 */
	protected void checkService(boolean mode) {
		checkInit();
		// 基本サービスのチェック.
		httpWorkerService.checkService(mode);
		httpServerService.checkService(mode);
		// 登録サービスのチェック.
		final int len = quinaServiceManager.size();
		for(int i = 0; i < len; i ++) {
			quinaServiceManager.get(i).checkService(mode);
		}
	}

	/**
	 * コンフィグディレクトリを設定.
	 * この処理はコンフィグディレクトリを設定するだけで、
	 * コンフィグ内容を反映するわけではありません.
	 * @param configDir コンフィグディレクトリを設定します.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina setConfigDirectory(String configDir) {
		checkService(true);
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
		checkInit();
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
	 * コンフィグ情報を読み込んで反映します.
	 * @param configDir コンフィグディレクトリを設定します.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina loadConfig(String configDir) {
		checkService(true);
		// 存在する場合コンフィグディレクトリを設定.
		if(configDir != null && !configDir.isEmpty()) {
			setConfigDirectory(configDir);
		}
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
		httpWorkerService.loadConfig(configDir);
		httpServerService.loadConfig(configDir);
		// 登録サービスのコンフィグ情報を読み込む.
		final int len = quinaServiceManager.size();
		for(int i = 0; i < len; i ++) {
			quinaServiceManager.get(i).loadConfig(this.configDir);
		}
		return this;
	}

	// ログのコンフィグ定義.
	private static final boolean loadLogConfig(String configDir) {
		final LogFactory logFactory = LogFactory.getInstance();
		// LogFactoryが既にコンフィグ設定されている場合.
		if(logFactory.isFixConfig()) {
			// 処理しない.
			return true;
		// ディレクトリが設定されてない場合.
		} else if(configDir == null || configDir.isEmpty()) {
			return false;
		}
		// log.jsonのコンフィグファイルを取得.
		IndexMap<String, Object> json = QuinaUtil.loadJson(configDir, "log");
		if(json == null) {
			return false;
		}
		// ログコンフィグをセットしてFix
		return logFactory.loadConfigByFix(json);
	}
	
	// LogConfigアノテーションからログ定義を読み込む.
	private static final boolean loadLogConfigByAnnotation(Class<?> mainClass) {
		// LogFactoryが既にコンフィグ設定されている場合.
		if(LogFactory.getInstance().isFixConfig()) {
			// 処理しない.
			return true;
		}
		// LogConfigの初期処理.
		if(AnnotationLog.loadLogConfig(mainClass)) {
			// 正しく設定された場合はFixさせる.
			LogFactory.getInstance().fixConfig();
			return true;
		}
		return false;
	}

	// シャットダウンマネージャのコンフィグ条件を設定.
	private final boolean loadShutdownManagerConfig(String configDir) {
		final ShutdownManagerInfo info = shutdownManager.getInfo();
		// shutdownManagerのコンフィグが既に設定されている場合.
		if(info.isConfig()) {
			// 処理しない.
			return true;
		}
		// shutdown.jsonのコンフィグファイルを取得.
		IndexMap<String, Object> json = QuinaUtil.loadJson(configDir, "shutdown");
		if(json == null) {
			return false;
		}
		// shutdownManagerのコンフィグ条件をセット.
		info.config(json);
		return true;
	}

	// Etagマネージャのコンフィグ条件を設定.
	private final boolean loadEtagManagerConfig(String configDir) {
		final EtagManagerInfo info = router.getEtagManagerInfo();
		if(info.isDone()) {
			// 処理しない.
			return true;
		}
		// etag.jsonのコンフィグファイルを取得.
		IndexMap<String, Object> json = QuinaUtil.loadJson(configDir, "etag");
		if(json == null) {
			return false;
		}
		// etagManagerのコンフィグ条件をセット.
		info.config(json);
		return true;
	}

	/**
	 * シャットダウンマネージャ情報を取得.
	 * @return ShutdownManagerInfo シャットダウンマネージャ情報が返却されます.
	 */
	public ShutdownManagerInfo getShutdownManagerInfo() {
		checkInit();
		return shutdownManager.getInfo();
	}

	/**
	 * Etag管理定義情報を取得.
	 * @return EtagManagerInfo Etag管理定義情報が返却されます.
	 */
	public EtagManagerInfo getEtagManagerInfo() {
		checkInit();
		return router.getEtagManagerInfo();
	}

	/**
	 * HttpWorkerのConfig情報を取得.
	 * @return QuinaConfig HttpWorkerのConfigが返却されます.
	 */
	public QuinaConfig getHttpWorkerConfig() {
		checkInit();
		return httpWorkerService.getConfig();
	}

	/**
	 * HttpServerのConfig情報を取得.
	 * @return QuinaConfig HttpServerのConfigが返却されます.
	 */
	public QuinaConfig getHttpServerConfig() {
		checkInit();
		return httpServerService.getConfig();
	}
	
	/**
	 * 全てのQuinaサービスを開始処理.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina start() {
		checkService(true);
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
			// QuinaServiceManagerを完了させる.
			quinaServiceManager.fix();
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
	 * 全てのQuinaサービスを開始処理してシャットダウン待機処理.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina startAwait() {
		return start().await();
	}

	/**
	 * Quinaサービス開始処理[start()]が呼ばれたかチェック.
	 * @return boolean [true]の場合開始しています.
	 */
	public boolean isStart() {
		checkInit();
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
		checkInit();
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
		checkInit();
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
		checkInit();
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
		// ログの停止.
		LogFactory.getInstance().stopLogWriteWorker();
		return this;
	}

	/**
	 * 全てのQuinaのサービスが終了完了したかチェック.
	 * @return boolean trueの場合、全てのサービスが終了完了しています.
	 */
	public boolean isExit() {
		checkInit();
		// 登録サービスの停止チェック.
		final int len = quinaServiceManager.size();
		for(int i = 0; i < len; i ++) {
			if(!quinaServiceManager.get(i).isExit()) {
				return false;
			}
		}
		// 基本サービスの停止チェック.
		return httpWorkerService.isExit() &&
			httpServerService.isExit() &&
			LogFactory.getInstance().isExitLogWriteWorker();
	}

	/**
	 * 全てのQuinaサービスが停止完了するまで待機.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina await() {
		checkInit();
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
	 * コマンドライン引数管理オブジェクトを取得.
	 * @return Args コマンドライン引数管理オブジェクトが返却されます.
	 */
	public Args getArgs() {
		checkInit();
		return args;
	}

	/**
	 * ワーカー要素を登録します.
	 * @param em Worker要素を設定します.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina registerWorker(WorkerElement em) {
		checkInit();
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
		checkInit();
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
		checkInit();
		return httpServerService.getHttpServerCall();
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
