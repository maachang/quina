package quina.command.generateCdi;

import quina.QuinaServiceManager;
import quina.Router;
import quina.annotation.cdi.AnnotationCdiConstants;
import quina.annotation.cdi.CdiHandleManager;
import quina.annotation.cdi.CdiHandleScoped;
import quina.annotation.cdi.CdiReflectManager;
import quina.annotation.cdi.CdiScoped;
import quina.annotation.cdi.CdiServiceManager;
import quina.annotation.cdi.ServiceScoped;
import quina.annotation.quina.QuinaServiceScoped;
import quina.annotation.route.AnyRoute;
import quina.annotation.route.ErrorRoute;
import quina.annotation.route.Route;

/**
 * GenerateCdi定義関連.
 */
public class GCdiConstants {
	private GCdiConstants() {}
	
	/**
	 * GenerateCdiバージョン.
	 */
	public static final String VERSION = "0.0.1";
	
	/**
	 *  GenerateCdiコマンド名.
	 */
	public static final String COMMAND_NAME = "genCdi";
	
	/**
	 * Cdi出力先ディレクトリ名.
	 */
	public static final String CDI_DIRECTORY_NAME = 
		GCdiUtil.packageNameToDirectory(
			AnnotationCdiConstants.CDI_PACKAGE_NAME);
	
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
	 * 出力ソースファイル群.
	 */
	public static final String[] OUTPUT_SOURCE_ARRAY = new String[] {
		AUTO_ROUTE_SOURCE_NAME
		,CDI_SERVICE_SOURCE_NAME
		,CDI_REFLECT_SOURCE_NAME
		,QUINA_SERVICE_SOURCE_NAME
		,CDI_HANDLE_SOURCE_NAME
	};

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
			|| GCdiUtil.isAnnotation(c, ServiceScoped.class)
			|| GCdiUtil.isAnnotation(c, CdiScoped.class)
		;
	}
}
