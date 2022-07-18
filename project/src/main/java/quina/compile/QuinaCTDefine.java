package quina.compile;

import quina.annotation.QuinaLoopScoped;
import quina.annotation.QuinaServiceScoped;
import quina.compile.cdi.annotation.CdiHandleScoped;
import quina.compile.cdi.annotation.CdiScoped;
import quina.compile.cdi.annotation.ServiceScoped;
import quina.compile.cdi.annotation.proxy.ProxyScoped;
import quina.route.annotation.AnyRoute;
import quina.route.annotation.ErrorRoute;
import quina.route.annotation.Route;

/**
 * 対象Annotation定義判別処理.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class QuinaCTDefine {
	private QuinaCTDefine() {}
	
	// ターゲットアノテーション.
	private static final Class[] TARGET_ANNOTATIONS = new Class[] {
		Route.class
		,AnyRoute.class
		,ErrorRoute.class
		,QuinaServiceScoped.class
		,CdiHandleScoped.class
		,QuinaLoopScoped.class
	};
	
	// ターゲット継承アノテーション.
	private static final Class[] TARGET_ANNOTATIONS_BY_EXTENDS = new Class[] {
		ServiceScoped.class
		,CdiScoped.class
	};
	
	// ターゲットプロキシーアノテーション.
	private static final Class[] TARGET_ANNOTATIONS_BY_PROXYS = new Class[] {
		ProxyScoped.class
	};
	
	
	/**
	 * GenerateCdiで利用対象のアノテーションが定義されているかチェック.
	 * @param c 確認するクラスを設定します.
	 * @return boolean true の場合、GenerateCdiで利用可能なアノテーションが
	 *                 存在します.
	 */
	public static final boolean isAnnotation(Class<?> c) {
		int i, len;
		// 対象アノテーション.
		len = TARGET_ANNOTATIONS.length;
		for(i = 0; i < len; i ++) {
			if(c.isAnnotationPresent(TARGET_ANNOTATIONS[i])) {
				return true;
			}
		}
		// 対象継承アノテーション.
		len = TARGET_ANNOTATIONS_BY_EXTENDS.length;
		for(i = 0; i < len; i ++) {
			if(QuinaCTUtil.isAnnotation(
				c, TARGET_ANNOTATIONS_BY_EXTENDS[i])) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * ProxyScopedで利用対象のアノテーションが定義されてるかチェック.
	 * @param c 確認するクラスを設定します.
	 * @return boolean true の場合、ProxyScopedで利用可能なアノテーションが
	 *                 存在します.
	 */
	public static final boolean isProxyAnnotation(Class<?> c) {
		int i, len;
		// 対象プロキシーアノテーション.
		len = TARGET_ANNOTATIONS_BY_PROXYS.length;
		for(i = 0; i < len; i ++) {
			if(c.isAnnotationPresent(
				TARGET_ANNOTATIONS_BY_PROXYS[i])) {
				return true;
			}
		}
		return false;
	}

}
