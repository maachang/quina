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
	public static final String load(Component c) {
		if(c == null) {
			throw new QuinaException("The specified component is Null.");
		}
		Route route = c.getClass().getAnnotation(Route.class);
		if(route == null) {
			return null;
		}
		return route.value();
	}
}
