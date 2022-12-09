package quina.compile;

import quina.QuinaServiceManager;
import quina.compile.cdi.annotation.AnnotationCdiConstants;
import quina.compile.cdi.annotation.CdiHandleManager;
import quina.compile.cdi.annotation.CdiInjectFieldManager;
import quina.compile.cdi.annotation.CdiServiceManager;
import quina.compile.cdi.annotation.proxy.AnnotationProxyScopedConstants;
import quina.compile.cdi.annotation.proxy.ProxyScopedManager;
import quina.route.annotation.AnnotationRoute;
import quina.worker.QuinaWorkerService;

/**
 * QuinaCT(CompieTool)定義関連.
 */
public class QuinaCTConstants {
	private QuinaCTConstants() {}
	
	/**
	 * QuinaCT(CompieTool)バージョン.
	 */
	public static final String VERSION = "0.0.1";
	
	/**
	 * QuinaCT(CompieTool)コマンド名.
	 */
	public static final String COMMAND_NAME = "qct";
	
	/**
	 * Cdi出力先ディレクトリ名.
	 */
	public static final String CDI_DIRECTORY_NAME =
		QuinaCTUtil.packageNameToDirectory(
			AnnotationCdiConstants.CDI_PACKAGE_NAME);
	
	/**
	 * ProxyScoped出力先ディレクトリ名.
	 */
	public static final String CDI_PROXY_DIRECTORY_NAME =
		QuinaCTUtil.packageNameToDirectory(
			AnnotationProxyScopedConstants.OUTPUT_AUTO_SOURCE_PROXY_PACKAGE_NAME);
	
	/**
	 * AutoRoute出力先Javaソースファイル名.
	 */
	public static final String AUTO_ROUTE_SOURCE_NAME =
		AnnotationRoute.AUTO_READ_ROUTE_CLASS + ".java";
	
	/**
	 * AutoCdiService出力先Javaソースファイル名.
	 */
	public static final String CDI_SERVICE_SOURCE_NAME =
		CdiServiceManager.AUTO_READ_CDI_SERVICE_CLASS + ".java";
	
	/**
	 * AutoCdiInjectField出力先Javaソースファイル名.
	 */
	public static final String CDI_INJECT_FIELD_SOURCE_NAME =
		CdiInjectFieldManager.AUTO_READ_CDI_INJECT_FIELD_CLASS + ".java";
	
	/**
	 * AutoQuinaService出力先Javaソースファイル名.
	 */
	public static final String QUINA_SERVICE_SOURCE_NAME =
		QuinaServiceManager.AUTO_READ_QUINA_SERVICE_CLASS + ".java";
	
	/**
	 * AutoHandle出力先Javaソースファイル名.
	 */
	public static final String CDI_HANDLE_SOURCE_NAME =
		CdiHandleManager.AUTO_READ_CDI_HANDLE_CLASS + ".java";
	
	/**
	 * ProxyScoped出力先Javaソースファイル名.
	 */
	public static final String CDI_PROXY_SCOPED_SOURCE_NAME =
		ProxyScopedManager.AUTO_READ_PROXY_SCOPED_CLASS + ".java";
	
	/**
	 * QuinaLoopScoped出力先Javaソースファイル名.
	 */
	public static final String QUINA_LOOP_SCOPED_SOURCE_NAME =
		QuinaWorkerService.AUTO_READ_QUINA_LOOP_ELEMENT_CLASS + ".java";

	
	/**
	 * 出力ソースファイル群.
	 */
	public static final String[] OUTPUT_SOURCE_ARRAY = new String[] {
		AUTO_ROUTE_SOURCE_NAME
		,CDI_SERVICE_SOURCE_NAME
		,CDI_INJECT_FIELD_SOURCE_NAME
		,QUINA_SERVICE_SOURCE_NAME
		,CDI_HANDLE_SOURCE_NAME
		,CDI_PROXY_SCOPED_SOURCE_NAME
		,QUINA_LOOP_SCOPED_SOURCE_NAME
	};
	
	/**
	 * デフォルトのNativeConfigDirectory.
	 */
	public static final String DEF_NATIVE_CONFIG_DIR = "nativeImageConfig";
}
