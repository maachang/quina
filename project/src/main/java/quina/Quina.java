package quina;

import quina.compile.cdi.annotation.CdiHandleManager;
import quina.compile.cdi.annotation.CdiReflectManager;
import quina.compile.cdi.annotation.CdiServiceManager;
import quina.compile.cdi.annotation.proxy.ProxyScopedManager;
import quina.component.file.EtagManagerInfo;
import quina.exception.QuinaException;
import quina.http.HttpContext;
import quina.http.controll.ipv4.IpPermissionControllService;
import quina.http.server.HttpServerContext;
import quina.route.Router;
import quina.shutdown.ShutdownManagerInfo;
import quina.util.Args;
import quina.worker.QuinaLoopManager;
import quina.worker.QuinaWorkerCall;

/**
 * Quinaを起動するためのオブジェクト.<br><br>
 * 
 * たとえば以下のように実装することでQuinaの
 * Webサービスが起動し、待機します.<br>
 * 
 * <pre><code>
 * ＜例＞
 * 
 * public void Exsample {
 *   public static void main(String[] args) {
 *     Quina.init(Exsample.class, args)
 *       .startAwait();
 *   }
 * }
 * </code></pre>
 * 
 * またQuinaが提供するアノテーション等を利用することで
 * Quinaに対する基本定義も行えます.<br>
 * 
 * <pre><code>
 * ＠CdiScoped
 * ＠LogConfig(directory="./logDir")
 * ＠LogConfig(name="greeting", directory="./logDir")
 * ＠QuinaServiceSelection(name="storage", define="jdbc")
 * public class QuinaTest {
 *   ＠LogDefine
 *   private static Log log;
 * 
 *   // コンストラクタ.
 *   private QuinaTest() {}
 * 
 *   // メイン実行.
 *   public static final void main(String[] args)
 *     throws Exception {
 *     // 初期化処理して開始処理.
 *     Quina.init(QuinaTest.class, args);
 * 
 *     log.info("start Quina Test");
 * 
 *     // 開始＋Await.
 *     Quina.get().startAwait();
 *   }
 * }
 * </code></pre>
 */
public final class Quina {
	
	// QuinaMembers.
	private final QuinaMembers quinaMembers;
	
	// Quina初期化処理.
	private final QuinaInit quinaInit;
	
	// Quinaスタートアップ処理.
	private final QuinaStartup quinaStartup;
	
	// コンストラクタ.
	private Quina() {
		// 初期化処理.
		final QuinaMembers mem = new QuinaMembers();
		final QuinaInit init = new QuinaInit(mem);
		final QuinaStartup startup = new QuinaStartup(mem);
		this.quinaMembers = mem;
		this.quinaInit = init;
		this.quinaStartup = startup;
	}
	
	// シングルトン.
	private static final Quina SNGL = new Quina();
	
	/**
	 * quinaオブジェクトを取得.
	 * @return Quina quinaが返却されます.
	 */
	public static final Quina get() {
		return SNGL;
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
	 * Quina初期化処理が行われているかチェック.
	 * @return boolean trueの場合、初期化されています.
	 */
	public static final boolean isInit() {
		return SNGL.quinaMembers.isInit();
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
		SNGL.quinaMembers.setConfigDirectory(confDir);
		return SNGL;
	}
	
	/**
	 * コンフィグディレクトリを取得.
	 * @return String コンフィグディレクトリが返却されます.
	 */
	public static final String configDirectory() {
		return SNGL.quinaMembers.getConfigDirectory();
	}
	
	/**
	 * コマンドライン引数管理オブジェクトを取得.
	 * @return Args コマンドライン引数管理オブジェクトが返却されます.
	 */
	public static final Args args() {
		return SNGL.getArgs();
	}
	
	/**
	 * HttpContextを取得.
	 * このContextは、QuinaWorkerService内で実行される
	 * ものに関して、利用することが出来ます.
	 * なので、他のスレッドでこの処理を呼び出す場合は
	 * この処理は利用出来ません.
	 * 
	 * その場合は以下のような形で実装することで対応
	 * 出来ます.
	 * 
	 * <code><pre>
	 * final HttpContext ctx = Quina.getContext();
	 * xxxx.execute(() -> {
	 *   ctx.anyResponse().send("hoge");
	 * }
	 * </pre></code>
	 * 
	 * @return HttpContext HttpContextが返却されます.
	 */
	public static final HttpContext getHttpContext() {
		return HttpServerContext.get();
	}
	
	/**
	 * コマンドライン引数管理オブジェクトを取得.
	 * @return Args コマンドライン引数管理オブジェクトが返却されます.
	 */
	public Args getArgs() {
		checkNoneExecuteInit();
		return quinaMembers.getArgs();
	}
	
	/**
	 * initialize処理が呼ばれてる場合は例外.
	 */
	protected void checkExecuteInit() {
		quinaMembers.checkExecuteInit();
	}
	
	/**
	 * initialize処理が呼び出されてない場合は例外.
	 */
	protected void checkNoneExecuteInit() {
		quinaMembers.checkNoneExecuteInit();
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
		quinaInit.initialize(mainObject.getClass(), mainObject, args);
		return this;
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
		quinaInit.initialize(mainClass, null, args);
		return this;
	}
	
	/**
	 * Quina初期化処理が行われているかチェック.
	 * @return boolean trueの場合、初期化されています.
	 */
	public boolean isInitialize() {
		return quinaMembers.isInit();
	}
	
	/**
	 * Quinaに対して登録可能なオブジェクトを設定.
	 * @param o 登録可能なオブジェクトを設定します.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina addQuinaRegsterObject(Object o) {
		quinaInit.addQuinaRegsterObject(o);
		return this;
	}
	
	/**
	 * ルータを取得.
	 * @return Router ルータが返却されます.
	 */
	public Router getRouter() {
		checkNoneExecuteInit();
		return quinaMembers.router;
	}
	
	/**
	 * QuinaLoopManagerを取得.
	 * @return QuinaLoopManager QuinaLoopManagerが返却されます.
	 */
	public QuinaLoopManager getQuinaLoopManager() {
		return quinaMembers.quinaWorkerService;
	}
	
	/**
	 * QuinaServiceManagerを取得.
	 * @return QuinaServiceManager QuinaServiceManagerga
	 *                             返却されます.
	 */
	public QuinaServiceManager getQuinaServiceManager() {
		return quinaMembers.quinaServiceManager;
	}
	
	/**
	 * CDI（Contexts and Dependency Injection）
	 * サービスマネージャを取得.
	 * @return CidManager CDIサービスマネージャが返却されます.
	 */
	public CdiServiceManager getCdiServiceManager() {
		return quinaMembers.cdiManager;
	}
	
	/**
	 * CDI（Contexts and Dependency Injection）
	 * リフレクションマネージャを取得.
	 * @return CdiReflectManager CDIリフレクション
	 *                           マネージャが返却されます.
	 */
	public CdiReflectManager getCdiReflectManager() {
		return quinaMembers.cdiRefrectManager;
	}
	
	/**
	 * CDI（Contexts and Dependency Injection）
	 * アノテーションマネージャを取得.
	 * @return CdiHandleManager CDIアノテーション
	 *                          マネージャが返却されます.
	 */
	public CdiHandleManager getCdiHandleManager() {
		return quinaMembers.cdiHandleManager;
	}
	
	/**
	 * ProxyScopedアノテーション定義でのInvocationHandler
	 * 代替え管理マネージャが返却されます.
	 * @return ProxyScopedManager ProxyScopedマネージャーが返却されます.
	 */
	public ProxyScopedManager getProxyScopedManager() {
		return quinaMembers.proxyScopedManager;
	}
	

	/**
	 * シャットダウンマネージャ情報を取得.
	 * @return ShutdownManagerInfo シャットダウンマネージャ情報が返却されます.
	 */
	public ShutdownManagerInfo getShutdownManagerInfo() {
		checkNoneExecuteInit();
		return quinaMembers.shutdownManager.getInfo();
	}

	/**
	 * Etag管理定義情報を取得.
	 * @return EtagManagerInfo Etag管理定義情報が返却されます.
	 */
	public EtagManagerInfo getEtagManagerInfo() {
		checkNoneExecuteInit();
		return quinaMembers.router.getEtagManagerInfo();
	}
	
	/**
	 * IpV4パーミッションアクセスコントロールサービスを取得.
	 * @return IpPermissionAccessControllService
	 *     IpV4パーミッションアクセスコントロールサービスが返却されます.
	 */
	public IpPermissionControllService
		getIpPermissionAccessControllService() {
		final IpPermissionControllService ret =
			quinaMembers.ipPermissionAccessControllService;
		// コンフィグ読み込みがFixしてない場合はエラー.
		if(!ret.isLoadConfig()) {
			throw new QuinaException(
				"IpV4 permission access control service config " +
				"loading is not complete. ");
		}
		return ret;
	}

	/**
	 * HttpWorkerのConfig情報を取得.
	 * @return QuinaConfig HttpWorkerのConfigが返却されます.
	 */
	public QuinaConfig getHttpWorkerConfig() {
		checkNoneExecuteInit();
		return quinaMembers.quinaWorkerService.getConfig();
	}

	/**
	 * HttpServerのConfig情報を取得.
	 * @return QuinaConfig HttpServerのConfigが返却されます.
	 */
	public QuinaConfig getHttpServerConfig() {
		checkNoneExecuteInit();
		return quinaMembers.httpServerService.getConfig();
	}
	
	/**
	 * ワーカーが利用可能かチェック.
	 * @return boolean trueの場合、利用可能です.
	 */
	public boolean isWorker() {
		return quinaMembers.isWorker();
	}
	
	/**
	 * ワーカースレッドが停止状態かチェック.
	 * @return boolean trueの場合停止しています.
	 */
	public boolean isStopWorker() {
		return quinaMembers.isStopWorker();
	}
	
	/**
	 * ワーカー要素を登録します.
	 * @param em Workerコール要素を設定します.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina pushWorker(QuinaWorkerCall em) {
		quinaMembers.pushWorker(em);
		return this;
	}
	
	/**
	 * 指定Scopedアノテーションが定義されてる
	 * オブジェクトにCDIを注入.
	 * @param o 対象のオブジェクトを設定します.
	 * @return Quina quinaが返却されます.
	 */
	public Quina injectScoped(Object o) {
		SNGL.quinaMembers.injectScoped(o);
		return SNGL;
	}
	
	/**
	 * 全てのQuinaサービスを開始処理してシャットダウン待機処理.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina startAwait() {
		quinaStartup.startAwait();
		return this;
	}
	
	/**
	 * 全てのQuinaサービスを開始処理.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina start() {
		quinaStartup.start();
		return this;
	}
	
	/**
	 * 全てのQuinaサービスを終了開始処理.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina stop() {
		quinaStartup.stop();
		return this;
	}
	
	/**
	 * Quinaサービス開始処理[start()]が呼ばれたかチェック.
	 * @return boolean [true]の場合開始しています.
	 */
	public boolean isStart() {
		return quinaStartup.isStart();
	}
	
	/**
	 * 全てのQuinaサービスが起動済みかチェック.
	 * @return boolean trueの場合、全てのQuinaサービスが起動しています.
	 */
	public boolean isStarted() {
		return quinaStartup.isStarted();
	}

	/**
	 * 全てのQuinaサービスが起動済みになるまで待機.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina awaitStarted() {
		quinaStartup.awaitStarted();
		return this;
	}

	/**
	 * 全てのQuinaのサービスが終了完了したかチェック.
	 * @return boolean trueの場合、全てのサービスが終了完了しています.
	 */
	public boolean isExit() {
		return quinaStartup.isExit();
	}
	
	/**
	 * 全てのQuinaサービスが停止完了するまで待機.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina await() {
		quinaStartup.await();
		return this;
	}
}
