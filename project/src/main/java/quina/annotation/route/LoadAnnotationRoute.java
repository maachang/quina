package quina.annotation.route;

import quina.component.Component;
import quina.exception.QuinaException;

/**
 * RouteのAnnotationを読み込んでPathを取得.
 */
public class LoadAnnotationRoute {
	private LoadAnnotationRoute() {}
	
	/**
	 * Annotationに定義されてるRouteのパスを取得.
	 * @param c コンポーネントを設定します.
	 * @return String Routeのパスが返却されます.
	 *                nullの場合、Routeのパスが設定されていません.
	 */
	public static final String loadRoute(Component c) {
		if(c == null) {
			throw new QuinaException("The specified component is Null.");
		}
		Route route = c.getClass().getAnnotation(Route.class);
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
			throw new QuinaException("The specified component is Null.");
		}
		return c.getClass().isAnnotationPresent(AnyRoute.class);
	}
	
	/**
	 * Annotationに定義されてるErrorルートが定義されてるか取得.
	 * @param c コンポーネントを設定します.
	 * @return boolean Errorルートの場合 true が返却されます.
	 */
	public static final boolean loadErrorRoute(Component c) {
		if(c == null) {
			throw new QuinaException("The specified component is Null.");
		}
		return c.getClass().isAnnotationPresent(ErrorRoute.class);
	}

	
	/**
	 * Annotationに定義されてるFilePathのパスを取得.
	 * @param c コンポーネントを設定します.
	 * @return String ファイルのパスが返却されます.
	 *                nullの場合、ファイルのパスが設定されていません.
	 */
	public static final String loadFilePath(Component c) {
		if(c == null) {
			throw new QuinaException("The specified component is Null.");
		}
		FilePath filePath = c.getClass().getAnnotation(FilePath.class);
		if(filePath == null) {
			return null;
		}
		return filePath.value();
	}

}
