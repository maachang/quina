package quina.command;

import static quina.command.generateCdi.GCdiConstants.AUTO_ROUTE_SOURCE_NAME;
import static quina.command.generateCdi.GCdiConstants.CDI_DIRECTORY_NAME;
import static quina.command.generateCdi.GCdiConstants.CDI_REFLECT_SOURCE_NAME;
import static quina.command.generateCdi.GCdiConstants.CDI_SERVICE_SOURCE_NAME;
import static quina.command.generateCdi.GCdiConstants.COMMAND_NAME;
import static quina.command.generateCdi.GCdiConstants.PROXY_SCOPED_SOURCE_NAME;
import static quina.command.generateCdi.GCdiConstants.QUINA_SERVICE_SOURCE_NAME;
import static quina.command.generateCdi.GCdiConstants.VERSION;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import quina.annotation.AnnotationUtil;
import quina.command.generateCdi.GCdiConstants;
import quina.command.generateCdi.GCdiExtraction;
import quina.command.generateCdi.GCdiOutputJavaSrc;
import quina.command.generateCdi.GCdiOutputResourceItem;
import quina.command.generateCdi.GCdiParams;
import quina.command.generateCdi.GCdiRemoveFileOrDir;
import quina.command.generateCdi.GCdiUtil;
import quina.command.generateCdi.NativeImages;
import quina.command.generateCdi.ProxyOutputJavaSrc;
import quina.exception.QuinaException;
import quina.util.Args;
import quina.util.FileUtil;

/**
 * CDI関連のリフレクションに対するJavaソースコードを生成.
 * 
 * このコマンドは graalvm での native-image で利用できない
 * Reflection 関連に対する、代替え的処理として、Javaの
 * ソースコードを作成し、native-image を作成できるようにします.
 */
public class GenerateCdi {
	
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
		System.out.println("  --verbose");
		System.out.println("     Display detailed information.");
		System.out.println("  -s [--source] {directory}");
		System.out.println("     * Settings are required.");
		System.out.println("     Set the output destination Java source code directory.");
		System.out.println("     For the directory, specify the top package name directory.");
		System.out.println("  -c [--class] {directory}");
		System.out.println("     Set the directory for the target class files.");
		System.out.println("     If this definition is not specified, one or more");
		System.out.println("     -j or --jar definitions are required. ");
		System.out.println("  -j [--jar] {jarFileName}");
		System.out.println("     Set the directory where the jar files are stored.");
		System.out.println("     This definition can be defined more than once.");
		System.out.println("     If you do not make this definition, you will need");
		System.out.println("     the -c or --class definition. ");
		System.out.println("  -n [--nativeImage] {directory}");
		System.out.println("     Set the config definition output destination directory");
		System.out.println("     for Native-Image of graalvm.");
		System.out.println("  -r [--resource]");
		System.out.println("     Add the \".properties\" file in the class or jar for the ");
		System.out.println("     target classpath to Resoure.json which is defined in the ");
		System.out.println("     Native Image of GraalVM. ");
		System.out.println("  -d [--delete]");
		System.out.println("     Delete the file output by this command and exit.");
		System.out.println();
	}
	
	// エラーハンドル処理.
	private static final void errorHandle(String javaSourceDir) {
		if(javaSourceDir == null || javaSourceDir.isEmpty()) {
			return;
		}
		// エラーが発生した場合は、生成されるGCi情報を破棄する.
		try {
			// 
			GCdiRemoveFileOrDir.removeOutAutoJavaSource(javaSourceDir);
		} catch(Exception e) {}
		try {
			// 
			GCdiRemoveFileOrDir.removeProxyDirectory(javaSourceDir);
		} catch(Exception e) {}
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
		
		// 詳細表示フラグを設定.
		boolean verboseFlag = false;
		if(args.isValue("-verbose")) {
			verboseFlag = true;
		}
		
		// javaソースディレクトリを取得.
		String javaSourceDir = args.get("-s", "--source");
		// javaソースディレクトリが指定されていない場合.
		if(javaSourceDir == null) {
			outHelp();
			System.err.println("[ERROR] The output destination Java source directory has not been set.");
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
		
		// GCdiで出力されるファイル群を全削除して処理終了するか取得.
		boolean deleteOutFileOnlyFlag = args.isValue("-d", "--delete");
		
		// classディレクトリを取得.
		String clazzDir = args.get("-c", "--class");
		// classディレクトリが存在しない場合.
		if(clazzDir != null && !new File(clazzDir).isDirectory()) {
			// ただしdeleteOutFileOnlyFlagがtrueの場合はエラーにしない.
			if(!deleteOutFileOnlyFlag) {
				System.err.println("[ERROR] The specified class directory \"" +
					clazzDir + "\" is not a directory. ");
				System.exit(1);
				return;
			}
		}
		
		// jar directory Listを取得.
		List<String> jarDirList = new ArrayList<String>();
		for(int i = 0;; i ++) {
			String jarName = args.next(i, "-j", "--jar");
			if(jarName == null) {
				break;
			}
			jarDirList.add(jarName);
		}
		
		// classディレクトリとjarディレクトリの両方が指定されていない場合.
		if(clazzDir == null && jarDirList.size() == 0) {
			// ただしdeleteOutFileOnlyFlagがtrueの場合はエラーにしない.
			if(!deleteOutFileOnlyFlag) {
				outHelp();
				System.err.println(
					"[ERROR] The extraction source class directory and jar " +
					"directory has not been set.");
				System.exit(1);
				return;
			}
		}
		
		// classディレクトリを整頓.
		// ただしdeleteOutFileOnlyFlagがtrueの場合はエラーにしない.
		if(!deleteOutFileOnlyFlag) {
			clazzDir = AnnotationUtil.slashPath(clazzDir);
		} else {
			// deleteOutFileOnlyFlagがtrueの場合はclassディレクトリは空をセット.
			clazzDir = null;
		}
		
		// jarディレクトリリストを整頓.
		String[] jarDirArray = null;
		if(jarDirList.size() > 0) {
			int len = jarDirList.size();
			jarDirArray = new String[len];
			for(int i = 0; i < len; i ++) {
				jarDirArray[i] = AnnotationUtil.slashPath(
					jarDirList.get(i));
			}
		} else {
			jarDirArray = new String[0];
		}
		jarDirList = null;
		
		// GraalVMのNative-Imageコンフィグ出力先ディレクトリを取得.
		String nativeImgDir = args.get("-n", "--nativeImage");
		if(nativeImgDir == null ||
			(nativeImgDir = nativeImgDir.trim()).isEmpty()) {
			// デフォルト内容をセット.
			nativeImgDir = GCdiConstants.DEF_NATIVE_CONFIG_DIR;
		}
		
		// nativeImgDirディレクトリを整頓.
		nativeImgDir = AnnotationUtil.slashPath(nativeImgDir);
		
		// classPath内のリソースファイルをResourceItemに含めるか取得.
		boolean resourceItemFlag = args.isValue("-r", "--resource");
		
		// 処理開始.
		System.out.println("start " + this.getClass().getSimpleName() +
			" version: " + VERSION);
		System.out.println();
		System.out.println(" target outputPath    : " +
			FileUtil.getFullPath(javaSourceDir));
		System.out.println(" target classPath     : " +
			(clazzDir ==null ? "" : FileUtil.getFullPath(clazzDir)));
		System.out.print(" target jarPath       : ");
		if(jarDirArray.length > 0) {
			System.out.println(FileUtil.getFullPath(jarDirArray[0]));
			int len = jarDirArray.length;
			for(int i = 1; i < len; i ++) {
				System.out.println("                   : " +
					FileUtil.getFullPath(jarDirArray[i]));
			}
		} else {
			System.out.println();
		}
		System.out.println(" target outNativeConf : " +
			FileUtil.getFullPath(nativeImgDir));
		
		System.out.println();
		
		// 処理開始.
		long time = System.currentTimeMillis();
		
		// GCdiで出力するファイル内容を削除して終了する場合.
		if(deleteOutFileOnlyFlag) {
			// ファイルを削除.
			GCdiRemoveFileOrDir.removeOutGCdi(javaSourceDir, nativeImgDir);
			time = System.currentTimeMillis() - time;
			System.out.println("The file output by Generate Cdi has been deleted. ");
			System.out.println();
			System.out.println("success: " + time + " msec");
			System.out.println();
			// 正常終了.
			System.exit(0);
			return;
		}
		
		try {
			
			// jarファイル名群を取得.
			String[] jarFileArray;
			if(jarDirArray.length > 0) {
				jarFileArray = GCdiUtil.findJarFiles(jarDirArray);
			} else {
				jarFileArray = new String[0];
			}
			jarDirArray = null;
			
			// params.
			GCdiParams params = new GCdiParams(
				clazzDir, verboseFlag, resourceItemFlag, jarFileArray);
			
			// クラス一覧を取得.
			List<String> clazzList = new ArrayList<String>();
			// クラスディレクトリのクラス一覧を取得.
			if(clazzDir != null) {
				GCdiUtil.findClassDirByClassNames(clazzList, params, clazzDir);
			}
			// jarファイル群からクラス一覧を取得.
			if(jarFileArray.length > 0) {
				int len = jarFileArray.length;
				for(int i = 0; i < len; i ++) {
					GCdiUtil.findJarByClassNames(
						clazzList, params, jarFileArray[i]);
				}
			}
			
			// ClassDirから、対象となるクラスを抽出.
			GCdiExtraction.extraction(clazzList, javaSourceDir, clazzDir, params);
			clazzList = null;
			
			// 出力先のソースコードを全削除.
			GCdiRemoveFileOrDir.removeOutAutoJavaSource(javaSourceDir);
			
			// 最初にリソースファイルをResourceItemにセット.
			GCdiOutputResourceItem.outputResourceItem(params);
			
			// GraalVM用のNativeImageコンフィグ群を出力.
			NativeImages.outputNativeConfig(nativeImgDir, null);
			
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
			
			// 開始処理.
			System.out.println();
			
			// ProxyScopedソースコードの自動作成を行う.
			ProxyOutputJavaSrc.proxyScoped(javaSourceDir, params);
			
			// [Router]ファイル出力.
			if(!params.isRouteEmpty()) {
				GCdiOutputJavaSrc.routerScoped(javaSourceDir, params);
				System.out.println( " routerScoped         : " +
					new File(javaSourceDir).getCanonicalPath() +
					"/" + CDI_DIRECTORY_NAME + "/" + AUTO_ROUTE_SOURCE_NAME);
			}
			
			// [(CDI)ServiceScoped]ファイル出力.
			if(!params.isCdiEmpty()) {
				GCdiOutputJavaSrc.serviceScoped(javaSourceDir, params);
				System.out.println( " serviceScoped        : " +
					new File(javaSourceDir).getCanonicalPath() +
					"/" + CDI_DIRECTORY_NAME + "/" + CDI_SERVICE_SOURCE_NAME);
			}
			
			// [QuinaService]ファイル出力.
			if(!params.isQuinaServiceEmpty()) {
				GCdiOutputJavaSrc.quinaServiceScoped(javaSourceDir, params);
				System.out.println( " quinaServiceScoped   : " +
					new File(javaSourceDir).getCanonicalPath() +
					"/" + CDI_DIRECTORY_NAME + "/" + QUINA_SERVICE_SOURCE_NAME);
			}
			
			// [CdiReflect]ファイル出力.
			if(!params.isCdiReflectEmpty()) {
				GCdiOutputJavaSrc.cdiReflect(javaSourceDir, params);
				System.out.println( " cdiReflect           : " +
					new File(javaSourceDir).getCanonicalPath() +
					"/" + CDI_DIRECTORY_NAME + "/" + CDI_REFLECT_SOURCE_NAME);
			}
			
			// [CdiHandle]ファイル出力.
			if(!params.isCdiHandleEmpty()) {
				GCdiOutputJavaSrc.cdiHandle(javaSourceDir, params);
				System.out.println( " cdiHandle            : " +
					new File(javaSourceDir).getCanonicalPath() +
					"/" + CDI_DIRECTORY_NAME + "/" + CDI_SERVICE_SOURCE_NAME);
			}
			
			// [ProxyScoped]ファイル出力.
			if(!params.isProxyScopedEmpty()) {
				GCdiOutputJavaSrc.proxyScoped(javaSourceDir, params);
				System.out.println( " proxyScoped          : " +
					new File(javaSourceDir).getCanonicalPath() +
					"/" + CDI_DIRECTORY_NAME + "/" + PROXY_SCOPED_SOURCE_NAME);
			}
			
			time = System.currentTimeMillis() - time;
			System.out.println();
			System.out.println("success: " + time + " msec");
			System.out.println();
			
			System.exit(0);
		} catch(QuinaException qe) {
			errorHandle(javaSourceDir);
			throw qe;
		} catch(Exception e) {
			errorHandle(javaSourceDir);
			throw new QuinaException(e);
		}
	}
}
