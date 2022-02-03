package quina.compile;

import java.util.ArrayList;
import java.util.List;

/**
 * QuinaCompileTool用パラメータ.
 */
public class QuinaCTParams {
	// verbose..
	public boolean verbose = false;
	
	// Route用クラス名リスト.
	public final List<String> routeList = new ArrayList<String>();
	
	// RouteAny用クラス名.
	public String any = null;
	
	// RouteError用クラス名リスト.
	public final List<String> errList = new ArrayList<String>();
	
	// CdiService用クラス名リスト.
	public final List<String> cdiList = new ArrayList<String>();
	
	// CdiReflect用クラス名リスト.
	public final List<String> refList = new ArrayList<String>();
	
	// QuinaService用クラス名リスト.
	public final List<String> qsrvList = new ArrayList<String>();
	
	// CdiHandle用クラス名リスト.
	public final List<String> hndList = new ArrayList<String>();
	
	// ProxyScoped用クラス名リスト.
	public final List<String> prxList = new ArrayList<String>();
	
	// QuinaLoopScoped用クラス名リスト.
	public final List<String> loopList = new ArrayList<String>();
	
	// クラス内のresourceファイル群.
	public final List<String> regResourceList = new ArrayList<String>();
	
	// ResourceItem登録判定.
	// graalvmのnativeImageコンパイルでリソースファイル定義が必要なので
	// それを利用する場合trueがセットされる.
	public boolean registerResourceItemFlag;
	
	// classLoader.
	public ClassLoader cl;
	
	/**
	 * コンストラクタ.
	 * @param clazzDir
	 * @param verbose
	 * @param registerResourceItemFlag
	 * @param jarDirArray
	 * @throws Exception
	 */
	public QuinaCTParams(String clazzDir, boolean verbose,
		boolean registerResourceItemFlag, String... jarDirArray)
		throws Exception {
		this.cl = QuinaCTUtil.createClassLoader(clazzDir, jarDirArray);
		this.verbose = verbose;
		this.registerResourceItemFlag = registerResourceItemFlag;
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
			&& regResourceList.size() == 0 && loopList.size() == 0
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
	 * QuinaLoop定義が空の場合.
	 * @return
	 */
	public boolean isQuinaLoopEmpty() {
		return loopList.size() == 0;
	}
	
	/**
	 * ProxyScoped定義が空の場合.
	 * @return
	 */
	public boolean isProxyScopedEmpty() {
		return prxList.size() == 0;
	}
	
	/**
	 * ResourceItem定義が空の場合.
	 * @return
	 */
	public boolean isRegResourceItemEmpty() {
		return regResourceList.size() == 0;
	}
}
