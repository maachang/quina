package quina.annotation.route;

import quina.component.Component;
import quina.component.ErrorComponent;
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
			throw new QuinaException("The specified component is Null.");
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
			throw new QuinaException("The specified component is Null.");
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
			throw new QuinaException("The specified component is Null.");
		}
		return c.isAnnotationPresent(AnyRoute.class);
	}
	
	/**
	 * Annotationに定義されてるErrorルートが定義されてるか取得.
	 * @param c エラーコンポーネントを設定します.
	 * @return int[] {start, end} の条件で返却されます.
	 */
	public static final int[] loadErrorRoute(ErrorComponent c) {
		if(c == null) {
			throw new QuinaException("The specified component is Null.");
		}
		return loadErrorRoute(c.getClass());
	}
	
	/**
	 * Annotationに定義されてるErrorルートが定義されてるか取得.
	 * @param c エラーコンポーネントクラスを設定します.
	 * @return int[] {start, end} の条件で返却されます.
	 */
	public static final int[] loadErrorRoute(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified component is Null.");
		}
		ErrorRoute error = c.getAnnotation(ErrorRoute.class);
		if(error == null) {
			return null;
		}
		// 単独ステータス指定.
		if(error.status() > 0) {
			return new int[] {error.status(), 0};
		// 範囲ステータス指定.
		} else if(error.start() > 0) {
			if(error.end() > 0) {
				// 範囲ステータス指定.
				return new int[] {error.start(), error.end()};
			} else {
				// 単独ステータス指定.
				return new int[] {error.start(), 0};
			}
		}
		// any指定.
		return new int[] {0, 0};
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
			throw new QuinaException("The specified component is Null.");
		}
		FilePath filePath = c.getAnnotation(FilePath.class);
		if(filePath == null) {
			return null;
		}
		return filePath.value();
	}

}
