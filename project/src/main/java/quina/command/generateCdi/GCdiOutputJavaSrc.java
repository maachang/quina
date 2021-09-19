package quina.command.generateCdi;

import static quina.command.generateCdi.GCdiConstants.AUTO_ROUTE_SOURCE_NAME;
import static quina.command.generateCdi.GCdiConstants.CDI_DIRECTORY_NAME;
import static quina.command.generateCdi.GCdiConstants.CDI_HANDLE_SOURCE_NAME;
import static quina.command.generateCdi.GCdiConstants.CDI_REFLECT_SOURCE_NAME;
import static quina.command.generateCdi.GCdiConstants.CDI_SERVICE_SOURCE_NAME;
import static quina.command.generateCdi.GCdiConstants.OUTPUT_SOURCE_ARRAY;
import static quina.command.generateCdi.GCdiConstants.QUINA_SERVICE_SOURCE_NAME;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import quina.QuinaServiceManager;
import quina.Router;
import quina.annotation.cdi.AnnotationCdiConstants;
import quina.annotation.cdi.CdiHandleManager;
import quina.annotation.cdi.CdiReflectManager;
import quina.annotation.cdi.CdiServiceManager;
import quina.annotation.route.AnnotationRoute;
import quina.util.FileUtil;

/**
 * Cdiを行うJavaソースコードを出力.
 */
public class GCdiOutputJavaSrc {
	private GCdiOutputJavaSrc() {}
	
	// 出力処理.
	private static final void println(Writer w, int tab, String s)
		throws IOException {
		for(int i = 0; i < tab; i ++) {
			w.append("\t");
		}
		w.append(s);
		w.append("\n");
	}
	
	// クラス名からメソッド名変換.
	private static final String convClassByMethodName(String clazz) {
		final StringBuilder buf = new StringBuilder();
		int len = clazz.length();
		for(int i = 0; i < len; i ++) {
			if(clazz.charAt(i) == '.') {
				buf.append("_");
			} else {
				buf.append(clazz.charAt(i));
			}
		}
		return buf.toString();
	}
	
	// 出力作のディレクトリ内の自動生成のJavaソースを削除.
	public static final void removeOutAutoJavaSource(String outSourceDirectory)
		throws Exception {
		String[] javaSrcs = OUTPUT_SOURCE_ARRAY;
		String outDir = outSourceDirectory + "/" + CDI_DIRECTORY_NAME + "/";
		int len = javaSrcs.length;
		for(int i = 0; i < len; i ++) {
			try {
				FileUtil.removeFile(outDir + javaSrcs[i]);
			} catch(Exception e) {}
		}
	}
	
	// 抽出した@Route定義されたComponentをJavaファイルに出力.
	public static final void componentRoute(String outSourceDirectory,
		GCdiParams params)
		throws Exception {
		String outDir = outSourceDirectory + "/" + CDI_DIRECTORY_NAME;
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		String outFileName = outDir + "/" + AUTO_ROUTE_SOURCE_NAME;
		BufferedWriter w = null;
		try {
			// ソースコードを出力.
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName)));
			println(w, 0, "package " + AnnotationCdiConstants.CDI_PACKAGE_NAME + ";");
			println(w, 0, "");
			println(w, 0, "import quina.Quina;");
			println(w, 0, "import quina.Router;");
			println(w, 0, "");
			println(w, 0, "/**");
			println(w, 0, " * Route Annotation Registers the configured Component group as a Router.");
			println(w, 0, " */");
			println(w, 0, "public final class " + Router.AUTO_READ_ROUTE_CLASS + " {");
			println(w, 1, "private " + Router.AUTO_READ_ROUTE_CLASS + "() {}");
			
			println(w, 1, "");
			println(w, 1, "/**");
			println(w, 1, " * Route Annotation Performs Router registration processing for the");
			println(w, 1, " * set Component group.");
			println(w, 1, " *");
			println(w, 1, " * @exception Exception When Router registration fails.");
			println(w, 1, " */");
			println(w, 1, "public static final void " + Router.AUTO_READ_ROUTE_METHOD + "() throws Exception {");
			
			println(w, 2, "");
			println(w, 2, "// Get the Router to be registered.");
			println(w, 2, "final Router router = Quina.get().getRouter();");
			
			String clazzName;
			int len = params.routeList.size();
			for(int i = 0; i < len; i ++) {
				clazzName = params.routeList.get(i);
				println(w, 2, "");
				println(w, 2, "// Register the \""+ clazzName + "\"");
				println(w, 2, "// component in the @Route.");
				println(w, 2, "router.route(new " + clazzName + "());");
			}
			
			if(params.any != null) {
				println(w, 2, "");
				println(w, 2, "// Register the \""+ params.any + "\"");
				println(w, 2, "// component in the @AnyRoute.");
				println(w, 2, "router.any(new " + params.any + "());");
			}
			
			Class<?> c;
			len = params.errList.size();
			for(int i = 0; i < len; i ++) {
				clazzName = params.errList.get(i);
				c = Class.forName(clazzName, true, params.cl);
				int[] es = AnnotationRoute.loadErrorRoute(c);
				println(w, 2, "");
				if(es[0] == 0) {
					println(w, 2, "// Register the \""+ clazzName + "\"");
					println(w, 2, "// component in the @ErrorRoute.");
				} else if(es[1] == 0) {
					println(w, 2, "// Register the \""+ clazzName + "\"");
					println(w, 2, "// component in the @ErrorRoute(" +
						es[0] + ")");
					println(w, 2, "// Register the \""+ clazzName + "\"");
					println(w, 2, "// component in the @ErrorRoute(" +
						es[0] + ", " + es[1] + ")");
				}
				println(w, 2, "router.error(new " + clazzName + "());");
			}
			
			println(w, 1, "}");
			
			println(w, 0, "}");
			
			w.close();
			w = null;
			
		} finally {
			if(w != null) {
				try {
					w.close();
				} catch(Exception e) {}
			}
		}
	}
	
	// 抽出した@ServiceScoped定義されたオブジェクトをJavaファイルに出力.
	public static final void cdiService(String outSourceDirectory,
		GCdiParams params)
		throws Exception {
		String outDir = outSourceDirectory + "/" + CDI_DIRECTORY_NAME;
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		String outFileName = outDir + "/" + CDI_SERVICE_SOURCE_NAME;
		BufferedWriter w = null;
		try {
			// ソースコードを出力.
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName)));
			println(w, 0, "package " + AnnotationCdiConstants.CDI_PACKAGE_NAME + ";");
			println(w, 0, "");
			println(w, 0, "import quina.Quina;");
			println(w, 0, "import quina.annotation.cdi.CdiServiceManager;");
			println(w, 0, "");
			println(w, 0, "/**");
			println(w, 0, " * ServiceScoped Annotation Registers the defined service object.");
			println(w, 0, " */");
			println(w, 0, "public final class " + CdiServiceManager.AUTO_READ_CDI_SERVICE_CLASS + " {");
			println(w, 1, "private " + CdiServiceManager.AUTO_READ_CDI_SERVICE_CLASS + "() {}");
			
			println(w, 1, "");
			println(w, 1, "/**");
			println(w, 1, " * ServiceScoped Annotation Registers the defined service object.");
			println(w, 1, " *");
			println(w, 1, " * @exception Exception If the service registration fails.");
			println(w, 1, " */");
			println(w, 1, "public static final void " + CdiServiceManager.AUTO_READ_CDI_SERVICE_METHOD +
				"() throws Exception {");
			
			println(w, 2, "");
			println(w, 2, "// Get the Service Manager to be registered.");
			println(w, 2, "final CdiServiceManager cdiManager = Quina.get().getCdiServiceManager();");
			
			String clazzName;
			int len = params.cdiList.size();
			for(int i = 0; i < len; i ++) {
				clazzName = params.cdiList.get(i);
				println(w, 2, "");
				println(w, 2, "// Register the \""+ clazzName + "\"");
				println(w, 2, "// object in the @ServiceScoped.");
				println(w, 2, "cdiManager.put(new " + clazzName + "());");
			}
			
			println(w, 1, "}");
			
			println(w, 0, "}");
			
			w.close();
			w = null;
			
		} finally {
			if(w != null) {
				try {
					w.close();
				} catch(Exception e) {}
			}
		}
	}
	
	// 抽出した@ServiceScoped定義されたオブジェクトをJavaファイルに出力.
	public static final void cdiReflect(String outSourceDirectory,
		GCdiParams params)
		throws Exception {
		String outDir = outSourceDirectory + "/" + CDI_DIRECTORY_NAME;
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		String outFileName = outDir + "/" + CDI_REFLECT_SOURCE_NAME;
		BufferedWriter w = null;
		try {
			// ソースコードを出力.
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName)));
			println(w, 0, "package " + AnnotationCdiConstants.CDI_PACKAGE_NAME + ";");
			println(w, 0, "");
			println(w, 0, "import java.lang.reflect.Field;");
			println(w, 0, "");
			println(w, 0, "import quina.Quina;");
			println(w, 0, "import quina.annotation.cdi.CdiReflectManager;");
			println(w, 0, "import quina.annotation.cdi.CdiReflectElement;");
			println(w, 0, "");
			println(w, 0, "/**");
			println(w, 0, " * Reads the CDI reflection information.");
			println(w, 0, " */");
			println(w, 0, "public final class " + CdiReflectManager.AUTO_READ_CDI_REFLECT_CLASS + " {");
			println(w, 1, "private " + CdiReflectManager.AUTO_READ_CDI_REFLECT_CLASS + "() {}");
			
			println(w, 1, "");
			println(w, 1, "/**");
			println(w, 1, " * Reads the CDI reflection information.");
			println(w, 1, " *");
			println(w, 1, " * @exception Exception If the cdi reflect registration fails.");
			println(w, 1, " */");
			println(w, 1, "public static final void " + CdiReflectManager.AUTO_READ_CDI_REFLECT_METHOD +
				"() throws Exception {");
			
			Class<?> c;
			String clazzName;
			int len = params.refList.size();
			for(int i = 0; i < len; i ++) {
				clazzName = params.refList.get(i);
				c = Class.forName(clazzName, true, params.cl);
				final Field[] list = c.getDeclaredFields();
				int lenJ = list.length;
				if(lenJ == 0) {
					continue;
				}
				println(w, 2, "");
				println(w, 2, "// Register the field group of the target class");
				println(w, 2, "// \""+ clazzName + "\"");
				
				println(w, 2, convClassByMethodName(clazzName) + "();");
			}
			
			println(w, 1, "}");
			
			for(int i = 0; i < len; i ++) {
				clazzName = params.refList.get(i);
				c = Class.forName(clazzName, true, params.cl);
				final Field[] list = c.getDeclaredFields();
				int lenJ = list.length;
				if(lenJ == 0) {
					continue;
				}
				println(w, 0, "");
				println(w, 1, "// Register the field group of the target class \""+ clazzName + "\"");
				println(w, 1, "private static final void " + convClassByMethodName(clazzName) + "()");
				println(w, 2, "throws Exception {");
				
				
				println(w, 2, "CdiReflectManager refManager = Quina.get().getCdiReflectManager();");
				println(w, 2, "Class<?> cls = " + clazzName + ".class;");
				println(w, 2, "CdiReflectElement element = refManager.register(cls);");
				println(w, 2, "Field field = null;");
				println(w, 2, "boolean staticFlag = false;");
	
				for(int j = 0; j < lenJ; j ++) {
					println(w, 2, "");
					println(w, 2, "field = cls.getDeclaredField(\"" + list[j].getName() + "\");");
					println(w, 2, "staticFlag = " + Modifier.isStatic(list[j].getModifiers()) + ";");
					println(w, 2, "element.add(staticFlag, field);");
				}
				println(w, 1, "}");
			}
			
			println(w, 0, "}");
			
			w.close();
			w = null;
			
		} finally {
			if(w != null) {
				try {
					w.close();
				} catch(Exception e) {}
			}
		}
	}
	
	// 抽出した@QuinaServiceScoped定義されたオブジェクトをJavaファイルに出力.
	public static final void quinaService(String outSourceDirectory,
		GCdiParams params)
		throws Exception {
		String outDir = outSourceDirectory + "/" + CDI_DIRECTORY_NAME;
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		String outFileName = outDir + "/" + QUINA_SERVICE_SOURCE_NAME;
		BufferedWriter w = null;
		try {
			// ソースコードを出力.
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName)));
			println(w, 0, "package " + AnnotationCdiConstants.CDI_PACKAGE_NAME + ";");
			println(w, 0, "");
			println(w, 0, "import quina.Quina;");
			println(w, 0, "import quina.QuinaServiceManager;");
			println(w, 0, "");
			println(w, 0, "/**");
			println(w, 0, " * QuinaServiceScoped Annotation Registers the defined service object.");
			println(w, 0, " */");
			println(w, 0, "public final class " + QuinaServiceManager.AUTO_READ_QUINA_SERVICE_CLASS + " {");
			println(w, 1, "private " + QuinaServiceManager.AUTO_READ_QUINA_SERVICE_CLASS + "() {}");
			
			println(w, 1, "");
			println(w, 1, "/**");
			println(w, 1, " * QuinaServiceScoped Annotation Registers the defined service object.");
			println(w, 1, " *");
			println(w, 1, " * @exception Exception If the service registration fails.");
			println(w, 1, " */");
			println(w, 1, "public static final void " + QuinaServiceManager.AUTO_READ_QUINA_SERVICE_METHOD +
				"() throws Exception {");
			
			println(w, 2, "");
			println(w, 2, "// Get the Quina Service Manager to be registered.");
			println(w, 2, "final QuinaServiceManager qsrvManager = Quina.get().getQuinaServiceManager();");
			
			String clazzName;
			int len = params.qsrvList.size();
			for(int i = 0; i < len; i ++) {
				clazzName = params.qsrvList.get(i);
				println(w, 2, "");
				println(w, 2, "// Register the \""+ clazzName + "\"");
				println(w, 2, "// object in the @QuinaServiceScoped.");
				println(w, 2, "qsrvManager.put(new " + clazzName + "());");
			}
			
			println(w, 1, "}");
			
			println(w, 0, "}");
			
			w.close();
			w = null;
			
		} finally {
			if(w != null) {
				try {
					w.close();
				} catch(Exception e) {}
			}
		}
	}
	
	// 抽出した@CdiHandleScoped定義されたオブジェクトをJavaファイルに出力.
	public static final void cdiHandle(String outSourceDirectory,
		GCdiParams params)
		throws Exception {
		String outDir = outSourceDirectory + "/" + CDI_DIRECTORY_NAME;
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		String outFileName = outDir + "/" + CDI_HANDLE_SOURCE_NAME;
		BufferedWriter w = null;
		try {
			// ソースコードを出力.
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName)));
			println(w, 0, "package " + AnnotationCdiConstants.CDI_PACKAGE_NAME + ";");
			println(w, 0, "");
			println(w, 0, "import quina.Quina;");
			println(w, 0, "import quina.annotation.cdi.CdiHandleManager;");
			println(w, 0, "");
			println(w, 0, "/**");
			println(w, 0, " * CdiAnnotationScoped Annotation Registers the defined service object.");
			println(w, 0, " */");
			println(w, 0, "public final class " +
				CdiHandleManager.AUTO_READ_CDI_HANDLE_CLASS + " {");
			println(w, 1, "private " + 
				CdiHandleManager.AUTO_READ_CDI_HANDLE_CLASS + "() {}");
			
			println(w, 1, "");
			println(w, 1, "/**");
			println(w, 1, " * CdiAnnotationScoped Annotation Registers the defined service object.");
			println(w, 1, " *");
			println(w, 1, " * @exception Exception If the service registration fails.");
			println(w, 1, " */");
			println(w, 1, "public static final void " +
				CdiHandleManager.AUTO_READ_CDI_HANDLE_METHOD + "() throws Exception {");
			
			println(w, 2, "");
			println(w, 2, "// Cdi Handle Manager to be registered.");
			println(w, 2, "final CdiHandleManager chdManager = Quina.get().CdiHandleManager();");
			
			String clazzName;
			int len = params.qsrvList.size();
			for(int i = 0; i < len; i ++) {
				clazzName = params.hndList.get(i);
				println(w, 2, "");
				println(w, 2, "// Register the \""+ clazzName + "\"");
				println(w, 2, "// object in the @CdiAnnotationScoped.");
				println(w, 2, "chdManager.put(new " + clazzName + "());");
			}
			
			println(w, 1, "}");
			
			println(w, 0, "}");
			
			w.close();
			w = null;
			
		} finally {
			if(w != null) {
				try {
					w.close();
				} catch(Exception e) {}
			}
		}
	}
}
