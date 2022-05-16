package quina.compile;

import java.io.File;
import java.util.List;

import quina.compile.cdi.CdiOutputJavaProxySrc;
import quina.compile.cdi.CdiOutputJavaSrc;
import quina.compile.cdi.CdiRemoveFileOrDir;
import quina.compile.graalvm.GraalvmAppendResourceItem;
import quina.compile.graalvm.GraalvmOutNativeConfig;
import quina.exception.QuinaException;
import quina.util.Args;

/**
 * QuinaCompileTool.
 * 
 * Quinaで提供されているAnnotationやGraalvmのNativeImageに
 * 対するコンパイルツールです.
 * 
 * 対象プロジェクトをコンパイルし、その後この処理を実施する
 * ことで、必要なJavaソースファイルを自動生成し、Graalvmの
 * NativeImage実行に対するコンフィグ定義も合わせて出力します.
 */
public class QuinaCT {
	
	/**
	 * メイン処理.
	 * @param args
	 * @throws Exception
	 */
	public static final void main(String[] args) throws Exception {
		QuinaCT cmd = new QuinaCT(args);
		try {
			cmd.executeCmd();
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
	protected QuinaCT(String[] args) {
		this.args = new Args(args);
	}
	
	// コマンド実行.
	protected void executeCmd() throws Exception {
		// バージョンを表示.
		if(args.isValue("-v", "--version")) {
			QuinaCTCmdParams.outVersion();
			System.exit(0);
			return;
		// ヘルプ内容を表示.
		} else if(args.isValue("-h", "--help")) {
			QuinaCTCmdParams.outHelp();
			System.exit(0);
			return;
		}
		
		// コマンドパラメータを取得.
		QuinaCTCmdParams cmdParams = QuinaCTCmdParams.getParams(args);
		if(cmdParams == null) {
			// 正しくコマンドパラメータが取得できない場合.
			System.exit(1);
			return;
		}
		
		// 実行に対するコンソール表示処理.
		outConsole(cmdParams);
		
		// 実行処理.
		execute(cmdParams);
	}
	

	
	// パラメータ内容をコンソール出力.
	protected final void outConsole(QuinaCTCmdParams cmd)
		throws Exception {
		// 処理開始.
		System.out.println("start " + this.getClass().getSimpleName() +
			" version: " + QuinaCTConstants.VERSION);
		System.out.println();
		
		// コマンドパラメータ内容を出力.
		System.out.println(cmd);
		System.out.println();

	}

	
	// 実行処理.
	protected final void execute(QuinaCTCmdParams cmdPms) {
		
		// 処理開始.
		long time = System.currentTimeMillis();
		
		// Cdiで出力するファイル内容を削除して終了する場合.
		if(cmdPms.deleteOutFileOnlyFlag) {
			// ファイルを削除.
			CdiRemoveFileOrDir.removeOutCdi(cmdPms.javaSourceDir, cmdPms.nativeImgDir);
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
			
			// params.
			QuinaCTParams params = new QuinaCTParams(
				cmdPms.clazzDir, cmdPms.verboseFlag, cmdPms.resourceItemFlag,
				cmdPms.jarFileArray);
			
			// クラス一覧を取得.
			List<String> clazzList = QuinaCTUtil.findClassList(
				params, cmdPms.clazzDir, cmdPms.jarFileArray);
			
			// ClassDirから、対象となるクラスを抽出.
			QuinaCTExtraction.extraction(params, clazzList);
			clazzList = null;
			
			// 出力先のソースコードを全削除.
			CdiRemoveFileOrDir.removeOutAutoJavaSource(cmdPms.javaSourceDir);
			
			// 最初にリソースファイルをResourceItemにセット.
			GraalvmAppendResourceItem.append(params);
			
			// GraalVM用のNativeImageコンフィグ群を出力.
			GraalvmOutNativeConfig.output(cmdPms.nativeImgDir, null);
			
			// 後処理.
			resultExit(cmdPms, params, time);
		} catch(QuinaException qe) {
			errorRemove(cmdPms.javaSourceDir);
			throw qe;
		} catch(Exception e) {
			errorRemove(cmdPms.javaSourceDir);
			throw new QuinaException(e);
		}
	}
	
	// 後処理.
	protected void resultExit(QuinaCTCmdParams cmdPms, QuinaCTParams params, long time)
		throws Exception {
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
		//CdiOutputJavaSrcByAutoProxy.proxyScoped(cmdPms.javaSourceDir, params);
		CdiOutputJavaProxySrc.proxyScoped(cmdPms.javaSourceDir, params);
		
		// [Router]ファイル出力.
		if(!params.isRouteEmpty()) {
			CdiOutputJavaSrc.routerScoped(cmdPms.javaSourceDir, params);
			System.out.println( " routerScoped         : " +
				new File(cmdPms.javaSourceDir).getCanonicalPath() +
				"/" + QuinaCTConstants.CDI_DIRECTORY_NAME + "/" +
					QuinaCTConstants.AUTO_ROUTE_SOURCE_NAME);
		}
		
		// [(CDI)ServiceScoped]ファイル出力.
		if(!params.isCdiEmpty()) {
			CdiOutputJavaSrc.serviceScoped(cmdPms.javaSourceDir, params);
			System.out.println( " serviceScoped        : " +
				new File(cmdPms.javaSourceDir).getCanonicalPath() +
				"/" + QuinaCTConstants.CDI_DIRECTORY_NAME + "/" +
					QuinaCTConstants.CDI_SERVICE_SOURCE_NAME);
		}
		
		// [QuinaService]ファイル出力.
		if(!params.isQuinaServiceEmpty()) {
			CdiOutputJavaSrc.quinaServiceScoped(cmdPms.javaSourceDir, params);
			System.out.println( " quinaServiceScoped   : " +
				new File(cmdPms.javaSourceDir).getCanonicalPath() +
				"/" + QuinaCTConstants.CDI_DIRECTORY_NAME + "/" +
					QuinaCTConstants.QUINA_SERVICE_SOURCE_NAME);
		}
		
		// [CdiInjectField]ファイル出力.
		if(!params.isCdiInjectFieldEmpty()) {
			CdiOutputJavaSrc.cdiInjectField(cmdPms.javaSourceDir, params);
			System.out.println( " cdiInjectField           : " +
				new File(cmdPms.javaSourceDir).getCanonicalPath() +
				"/" + QuinaCTConstants.CDI_DIRECTORY_NAME + "/" +
					QuinaCTConstants.CDI_INJECT_FIELD_SOURCE_NAME);
		}
		
		// [CdiHandle]ファイル出力.
		if(!params.isCdiHandleEmpty()) {
			CdiOutputJavaSrc.cdiHandle(cmdPms.javaSourceDir, params);
			System.out.println( " cdiHandle            : " +
				new File(cmdPms.javaSourceDir).getCanonicalPath() +
				"/" + QuinaCTConstants.CDI_DIRECTORY_NAME + "/" +
					QuinaCTConstants.CDI_SERVICE_SOURCE_NAME);
		}
		
		// [ProxyScoped]ファイル出力.
		if(!params.isProxyScopedEmpty()) {
			CdiOutputJavaSrc.proxyScoped(cmdPms.javaSourceDir, params);
			System.out.println( " proxyScoped          : " +
				new File(cmdPms.javaSourceDir).getCanonicalPath() +
				"/" + QuinaCTConstants.CDI_DIRECTORY_NAME + "/" +
					QuinaCTConstants.CDI_PROXY_SCOPED_SOURCE_NAME);
		}
		
		// [QuinaLoopScoped]ファイル出力.
		if(!params.isQuinaLoopEmpty()) {
			CdiOutputJavaSrc.quinaLoopScoped(cmdPms.javaSourceDir, params);
			System.out.println( " quinaLoopScoped      : " +
				new File(cmdPms.javaSourceDir).getCanonicalPath() +
				"/" + QuinaCTConstants.CDI_DIRECTORY_NAME + "/" +
					QuinaCTConstants.QUINA_LOOP_SCOPED_SOURCE_NAME);
		}
		
		
		time = System.currentTimeMillis() - time;
		System.out.println();
		System.out.println("success: " + time + " msec");
		System.out.println();
		
		System.exit(0);
	}
	
	// エラーハンドル処理.
	protected static final void errorRemove(String javaSourceDir) {
		if(javaSourceDir == null || javaSourceDir.isEmpty()) {
			return;
		}
		// エラーが発生した場合は、生成されるGCi情報を破棄する.
		try {
			// 
			CdiRemoveFileOrDir.removeOutAutoJavaSource(javaSourceDir);
		} catch(Exception e) {}
		try {
			// 
			CdiRemoveFileOrDir.removeProxyDirectory(javaSourceDir);
		} catch(Exception e) {}
	}
}
