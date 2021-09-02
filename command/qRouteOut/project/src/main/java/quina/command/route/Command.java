package quina.command.route;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import quina.Router;
import quina.annotation.route.Route;
import quina.component.Component;

/**
 * コマンド実行.
 * 
 * このコマンドにより、指定されたJavaクラスフォルダーから
 * quina.component.Componentを継承したQuinaのComponentに
 * 対してquina.annotation.route.Route定義されてるものを
 * 抽出して、対象プロジェクトにquinax.RouteComponents.java
 * を作成します.
 */
public class Command {

	// バージョン.
	private static final String VERSION = "0.0.1";
	
	// 出力先ディレクトリ名.
	private static final String DIRECTORY_NAME = packageNameToDirectory(
		Router.AUTO_READ_ROUTE_PACKAGE);
	
	// 出力先Javaソースファイル名.
	private static final String JAVA_SOURCE_NAME = Router.AUTO_READ_ROUTE_CLASS +
		".java";
	
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
		clazzDir = pathSlash(clazzDir);
		
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
		javaSourceDir = pathSlash(javaSourceDir);
		
		// @Route指定されてるComponentを抽出.
		List<String> routeList = extractComponentRoute(clazzDir);
		
		// 抽出した内容が存在する場合は、抽出条件をファイルに出力.
		if(routeList.size() == 0) {
			// 存在しない場合はエラー.
			System.err.println("[ERROR] @Route The defined Component object does not exist.");
			System.exit(1);
			return;
		}
		
		// ファイル出力.
		outputComponentRoute(routeList, javaSourceDir);
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
	
	// パスのセパレータをスラッシュに統一.
	private static final String pathSlash(String name) {
		char c;
		StringBuilder buf = new StringBuilder();
		int len = name.length();
		for(int i = 0; i < len; i ++) {
			c = name.charAt(i);
			if(c == '\\') {
				buf.append('/');
			} else {
				buf.append(c);
			}
		}
		name = buf.toString();
		if(name.endsWith("/")) {
			return name.substring(0, name.length() - 1);
		}
		return name;
	}
	
	// @Route指定されてるComponentを抽出.
	private static final List<String> extractComponentRoute(String dir)
		throws Exception {
		List<String> ret = new ArrayList<String>();
		ClassLoader cl = createClassLoader(dir);
		readComponentRoute(ret, cl, dir, "");
		return ret;
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
	private static final void readComponentRoute(List<String> out, ClassLoader cl,
		String dir, String packageName)
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
				readComponentRoute(out, cl, target, createPackageName(packageName, list[i]));
			// クラスファイルの場合.
			} else if(list[i].endsWith(".class")) {
				// クラス名を取得.
				className = createClassName(packageName, list[i]);
				try {
					// クラスを取得.
					final Class c = Class.forName(className, true, cl);
					// Routeのアノテーションが設定されていない場合.
					if(!c.isAnnotationPresent(Route.class)) {
						continue;
					}
					// クラスのインスタンスを生成.
					final Object o = c.getDeclaredConstructor().newInstance();
					// 対象がコンポーネントクラスでない場合は処理しない.
					if(!(o instanceof Component)) {
						continue;
					}
					// @Route付属のコンポーネントを登録.
					out.add(className);
				} catch(Exception e) {
					// 例外は無視.
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
	
	// 抽出した@Route定義されたComponentをJavaファイルに出力.
	private static final void outputComponentRoute(List<String> routeList, String outSourceDirectory)
		throws Exception {
		String outDir = outSourceDirectory + "/" + DIRECTORY_NAME;
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		String outFileName = outDir + "/" + JAVA_SOURCE_NAME;
		BufferedWriter w = null;
		try {
			// ソースコードを出力.
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName)));
			println(w, 0, "package " + Router.AUTO_READ_ROUTE_PACKAGE + ";");
			println(w, 0, "");
			println(w, 0, "import quina.Quina;");
			println(w, 0, "import quina.Router;");
			//println(w, 0, "import quina.component.Component;");
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
				println(w, 2, "// Register the \""+ clazzName + "\" component in the Router. ");
				//println(w, 2, "router.route((Component)(Class.forName(\"" + clazzName + "\")");
				//println(w, 3, ".getDeclaredConstructor().newInstance()");
				//println(w, 2, "));");
				println(w, 2, "router.route(new " + clazzName + "());");
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
	
	// 出力処理.
	private static final void println(Writer w, int tab, String s)
		throws IOException {
		for(int i = 0; i < tab; i ++) {
			w.append("\t");
		}
		w.append(s);
		w.append("\n");
	}
}
