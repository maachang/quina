package quina.command.generateCdi;

import java.util.List;

import quina.QuinaService;
import quina.annotation.cdi.CdiHandle;
import quina.annotation.cdi.CdiHandleScoped;
import quina.annotation.cdi.CdiScoped;
import quina.annotation.cdi.ServiceScoped;
import quina.annotation.quina.QuinaServiceScoped;
import quina.annotation.reflection.ProxyScoped;
import quina.annotation.route.AnnotationRoute;
import quina.annotation.route.AnyRoute;
import quina.annotation.route.ErrorRoute;
import quina.annotation.route.Route;
import quina.component.Component;
import quina.component.error.ErrorComponent;

/**
 * Cdiオブジェクトを抽出.
 */
public class GCdiExtraction {
	private GCdiExtraction() {}
	
	/**
	 * Cdiオブジェクトを抽出.
	 * @param classList 抽出されたクラス名のリストを設定します.
	 * @param params Cdiパラメータを設定します.
	 * @throws Exception 例外.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final void extraction(List<String> classList, GCdiParams params)
		throws Exception {
		String className;
		final int len = classList != null ? classList.size() : 0;
		for(int i = 0; i < len; i ++) {
			// クラス名を取得.
			className = classList.get(i);
			// クラスを取得.
			final Class c;
			try {
				c = GCdiUtil.getClass(className, params);
			} catch(NoClassDefFoundError e) {
				// 詳細情報を表示する場合.
				if(params.isVerbose()) {
					// 何らかの理由で読み込み失敗の場合.
					System.out.println();
					System.out.println("    # loadError    :");
					System.out.println("        src        : " + className);
					System.out.println("        error      : " + e);
				}
				// クラス取得に失敗する場合は無視.
				continue;
			}
			// 対象がAnnotationの場合は処理しない.
			if(c.isAnnotation()) {
				continue;
			}
			// 利用可能なアノテーションが定義されていない場合.
			if(!GCdiConstants.isDefineAnnotation(c) &&
				!GCdiConstants.isProxyAnnotation(c)) {
				// クラスのインスタンスを生成.
				Object o;
				try {
					o = GCdiUtil.newInstance(c);
				} catch(Exception e) {
					continue;
				}
				// アノテーションなしのコンポーネントの場合.
				if(o instanceof Component || o instanceof ErrorComponent) {
					// Reflectリストに追加.
					params.refList.add(className);
				// アノテーションなしのQuinaServiceの場合.
				} else if(o instanceof QuinaService) {
					// Reflectリストに追加.
					params.refList.add(className);
				// アノテーションなしのCdiHandleの場合.
				} else if(o instanceof CdiHandle) {
					// Reflectリストに追加.
					params.refList.add(className);
				}
				continue;
			}
			// CdiScoped定義のクラスの場合.
			// 継承アノテーションありで検索.
			if(GCdiUtil.isAnnotation(c, CdiScoped.class)) {
				System.out.println("  > cdiScoped      : " + className);
				// Reflectリストに追加.
				params.refList.add(className);
				continue;
			}
			
			// ServiceScoped定義のCdiServiceの場合.
			// 継承アノテーションありで検索.
			if(GCdiUtil.isAnnotation(c, ServiceScoped.class)) {
				System.out.println("  > cdiService     : " + className);
				// Cdiリストに追加.
				params.cdiList.add(className);
				// Reflectリストに追加.
				params.refList.add(className);
				continue;
			}
			
			// ProxyScoped定義の場合.
			if(c.isAnnotationPresent(ProxyScoped.class)) {
				System.out.println("  > proxy          : " + className);
				// ProxyScopedリストに追加.
				params.prxList.add(className);
				continue;
			}
			
			// クラスのインスタンスを生成.
			final Object o = GCdiUtil.newInstance(c);
			
			// QuinaServiceScoped定義のQuinaServiceの場合.
			if(o instanceof QuinaService &&
				c.isAnnotationPresent(QuinaServiceScoped.class)) {
				System.out.println("  > quinaService   : " + className);
				// QuinaServiceリストに追加.
				params.qsrvList.add(className);
				// Reflectリストに追加.
				params.refList.add(className);
				continue;
			}
			
			// CdiHandle定義のCdiAnnotationScopedの場合.
			if(o instanceof CdiHandle &&
				c.isAnnotationPresent(CdiHandleScoped.class)) {
				System.out.println("  > cdiHandle      : " + className);
				// CdiHandleリストに追加.
				params.hndList.add(className);
				// Reflectリストに追加.
				params.refList.add(className);
				continue;
			}
			
			// 対象がコンポーネントクラスの場合.
			if(o instanceof Component) {
				// @Route付属のコンポーネントを登録.
				if(c.isAnnotationPresent(Route.class)) {
					Route r = (Route)c.getAnnotation(Route.class);
					if(r != null) {
						System.out.println("  > route          : " + className);
						System.out.println("                      path: " + r.value());
					} else {
						System.out.println("  > route          : " + className);
					}
					// RouterListに登録.
					params.routeList.add(className);
				// @AnyRoute付属のコンポーネントを登録.
				} else if(c.isAnnotationPresent(AnyRoute.class)) {
					System.out.println("  > any            : " + className);
					// AnyRouteに登録.
					params.any = className;
				}
				// Reflectリストに追加.
				params.refList.add(className);
			// 対象がエラーコンポーネントの場合.
			} else if(o instanceof ErrorComponent) {
				// @ErrorRoute付属のコンポーネントを登録.
				if(c.isAnnotationPresent(ErrorRoute.class)) {
					int[] es = AnnotationRoute.loadErrorRoute(c);
					if(es == null || es[0] == 0) {
						System.out.println("  > error          : " + className);
					} else if(es[1] == 0) {
						System.out.println("  > error          : " + className);
						System.out.println("                      status: " + es[0]);
					} else {
						System.out.println("  > error          : " + className);
						System.out.println("                      status: " +
							es[0] + "-" + es[1]);
					}
					// ErrorRouteリストに登録.
					params.errList.add(className);
				}
				// Reflectリストに追加.
				params.refList.add(className);
			}
		}
	}
}
