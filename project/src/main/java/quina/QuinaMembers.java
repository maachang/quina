package quina;

import quina.annotation.AnnotationQuina;
import quina.compile.cdi.annotation.CdiHandleManager;
import quina.compile.cdi.annotation.CdiReflectManager;
import quina.compile.cdi.annotation.CdiServiceManager;
import quina.compile.cdi.annotation.proxy.ProxyScopedManager;
import quina.exception.QuinaException;
import quina.http.controll.ipv4.IpPermissionAccessControllService;
import quina.http.server.HttpServerService;
import quina.logger.LogFactory;
import quina.route.Router;
import quina.shutdown.ShutdownCall;
import quina.shutdown.ShutdownManager;
import quina.storage.MemoryStorageService;
import quina.storage.StorageConstants;
import quina.util.Args;
import quina.util.AtomicObject;
import quina.util.Env;
import quina.util.FileUtil;
import quina.util.TwoStepsFlag;
import quina.worker.QuinaWorkerCall;
import quina.worker.QuinaWorkerService;

/**
 * Quinaメンバー情報.
 */
final class QuinaMembers {
	
	// メインクラス.
	protected Class<?> mainClass;
	
	// メインオブジェクト.
	protected Object mainObject;
	
	// 初期化実行フラグ.
	private final TwoStepsFlag initFlag = new TwoStepsFlag();
	
	// コンフィグディレクトリ.
	protected final AtomicObject<String> configDir =
		new AtomicObject<String>(null);

	// ルータオブジェクト.
	protected final Router router = new Router();
	
	// Quinaワーカーサービス.
	protected final QuinaWorkerService quinaWorkerService =
		new QuinaWorkerService();
	
	// Httpサーバーサービス.
	protected final HttpServerService httpServerService =
		new HttpServerService();

	// シャットダウンマネージャー.
	protected final ShutdownManager shutdownManager =
		new ShutdownManager();

	// IpV4パーミッションアクセスコントロールサービス.
	protected final IpPermissionAccessControllService
		ipPermissionAccessControllService =
			new IpPermissionAccessControllService();
	
	// Quinaメインオブジェクトコマンド引数管理.
	protected Args args;

	// Quinaサービス管理.
	protected final QuinaServiceManager quinaServiceManager =
		new QuinaServiceManager();
	
	// CDIサービスマネージャ.
	protected final CdiServiceManager cdiManager =
		new CdiServiceManager();
	
	// CDIリフレクションマネージャ.
	protected final CdiReflectManager cdiRefrectManager =
		new CdiReflectManager();
	
	// CDIアノテーションマネージャ.
	protected final CdiHandleManager cdiHandleManager =
		new CdiHandleManager();
	
	// ProxyScopedマネージャ.
	protected final ProxyScopedManager proxyScopedManager =
		new ProxyScopedManager();
	
	/**
	 * QuinaShutdownCall.
	 */
	protected static final class QuinaShutdownCall
		extends ShutdownCall {
		@Override
		public void call() {
			// Quinaの停止を実施.
			LogFactory.getInstance().get()
				.info("* * A shutdown hook has been detected * *");
			final Quina q = Quina.get();
			// サービス停止.
			q.stop();
			// すべてのサービスが終了するまで待機.
			while(!q.isExit()) {
				QuinaUtil.sleep();
			}
		}
	}
	
	/**
	 * コンストラクタ.
	 */
	public QuinaMembers() {
		// Httpサーバーサービスの初期化処理.
		httpServerService.init(quinaWorkerService);
	}
	
	/**
	 * initialize処理が呼ばれてる場合は例外.
	 */
	public void checkExecuteInit() {
		// 初期化実行済みの場合例外.
		if(initFlag.isExecuted()) {
			throw new QuinaException(
				"Quina has already been initialized.");
		}
	}
	
	/**
	 * initialize処理が呼び出されてない場合は例外.
	 */
	public void checkNoneExecuteInit() {
		// 初期化実行済みでない場合.
		if(!initFlag.isExecuted()) {
			throw new QuinaException(
				"Quina initialization process has not " +
				"been executed.");
		}
	}
	
	/**
	 * 初期化処理が実施されたか取得.
	 * @return boolean true の場合初期化済みです.
	 */
	public boolean isInit() {
		return initFlag.isExecuted();
	}
	
	/**
	 * initialize処理開始.
	 * @return boolean falseの場合別のスレッドが初期化
	 *                 完了しています.
	 */
	public boolean startInit() {
		return initFlag.start();
	}
	
	/**
	 * initialize処理成功.
	 */
	public void successInit() {
		initFlag.forcedSuccess();
	}
	
	/**
	 * initialize処理失敗.
	 */
	public void failureInit() {
		initFlag.failure();
	}
	
	/**
	 * Quinaメインクラスを設定.
	 * @param c Quinaメインクラスを設定します.
	 * @param o Quinaメインオブジェクトを設定します.
	 */
	public void setMainClass(Class<?> c, Object o) {
		mainClass = c;
		mainObject = o;
	}
	
	/**
	 * Quinaメインクラスを取得.
	 * @return Class<?> Quinaメインクラスが返却されます.
	 */
	public Class<?> getMainClass() {
		return mainClass;
	}
	
	/**
	 * Quinaメインオブジェクトを取得.
	 * @return Object Quinaメインオブジェクトが返却されます.
	 */
	public Object getMainObject() {
		return mainObject;
	}
	
	/**
	 * Argsオブジェクトを設定.
	 * @param args Quinaメインオブジェクトのメインメソッドの第一引数を設定します.
	 */
	public void setArgs(String[] args) {
		// メインメソッドの第一引数が設定されている場合.
		if(args != null && args.length > 0) {
			this.args = new Args(args);
		// メインメソッドの第一引数が設定されてない場合.
		} else {
			this.args = new Args();
		}
	}
	
	/**
	 * Argsオブジェクトを取得.
	 * @return Args Argsオブジェクトが返却されます.
	 */
	public Args getArgs() {
		return args;
	}
	
	/**
	 * AnnotationでMainクラスに定義されているコンフィグ
	 * ディレクトリを取得.
	 * @return String アノテーション定義されている
	 *                コンフィグディレクトリが返却されます.
	 */
	public String getAnnotationDefineByConfigDirectory() {
		return AnnotationQuina.loadConfigDirectory(
			mainClass);
	}
	
	/**
	 * 規定で登録するQuina定義済みサービスを定義.
	 */
	public void regQuinaDefineService() {
		
		// MemoryStorageサービス定義.
		MemoryStorageService service = new MemoryStorageService();
		quinaServiceManager.put(StorageConstants.SERVICE_NAME,
			MemoryStorageService.SERVICE_DEFINE, service);
	}
	
	/**
	 * CdiReflectManager読み込み.
	 */
	public void autoCdiReflect() {
		cdiRefrectManager.autoCdiReflect();
	}
	
	/**
	 * AutoCdiService読み込み.
	 */
	public void autoCdiService() {
		cdiManager.autoCdiService();
	}
	
	/**
	 * AutoQuinaService読み込みを実行.
	 */
	public void autoQuinaService() {
		quinaServiceManager.autoQuinaService();
	}
	
	/**
	 * QuinaServiceSelectionを反映.
	 */
	public void regQuinaServiceSelection() {
		quinaServiceManager.regQuinaServiceSelection(mainClass);
	}
	
	/**
	 * CdiAnnotationScopedアノテーションを反映してFix.
	 */
	public void autoCdiHandleManagerAndFix() {
		cdiHandleManager.autoCdiHandleManager();
		cdiHandleManager.fix();
	}
	
	/**
	 * ProxyScopedアノテーションを反映してFix.
	 */
	public void autoProxyScopedManagerAndFix() {
		proxyScopedManager.autoProxyScopedManager();
		proxyScopedManager.fix();
	}
	
	/**
	 * コンフィグディレクトリを設定.
	 * この処理はコンフィグディレクトリを定義します.
	 * この処理はQuina.init() or Quina.get().initialize() 処理の
	 * 前に定義する必要があります.
	 * @param configDir コンフィグディレクトリを設定します.
	 * @return String 設定されてコンフィグディレクトリ名が返却されます.
	 */
	public String setConfigDirectory(String confDir) {
		// init処理前に実行.
		checkExecuteInit();
		try {
			// コンフィグディレクトリが存在しない場合.
			if(confDir == null || confDir.isEmpty()) {
				confDir = QuinaUtil.getConfigPath();
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
				configDir.set(confDir);
			}
			return confDir;
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
	
	/**
	 * コンフィグディレクトリを取得.
	 * @return String コンフィグディレクトリが返却されます.
	 */
	public String getConfigDirectory() {
		return configDir.get();
	}
	
	/**
	 * IpV4パーミッションアクセスコントロールサービスの
	 * コンフィグ定義ロードとFix.
	 */
	public void loadIpPermissionACSConfigAndFix() {
		ipPermissionAccessControllService.loadConfig(configDir.get());
		ipPermissionAccessControllService.fixConfig();
	}
	
	/**
	 * シャットダウンマネージャにquinaシャットダウンコールを登録.
	 */
	public void regShutdownManager() {
		this.shutdownManager.getInfo().register(
			new QuinaShutdownCall());
	}
	
	/**
	 * QuinaWorkerサービスの初期化処理.
	 */
	public void initQuinaWorkerService() {
		// 共通ワーカーハンドラをセット.
		this.quinaWorkerService.setHandler(
			new QuinaContextHandler());
		
		// QuinaLoopScopedアノテーションを反映.
		this.quinaWorkerService.autoQuinaLoopElement();
		
		// ワーカーコールハンドラをセット.
		int len = QuinaConstants.REG_WORKER_CALL_HANDLES.length;
		for(int i = 0; i < len; i ++) {
			this.quinaWorkerService.addCallHandle(
				QuinaConstants.REG_WORKER_CALL_HANDLES[i]);
		}
	}
	
	/**
	 * 指定Scopedアノテーションが定義されてる
	 * オブジェクトにCDIを注入.
	 * @param o 対象のオブジェクトを設定します.
	 */
	public void injectScoped(Object o) {
		if(!cdiHandleManager.isFix()) {
			throw new QuinaException("Not CdiHandleManager is completed.");
		} else if(o == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		cdiHandleManager.inject(o);
	}
	
	/**
	 * サービスの状態チェック.
	 * @param mode [true]を指定した場合、開始中の場合、
	 *             エラーが発生します.
	 *             [false]を指定した場合、停止中の場合、
	 *             エラーが発生します.
	 */
	public void checkService(boolean mode) {
		checkNoneExecuteInit();
		// 基本サービスのチェック.
		quinaWorkerService.checkService(mode);
		httpServerService.checkService(mode);
		ipPermissionAccessControllService.checkService(mode);
		// 登録サービスのチェック.
		final int len = quinaServiceManager.size();
		for(int i = 0; i < len; i ++) {
			quinaServiceManager.get(i).checkService(mode);
		}
	}
	
	/**
	 * ワーカーが利用可能かチェック.
	 * @return boolean trueの場合、利用可能です.
	 */
	public boolean isWorker() {
		return quinaWorkerService.isStarted();
	}
	
	/**
	 * ワーカースレッドが停止状態かチェック.
	 * @return boolean trueの場合停止しています.
	 */
	public boolean isStopWorker() {
		return quinaWorkerService.isExit();
	}
	
	/**
	 * ワーカー要素を登録します.
	 * @param em Workerコール要素を設定します.
	 */
	public void pushWorker(QuinaWorkerCall em) {
		checkNoneExecuteInit();
		try {
			quinaWorkerService.push(em);
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
}
