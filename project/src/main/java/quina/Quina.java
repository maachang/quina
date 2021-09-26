package quina;

import quina.annotation.cdi.CdiHandleManager;
import quina.annotation.cdi.CdiReflectManager;
import quina.annotation.cdi.CdiServiceManager;
import quina.annotation.log.AnnotationLog;
import quina.annotation.quina.AnnotationQuina;
import quina.component.EtagManagerInfo;
import quina.component.ExecuteComponent;
import quina.exception.CoreException;
import quina.exception.QuinaException;
import quina.http.HttpContext;
import quina.http.HttpCustomAnalysisParams;
import quina.http.server.HttpServerContext;
import quina.http.server.HttpServerService;
import quina.http.server.HttpServerWorkerCallHandler;
import quina.logger.LogFactory;
import quina.net.nio.tcp.NioUtil;
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
import quina.worker.QuinaWorkerCall;
import quina.worker.QuinaWorkerService;

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
	private final AtomicObject<String> configDir =
		new AtomicObject<String>(null);

	// ルータオブジェクト.
	private Router router;
	
	// Quinaワーカーサービス.
	private QuinaWorkerService workerService;

	// Httpサーバーサービス.
	private HttpServerService httpServerService;

	// シャットダウンマネージャー.
	private ShutdownManager shutdownManager;

	// 実行引数管理.
	private Args args;

	// Quinaサービス管理.
	private final QuinaServiceManager quinaServiceManager =
		new QuinaServiceManager();
	
	// CDIサービスマネージャ.
	private final CdiServiceManager cdiManager =
		new CdiServiceManager();
	
	// CDIリフレクションマネージャ.
	private final CdiReflectManager cdiRefrectManager =
		new CdiReflectManager();
	
	// CDIアノテーションマネージャ.
	private final CdiHandleManager cdiHandleManager =
		new CdiHandleManager();

	// コンストラクタ.
	private Quina() {
	}
	
	/**
	 * 初期化処理が実施されたか取得.
	 * @return boolean true の場合初期化済みです.
	 */
	public boolean isInit() {
		return initFlag.isExecuted();
	}

	/**
	 * initialize処理が呼ばれてる場合は例外.
	 */
	private final void _checkExecuteInit() {
		// 初期化実行済みの場合.
		if(initFlag.isExecuted()) {
			throw new QuinaException(
				"Quina has already been initialized.");
		}
	}

	/**
	 * initialize処理が呼び出されてない場合は例外.
	 */
	private final void _checkNoneExecuteInit() {
		// 初期化実行済みでない場合.
		if(!initFlag.isExecuted()) {
			throw new QuinaException(
				"Quina initialization process has not " +
				"been executed.");
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
	public Quina initialize(Class<?> mainClass, String[] args) {
		return initialize(mainClass, null, args);
	}

	// Quina初期設定.
	private final Quina initialize(Class<?> mainClass, Object mainObject,
		String[] args) {
		// 既に実行済みの場合エラー.
		_checkExecuteInit();
		try {
			// 初期化処理開始.
			if(!initFlag.start()) {
				// 他のスレッドが初期化完了.
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
			
			// アノテーションからコンフィグディレクトリを取得.
			String confDir = AnnotationQuina.loadConfigDirectory(
				mainClass);
			
			// アノテーションのコンフィグディレクトリが
			// 存在する場合.
			if(confDir != null && !confDir.isEmpty()) {
				// コンフィグディレクトリをセット.
				setConfigDirectory(confDir);
			}
			
			// コンフィグディレクトリを取得.
			confDir = getConfigDirectory();
			
			// 外部コンフィグファイルが存在しない場合は
			// LogConfigの初期処理.
			_loadLogConfigByAnnotation(mainClass);
			
			// 対象コンフィグに対するログ定義を読み込む.
			_loadLogConfig(confDir);
			
			// LogコンフィグをFixさせる.
			LogFactory.getInstance().fixConfig();
			
			// Argsを設定.
			if(args != null) {
				this.args = new Args(args);
			}
			
			// シャットダウンデフォルトトークンを設定.
			ShutdownConstants.setDefaultToken(DEFAULT_TOKEN);
			
			// ルーターオブジェクト生成.
			this.router = new Router();
			
			// シャットダウンマネージャを生成し、quinaシャットダウン
			// コールをセット.
			this.shutdownManager = new ShutdownManager();
			this.shutdownManager.getInfo().register(
				new QuinaShutdownCall());
			
			// Httpワーカーサービスを生成.
			this.workerService = new QuinaWorkerService();
			// 共通ワーカーハンドラをセット.
			this.workerService.setHandler(
				new QuinaWorkerHandlerImpl());
			// Httpワーカーハンドラをセット.
			this.workerService.addCallHandle(
				new HttpServerWorkerCallHandler());
			
			// HTTP関連サービスを生成.
			this.httpServerService = new HttpServerService(
				this.workerService);
			
			// AppendMimeのAnnotationを読み込む.
			AnnotationQuina.loadAppendMimeType(mainClass);
			
			// ServiceScopedアノテーションを反映.
			_updateAnnotationService();
			
			// QuinaServiceScopedアノテーションを反映.
			_updateAnnotationQuinaService();
			
			// CdiScopedアノテーションを反映.
			if(mainObject != null &&
				AnnotationQuina.isCdiScoped(mainObject)) {
				cdiHandleManager.inject(mainObject);
			} else if(AnnotationQuina.isCdiScoped(mainClass)) {
				cdiHandleManager.inject(mainClass);
			}
			
			// 登録してQuinaのコンフィグ情報をロードする.
			_loadConfig(confDir);
			
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
	
	// コンフィグ情報を読み込んで反映します.
	private final void _loadConfig(String confDir) {
		// 指定コンフィグディレクトリが存在しない場合.
		if(confDir == null || confDir.isEmpty()) {
			return;
		}
		// Promiseワーカーのコンフィグ設定.
		PromiseWorkerManager.getInstance().getConfig()
			.loadConfig(confDir);
		// シャットダウンマネージャのコンフィグ定義.
		_loadShutdownManagerConfig(confDir);
		// Etagマネージャのコンフィグ定義.
		_loadEtagManagerConfig(confDir);
		// 標準コンポーネントのコンフィグ情報を読み込む.
		workerService.loadConfig(confDir);
		httpServerService.loadConfig(confDir);
		// 登録サービスのコンフィグ情報を読み込む.
		final int len = quinaServiceManager.size();
		for(int i = 0; i < len; i ++) {
			// loadConfigが呼ばれてない場合のみ読み込み.
			if(!quinaServiceManager.get(i).isLoadConfig()) {
				quinaServiceManager.get(i).loadConfig(confDir);
			}
		}
	}

	// ログのコンフィグ定義.
	private static final boolean _loadLogConfig(String confDir) {
		// コンフィグディレクトリが存在しない場合.
		if(confDir == null || confDir.isEmpty()) {
			return false;
		}
		final LogFactory logFactory = LogFactory.getInstance();
		// LogFactoryが既にコンフィグ設定されている場合.
		if(logFactory.isFixConfig()) {
			// 処理しない.
			return true;
		}
		// log.jsonのコンフィグファイルを取得.
		IndexMap<String, Object> json = QuinaUtil.loadJson(
			confDir, "log");
		if(json == null) {
			return false;
		}
		// ログコンフィグをセット.
		return logFactory.loadConfig(json);
	}
	
	// LogConfigアノテーションからログ定義を読み込む.
	private static final boolean _loadLogConfigByAnnotation(Class<?> mainClass) {
		// LogFactoryが既にコンフィグ設定されている場合.
		if(LogFactory.getInstance().isFixConfig()) {
			// 処理しない.
			return true;
		// LogConfigの初期処理.
		} else if(AnnotationLog.loadLogConfig(mainClass)) {
			return true;
		}
		return false;
	}

	// シャットダウンマネージャのコンフィグ条件を設定.
	private final boolean _loadShutdownManagerConfig(String configDir) {
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
	private final boolean _loadEtagManagerConfig(String configDir) {
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
	
	// サービス群に対してAnnotation関連を反映.
	private final void _updateAnnotationService() {
		final int len = cdiManager.size();
		for(int i = 0; i < len; i ++) {
			// アノテーションを注入.
			cdiHandleManager.inject(
				cdiManager.getService(i));
		}
	}
	
	// QuinaService群に対してAnnotation関連を反映.
	private final void _updateAnnotationQuinaService() {
		final int len = quinaServiceManager.size();
		for(int i = 0; i < len; i ++) {
			// アノテーションを注入.
			cdiHandleManager.inject(
				quinaServiceManager.get(i));
		}
	}
	

	/**
	 * コンフィグディレクトリを設定.
	 * この処理はコンフィグディレクトリを定義します.
	 * この処理はQuina.init() or Quina.get().initialize() 処理の
	 * 前に定義する必要があります.
	 * @param configDir コンフィグディレクトリを設定します.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina setConfigDirectory(String confDir) {
		// init処理前に実行.
		_checkExecuteInit();
		try {
			// コンフィグディレクトリが存在しない場合.
			if(confDir == null || confDir.isEmpty()) {
				// null で登録.
				this.configDir.set(null);;
				return this;
			}
			// 存在する場合Env定義を取り入れたパス生成.
			confDir = Env.path(confDir);
			// 対象のコンフィグディレクトリが存在しない.
			if(!FileUtil.isDir(confDir)) {
				throw new QuinaException(
					"The specified config directory \"" +
					confDir + "\" does not exist. ");
			// 存在する場合はコンフィグディレクトリとして
			// セット.
			} else {
				confDir = FileUtil.getFullPath(confDir);
				if(!confDir.endsWith("/")) {
					confDir = confDir + "/";
				}
				this.configDir.set(confDir);
			}
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
		return this;
	}

	/**
	 * コンフィグディレクトリを取得.
	 * @return String 設定されているコンフィグディレクトリが
	 *                返却されます.
	 */
	public String getConfigDirectory() {
		return configDir.get();
	}
	
	/**
	 * HTTPパラメータ解析をカスタマイズ解析するオブジェクトを設定.
	 * @return custom カスタムオブジェクトを設定します.
	 */
	public void setHttpCustomAnalysisParams(
		HttpCustomAnalysisParams custom) {
		ExecuteComponent.getInstance()
			.setHttpCustomAnalysisParams(custom);
	}
	
	/**
	 * HTTPパラメータ解析をカスタマイズ解析するオブジェクトを取得.
	 * @return HttpCustomAnalysisParams カスタムオブジェクトが返却されます.
	 */
	public HttpCustomAnalysisParams getHttpCustomAnalysisParams() {
		return ExecuteComponent.getInstance()
			.getHttpCustomAnalysisParams();
	}
	
	/**
	 * Quina初期設定.
	 * この処理はQuinaを利用する場合、必ず１度呼び出す必要があります.
	 * 
	 * これにより対象オブジェクトにCdi関連が定義されてる内容に
	 * 関して、注入処理が行われます.
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
	 * コンフィグディレクトリを設定.
	 * この処理はコンフィグディレクトリを定義します.
	 * この処理はQuina.init() or Quina.get().initialize() 処理の
	 * 前に定義する必要があります.
	 * @param configDir コンフィグディレクトリを設定します.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public static final Quina configDirectory(String confDir) {
		return SNGL.setConfigDirectory(confDir);
	}
	
	/**
	 * 指定Scopedアノテーションが定義されてる
	 * オブジェクトにCDIを注入.
	 * @param o 対象のオブジェクトを設定します.
	 * @return Quina quinaが返却されます.
	 */
	public static final Quina injectScoped(Object o) {
		if(!SNGL.cdiHandleManager.isFix()) {
			throw new QuinaException("Not completed. ");
		} else if(o == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		SNGL.cdiHandleManager.inject(o);
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
	 * HttpContextを取得.
	 * @return HttpContext HttpContextが返却されます.
	 */
	public static final HttpContext getContext() {
		return HttpServerContext.get();
	}
	
	/**
	 * ルータを取得.
	 * @return Router ルータが返却されます.
	 */
	public Router getRouter() {
		_checkNoneExecuteInit();
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
	private final void _checkService(boolean mode) {
		_checkNoneExecuteInit();
		// 基本サービスのチェック.
		workerService.checkService(mode);
		httpServerService.checkService(mode);
		// 登録サービスのチェック.
		final int len = quinaServiceManager.size();
		for(int i = 0; i < len; i ++) {
			quinaServiceManager.get(i).checkService(mode);
		}
	}

	/**
	 * シャットダウンマネージャ情報を取得.
	 * @return ShutdownManagerInfo シャットダウンマネージャ情報が返却されます.
	 */
	public ShutdownManagerInfo getShutdownManagerInfo() {
		_checkNoneExecuteInit();
		return shutdownManager.getInfo();
	}

	/**
	 * Etag管理定義情報を取得.
	 * @return EtagManagerInfo Etag管理定義情報が返却されます.
	 */
	public EtagManagerInfo getEtagManagerInfo() {
		_checkNoneExecuteInit();
		return router.getEtagManagerInfo();
	}

	/**
	 * HttpWorkerのConfig情報を取得.
	 * @return QuinaConfig HttpWorkerのConfigが返却されます.
	 */
	public QuinaConfig getHttpWorkerConfig() {
		_checkNoneExecuteInit();
		return workerService.getConfig();
	}

	/**
	 * HttpServerのConfig情報を取得.
	 * @return QuinaConfig HttpServerのConfigが返却されます.
	 */
	public QuinaConfig getHttpServerConfig() {
		_checkNoneExecuteInit();
		return httpServerService.getConfig();
	}
	
	/**
	 * 全てのQuinaサービスを開始処理.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina start() {
		_checkService(true);
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
			workerService.startService();
			workerService.awaitStartup();
			httpServerService.startService();
			httpServerService.awaitStartup();
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
		_checkNoneExecuteInit();
		// 基本サービスの開始処理[start()]が呼び出された場合.
		if(httpServerService.isStartService() &&
			workerService.isStartService()) {
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
		_checkNoneExecuteInit();
		// 基本サービスが起動している場合.
		if(httpServerService.isStarted() &&
			workerService.isStarted()) {
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
		_checkNoneExecuteInit();
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
		_checkNoneExecuteInit();
		// 基本サービスを停止.
		// 最初にサーバ停止で、次にワーカー停止.
		httpServerService.stopService();
		httpServerService.awaitExit();
		workerService.stopService();
		workerService.awaitExit();
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
		_checkNoneExecuteInit();
		// 登録サービスの停止チェック.
		final int len = quinaServiceManager.size();
		for(int i = 0; i < len; i ++) {
			if(!quinaServiceManager.get(i).isExit()) {
				return false;
			}
		}
		// 基本サービスの停止チェック.
		return workerService.isExit() &&
			httpServerService.isExit() &&
			LogFactory.getInstance().isExitLogWriteWorker();
	}

	/**
	 * 全てのQuinaサービスが停止完了するまで待機.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina await() {
		_checkNoneExecuteInit();
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
		_checkNoneExecuteInit();
		return args;
	}

	/**
	 * ワーカー要素を登録します.
	 * @param em Workerコール要素を設定します.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina registerWorker(QuinaWorkerCall em) {
		_checkNoneExecuteInit();
		try {
			workerService.push(em);
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
		_checkNoneExecuteInit();
		return workerService.isExit();
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
