package quina.compile.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import quina.annotation.AnnotationUtil;
import quina.exception.QuinaException;
import quina.util.Args;
import quina.util.FileUtil;

/**
 * Quina Compile Tool コマンドパラメータ.
 * @author maachang
 *
 */
class QuinaCTCmdParams {
	/** 詳細表示フラグ. **/
	protected boolean verboseFlag;
	/** Cdiで出力されるファイル群を全削除して処理終了フラグ. **/
	protected boolean deleteOutFileOnlyFlag;
	/** javaソースディレクトリ. **/
	protected String javaSourceDir;
	/** classディレクトリ. **/
	protected String clazzDir;
	/** jarディレクトリリスト. **/
	protected String[] jarDirArray;
	/** jarファイルリスト. **/
	protected String [] jarFileArray;
	/** GraalVMのNative-Imageコンフィグ出力先ディレクトリ. **/
	protected String nativeImgDir;
	/** classPath内のリソースファイルをResourceItemに含めるフラグ. **/
	protected boolean resourceItemFlag;
	
	/**
	 * コンストラクタ.
	 * @param verboseFlag 詳細表示フラグを設定します.
	 * @param deleteOutFileOnlyFlag Cdiで出力されるファイル群を
	 *                              全削除して処理終了するフラグを設定します.
	 * @param javaSourceDir javaソースディレクトリを設定します.
	 * @param clazzDir classディレクトリを設定します.
	 * @param jarDirArray jarディレクトリリストを設定します.
	 * @param nativeImgDir GraalVMのNative-Imageコンフィグ出力先
	 *                     ディレクトリを設定します.
	 * @param resourceItemFlag classPath内のリソースファイルを
	 *                         ResourceItemに含めるフラグを設定します.
	 */
	QuinaCTCmdParams(boolean verboseFlag, boolean deleteOutFileOnlyFlag,
		String javaSourceDir, String clazzDir, String[] jarDirArray,
		String nativeImgDir, boolean resourceItemFlag
	) {
		// jarディレクトリリストからjarファイル名群を取得.
		String[] jarFileArray;
		if(jarDirArray.length > 0) {
			jarFileArray = QuinaCTUtil.findJarFiles(jarDirArray);
		} else {
			jarFileArray = new String[0];
		}
		
		/** 詳細表示フラグ. **/
		this.verboseFlag = verboseFlag;
		/** Cdiで出力されるファイル群を全削除して処理終了するフラグ. **/
		this.deleteOutFileOnlyFlag = deleteOutFileOnlyFlag;
		/** javaソースディレクトリ. **/
		this.javaSourceDir = javaSourceDir;
		/** classディレクトリ. **/
		this.clazzDir = clazzDir;
		/** jarディレクトリリスト. **/
		this.jarDirArray = jarDirArray;
		/** jarファイルリスト. **/
		this.jarFileArray = jarFileArray;
		/** GraalVMのNative-Imageコンフィグ出力先ディレクトリ. **/
		this.nativeImgDir = nativeImgDir;
		/** classPath内のリソースファイルをResourceItemに含めるフラグ. **/
		this.resourceItemFlag = resourceItemFlag;
	}
	
	/**
	 * バージョンを表示.
	 */
	protected static final void outVersion() {
		System.out.println(QuinaCTConstants.VERSION);
	}
	
	/**
	 * ヘルプ情報を表示.
	 */
	protected static final void outHelp() {
		System.out.println("This command extracts the Route specification component.");
		System.out.println();
		System.out.println("With this command, Extracts the component specified by Route");
		System.out.println("                   from the specified class directory and");
		System.out.println("                   creates an automatic loading program.");
		System.out.println();
		System.out.println("Usage: " + QuinaCTConstants.COMMAND_NAME + " [options]");
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
	
	/**
	 * パラメータを取得.
	 * @param args コマンドパラメータを設定します.
	 * @return QuinaCTCmdParams QuinaCompileTool用コマンドパラメータが返却されます.
	 */
	protected static final QuinaCTCmdParams getParams(Args args) {
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
			return null;
		// javaソースディレクトリが存在しない場合.
		} else if(!new File(javaSourceDir).isDirectory()) {
			System.err.println("[ERROR] The specified Java source directory \"" +
				javaSourceDir + "\" is not a directory. ");
			return null;
		}
		
		// javaソースディレクトリを整頓.
		javaSourceDir = AnnotationUtil.slashPath(javaSourceDir);
		
		// Cdiで出力されるファイル群を全削除して処理終了するか取得.
		boolean deleteOutFileOnlyFlag = args.isValue("-d", "--delete");
		
		// classディレクトリを取得.
		String clazzDir = args.get("-c", "--class");
		// classディレクトリが存在しない場合.
		if(clazzDir != null && !new File(clazzDir).isDirectory()) {
			// ただしdeleteOutFileOnlyFlagがtrueの場合はエラーにしない.
			if(!deleteOutFileOnlyFlag) {
				System.err.println("[ERROR] The specified class directory \"" +
					clazzDir + "\" is not a directory. ");
				return null;
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
				return null;
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
			nativeImgDir = QuinaCTConstants.DEF_NATIVE_CONFIG_DIR;
		}
		
		// nativeImgDirディレクトリを整頓.
		nativeImgDir = AnnotationUtil.slashPath(nativeImgDir);
		
		// classPath内のリソースファイルをResourceItemに含めるか取得.
		boolean resourceItemFlag = args.isValue("-r", "--resource");
		
		// 返却内容を生成して返却.
		return new QuinaCTCmdParams(
			// 詳細表示フラグ.
			verboseFlag,
			// Cdiで出力されるファイル群を全削除して処理終了するフラグ.
			deleteOutFileOnlyFlag,
			// javaソースディレクトリ.
			javaSourceDir,
			// classディレクトリ.
			clazzDir,
			// jarディレクトリリスト.
			jarDirArray,
			// GraalVMのNative-Imageコンフィグ出力先ディレクトリ.
			nativeImgDir,
			// classPath内のリソースファイルをResourceItemに含めるフラグ.
			resourceItemFlag);
	}
	
	@Override
	public final String toString() {
		try {
			StringBuilder buf = new StringBuilder();
			buf.append(" target outputPath    : ")
				.append(javaSourceDir == null ? "" : FileUtil.getFullPath(javaSourceDir))
				.append("\n");
			buf.append(" target classPath     : ")
				.append((clazzDir == null ? "" : FileUtil.getFullPath(clazzDir)))
				.append("\n");
			buf.append(" target jarPath       : ");
			if(jarDirArray.length > 0) {
				buf.append(FileUtil.getFullPath(jarDirArray[0])).append("\n");
				final int len = jarDirArray == null ? 0 : jarDirArray.length;
				for(int i = 1; i < len; i ++) {
					buf.append("                   : ")
						.append(FileUtil.getFullPath(jarDirArray[i]))
						.append("\n");
				}
			} else {
				buf.append("\n");
			}
			buf.append(" target outNativeConf : ")
				.append(FileUtil.getFullPath(nativeImgDir))
				.append("\n");
			return buf.toString();
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}

}
