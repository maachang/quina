package quina.command;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import quina.CdiManager;
import quina.CdiReflectManager;
import quina.QuinaConstants;
import quina.Router;
import quina.annotation.AnnotationUtil;
import quina.annotation.cdi.CdiScoped;
import quina.annotation.cdi.ServiceScoped;
import quina.annotation.route.AnyRoute;
import quina.annotation.route.ErrorRoute;
import quina.annotation.route.LoadAnnotationRoute;
import quina.annotation.route.Route;
import quina.component.Component;
import quina.component.ErrorComponent;
import quina.util.Args;
import quina.util.FileUtil;

/**
 * CDI関連のリフレクションに対するJavaソースコードを生成.
 * 
 * このコマンドは graalvm での native-image で利用できない
 * Reflection 関連に対する、代替え的処理として、Javaの
 * ソースコードを作成し、native-image を作成できるようにします.
 * 
 * graalvm での native-image では、実行読み込み対象のプロジェクト
 * 全体のClass群やそのClass群を読み込むAnnotation関連のField
 * リフレクション群等の読み込みが、例外を求める事があり、それらを
 * 防ぐために以下の条件に対して、別途実装ソースコードを自動生成
 * して、それらを実行可能にするために対応します.
 * 
 * 1. CDI関連のComponent/Serviceに対するField群を列挙する
 *    ソースコードを生成.
 *    
 * 2. CDI関連のService群のオブジェクトを列挙するソースコードを
 *    生成.
 * 
 * 3. Routeアノテーション定義のComponent群のオブジェクトを列挙
 *    するソースコードを生成.
 * 
 * 利用想定としては、対象プロジェクトのコンパイル前にこの
 * コマンドを実行して、Cdi関連のリフレクション代替え用の
 * コードを生成して、プロジェクトをコンパイルする事で
 * graalvm の native-image 実行が可能になります.
 */
public class GenerateCdi {

	// バージョン.
	private static final String VERSION = "0.0.1";
	
	// コマンド名.
	private static final String COMMAND_NAME = "genCdi";
	
	// AutoRoute出力先ディレクトリ名.
	private static final String CDI_DIRECTORY_NAME = packageNameToDirectory(
		QuinaConstants.CDI_PACKAGE_NAME);
	
	// AutoRoute出力先Javaソースファイル名.
	private static final String AUTO_ROUTE_SOURCE_NAME =
		Router.AUTO_READ_ROUTE_CLASS + ".java";
	
	// AutoCdiService出力先Javaソースファイル名.
	private static final String CDI_SERVICE_SOURCE_NAME =
		CdiManager.AUTO_READ_CDI_SERVICE_CLASS + ".java";
	
	// AutoCdiReflect出力先Javaソースファイル名.
	private static final String CDI_REFLECT_SOURCE_NAME =
		CdiReflectManager.AUTO_READ_CDI_REFLECT_CLASS + ".java";
	
	// パラメータオブジェクト.
	private static final class GenerateCdiParams {
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
		
		// classLoader.
		public ClassLoader cl;
		
		public GenerateCdiParams(String clazzDir)
			throws Exception {
			cl = createClassLoader(clazzDir);
		}
		
		public boolean isEmpty() {
			return refList.size() == 0 && routeList.size() == 0 && 
				cdiList.size() == 0 && any == null && errList.size() == 0;
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
	}
	
	/**
	 * メイン処理.
	 * @param args
	 * @throws Exception
	 */
	public static final void main(String[] args) throws Exception {
		GenerateCdi cmd = new GenerateCdi(args);
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
	private GenerateCdi(String[] args) {
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
		System.out.println("Usage: " + COMMAND_NAME + " [options]");
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
		System.out.println("start " + this.getClass().getSimpleName() + " version: " + VERSION);
		System.out.println(" target classPath : " + new File(clazzDir).getCanonicalPath());
		System.out.println(" target outputPath: " + new File(javaSourceDir).getCanonicalPath());
		System.out.println("");
		
		long time = System.currentTimeMillis();
		
		// params.
		GenerateCdiParams params = new GenerateCdiParams(clazzDir);
		
		// ClassDirから、対象となるクラスを抽出.
		extractionClass(clazzDir, params);
		
		// 出力先のソースコードを全削除.
		removeOutAutoJavaSource(javaSourceDir);
		
		// 抽出した内容が存在する場合は、抽出条件をファイルに出力.
		if(params.isEmpty()) {
			time = System.currentTimeMillis() - time;
			// 存在しない場合は正常終了.
			System.out.println("There is no target condition to read.");
			System.out.println();
			System.out.println("success: " + time + " msec");
			System.out.println();
			System.exit(0);
			return;
		}
		
		// [Router]ファイル出力.
		if(!params.isRouteEmpty()) {
			outputComponentRoute(javaSourceDir, params);
		}
		
		// [CdiService]ファイル出力.
		if(!params.isCdiEmpty()) {
			outputCdiService(javaSourceDir, params);
		}
		
		// [CdiReflect]ファイル出力.
		if(!params.isCdiReflectEmpty()) {
			outputCdiReflect(javaSourceDir, params);
		}
		
		time = System.currentTimeMillis() - time;
		System.out.println();
		// [Router]ファイル出力内容が存在する場合.
		if(!params.isRouteEmpty()) {
			System.out.println( " routerOutput:     " +
				new File(javaSourceDir).getCanonicalPath() +
				"/" + CDI_DIRECTORY_NAME + "/" + AUTO_ROUTE_SOURCE_NAME);
		}
		// [CdiService]ファイル出力内容が存在する場合.
		if(!params.isCdiEmpty()) {
			System.out.println( " cdiServiceOutput: " +
				new File(javaSourceDir).getCanonicalPath() +
				"/" + CDI_DIRECTORY_NAME + "/" + CDI_SERVICE_SOURCE_NAME);
		}
		// [CdiReflect]ファイル出力内容が存在する場合.
		if(!params.isCdiReflectEmpty()) {
			System.out.println( " cdiReflectOutput: " +
					new File(javaSourceDir).getCanonicalPath() +
					"/" + CDI_DIRECTORY_NAME + "/" + CDI_REFLECT_SOURCE_NAME);
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
	private static final void extractionClass(String dir, GenerateCdiParams params)
		throws Exception {
		extractionClass(dir, "", params);
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
	
	// クラスからインスタンスを生成.
	private static final Object newInstance(Class<?> c)
		throws Exception {
		// クラスのインスタンスを生成.
		final Constructor<?> cons = c.getDeclaredConstructor();
		cons.setAccessible(true);
		return cons.newInstance();
	}
	
	// 1つのディレクトリに対して@Route指定されてるComponentを抽出.
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static final void extractionClass(String dir, String packageName,
		GenerateCdiParams params) throws Exception {
		String target, className;
		File f = new File(dir);
		String[] list = f.list();
		int len = list != null ? list.length : 0;
		for(int i = 0; i < len; i ++) {
			target = dir + "/" + list[i];
			// 対象がディレクトリの場合.
			if(new File(target).isDirectory()) {
				// 今回のディレクトリで再帰処理.
				extractionClass(target,
					createPackageName(packageName, list[i]),
					params);
			// クラスファイルの場合.
			} else if(list[i].endsWith(".class")) {
				// クラス名を取得.
				className = createClassName(packageName, list[i]);
				// クラスを取得.
				final Class c = Class.forName(
					className, true, params.cl);
				// RouteやAnyやErrorやServiceScopedやCdiScopedの
				// アノテーションが設定されていない場合.
				if(!c.isAnnotationPresent(Route.class) &&
					!c.isAnnotationPresent(AnyRoute.class) &&
					!c.isAnnotationPresent(ErrorRoute.class) &&
					!c.isAnnotationPresent(ServiceScoped.class) &&
					!c.isAnnotationPresent(CdiScoped.class)) {
					// クラスのインスタンスを生成.
					Object o;
					try {
						o = newInstance(c);
					} catch(Exception e) {
						continue;
					}
					// アノテーションなしのコンポーネントの場合.
					if(o instanceof Component || o instanceof ErrorComponent) {
						// Reflectリストに追加.
						params.refList.add(className);
					}
					continue;
				}
				// CdiScoped定義のクラスの場合.
				if(c.isAnnotationPresent(CdiScoped.class)) {
					System.out.println("  > cdiScoped: '" + className + "'");
					// Reflectリストに追加.
					params.refList.add(className);
				}
				// ServiceScoped定義のCdiServiceの場合.
				if(c.isAnnotationPresent(ServiceScoped.class)) {
					System.out.println("  > cdiService: '" + className + "'");
					// Cdiリストに追加.
					params.cdiList.add(className);
					// Reflectリストに追加.
					params.refList.add(className);
					continue;
				}
				// クラスのインスタンスを生成.
				final Object o = newInstance(c);
				// 対象がコンポーネントクラスの場合.
				if(o instanceof Component) {
					// @Route付属のコンポーネントを登録.
					if(c.isAnnotationPresent(Route.class)) {
						System.out.println("  > route: '" + className + "' path: '" +
							((Route)c.getAnnotation(Route.class)).value() + "'");
						params.routeList.add(className);
					// @Any付属のコンポーネントを登録.
					} else if(c.isAnnotationPresent(AnyRoute.class)) {
						System.out.println("  > any:   '" + className + "'");
						params.any = className;
					}
					// Reflectリストに追加.
					params.refList.add(className);
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
						params.errList.add(className);
					}
					// Reflectリストに追加.
					params.refList.add(className);
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
	
	// 出力作のディレクトリ内の自動生成のJavaソースを削除.
	private static final void removeOutAutoJavaSource(String outSourceDirectory)
		throws Exception {
		String[] javaSrcs = new String[] {
			AUTO_ROUTE_SOURCE_NAME,
			CDI_SERVICE_SOURCE_NAME,
			CDI_REFLECT_SOURCE_NAME
		};
		String outDir = outSourceDirectory + "/" + CDI_DIRECTORY_NAME + "/";
		int len = javaSrcs.length;
		for(int i = 0; i < len; i ++) {
			try {
				FileUtil.removeFile(outDir + javaSrcs[i]);
			} catch(Exception e) {}
		}
	}
	
	// 抽出した@Route定義されたComponentをJavaファイルに出力.
	private static final void outputComponentRoute(String outSourceDirectory,
		GenerateCdiParams params)
		throws Exception {
		String outDir = outSourceDirectory + "/" + CDI_DIRECTORY_NAME;
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		String outFileName = outDir + "/" + AUTO_ROUTE_SOURCE_NAME;
		BufferedWriter w = null;
		try {
			// ソースコードを出力.
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName)));
			println(w, 0, "package " + QuinaConstants.CDI_PACKAGE_NAME + ";");
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
	private static final void outputCdiService(String outSourceDirectory,
		GenerateCdiParams params)
		throws Exception {
		String outDir = outSourceDirectory + "/" + CDI_DIRECTORY_NAME;
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		String outFileName = outDir + "/" + CDI_SERVICE_SOURCE_NAME;
		BufferedWriter w = null;
		try {
			// ソースコードを出力.
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName)));
			println(w, 0, "package " + QuinaConstants.CDI_PACKAGE_NAME + ";");
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
	private static final void outputCdiReflect(String outSourceDirectory,
		GenerateCdiParams params)
		throws Exception {
		String outDir = outSourceDirectory + "/" + CDI_DIRECTORY_NAME;
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		String outFileName = outDir + "/" + CDI_REFLECT_SOURCE_NAME;
		BufferedWriter w = null;
		try {
			// ソースコードを出力.
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName)));
			println(w, 0, "package " + QuinaConstants.CDI_PACKAGE_NAME + ";");
			println(w, 0, "");
			println(w, 0, "import java.lang.reflect.Field;");
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
			println(w, 2, "Class<?> cls = null;");
			println(w, 2, "Field field = null;");
			println(w, 2, "boolean staticFlag = false;");
			println(w, 2, "CdiReflectElement element = null;");
			
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
				println(w, 2, "cls = " + clazzName + ".class;");
				println(w, 2, "element = refManager.register(cls);");
				println(w, 2, "if(element != null) {");

				for(int j = 0; j < lenJ; j ++) {
					println(w, 3, "");
					println(w, 3, "field = cls.getDeclaredField(\"" + list[j].getName() + "\");");
					println(w, 3, "staticFlag = " + Modifier.isStatic(list[j].getModifiers()) + ";");
					println(w, 3, "element.add(staticFlag, field);");
				}
				println(w, 3, "cls = null; element = null;");
				println(w, 2, "}");
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
