package quina.command.generateCdi;

import static quina.command.generateCdi.GCdiConstants.CDI_DIRECTORY_NAME;
import static quina.command.generateCdi.GCdiConstants.OUTPUT_SOURCE_ARRAY;
import static quina.command.generateCdi.GCdiConstants.PROXY_DIRECTORY_NAME;

import java.io.IOException;

import quina.util.FileUtil;

/**
 * GCdiが作成するファイル及びディレクトリを削除する
 * 処理群.
 */
public final class GCdiRemoveFileOrDir {
	private GCdiRemoveFileOrDir() {}
	
	/**
	 * GCdiが生成する内容を全削除.
	 * @param outSourceDirectory JavaSource出力先ディレクトリを設定します.
	 * @param nativeImgDir NativeConfig出力先ディレクトリを設定します.
	 */
	public static final void removeOutGCdi(
		String outSourceDirectory, String nativeImgDir) {
		removeOutAutoJavaSource(outSourceDirectory);
		removeProxyDirectory(outSourceDirectory);
		removeNativeConfigDirectory(nativeImgDir);
	}
	
	/**
	 * 自動出力対象のファイルを削除.
	 * @param outSourceDirectory JavaSource出力先ディレクトリを設定します.
	 * @throws IOException I/O例外.
	 */
	public static final void removeOutAutoJavaSource(
		String outSourceDirectory) {
		String[] javaSrcs = OUTPUT_SOURCE_ARRAY;
		String outDir = outSourceDirectory + "/" + CDI_DIRECTORY_NAME + "/";
		int len = javaSrcs.length;
		for(int i = 0; i < len; i ++) {
			try {
				FileUtil.removeFile(outDir + javaSrcs[i]);
			} catch(Exception e) {}
		}
	}
	
	/**
	 * 出力先のProxyパッケージのファイルとフォルダを削除処理.
	 * @param outSourceDirectory JavaSource出力先ディレクトリを設定します.
	 */
	public static final void removeProxyDirectory(
		String outSourceDirectory) {
		try {
			FileUtil.delete(
				outSourceDirectory + "/" +PROXY_DIRECTORY_NAME);
		} catch(Exception e) {
		}
	}
	
	/**
	 * NativeImage用のコンフィグ情報出力先フォルダを削除処理.
	 * @param nativeImgDir NativeConfig出力先ディレクトリを設定します.
	 */
	public static final void removeNativeConfigDirectory(
		String nativeImgDir) {
		try {
			FileUtil.delete(nativeImgDir);
		} catch(Exception e) {
		}
	}
}
