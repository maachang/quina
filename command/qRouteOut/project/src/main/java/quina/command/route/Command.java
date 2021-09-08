package quina.command.route;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import quina.CdiManager;
import quina.CdiReflectManager;
import quina.Router;
import quina.annotation.AnnotationUtil;
import quina.annotation.cdi.ServiceScoped;
import quina.annotation.route.AnyRoute;
import quina.annotation.route.ErrorRoute;
import quina.annotation.route.LoadAnnotationRoute;
import quina.annotation.route.Route;
import quina.component.Component;
import quina.component.ErrorComponent;

/**
 * コマンド実行.
 * 
 * このコマンドにより、指定されたJavaクラスフォルダーから
 * quina.component.Componentを継承したQuinaのComponentに
 * 対してquina.annotation.route.Route定義されてるものを
 * 抽出して、自動実行できるJavaソースコードを生成します.
 * 
 * また quina.annotation.service.ServiceScooedアノテーション
 * を定義したオブジェクトを抽出して、自動実行できるJavaソース
 * コードを生成します.
 * 
 * また graalvm の native-image では 多くのReflectionが対応
 * できてないものも多くあり、たとえばClass.getDeclaredFields
 * のような一覧を取得する系は、基本使えません。
 * 
 * そのため Component, ErrorComponent, @ServiceScoped の
 * Filed群を取得して CdiReflectManager に直接登録するソース
 * コードを出力します.
 */
public class Command {

	// バージョン.
	private static final String VERSION = "0.0.1";
	
	// AutoRoute出力先ディレクトリ名.
	private static final String AUTO_ROUTE_DIRECTORY_NAME = packageNameToDirectory(
		Router.AUTO_READ_ROUTE_PACKAGE);
	
	// AutoRoute出力先Javaソースファイル名.
	private static final String AUTO_ROUTE_SOURCE_NAME =
		Router.AUTO_READ_ROUTE_CLASS + ".java";
	
	// AutoCdiService出力先ディレクトリ名.
	private static final String CDI_SERVICE_DIRECTORY_NAME = packageNameToDirectory(
		CdiManager.AUTO_READ_CDI_SERVICE_PACKAGE);
	
	// AutoCdiService出力先Javaソースファイル名.
	private static final String CDI_SERVICE_SOURCE_NAME =
		CdiManager.AUTO_READ_CDI_SERVICE_CLASS + ".java";
	
	// AutoCdiReflect出力先ディレクトリ名.
	private static final String CDI_REFLECT_DIRECTORY_NAME = packageNameToDirectory(
		CdiReflectManager.AUTO_READ_CDI_REFLECT_PACKAGE);
	
	// AutoCdiReflect出力先Javaソースファイル名.
	private static final String CDI_REFLECT_SOURCE_NAME =
		CdiReflectManager.AUTO_READ_CDI_REFLECT_CLASS + ".java";
	
	/**
	 * メイン処理.
	 * @param args
	 * @throws Exception
	 */
	public static final void main(String[] args) throws Exception {
		Command cmd = new Command(args);
		try {
			cmd.execute();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/** プログラム引数の管理. **/
	private Args args;

	/**
	 * コンストラクタ.
	 * @param args
	 */
	private Command(String[] args) {
		this.args = new Args(args);
	}

	/**
	 * ヘルプ情報を表示.
	 */
	private void outHelp() {
		System.out.println("This command extracts the Route specification component.");
		System.out.println();
		System.out.println("With this command, Extracts the component specified by Route");
		System.out.println("                   from the specified class directory and");
		System.out.println("                   creates an automatic loading program.");
		System.out.println();
		System.out.println("Usage: qRouteOut [options]");
		System.out.println(" where options include:");

		System.out.println("  -v [--version]");
		System.out.println("     version information .");
		System.out.println("  -h [--help]");
		System.out.println("     the help contents.");
		System.out.println("  -c [--class] {directory}");
		System.out.println("     * Settings are required.");
		System.out.println("     Set the target class file output directory.");
		System.out.println("     Set the top directory of the package name.");
		System.out.println("  -s [--source] {directory}");
		System.out.println("     * Settings are required.");
		System.out.println("     Set the output destination Java source code directory.");
		System.out.println("     For the directory, specify the top package name directory.");
		System.out.println();
	}

	/**
	 * コマンド実行.
	 */
	public void execute() throws Exception {
		// バージョンを表示.
		if(args.isValue("-v", "--version")) {
			System.out.println(VERSION);
			System.exit(0);
			return;
		// ヘルプ内容を表示.
		} else if(args.isValue("-h", "--help")) {
			outHelp();
			System.exit(0);
			return;
		}
		
		// classディレクトリを取得.
		String clazzDir = args.get("-c", "--class");
		if(clazzDir == null) {
			outHelp();
			System.err.println("[ERROR] The extraction source class directory has not been set.");
			System.exit(1);
			return;
		} else if(!new File(clazzDir).isDirectory()) {
			System.err.println("[ERROR] The specified class directory \"" +
				clazzDir + "\" is not a directory. ");
			System.exit(1);
			return;
		}
		clazzDir = AnnotationUtil.slashPath(clazzDir);
		
		// javaソースディレクトリを取得.
		String javaSourceDir = args.get("-s", "--source");
		if(javaSourceDir == null) {
			outHelp();
			System.err.println("[ERROR] The output destination Java source directory has not been set.");
			System.exit(1);
			return;
		} else if(!new File(javaSourceDir).isDirectory()) {
			System.err.println("[ERROR] The specified Java source directory \"" +
				javaSourceDir + "\" is not a directory. ");
			System.exit(1);
			return;
		}
		javaSourceDir = AnnotationUtil.slashPath(javaSourceDir);
		
		// 処理開始.
		System.out.println("start qRouteOut version: " + VERSION);
		System.out.println(" target classPath : " + new File(clazzDir).getCanonicalPath());
		System.out.println(" target outputPath: " + new File(javaSourceDir).getCanonicalPath());
		System.out.println("");
		long time = System.currentTimeMillis();
		
		List<String> routeList = new ArrayList<String>();
		List<String> cdiList = new ArrayList<String>();
		List<String> refList = new ArrayList<String>();
		String[] any = new String[] { null };
		List<String> errList = new ArrayList<String>();
		ClassLoader cl = createClassLoader(clazzDir);
		
		// @Routeと@any指定されてるComponentを抽出.
		extractComponentRoute(refList, routeList, cdiList, any, errList, clazzDir, cl);
		
		// 抽出した内容が存在する場合は、抽出条件をファイルに出力.
		if(refList.size() == 0 && routeList.size() == 0 && 
			cdiList.size() == 0 && any[0] == null && errList.size() == 0) {
			// 存在しない場合はエラー.
			System.err.println(
				"[ERROR] @Route and @Any and @Error and @ServiceScoped The " +
				"defined Component object does not exist.");
			System.exit(1);
			return;
		}
		
		// [Router]ファイル出力.
		if(routeList.size() != 0 || any[0] != null || errList.size() != 0) {
			outputComponentRoute(routeList, any[0], errList, javaSourceDir, cl);
		}
		
		// [CdiService]ファイル出力.
		if(cdiList.size() != 0) {
			outputCdiService(cdiList, javaSourceDir);
		}
		
		// [CdiReflect]ファイル出力.
		if(refList.size() != 0) {
			outputCdiReflect(refList, javaSourceDir, cl);
		}
		
		
		time = System.currentTimeMillis() - time;
		System.out.println();
		// [Router]ファイル出力内容が存在する場合.
		if(routeList.size() != 0 || any[0] != null || errList.size() != 0) {
			System.out.println( " routerOutput: " +
				new File(javaSourceDir).getCanonicalPath() +
				"/" + AUTO_ROUTE_DIRECTORY_NAME + "/" + AUTO_ROUTE_SOURCE_NAME);
		}
		// [CdiService]ファイル出力内容が存在する場合.
		if(cdiList.size() != 0) {
			System.out.println( " cdiServiceOutput: " +
				new File(javaSourceDir).getCanonicalPath() +
				"/" + CDI_SERVICE_DIRECTORY_NAME + "/" + CDI_SERVICE_SOURCE_NAME);
		}
		// [CdiReflect]ファイル出力内容が存在する場合.
		if(refList.size() != 0) {
			System.out.println( " cdiReflectOutput: " +
					new File(javaSourceDir).getCanonicalPath() +
					"/" + CDI_REFLECT_DIRECTORY_NAME + "/" + CDI_REFLECT_SOURCE_NAME);
		}
		System.out.println();
		System.out.println("success: " + time + " msec");
		System.out.println();
		
		System.exit(0);
	}
	
	// パッケージ名をディレクトリ名に変換.
	private static final String packageNameToDirectory(String name) {
		char c;
		StringBuilder buf = new StringBuilder();
		int len = name.length();
		for(int i = 0; i < len; i ++) {
			c = name.charAt(i);
			if(c == '.') {
				buf.append('/');
			} else {
				buf.append(c);
			}
		}
		return buf.toString();
	}
	
	// @Route指定されてるComponentを抽出.
	private static final void extractComponentRoute(List<String> refList,
		List<String> routeOut, List<String> cdiOut, String[] anyOut,
		List<String> errOut, String dir, ClassLoader cl)
		throws Exception {
		readComponentRoute(refList, routeOut, cdiOut, anyOut, errOut, cl, dir, "");
	}
	
	// 指定Classディレクトリのクラスを読み込むクラローダーを作成.
	private static final ClassLoader createClassLoader(String dir)
		throws Exception {
		return new java.net.URLClassLoader(
			new java.net.URL[] {
				new File(dir + "/").toURI().toURL()
			}
			, Thread.currentThread().getContextClassLoader());
	}
	
	// 1つのディレクトリに対して@Route指定されてるComponentを抽出.
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static final void readComponentRoute(List<String> refList,
		List<String> routeOut, List<String> cdiOut, String[] anyOut,
		List<String> errOut, ClassLoader cl, String dir, String packageName)
		throws Exception {
		String target, className;
		File f = new File(dir);
		String[] list = f.list();
		int len = list != null ? list.length : 0;
		for(int i = 0; i < len; i ++) {
			target = dir + "/" + list[i];
			// 対象がディレクトリの場合.
			if(new File(target).isDirectory()) {
				// 今回のディレクトリで再帰処理.
				readComponentRoute(refList, routeOut, cdiOut, anyOut, errOut, cl,
					target, createPackageName(packageName, list[i]));
			// クラスファイルの場合.
			} else if(list[i].endsWith(".class")) {
				// クラス名を取得.
				className = createClassName(packageName, list[i]);
				// クラスを取得.
				final Class c = Class.forName(className, true, cl);
				// RouteやAnyやErrorやServiceScopedのアノテーションが
				// 設定されていない場合.
				if(!c.isAnnotationPresent(Route.class) &&
					!c.isAnnotationPresent(AnyRoute.class) &&
					!c.isAnnotationPresent(ErrorRoute.class) &&
					!c.isAnnotationPresent(ServiceScoped.class)) {
					// クラスのインスタンスを生成.
					Object o;
					try {
						o = c.getDeclaredConstructor().newInstance();
					} catch(Exception e) {
						continue;
					}
					// アノテーションなしのコンポーネントの場合.
					if(o instanceof Component || o instanceof ErrorComponent) {
						// Reflectリストに追加.
						refList.add(className);
					}
					continue;
				}
				// ServiceScoped定義のCdiServiceの場合.
				if(c.isAnnotationPresent(ServiceScoped.class)) {
					System.out.println("  > cdiService: '" + className + "'");
					cdiOut.add(className);
					// Reflectリストに追加.
					refList.add(className);
					continue;
				}
				// クラスのインスタンスを生成.
				final Object o = c.getDeclaredConstructor().newInstance();
				// 対象がコンポーネントクラスの場合.
				if(o instanceof Component) {
					// @Route付属のコンポーネントを登録.
					if(c.isAnnotationPresent(Route.class)) {
						System.out.println("  > route: '" + className + "' path: '" +
							((Route)c.getAnnotation(Route.class)).value() + "'");
						routeOut.add(className);
					// @Any付属のコンポーネントを登録.
					} else if(c.isAnnotationPresent(AnyRoute.class)) {
						System.out.println("  > any:   '" + className + "'");
						anyOut[0] = className;
					}
					// Reflectリストに追加.
					refList.add(className);
				// 対象がエラーコンポーネントの場合.
				} else if(o instanceof ErrorComponent) {
					// @Error付属のコンポーネントを登録.
					if(c.isAnnotationPresent(ErrorRoute.class)) {
						int[] es = LoadAnnotationRoute.loadErrorRoute(c);
						if(es[0] == 0) {
							System.out.println("  > error: '" + className + "'");
						} else if(es[1] == 0) {
							System.out.println("  > error: '" + className + "' status: " +
								es[0]);
							System.out.println("  > error: '" + className + "' status: " +
								es[0] + "-" + es[1]);
						}
						errOut.add(className);
					}
					// Reflectリストに追加.
					refList.add(className);
				}
			}
		}
	}
	
	// PackageNameを作成.
	private static final String createPackageName(String base, String dir) {
		if(base.isEmpty()) {
			return dir;
		}
		return base + "." + dir;
	}
	
	// パッケージ名＋クラスファイルでClass情報を取得.
	private static final String createClassName(String packageName, String fileName) {
		fileName = fileName.substring(0, fileName.length() - 6);
		if(packageName.isEmpty()) {
			return fileName;
		}
		return packageName + "." + fileName;
	}
	
	// 出力処理.
	private static final void println(Writer w, int tab, String s)
		throws IOException {
		for(int i = 0; i < tab; i ++) {
			w.append("\t");
		}
		w.append(s);
		w.append("\n");
	}
	
	// 抽出した@Route定義されたComponentをJavaファイルに出力.
	private static final void outputComponentRoute(List<String> routeList, String any,
		List<String> errList, String outSourceDirectory, ClassLoader cl)
		throws Exception {
		String outDir = outSourceDirectory + "/" + AUTO_ROUTE_DIRECTORY_NAME;
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		String outFileName = outDir + "/" + AUTO_ROUTE_SOURCE_NAME;
		BufferedWriter w = null;
		try {
			// ソースコードを出力.
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName)));
			println(w, 0, "package " + Router.AUTO_READ_ROUTE_PACKAGE + ";");
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
			int len = routeList.size();
			for(int i = 0; i < len; i ++) {
				clazzName = routeList.get(i);
				println(w, 2, "");
				println(w, 2, "// Register the \""+ clazzName + "\"");
				println(w, 2, "// component in the @Route.");
				println(w, 2, "router.route(new " + clazzName + "());");
			}
			
			if(any != null) {
				println(w, 2, "");
				println(w, 2, "// Register the \""+ any + "\"");
				println(w, 2, "// component in the @AnyRoute.");
				println(w, 2, "router.any(new " + any + "());");
			}
			
			Class<?> c;
			len = errList.size();
			for(int i = 0; i < len; i ++) {
				clazzName = errList.get(i);
				c = Class.forName(clazzName, true, cl);
				int[] es = LoadAnnotationRoute.loadErrorRoute(c);
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
	private static final void outputCdiService(List<String> cdiList, String outSourceDirectory)
		throws Exception {
		String outDir = outSourceDirectory + "/" + CDI_SERVICE_DIRECTORY_NAME;
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		String outFileName = outDir + "/" + CDI_SERVICE_SOURCE_NAME;
		BufferedWriter w = null;
		try {
			// ソースコードを出力.
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName)));
			println(w, 0, "package " + CdiManager.AUTO_READ_CDI_SERVICE_PACKAGE + ";");
			println(w, 0, "");
			println(w, 0, "import quina.Quina;");
			println(w, 0, "import quina.CdiManager;");
			println(w, 0, "");
			println(w, 0, "/**");
			println(w, 0, " * ServiceScoped Annotation Registers the defined service object.");
			println(w, 0, " */");
			println(w, 0, "public final class " + CdiManager.AUTO_READ_CDI_SERVICE_CLASS + " {");
			println(w, 1, "private " + CdiManager.AUTO_READ_CDI_SERVICE_CLASS + "() {}");
			
			println(w, 1, "");
			println(w, 1, "/**");
			println(w, 1, " * ServiceScoped Annotation Registers the defined service object.");
			println(w, 1, " *");
			println(w, 1, " * @exception Exception If the service registration fails.");
			println(w, 1, " */");
			println(w, 1, "public static final void " + CdiManager.AUTO_READ_CDI_SERVICE_METHOD +
				"() throws Exception {");
			
			println(w, 2, "");
			println(w, 2, "// Get the Service Manager to be registered.");
			println(w, 2, "final CdiManager cdiManager = Quina.get().getCdiManager();");
			
			String clazzName;
			int len = cdiList.size();
			for(int i = 0; i < len; i ++) {
				clazzName = cdiList.get(i);
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
	private static final void outputCdiReflect(List<String> refList, String outSourceDirectory,
		ClassLoader cl)
		throws Exception {
		String outDir = outSourceDirectory + "/" + CDI_REFLECT_DIRECTORY_NAME;
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		String outFileName = outDir + "/" + CDI_REFLECT_SOURCE_NAME;
		BufferedWriter w = null;
		try {
			// ソースコードを出力.
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName)));
			println(w, 0, "package " + CdiReflectManager.AUTO_READ_CDI_REFLECT_PACKAGE + ";");
			println(w, 0, "");
			println(w, 0, "import quina.Quina;");
			println(w, 0, "import quina.CdiReflectManager;");
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
			
			println(w, 2, "");
			println(w, 2, "// Get the Cdi Reflect to be registered.");
			println(w, 2, "final CdiReflectManager refManager = Quina.get().getCdiReflectManager();");
			println(w, 2, "");
			println(w, 2, "Object o = null;");
			println(w, 2, "Class<?> cls = null;");
			println(w, 2, "CdiReflectElement element = null;");
			
			Class<?> c;
			String clazzName;
			int len = refList.size();
			for(int i = 0; i < len; i ++) {
				clazzName = refList.get(i);
				c = Class.forName(clazzName, true, cl);
				final Field[] list = c.getDeclaredFields();
				int lenJ = list.length;
				if(lenJ == 0) {
					continue;
				}
				println(w, 2, "");
				println(w, 2, "// Register the field group of the target class");
				println(w, 2, "// \""+ clazzName + "\"");
				println(w, 2, "o = new " + clazzName + "();");
				println(w, 2, "element = refManager.register(o);");
				println(w, 2, "if(element != null) {");
				println(w, 3, "cls = o.getClass();");

				for(int j = 0; j < lenJ; j ++) {
					println(w, 3, "element.add(cls.getDeclaredField(\"" +
						list[j].getName() + "\"));");
				}
				println(w, 3, "cls = null; element = null;");
				println(w, 2, "}");
				println(w, 2, "o = null;");
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
