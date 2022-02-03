package quina.route.annotation;

import java.lang.reflect.InvocationTargetException;

import quina.compile.cdi.annotation.AnnotationCdiConstants;
import quina.component.Component;
import quina.component.error.ErrorComponent;
import quina.exception.QuinaException;

/**
 * RouteのAnnotationを読み込んでPathを取得.
 */
public class AnnotationRoute {
	private AnnotationRoute() {}
	
	/**
	 * Routeアノテーション自動読み込み実行用クラス名.
	 */
	public static final String AUTO_READ_ROUTE_CLASS = "LoadRouter";

	/**
	 * Routeアノテーション自動読み込み実行用メソッド名.
	 */
	public static final String AUTO_READ_ROUTE_METHOD = "load";
	
	/**
	 * AutoRoute実行.
	 * @return Router このオブジェクトが返却されます.
	 */
	public static final void autoRoute() {
		java.lang.Class<?> clazz;
		java.lang.reflect.Method method;
		try {
			// AutoRoute実行用のクラスを取得.
			clazz = Class.forName(
				AnnotationCdiConstants.CDI_PACKAGE_NAME + "." +
				AUTO_READ_ROUTE_CLASS);
			// 実行メソッドを取得.
			method = clazz.getMethod(AUTO_READ_ROUTE_METHOD);
		} catch(Exception e) {
			// クラスローディングやメソッド読み込みに失敗した場合は処理終了.
			return;
		}
		try {
			// Methodをstatic実行.
			method.invoke(null);
		} catch(InvocationTargetException it) {
			// メソッド実行でエラーの場合はエラー返却.
			throw new QuinaException(it.getCause());
		} catch(Exception e) {
			// メソッド実行でエラーの場合はエラー返却.
			throw new QuinaException(e);
		}
		return;
	}

	
	/**
	 * Annotationに定義されてるRouteのパスを取得.
	 * @param c コンポーネントを設定します.
	 * @return String Routeのパスが返却されます.
	 *                nullの場合、Routeのパスが設定されていません.
	 */
	public static final String loadRoute(Component c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		return loadRoute(c.getClass());
	}
	
	/**
	 * Annotationに定義されてるRouteのパスを取得.
	 * @param c コンポーネントクラスを設定します.
	 * @return String Routeのパスが返却されます.
	 *                nullの場合、Routeのパスが設定されていません.
	 */
	public static final String loadRoute(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		Route route = c.getAnnotation(Route.class);
		if(route == null) {
			return null;
		}
		return route.value();
	}
	
	/**
	 * Annotationに定義されてるAnyルートが定義されてるか取得.
	 * @param c コンポーネントを設定します.
	 * @return boolean Anyルートの場合 true が返却されます.
	 */
	public static final boolean loadAnyRoute(Component c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		return loadAnyRoute(c.getClass());
	}
	
	/**
	 * Annotationに定義されてるAnyルートが定義されてるか取得.
	 * @param c コンポーネントクラスを設定します.
	 * @return boolean Anyルートの場合 true が返却されます.
	 */
	public static final boolean loadAnyRoute(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		return c.isAnnotationPresent(AnyRoute.class);
	}
	
	/**
	 * Annotationに定義されてるErrorルートが定義されてるか取得.
	 * @param c エラーコンポーネントを設定します.
	 * @return Object[] {start, end, route} の条件で返却されます.
	 */
	public static final Object[] loadErrorRoute(ErrorComponent c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		return loadErrorRoute(c.getClass());
	}
	
	/**
	 * Annotationに定義されてるErrorルートが定義されてるか取得.
	 * @param c エラーコンポーネントクラスを設定します.
	 * @return Object[] {start, end, route} の条件で返却されます.
	 */
	public static final Object[] loadErrorRoute(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		ErrorRoute error = c.getAnnotation(ErrorRoute.class);
		if(error == null) {
			return null;
		}
		// 単独ステータス指定.
		if(error.status() > 0) {
			return new Object[] {error.status(), 0, error.route()};
		// 範囲ステータス指定.
		} else if(error.start() > 0) {
			if(error.end() > 0) {
				// 範囲ステータス指定.
				return new Object[] {error.start(), error.end(), error.route()};
			} else {
				// 単独ステータス指定.
				return new Object[] {error.start(), 0, error.route()};
			}
		}
		// any指定.
		return new Object[] {0, 0, error.route()};
	}
	
	/**
	 * Annotationに定義されてるFilePathのパスを取得.
	 * @param c コンポーネントを設定します.
	 * @return String ファイルのパスが返却されます.
	 *                nullの場合、ファイルのパスが設定されていません.
	 */
	public static final String loadFilePath(Component c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		return loadFilePath(c.getClass());
	}
	
	/**
	 * Annotationに定義されてるFilePathのパスを取得.
	 * @param c コンポーネントクラスを設定します.
	 * @return String ファイルのパスが返却されます.
	 *                nullの場合、ファイルのパスが設定されていません.
	 */
	public static final String loadFilePath(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		FilePath filePath = c.getAnnotation(FilePath.class);
		if(filePath == null) {
			return null;
		}
		return filePath.value();
	}
	
	/**
	 * Annotationに定義されてるResourcePackageのパスを取得.
	 * @param c コンポーネントを設定します.
	 * @return String ファイルのパスが返却されます.
	 *                nullの場合、リソースパッケージが設定されていません.
	 */
	public static final String loadResourcePackage(Component c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		return loadResourcePackage(c.getClass());
	}
	
	/**
	 * Annotationに定義されてるResourcePackageのパスを取得.
	 * @param c コンポーネントクラスを設定します.
	 * @return String ファイルのパスが返却されます.
	 *                nullの場合、リソースパッケージが設定されていません.
	 */
	public static final String loadResourcePackage(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		ResourcePackage resPackage = c.getAnnotation(ResourcePackage.class);
		if(resPackage == null) {
			return null;
		}
		return resPackage.value();
	}


}
