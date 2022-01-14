package quina.compile.tool;

import quina.QuinaServiceManager;
import quina.Router;
import quina.annotation.cdi.AnnotationCdiConstants;
import quina.annotation.cdi.CdiHandleManager;
import quina.annotation.cdi.CdiHandleScoped;
import quina.annotation.cdi.CdiReflectManager;
import quina.annotation.cdi.CdiScoped;
import quina.annotation.cdi.CdiServiceManager;
import quina.annotation.cdi.ServiceScoped;
import quina.annotation.proxy.AnnotationProxyScopedConstants;
import quina.annotation.proxy.ProxyScoped;
import quina.annotation.proxy.ProxyScopedManager;
import quina.annotation.quina.QuinaLoopScoped;
import quina.annotation.quina.QuinaServiceScoped;
import quina.annotation.route.AnyRoute;
import quina.annotation.route.ErrorRoute;
import quina.annotation.route.Route;
import quina.worker.QuinaWorkerService;

/**
 * GenerateCdi定義関連.
 */
public class QuinaCTConstants {
	private QuinaCTConstants() {}
	
	/**
	 * GenerateCdiバージョン.
	 */
	public static final String VERSION = "0.0.1";
	
	/**
	 *  GenerateCdiコマンド名.
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
		Router.AUTO_READ_ROUTE_CLASS + ".java";
	
	/**
	 * AutoCdiService出力先Javaソースファイル名.
	 */
	public static final String CDI_SERVICE_SOURCE_NAME =
		CdiServiceManager.AUTO_READ_CDI_SERVICE_CLASS + ".java";
	
	/**
	 * AutoCdiReflect出力先Javaソースファイル名.
	 */
	public static final String CDI_REFLECT_SOURCE_NAME =
		CdiReflectManager.AUTO_READ_CDI_REFLECT_CLASS + ".java";
	
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
		,CDI_REFLECT_SOURCE_NAME
		,QUINA_SERVICE_SOURCE_NAME
		,CDI_HANDLE_SOURCE_NAME
		,CDI_PROXY_SCOPED_SOURCE_NAME
		,QUINA_LOOP_SCOPED_SOURCE_NAME
	};
	
	/**
	 * デフォルトのNativeConfigDirectory.
	 */
	public static final String DEF_NATIVE_CONFIG_DIR = "nativeImageConfig";

	/**
	 * GenerateCdiで利用対象のアノテーションが定義されているかチェック.
	 * @param c 確認するクラスを設定します.
	 * @return boolean true の場合、GenerateCdiで利用可能なアノテーションが
	 *                 存在します.
	 */
	public static final boolean isDefineAnnotation(Class<?> c) {
		return c.isAnnotationPresent(Route.class)
			|| c.isAnnotationPresent(AnyRoute.class)
			|| c.isAnnotationPresent(ErrorRoute.class)
			|| c.isAnnotationPresent(QuinaServiceScoped.class)
			|| c.isAnnotationPresent(CdiHandleScoped.class)
			|| c.isAnnotationPresent(QuinaLoopScoped.class)
			|| QuinaCTUtil.isAnnotation(c, ServiceScoped.class)
			|| QuinaCTUtil.isAnnotation(c, CdiScoped.class)
		;
	}
	
	/**
	 * ProxyScopedで利用対象のアノテーションが定義されてるかチェック.
	 * @param c 確認するクラスを設定します.
	 * @return boolean true の場合、ProxyScopedで利用可能なアノテーションが
	 *                 存在します.
	 */
	public static final boolean isProxyAnnotation(Class<?> c) {
		return c.isAnnotationPresent(ProxyScoped.class);
	}
}
