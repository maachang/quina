package quina.compile.tool;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import quina.annotation.AnnotationUtil;
import quina.annotation.resource.AnnotationResource;
import quina.annotation.resource.ExecuteCopyResource;
import quina.exception.QuinaException;
import quina.util.Args;
import quina.util.FileUtil;

/**
 * ResourceScopedアノテーションが定義された
 * クラス内でBuildResourceアノテーション定義
 * されてる条件を元にリソースファイルを
 * コピーします.
 */
public class QuinaRCP {
	
	/**
	 * CopyResourceバージョン.
	 */
	public static final String VERSION = "0.0.1";
	
	/**
	 *  CopyResourceコマンド名.
	 */
	public static final String COMMAND_NAME = "qrcp";
	
	/**
	 * メイン処理.
	 * @param args
	 * @throws Exception
	 */
	public static final void main(String[] args)
		throws Exception {
		QuinaRCP cmd = new QuinaRCP(args);
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
	private QuinaRCP(String[] args) {
		this.args = new Args(args);
	}
	
	/**
	 * ヘルプ情報を表示.
	 */
	private void outHelp() {
		System.out.println("Copy the resource files that are not copied to the compiled class package.");
		System.out.println();
		System.out.println("With this command, Copy the resource file for the condition defined in the ");
		System.out.println("                   BuildResource annotation in the class where the ");
		System.out.println("                   ResourceScoped annotation is defined. ");
		System.out.println();
		System.out.println("Usage: " + COMMAND_NAME + " [options]");
		System.out.println(" where options include:");

		System.out.println("  -v [--version]");
		System.out.println("     version information .");
		System.out.println("  -h [--help]");
		System.out.println("     the help contents.");
		System.out.println("  -s [--source] {directory}");
		System.out.println("     * Settings are required.");
		System.out.println("     Set the output destination Java source code directory.");
		System.out.println("     For the directory, specify the top package name directory.");
		System.out.println("  -c [--class] {directory}");
		System.out.println("     Set the directory for the target class files.");
		System.out.println();
	}
	
	// コマンド実行.
	private void execute() throws Exception {
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
		
		// javaソースディレクトリを取得.
		String javaSourceDir = args.get("-s", "--source");
		// javaソースディレクトリが指定されていない場合.
		if(javaSourceDir == null) {
			outHelp();
			System.err.println(
				"[ERROR] The output destination Java source directory has not " +
				"been set.");
			System.exit(1);
			return;
		// javaソースディレクトリが存在しない場合.
		} else if(!new File(javaSourceDir).isDirectory()) {
			System.err.println("[ERROR] The specified Java source directory \"" +
				javaSourceDir + "\" is not a directory. ");
			System.exit(1);
			return;
		}
		
		// javaソースディレクトリを整頓.
		javaSourceDir = AnnotationUtil.slashPath(javaSourceDir);
		
		// classディレクトリを取得.
		String clazzDir = args.get("-c", "--class");
		// classディレクトリが存在しない場合.
		if(clazzDir != null && !new File(clazzDir).isDirectory()) {
			System.err.println("[ERROR] The specified class directory \"" +
				clazzDir + "\" is not a directory. ");
			System.exit(1);
			return;
		}
		
		// classディレクトリを整頓.
		clazzDir = AnnotationUtil.slashPath(clazzDir);
		
		// clazzDirのクラスローダーを取得.
		ClassLoader cl = _createClassLoader(clazzDir);
		
		// 処理開始.
		long time = System.currentTimeMillis();
		
		// クラスリストを取得.
		List<String> clazzList = new ArrayList<String>();
		_findClassDirByClassNames(clazzList, clazzDir);
		
		// Resourceファイルコピー実行処理.
		List<String> ret = _executeCopyResource(
			cl, javaSourceDir, clazzDir, clazzList);
		
		// 処理終了.
		time = System.currentTimeMillis() - time;
		
		// 出力.
		System.out.println("start " + this.getClass().getSimpleName() +
			" version: " + VERSION);
		System.out.println();
		System.out.println(" target outputPath    : " +
			FileUtil.getFullPath(javaSourceDir));
		System.out.println(" target classPath     : " +
			(clazzDir ==null ? "" : FileUtil.getFullPath(clazzDir)));
		System.out.println();
		
		// 
		final int len = ret.size();
		for(int i = 0; i < len; i += 4) {
			System.out.println("   src mode           : " + ret.get(i));
			System.out.println("                          " + ret.get(i + 1));
			System.out.println("   dest mode          : " + ret.get(i + 2));
			System.out.println("                          " + ret.get(i + 3));
		}
		
		// 処理結果.
		System.out.println(" success              : " + time + " msec");
		
		System.exit(0);
	}

	// Classディレクトリからクラス名一覧を取得.
	private static final int _findClassDirByClassNames(
		List<String> out, String classDir) {
		if(out == null) {
			throw new QuinaException(
				"The list object to store the result is not set.");
		}
		classDir = AnnotationUtil.slashPath(classDir);
		int[] ret = new int[] {0};
		_findClassDirByClassNames(out, ret, "", classDir);
		return ret[0];
	}
	
	// Classディレクトリからクラス名一覧を取得.
	private static final void _findClassDirByClassNames(
		List<String> out, int[] count, String packageName, String classDir) {
		String target;
		File f = new File(classDir);
		String[] list = f.list();
		int len = list != null ? list.length : 0;
		for(int i = 0; i < len; i ++) {
			target = classDir + "/" + list[i];
			// ディレクトリの場合.
			if(new File(target).isDirectory()) {
				// 今回のディレクトリで再帰処理.
				_findClassDirByClassNames(
					out, count, _createPackageName(
						packageName, list[i]), target);
			// クラスファイルの場合.
			} else if(list[i].endsWith(".class")) {
				out.add(_createClassName(packageName, list[i]));
				count[0] ++;
			}
		}
	}

	// PackageNameを作成.
	private static final String _createPackageName(
		String base, String target) {
		if(base == null || base.isEmpty()) {
			return target;
		}
		return base + "." + target;
	}

	// パッケージ名＋クラスファイルでClass名を取得.
	private static final String _createClassName(
		String packageName, String fileName) {
		if(fileName.endsWith(".class")) {
			fileName = fileName.substring(0, fileName.length() - 6);
		} else if(fileName.endsWith(".properties")) {
			fileName = fileName.substring(0, fileName.length() - 11);
		}
		if(packageName.isEmpty()) {
			return fileName;
		}
		return packageName + "." + fileName;
	}
	
	// 対象Classディレクトリのクラスローダーを取得.
	private static final ClassLoader _createClassLoader(String classDir)
		throws Exception {
		if(classDir == null) {
			throw new QuinaException(
				"Neither the class directory is set.");
		}
		URL[] url = new URL[] {
			new File(classDir + "/").toURI().toURL()
		};
		return new URLClassLoader(url,
			Thread.currentThread().getContextClassLoader());
	}
	
	// クラスをローディング.
	private static final Class<?> _getClass(String name, ClassLoader cl)
		throws ClassNotFoundException {
		return Class.forName(name, true, cl);
	}

	// Resourceファイルコピー実行処理.
	private static final List<String> _executeCopyResource(
		ClassLoader cl, String javaSourceDir, String clazzDir,
		List<String> clazzList) throws Exception {
		
		Class<?> c;
		ExecuteCopyResource ecr;
		List<String> ret = new ArrayList<String>();
		final int len = clazzList.size();
		for(int i = 0; i < len; i ++) {
			c = _getClass(clazzList.get(i), cl);
			ecr = AnnotationResource.resourceScoped(c, cl);
			if(ecr != null) {
				ecr.execute(ret, javaSourceDir, clazzDir);
			}
		}
		return ret;
	}

}
