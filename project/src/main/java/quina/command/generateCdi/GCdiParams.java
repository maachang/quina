package quina.command.generateCdi;

import java.util.ArrayList;
import java.util.List;

/**
 * GenerateCdiパラメータ.
 */
public class GCdiParams {
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
	
	// classLoader.
	public ClassLoader cl;
	
	public GCdiParams(String clazzDir, String[] jarDirArray)
		throws Exception {
		cl = GCdiUtil.createClassLoader(clazzDir, jarDirArray);
	}
	
	public boolean isEmpty() {
		return refList.size() == 0 && routeList.size() == 0 && 
			cdiList.size() == 0 && any == null &&
			errList.size() == 0 && qsrvList.size() == 0;
	}
	
	public boolean isRouteEmpty() {
		return routeList.size() == 0 && any == null &&
			errList.size() == 0;
	}
	
	public boolean isCdiEmpty() {
		return cdiList.size() == 0;
	}
	
	public boolean isCdiReflectEmpty() {
		return refList.size() == 0;
	}
	
	public boolean isQuinaServiceEmpty() {
		return qsrvList.size() == 0;
	}
	
	public boolean isCdiHandleEmpty() {
		return hndList.size() == 0;
	}
}
