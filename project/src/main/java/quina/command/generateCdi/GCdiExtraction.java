package quina.command.generateCdi;

import java.util.List;

import quina.QuinaService;
import quina.annotation.cdi.CdiHandle;
import quina.annotation.cdi.CdiHandleScoped;
import quina.annotation.cdi.CdiScoped;
import quina.annotation.cdi.ServiceScoped;
import quina.annotation.proxy.ProxyScoped;
import quina.annotation.quina.QuinaServiceScoped;
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
	public static final void extraction(
		List<String> classList, String javaSourceDir, String clazzDir,
		GCdiParams params)
		throws Exception {
		String className;
		final int len = classList == null ? 0 : classList.size();
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
					// 何らかの理由で読み込み失場合.
					System.out.println();
					System.out.println("    # loadError       :");
					System.out.println("        src           : " + className);
					System.out.println("        error         : " + e);
				}
				// クラス取得に失敗する場合は無視.
				continue;
			}
			// 対象がAnnotationの場合は処理しない.
			if(c.isAnnotation()) {
				continue;
			}
			
			// NativeImageコンフィグ用アノテーション定義の場合.
			if(NativeImages.executeExecuteStep(c, params)) {
				// 読み込まれたら対象クラス名を出力.
				System.out.println("  > loadNativeConf    : " + className);
			}
			
			// copyResourceアノテーション定義の場合.
			if(CopyResource.executeExecuteStep(c, javaSourceDir, clazzDir)) {
				// 読み込まれたら対象クラス名を出力.
				System.out.println("  > copyResource      : " + className);
			}
			
			// 利用可能なアノテーションが定義されていない場合.
			if(!GCdiConstants.isDefineAnnotation(c) &&
				!GCdiConstants.isProxyAnnotation(c)) {
				
				// クラスのインスタンスを生成.
				Object o;
				try {
					o = GCdiUtil.newInstance(c);
				} catch(java.lang.NoClassDefFoundError e) {
					if(params.verbose) {
						System.out.println(
							"  > [WARNING] " + e.getClass().getName() +
							": " + c.getName());
					}
					// クラスロードエラーは無視.
					continue;
				} catch(Exception e) {
					if(params.verbose) {
						System.out.println(
							"  > [WARNING] " + e.getClass().getName() +
							": " + c.getName());
					}
					// 例外も無視.
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
				System.out.println("  > cdiScoped         : " + className);
				// Reflectリストに追加.
				params.refList.add(className);
				continue;
			}
			
			// ServiceScoped定義のCdiServiceの場合.
			// 継承アノテーションありで検索.
			if(GCdiUtil.isAnnotation(c, ServiceScoped.class)) {
				System.out.println("  > cdiService        : " + className);
				// Cdiリストに追加.
				params.cdiList.add(className);
				// Reflectリストに追加.
				params.refList.add(className);
				continue;
			}
			
			// ProxyScoped定義の場合.
			if(c.isAnnotationPresent(ProxyScoped.class)) {
				System.out.println("  > proxy             : " + className);
				// ProxyScopedリストに追加.
				params.prxList.add(className);
				continue;
			}
			
			// クラスのインスタンスを生成.
			final Object o = GCdiUtil.newInstance(c);
			
			// QuinaServiceScoped定義のQuinaServiceの場合.
			if(o instanceof QuinaService &&
				c.isAnnotationPresent(QuinaServiceScoped.class)) {
				System.out.println("  > quinaService      : " + className);
				// QuinaServiceリストに追加.
				params.qsrvList.add(className);
				// Reflectリストに追加.
				params.refList.add(className);
				continue;
			}
			
			// CdiHandle定義のCdiAnnotationScopedの場合.
			if(o instanceof CdiHandle &&
				c.isAnnotationPresent(CdiHandleScoped.class)) {
				System.out.println("  > cdiHandle         : " + className);
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
						System.out.println("  > route             : " + className);
						System.out.println("                         path: " + r.value());
					} else {
						System.out.println("  > route             : " + className);
					}
					// RouterListに登録.
					params.routeList.add(className);
				// @AnyRoute付属のコンポーネントを登録.
				} else if(c.isAnnotationPresent(AnyRoute.class)) {
					System.out.println("  > any               : " + className);
					// AnyRouteに登録.
					params.any = className;
				}
				// Reflectリストに追加.
				params.refList.add(className);
			// 対象がエラーコンポーネントの場合.
			} else if(o instanceof ErrorComponent) {
				// @ErrorRoute付属のコンポーネントを登録.
				if(c.isAnnotationPresent(ErrorRoute.class)) {
					StringBuilder buf = new StringBuilder();
					Object[] es = AnnotationRoute.loadErrorRoute(c);
					int esLen = es == null ? 0 : es.length;
					for(int e = 0; e < esLen; e ++) {
						if(e != 0) {
							buf.append(", ");
						}
						if(es[e] instanceof String) {
							buf.append("\"").append(es[e]).append("\"");
						} else {
							buf.append(es[e]);
						}
					}
					System.out.println("  > error             : " + className);
					if(esLen > 0) {
						System.out.println("                          " + buf.toString());
					}
					buf = null;
					// ErrorRouteリストに登録.
					params.errList.add(className);
				}
				// Reflectリストに追加.
				params.refList.add(className);
			}
		}
	}
}
