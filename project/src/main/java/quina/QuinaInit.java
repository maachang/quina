package quina;

import quina.annotation.cdi.CdiHandleManager;
import quina.annotation.cdi.CdiServiceManager;
import quina.annotation.log.AnnotationLog;
import quina.annotation.quina.AnnotationQuina;
import quina.component.file.EtagManagerInfo;
import quina.exception.CoreException;
import quina.exception.QuinaException;
import quina.logger.LogFactory;
import quina.net.nio.tcp.NioUtil;
import quina.shutdown.ShutdownConstants;
import quina.shutdown.ShutdownManagerInfo;
import quina.util.collection.QuinaMap;

/**
 * Quina初期化処理.
 */
final class QuinaInit {
	
	// 基本的なQuinaShutdownトークン.
	protected static final String DEFAULT_SHUTDOWN_TOKEN = "@aniuq";
	
	// QuinaMembers.
	private final QuinaMembers quinaMembers;
	
	/**
	 * コンストラクタ.
	 * @param quinaMembers QuinaMembersを設定します.
	 */
	QuinaInit(QuinaMembers quinaMembers) {
		this.quinaMembers = quinaMembers;
	}
	
	/**
	 * Quina初期設定.
	 * @param mainClass Quinaメインクラスを設定します.
	 * @param mainObject Quinaメインオブジェクトを設定します.
	 * @param args Quinaメインオブジェクトの実行メソッド第一引数を設定します.
	 */
	public final void initialize(Class<?> mainClass, Object mainObject,
		String[] args) {
		// 既に実行済みの場合エラー.
		quinaMembers.checkExecuteInit();
		try {
			// 初期化処理開始.
			if(!quinaMembers.startInit()) {
				// 他のスレッドが初期化完了.
				return;
			}
			// ネットワーク初期処理.
			NioUtil.initNet();
			
			// SystemPropertyの初期処理.
			AnnotationQuina.loadSystemProperty(mainClass);
			
			// quinaMainクラスを設定.
			quinaMembers.setMainClass(mainClass, mainObject);
			
			// CdiReflectManager読み込み.
			quinaMembers.autoCdiReflect();
			// AutoCdiService読み込み.
			quinaMembers.autoCdiService();
			
			// Quinaサービス定義サービスを登録.
			quinaMembers.regQuinaDefineService();
			
			// AutoQuinaService読み込みを実行.
			quinaMembers.autoQuinaService();
			// QuinaServiceSelectionを反映.
			quinaMembers.regQuinaServiceSelection();
			
			// CdiAnnotationScopedアノテーションを反映してFix.
			quinaMembers.autoCdiHandleManagerAndFix();
			
			// ProxyScopedアノテーションを反映.
			quinaMembers.autoProxyScopedManagerAndFix();
			
			// アノテーションからコンフィグディレクトリを取得.
			String confDir = quinaMembers
				.getAnnotationDefineByConfigDirectory();
			
			// コンフィグディレクトリをセット.
			confDir = quinaMembers.setConfigDirectory(confDir);
			
			// 外部コンフィグファイルが存在しない場合は
			// LogConfigの初期処理.
			loadLogConfigByAnnotation();
			
			// 対象コンフィグに対するログ定義を読み込む.
			loadLogConfig();
			
			// LogコンフィグをFixさせる.
			LogFactory.getInstance().fixConfig();
			
			// Argsを設定.
			quinaMembers.setArgs(args);
			
			// シャットダウンデフォルトトークンを設定.
			ShutdownConstants.setDefaultToken(
				DEFAULT_SHUTDOWN_TOKEN);
			
			// ipPermissionアクセスコントロールのコンフィグを読み込んでFix.
			quinaMembers.loadIpPermissionACSConfigAndFix();
			
			// シャットダウンマネージャを生成し、quinaシャットダウンコールを登録.
			quinaMembers.regShutdownManager();
			
			// QuinaWorkerサービスの初期化処理.
			quinaMembers.initQuinaWorkerService();
			
			// AppendMimeのAnnotationを読み込む.
			AnnotationQuina.loadAppendMimeType(mainClass);
			
			// ServiceScopedアノテーションを反映.
			updateAnnotationByCdiService();
			
			// QuinaServiceScopedアノテーションを反映.
			updateAnnotationByQuinaService();
			
			// メインオブジェクト/クラスにCdiScopedアノテーションを反映.
			updateAnnotationByMain();
			
			// 登録してQuinaのコンフィグ情報をロードする.
			loadConfig();
			
			// 初期化成功.
			quinaMembers.successInit();
		} catch(CoreException ce) {
			// 初期化失敗.
			quinaMembers.failureInit();
			throw ce;
		} catch(Exception e) {
			// 初期化失敗.
			quinaMembers.failureInit();
			throw new QuinaException(e);
		}
	}
	
	/**
	 * Cdiサービス(ServiceScoped)群に対してAnnotation関連を反映.
	 */
	public final void updateAnnotationByCdiService() {
		final CdiHandleManager chman = quinaMembers.cdiHandleManager;
		final CdiServiceManager cman = quinaMembers.cdiManager;
		final int len = cman.size();
		for(int i = 0; i < len; i ++) {
			// アノテーションを注入.
			chman.inject(cman.getService(i));
		}
	}
	
	/**
	 * QuinaService(QuinaServiceScoped)群に対してAnnotation関連を反映.
	 */
	public final void updateAnnotationByQuinaService() {
		final CdiHandleManager chman = quinaMembers.cdiHandleManager;
		final QuinaServiceManager qsman = quinaMembers.quinaServiceManager;
		final int len = qsman.size();
		for(int i = 0; i < len; i ++) {
			// アノテーションを注入.
			chman.inject(qsman.get(i));
		}
	}
	
	/**
	 * Quinaメインオブジェクト/クラスに対してCdiScopedアノテーションを反映.
	 */
	public final void updateAnnotationByMain() {
		final Object mainObject = quinaMembers.mainObject;
		final Class<?> mainClass = quinaMembers.mainClass;
		final CdiHandleManager chman = quinaMembers.cdiHandleManager;
		// メインオブジェクトにCdiScopedアノテーションが定義されている場合.
		if(mainObject != null &&
			AnnotationQuina.isCdiScoped(mainObject)) {
			// メインオブジェクトにアノテーションを注入.
			chman.inject(mainObject);
		// メインクラスにCdiScopedアノテーションが定義されている場合.
		} else if(AnnotationQuina.isCdiScoped(mainClass)) {
			// メインクラスにアノテーションを注入.
			chman.inject(mainClass);
		}
	}
	
	// コンフィグ情報を読み込んで反映します.
	public final void loadConfig() {
		final String confDir = quinaMembers.getConfigDirectory();
		// 指定コンフィグディレクトリが存在しない場合.
		if(confDir == null || confDir.isEmpty()) {
			return;
		}
		// シャットダウンマネージャのコンフィグ定義.
		loadShutdownManagerConfig();
		// Etagマネージャのコンフィグ定義.
		loadEtagManagerConfig();
		// 標準コンポーネントのコンフィグ情報を読み込む.
		quinaMembers.quinaWorkerService.loadConfig(confDir);
		quinaMembers.httpServerService.loadConfig(confDir);
		// 登録サービスのコンフィグ情報を読み込む.
		final QuinaServiceManager qsman = quinaMembers.quinaServiceManager;
		final int len = qsman.size();
		for(int i = 0; i < len; i ++) {
			// loadConfigが呼ばれてない場合のみ読み込み.
			if(!qsman.get(i).isLoadConfig()) {
				qsman.get(i).loadConfig(confDir);
			}
		}
	}

	// ログのコンフィグ定義.
	public final boolean loadLogConfig() {
		final String confDir = quinaMembers.getConfigDirectory();
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
		QuinaMap<String, Object> json = QuinaUtil.loadJson(
			confDir, "log");
		if(json == null) {
			return false;
		}
		// ログコンフィグをセット.
		return logFactory.loadConfig(json);
	}
	
	// LogConfigアノテーションからログ定義を読み込む.
	public final boolean loadLogConfigByAnnotation() {
		final Class<?> mainClass = quinaMembers.getMainClass();
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
	public final boolean loadShutdownManagerConfig() {
		final String confDir = quinaMembers.getConfigDirectory();
		// コンフィグディレクトリが存在しない場合.
		if(confDir == null || confDir.isEmpty()) {
			return false;
		}
		final ShutdownManagerInfo info = quinaMembers
			.shutdownManager.getInfo();
		// shutdownManagerのコンフィグが既に設定されている場合.
		if(info.isConfig()) {
			// 処理しない.
			return true;
		}
		// shutdown.jsonのコンフィグファイルを取得.
		QuinaMap<String, Object> json = QuinaUtil.loadJson(confDir, "shutdown");
		if(json == null) {
			return false;
		}
		// shutdownManagerのコンフィグ条件をセット.
		info.config(json);
		return true;
	}

	// Etagマネージャのコンフィグ条件を設定.
	public final boolean loadEtagManagerConfig() {
		final String confDir = quinaMembers.getConfigDirectory();
		// コンフィグディレクトリが存在しない場合.
		if(confDir == null || confDir.isEmpty()) {
			return false;
		}
		final EtagManagerInfo info = quinaMembers
			.router.getEtagManagerInfo();
		if(info.isDone()) {
			// 処理しない.
			return true;
		}
		// etag.jsonのコンフィグファイルを取得.
		QuinaMap<String, Object> json =
			QuinaUtil.loadJson(confDir, "etag");
		if(json == null) {
			return false;
		}
		// etagManagerのコンフィグ条件をセット.
		info.config(json);
		return true;
	}
	
}
