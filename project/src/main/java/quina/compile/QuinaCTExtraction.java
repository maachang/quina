package quina.compile;

import java.util.List;

import quina.QuinaService;
import quina.annotation.QuinaLoopScoped;
import quina.annotation.QuinaServiceScoped;
import quina.compile.cdi.annotation.CdiHandle;
import quina.compile.cdi.annotation.CdiHandleScoped;
import quina.compile.cdi.annotation.CdiScoped;
import quina.compile.cdi.annotation.ServiceScoped;
import quina.compile.cdi.annotation.proxy.ProxyScoped;
import quina.compile.graalvm.GraalvmOutNativeConfig;
import quina.component.Component;
import quina.component.error.ErrorComponent;
import quina.route.annotation.AnnotationRoute;
import quina.route.annotation.AnyRoute;
import quina.route.annotation.ErrorRoute;
import quina.route.annotation.Route;
import quina.worker.QuinaLoopElement;

/**
 * Cdiオブジェクトを抽出.
 * 現状のコンパイル環境からCdiオブジェクトを抽出.
 */
public class QuinaCTExtraction {
	private QuinaCTExtraction() {}
	
	/**
	 * Cdiオブジェクトを抽出.
	 * @param params QuinaCompileToolパラメータを設定します.
	 * @throws Exception 例外.
	 */
	public static final void extraction(
		QuinaCTParams params)
		throws Exception {
		Class<?> c;
		String cname;
		try {
			final List<String> classList = params.classFileList;
			final int len = classList.size();
			// 読み込まれたクラス名群の内容を走査する.
			for(int i = 0; i < len; i ++) {
				// クラス名を取得.
				cname = classList.get(i);
				// クラスオブジェクトを取得.
				if((c = getClass(params, cname)) == null) {
					// クラス取得失敗の場合は処理しない.
					continue;
				}
				
				// NativeImageコンフィグ用アノテーション定義を読み込む.
				executeExecuteStep(params, c, cname);
				
				// このクラスがAnnotation定義自体の場合は処理しない.
				if(c.isAnnotation()) {
					continue;
				}
				
				// 非アノテーションに対するCdi条件の抽出を実施.
				if(noAnnotationByInjectField(params, c, cname)) {
					// 対象の場合.
					continue;
				}
				
				// Cdi条件アノテーションの抽出を実施.
				if(annotationByInjectField(params, c, cname)) {
					// 対象の場合.
					continue;
				}
				
				// アノテーションコンポーネントクラスの抽出を実施.
				if(annotationComponent(params, c, cname)) {
					// 対象の場合.
					continue;
				}
				
				// アノテーションループ要素クラスの抽出を実施.
				if(annotationLoopElement(params, c, cname)) {
					// 対象の場合.
					continue;
				}
			}
		} finally {
			params.classFileList.clear();
		}
	}
	
	// アノテーションなしのInjectField確認設定を行う.
	private static final boolean noAnnotationByInjectField(
		QuinaCTParams params, Class<?> c, String cname) {
		// 利用可能なアノテーションが定義されている場合.
		if(QuinaCTDefine.isAnnotation(c) ||
			QuinaCTDefine.isProxyAnnotation(c)) {
			// 処理しない.
			return false;
		}
		
		// クラスのインスタンスを生成.
		Object o = null;
		if((o = getObject(params, c)) == null) {
			// 生成失敗の場合.
			return true;
		}
		
		// アノテーションなしのコンポーネントの場合.
		if(o instanceof Component ||
			o instanceof ErrorComponent) {
			// InjectFieldリストに追加.
			params.injFdList.add(cname);
		// アノテーションなしのQuinaServiceの場合.
		} else if(o instanceof QuinaService) {
			// InjectFieldリストに追加.
			params.injFdList.add(cname);
		// アノテーションなしのCdiHandleの場合.
		} else if(o instanceof CdiHandle) {
			// InjectFieldリストに追加.
			params.injFdList.add(cname);
		// アノテーションなしのQuinaLoopElement.
		} else if(o instanceof QuinaLoopElement) {
			// InjectFieldリストに追加.
			params.injFdList.add(cname);
		}
		//処理完了の場合.
		return true;
	}
	
	// アノテーションありのInjectField確認設定を行う.
	private static final boolean annotationByInjectField(
		QuinaCTParams params, Class<?> c, String cname)
		throws Exception {
		// CdiScoped定義のクラスの場合.
		// 継承アノテーションありで検索.
		if(QuinaCTUtil.isAnnotation(c, CdiScoped.class)) {
			System.out.println("  > cdiScoped         : " + cname);
			// InjectFieldリストに追加.
			params.injFdList.add(cname);
			return true;
		}
		
		// ServiceScoped定義のCdiServiceの場合.
		// 継承アノテーションありで検索.
		if(QuinaCTUtil.isAnnotation(c, ServiceScoped.class)) {
			System.out.println("  > cdiService        : " + cname);
			// Cdiリストに追加.
			params.cdiList.add(cname);
			// InjectFieldリストに追加.
			params.injFdList.add(cname);
			return true;
		}
		
		// ProxyScoped定義の場合.
		if(c.isAnnotationPresent(ProxyScoped.class)) {
			System.out.println("  > proxy             : " + cname);
			// ProxyScopedリストに追加.
			params.prxList.add(cname);
			return true;
		}
		
		// クラスのインスタンスを生成.
		Object o = null;
		if((o = getObject(params, c)) == null) {
			// 生成失敗の場合.
			return false;
		}
		
		// QuinaServiceScoped定義のQuinaServiceの場合.
		if(o instanceof QuinaService &&
			c.isAnnotationPresent(QuinaServiceScoped.class)) {
			System.out.println("  > quinaService      : " + cname);
			// QuinaServiceリストに追加.
			params.qsrvList.add(cname);
			// InjectFieldリストに追加.
			params.injFdList.add(cname);
			return true;
		}
		
		// CdiHandle定義のCdiAnnotationScopedの場合.
		if(o instanceof CdiHandle &&
			c.isAnnotationPresent(CdiHandleScoped.class)) {
			System.out.println("  > cdiHandle         : " + cname);
			// CdiHandleリストに追加.
			params.hndList.add(cname);
			// InjectFieldリストに追加.
			params.injFdList.add(cname);
			return true;
		}
		
		return false;
	}
	
	// アノテーションコンポーネント定義.
	private static final boolean annotationComponent(
		QuinaCTParams params, Class<?> c,String cname)
		throws Exception {
		
		// クラスのインスタンスを生成.
		Object o = null;
		if((o = getObject(params, c)) == null) {
			// 生成失敗の場合.
			return false;
		}
		
		// 対象がコンポーネントクラスの場合.
		if(o instanceof Component) {
			// @Route付属のコンポーネントを登録.
			if(c.isAnnotationPresent(Route.class)) {
				Route r = (Route)c.getAnnotation(Route.class);
				if(r != null) {
					System.out.println("  > route             : " + cname);
					System.out.println("                         path: " + r.value());
				} else {
					System.out.println("  > route             : " + cname);
				}
				// RouterListに登録.
				params.routeList.add(cname);
			// @AnyRoute付属のコンポーネントを登録.
			} else if(c.isAnnotationPresent(AnyRoute.class)) {
				System.out.println("  > any               : " + cname);
				// AnyRouteに登録.
				params.any = cname;
			}
			// InjectFieldリストに追加.
			params.injFdList.add(cname);
			return true;
		}
		
		// 対象がエラーコンポーネントの場合.
		if(o instanceof ErrorComponent) {
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
				System.out.println(
				"  > error             : " + cname);
				if(esLen > 0) {
					System.out.println(
				"                          " + buf.toString());
				}
				buf = null;
				// ErrorRouteリストに登録.
				params.errList.add(cname);
			}
			// InjectFieldリストに追加.
			params.injFdList.add(cname);
			return true;
		}
		return false;
	}
	
	// アノテーションループ要素定義.
	private static final boolean annotationLoopElement(
		QuinaCTParams params, Class<?> c, String cname)
		throws Exception {
		
		// クラスのインスタンスを生成.
		Object o = null;
		if((o = getObject(params, c)) == null) {
			// 生成失敗の場合.
			return false;
		}
		
		// QuinaLoopElementの場合.
		if(o instanceof QuinaLoopElement) {
			// ＠QuinaLoopScoped付属のQuinaLoopElementを登録.
			if(c.isAnnotationPresent(QuinaLoopScoped.class)) {
				System.out.println("  > quinaLoopElement  : " + cname);
				// Loopリストに登録.
				params.loopList.add(cname);
			}
			// InjectFieldリストに追加.
			params.injFdList.add(cname);
			return true;
		}
		return false;
	}
	
	// クラスオブジェクトを取得.
	private static final Class<?> getClass(
		QuinaCTParams params, String name) {
		try {
			return QuinaCTUtil.getClass(name, params);
		} catch(Throwable e) {
			// 詳細情報を表示する場合.
			if(params.isVerbose()) {
				// 何らかの理由で読み込み失場合.
				System.out.println("  > [WARNING] " + name);
				System.out.println("    src               : " + name);
				System.out.println("    error             : " + e);
			}
			// クラス取得に失敗する場合は無視.
			return null;
		}
	}
	
	// 対象オブジェクトを取得.
	private static final Object getObject(
		QuinaCTParams params, Class<?> c) {
		// クラスのインスタンスを生成.
		try {
			return QuinaCTUtil.newInstance(c);
		} catch(Throwable e) {
			// この場合、Class生成は出来るが、一方でオブジェクト化で
			// 読み込みオブジェクトが足らない場合があって、読み込み失敗
			// する場合"java.lang.NoSuchMethodException"等があるので、
			// その場合は読み込みしないようにする.
			//
			// この場合、読み込みは失敗するが、アノテーションの定義は
			// 抽出出来る限り行わなければ「正しい結果」にならない.
			//
			// 注意が必要.
			if(params.isVerbose()) {
				System.out.println(
					"  > [WARNING] " + e.getClass().getName() +
					": " + c.getName());
			}
			return null;
		}
	}
	
	// NativeImageコンフィグ用アノテーション定義を読み込む.
	private static final void executeExecuteStep(
		QuinaCTParams params, Class<?> c, String cname) {
		// NativeImageコンフィグ用アノテーション定義の場合.
		if(GraalvmOutNativeConfig.executeExecuteStep(c, params)) {
			// 読み込まれたら対象クラス名を出力.
			System.out.println("  > loadNativeConf    : " + cname);
		}
	}
}
