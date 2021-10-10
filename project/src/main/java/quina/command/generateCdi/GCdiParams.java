package quina.command.generateCdi;

import java.util.ArrayList;
import java.util.List;

/**
 * GenerateCdiパラメータ.
 */
public class GCdiParams {
	// verbose..
	public boolean verbose = false;
	
	// Route.
	public final List<String> routeList = new ArrayList<String>();
	
	// RouteAny.
	public String any = null;
	
	// RouteError.
	public final List<String> errList = new ArrayList<String>();
	
	// CdiService.
	public final List<String> cdiList = new ArrayList<String>();
	
	// CdiReflect.
	public final List<String> refList = new ArrayList<String>();
	
	// QuinaService.
	public final List<String> qsrvList = new ArrayList<String>();
	
	// CdiHandle.
	public final List<String> hndList = new ArrayList<String>();
	
	// ProxyScoped.
	public final List<String> prxList = new ArrayList<String>();
	
	// classLoader.
	public ClassLoader cl;
	
	/**
	 * コンストラクタ.
	 * @param clazzDir
	 * @param verbose
	 * @param jarDirArray
	 * @throws Exception
	 */
	public GCdiParams(String clazzDir, boolean verbose, String... jarDirArray)
		throws Exception {
		this.cl = GCdiUtil.createClassLoader(clazzDir, jarDirArray);
		this.verbose = verbose;
	}
	
	/**
	 * 詳細表示.
	 * @return
	 */
	public boolean isVerbose() {
		return verbose;
	}
	
	/**
	 * 全情報が空の場合.
	 * @return
	 */
	public boolean isEmpty() {
		return refList.size() == 0 && routeList.size() == 0
			&& cdiList.size() == 0 && any == null
			&& errList.size() == 0 && qsrvList.size() == 0
			&& hndList.size() == 0 && prxList.size() == 0
		;
	}
	
	/**
	 * Route定義が空の場合.
	 * @return
	 */
	public boolean isRouteEmpty() {
		return routeList.size() == 0 && any == null &&
			errList.size() == 0;
	}
	
	/**
	 * CdiScoped定義が空の場合.
	 * @return
	 */
	public boolean isCdiEmpty() {
		return cdiList.size() == 0;
	}
	
	/**
	 * CdiReflect定義が空の場合.
	 * @return
	 */
	public boolean isCdiReflectEmpty() {
		return refList.size() == 0;
	}
	
	/**
	 * QuinaService定義が空の場合.
	 * @return
	 */
	public boolean isQuinaServiceEmpty() {
		return qsrvList.size() == 0;
	}
	
	/**
	 * CdiHandle定義が空の場合.
	 * @return
	 */
	public boolean isCdiHandleEmpty() {
		return hndList.size() == 0;
	}
	
	/**
	 * ProxyScoped定義が空の場合.
	 * @return
	 */
	public boolean isProxyScopedEmpty() {
		return prxList.size() == 0;
	}
}
