package quina.command.generateCdi;

import static quina.command.generateCdi.GCdiConstants.AUTO_ROUTE_SOURCE_NAME;
import static quina.command.generateCdi.GCdiConstants.CDI_DIRECTORY_NAME;
import static quina.command.generateCdi.GCdiConstants.CDI_HANDLE_SOURCE_NAME;
import static quina.command.generateCdi.GCdiConstants.CDI_REFLECT_SOURCE_NAME;
import static quina.command.generateCdi.GCdiConstants.CDI_SERVICE_SOURCE_NAME;
import static quina.command.generateCdi.GCdiConstants.OUTPUT_SOURCE_ARRAY;
import static quina.command.generateCdi.GCdiConstants.PROXY_SCOPED_SOURCE_NAME;
import static quina.command.generateCdi.GCdiConstants.QUINA_SERVICE_SOURCE_NAME;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import quina.QuinaServiceManager;
import quina.Router;
import quina.annotation.cdi.AnnotationCdiConstants;
import quina.annotation.cdi.CdiHandleManager;
import quina.annotation.cdi.CdiReflectManager;
import quina.annotation.cdi.CdiServiceManager;
import quina.annotation.proxy.ProxyScopedManager;
import quina.annotation.route.AnnotationRoute;
import quina.exception.QuinaException;
import quina.util.FileUtil;
import quina.util.StringUtil;

/**
 * Cdiを行うJavaソースコードを出力.
 */
public class GCdiOutputJavaSrc {
	private GCdiOutputJavaSrc() {}
	
	// 出力処理.
	private static final void println(Writer w, int tab, String s)
		throws IOException {
		StringUtil.println(w, tab, s);
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
	
	/**
	 * 出力作のディレクトリ内の自動生成のJavaソースを削除.
	 * @param outSourceDirectory 出力先ディレクトリを設定します.
	 * @throws IOException I/O例外.
	 */
	public static final void removeOutAutoJavaSource(String outSourceDirectory)
		throws IOException {
		String[] javaSrcs = OUTPUT_SOURCE_ARRAY;
		String outDir = outSourceDirectory + "/" + CDI_DIRECTORY_NAME + "/";
		int len = javaSrcs.length;
		for(int i = 0; i < len; i ++) {
			try {
				FileUtil.removeFile(outDir + javaSrcs[i]);
			} catch(Exception e) {}
		}
	}
	
	// 対象のクラスがPublic定義で空のpublicコンストラクタが
	// 利用可能かチェック.
	private static final boolean isPublicClass(
		String clazzName, GCdiParams params)
		throws ClassNotFoundException {
		return isPublicClass(GCdiUtil.getClass(clazzName, params.cl));

	}
	
	// 対象のクラスがPublic定義で空のpublicコンストラクタが
	// 利用可能かチェック.
	private static final boolean isPublicClass(Class<?> c) {
		if(GCdiConstants.isDefineAnnotation(c) ||
			GCdiConstants.isProxyAnnotation(c)) {
			GCdiUtil.checkPublicClass(c);
			return true;
		}
		return false;
	}
	
	// 対象フィールドがCdiFieldでfinal定義でないかチェック.
	private static final boolean isCdiField(Class<?> c, Field f) {
		// 何らかのアノテーション定義が存在する場合.
		if(f.getAnnotations().length > 0) {
			// フィールドが final 定義の場合エラー.
			if(Modifier.isFinal(f.getModifiers())) {
				throw new QuinaException(
					"The specified field (class: " +
					c.getName() + " field: " + f.getName() +
					") is the final definition while the Cdi " +
					"injection annotation definition. ");
			}
			return true;
		}
		return false;
	}
	
	// 対象のクラスリストで出力条件が存在するかチェック.
	private static final boolean isOutClassList(
		List<String> classlist, GCdiParams params)
		throws ClassNotFoundException {
		// publicでないクラスで空のコンストラクタで
		// ないかチェック.
		boolean notFlag = true;
		String clazzName;
		int len = classlist.size();
		for(int i = 0; i < len; i ++) {
			clazzName = classlist.get(i);
			if(isPublicClass(clazzName, params)) {
				// 存在する場合.
				notFlag = false;
				break;
			}
		}
		return !notFlag;
	}
	
	/**
	 * 抽出したRoute定義されたComponentをJavaファイルに出力.
	 * @param outSourceDirectory 出力先ディレクトリを設定します.
	 * @param params GenerateGciパラメータを設定します.
	 * @throws IOException I/O例外.
	 * @throws ClassNotFoundException クラス非存在例外.
	 */
	public static final void routerScoped(String outSourceDirectory,
		GCdiParams params)
		throws IOException, ClassNotFoundException {
		String outDir = outSourceDirectory + "/" + CDI_DIRECTORY_NAME;
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		String outFileName = outDir + "/" + AUTO_ROUTE_SOURCE_NAME;
		BufferedWriter w = null;
		try {
			// 出力可能かチェック.
			isOutClassList(params.routeList, params);
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
			
			// 通常Componentの出力.
			String clazzName;
			int len = params.routeList.size();
			for(int i = 0; i < len; i ++) {
				clazzName = params.routeList.get(i);
				// pubilcのクラス定義のみ対象とする.
				if(isPublicClass(clazzName, params)) {
					println(w, 2, "");
					println(w, 2, "// Register the \""+ clazzName + "\"");
					println(w, 2, "// component in the @Route.");
					println(w, 2, "router.route(new " + clazzName + "());");
				}
			}
			
			// AnyComponentの出力.
			if(params.any != null) {
				// pubilcのクラス定義のみ対象とする.
				if(isPublicClass(params.any, params)) {
					println(w, 2, "");
					println(w, 2, "// Register the \""+ params.any + "\"");
					println(w, 2, "// component in the @AnyRoute.");
					println(w, 2, "router.any(new " + params.any + "());");
				}
			}
			
			// ErrorComponentの出力.
			Class<?> c;
			len = params.errList.size();
			for(int i = 0; i < len; i ++) {
				clazzName = params.errList.get(i);
				c = GCdiUtil.getClass(clazzName, params);
				// pubilcのクラス定義のみ対象とする.
				if(isPublicClass(c)) {
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
	
	/**
	 * 抽出したServiceScoped定義されたオブジェクトをJavaファイルに出力.
	 * @param outSourceDirectory 出力先ディレクトリを設定します.
	 * @param params GenerateGciパラメータを設定します.
	 * @throws IOException I/O例外.
	 * @throws ClassNotFoundException クラス非存在例外.
	 */
	public static final void serviceScoped(String outSourceDirectory,
		GCdiParams params)
		throws IOException, ClassNotFoundException {
		String outDir = outSourceDirectory + "/" + CDI_DIRECTORY_NAME;
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		String outFileName = outDir + "/" + CDI_SERVICE_SOURCE_NAME;
		BufferedWriter w = null;
		try {
			// 出力可能かチェック.
			isOutClassList(params.cdiList, params);
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
				// pubilcのクラス定義のみ対象とする.
				if(isPublicClass(clazzName, params)) {
					println(w, 2, "");
					println(w, 2, "// Register the \""+ clazzName + "\"");
					println(w, 2, "// object in the @ServiceScoped.");
					println(w, 2, "cdiManager.put(new " + clazzName + "());");
				}
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
	
	/**
	 * 抽出したQuinaServiceScoped定義されたオブジェクトをJavaファイルに出力.
	 * @param outSourceDirectory 出力先ディレクトリを設定します.
	 * @param params GenerateGciパラメータを設定します.
	 * @throws IOException I/O例外.
	 * @throws ClassNotFoundException クラス非存在例外.
	 */
	public static final void quinaServiceScoped(String outSourceDirectory,
		GCdiParams params)
		throws IOException, ClassNotFoundException {
		String outDir = outSourceDirectory + "/" + CDI_DIRECTORY_NAME;
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		String outFileName = outDir + "/" + QUINA_SERVICE_SOURCE_NAME;
		BufferedWriter w = null;
		try {
			// 出力可能かチェック.
			isOutClassList(params.qsrvList, params);
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
				// pubilcのクラス定義のみ対象とする.
				if(isPublicClass(clazzName, params)) {
					println(w, 2, "");
					println(w, 2, "// Register the \""+ clazzName + "\"");
					println(w, 2, "// object in the @QuinaServiceScoped.");
					println(w, 2, "qsrvManager.put(new " + clazzName + "());");
				}
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
	
	/**
	 * 抽出したCdiReflect定義されたオブジェクトをJavaファイルに出力.
	 * @param outSourceDirectory 出力先ディレクトリを設定します.
	 * @param params GenerateGciパラメータを設定します.
	 * @throws IOException I/O例外.
	 * @throws ClassNotFoundException クラス非存在例外.
	 */
	public static final void cdiReflect(String outSourceDirectory,
		GCdiParams params)
		throws IOException, ClassNotFoundException {
		String outDir = outSourceDirectory + "/" + CDI_DIRECTORY_NAME;
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		String outFileName = outDir + "/" + CDI_REFLECT_SOURCE_NAME;
		BufferedWriter w = null;
		try {
			// エラーチェック.
			Class<?> c;
			String clazzName;
			int len = params.refList.size();
			for(int i = 0; i < len; i ++) {
				clazzName = params.refList.get(i);
				c = GCdiUtil.getClass(clazzName, params);
				// subClassを含むFieldをすべて取得.
				final List<Field> list = GCdiUtil.getFields(c);
				final int lenJ = list.size();
				if(lenJ == 0) {
					continue;
				}
				for(int j = 0; j < lenJ; j ++) {
					// Cdiアノテーションがあってfinal定義でない場合.
					isCdiField(c, list.get(j));
				}
			}
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
			
			len = params.refList.size();
			for(int i = 0; i < len; i ++) {
				clazzName = params.refList.get(i);
				c = GCdiUtil.getClass(clazzName, params);
				// subClassを含むFieldをすべて取得.
				final List<Field> list = GCdiUtil.getFields(c);
				final int lenJ = list.size();
				if(lenJ == 0) {
					continue;
				}
				
				// Cdiアノテーションあってfinal定義でないものをチェック.
				boolean notOut = true;
				for(int j = 0; j < lenJ; j ++) {
					// Cdiアノテーションがあってfinal定義でない場合.
					if(isCdiField(c, list.get(j))) {
						notOut = false;
					}
				}
				// 存在しない場合.
				if(notOut) {
					// リストから削除(nullセット).
					params.refList.set(i, null);
				} else {
					// 存在する場合.
					println(w, 2, "");
					println(w, 2, "// Register the field group of the target class");
					println(w, 2, "// \""+ clazzName + "\"");
					
					println(w, 2, convClassByMethodName(clazzName) + "();");
				}
			}
			
			println(w, 1, "}");
			
			for(int i = 0; i < len; i ++) {
				clazzName = params.refList.get(i);
				// CdiアノテーションのFieldが存在しない場合.
				if(clazzName == null) {
					// 次の処理を行う.
					continue;
				}
				c = GCdiUtil.getClass(clazzName, params);
				// subClassを含むFieldをすべて取得.
				final List<Field> list = GCdiUtil.getFields(c);
				final int lenJ = list.size();
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
					// Cdiアノテーションがあってfinal定義でない場合.
					if(isCdiField(c, list.get(j))) {
						println(w, 2, "");
						println(w, 2, "field = cls.getDeclaredField(\"" + list.get(j).getName() + "\");");
						println(w, 2, "staticFlag = " + Modifier.isStatic(list.get(j).getModifiers()) + ";");
						println(w, 2, "element.add(staticFlag, field);");
					}
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
	
	/**
	 * 抽出したCdiHandleScoped定義されたオブジェクトをJavaファイルに出力.
	 * @param outSourceDirectory 出力先ディレクトリを設定します.
	 * @param params GenerateGciパラメータを設定します.
	 * @throws IOException I/O例外.
	 * @throws ClassNotFoundException クラス非存在例外.
	 */
	public static final void cdiHandle(String outSourceDirectory,
		GCdiParams params)
		throws IOException, ClassNotFoundException {
		String outDir = outSourceDirectory + "/" + CDI_DIRECTORY_NAME;
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		String outFileName = outDir + "/" + CDI_HANDLE_SOURCE_NAME;
		BufferedWriter w = null;
		try {
			// 出力可能かチェック.
			isOutClassList(params.hndList, params);
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
			println(w, 2, "final CdiHandleManager chdManager = Quina.get().getCdiHandleManager();");
			String clazzName;
			final int len = params.hndList.size();
			for(int i = 0; i < len; i ++) {
				clazzName = params.hndList.get(i);
				// pubilcのクラス定義のみ対象とする.
				if(isPublicClass(clazzName, params)) {
					println(w, 2, "");
					println(w, 2, "// Register the \""+ clazzName + "\"");
					println(w, 2, "// object in the @CdiHandleScoped.");
					println(w, 2, "chdManager.put(new " + clazzName + "());");
				}
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
	
	/**
	 * 抽出したProxyScoped定義されたオブジェクトをJavaファイルに出力.
	 * @param outSourceDirectory 出力先ディレクトリを設定します.
	 * @param params GenerateGciパラメータを設定します.
	 * @throws IOException I/O例外.
	 * @throws ClassNotFoundException クラス非存在例外.
	 */
	public static final void proxyScoped(String outSourceDirectory,
		GCdiParams params)
		throws IOException, ClassNotFoundException {
		String outDir = outSourceDirectory + "/" + CDI_DIRECTORY_NAME;
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		String outFileName = outDir + "/" + PROXY_SCOPED_SOURCE_NAME;
		BufferedWriter w = null;
		try {
			// 出力可能かチェック.
			isOutClassList(params.hndList, params);
			// ソースコードを出力.
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName)));
			println(w, 0, "package " + AnnotationCdiConstants.CDI_PACKAGE_NAME + ";");
			println(w, 0, "");
			println(w, 0, "import quina.Quina;");
			println(w, 0, "import quina.annotation.proxy.ProxyScopedManager;");
			println(w, 0, "");
			println(w, 0, "/**");
			println(w, 0, " * ProxyScoped Annotation Registers the defined service object.");
			println(w, 0, " */");
			println(w, 0, "public final class " +
				ProxyScopedManager.AUTO_READ_PROXY_SCOPED_CLASS + " {");
			println(w, 1, "private " + 
				ProxyScopedManager.AUTO_READ_PROXY_SCOPED_CLASS + "() {}");
			
			println(w, 1, "");
			println(w, 1, "/**");
			println(w, 1, " * ProxyScoped Annotation Registers the define object.");
			println(w, 1, " *");
			println(w, 1, " * @exception Exception If the registration fails.");
			println(w, 1, " */");
			println(w, 1, "public static final void " +
				ProxyScopedManager.AUTO_READ_PROXY_SCOPED_METHOD + "() throws Exception {");
			
			println(w, 2, "");
			println(w, 2, "// Proxy Scoped Manager to be registered.");
			println(w, 2, "final ProxyScopedManager prxManager = Quina.get().getProxyScopedManager();");
			String clazzName;
			final int len = params.prxList.size();
			for(int i = 0; i < len; i ++) {
				clazzName = params.prxList.get(i);
				// pubilcのクラス定義のみ対象とする.
				if(isPublicClass(clazzName, params)) {
					println(w, 2, "");
					println(w, 2, "// Register the \""+ clazzName + "\"");
					println(w, 2, "// object in the @ProxyScoped.");
					println(w, 2, "prxManager.put(\"" + clazzName + "\",");
					
					println(w, 3, "new quina.annotation.proxy.QuinaProxy() {");
					println(w, 4, "public Class<?> getProxyClass() {");
					println(w, 5, "return quinax.proxy.AutoProxyQuinaProxyStatement.class;");
					println(w, 4, "}");
					println(w, 4, "public Object newInstance(");
					println(w, 5, "quina.annotation.proxy.ProxySettingArgs args) {");
					println(w, 5, GCdiUtil.getAutoProxyClassName(clazzName) + " ret = new");
					println(w, 6, GCdiUtil.getAutoProxyClassName(clazzName) + "();");
					println(w, 5, "ret.__initialSetting(args);");
					println(w, 5, "return ret;");
					println(w, 4, "}");
					println(w, 3, "}");
					println(w, 2, ");");
				}
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
