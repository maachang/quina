package quina.compile.tool.gdi;

import java.io.IOException;

import quina.compile.tool.QuinaCTConstants;
import quina.util.FileUtil;

/**
 * Cdiで作成するファイル及びディレクトリを削除する処理群.
 */
public final class CdiRemoveFileOrDir {
	private CdiRemoveFileOrDir() {}
	
	/**
	 * Cdiが生成する内容を全削除.
	 * @param outSourceDirectory JavaSource出力先ディレクトリを設定します.
	 * @param nativeImgDir NativeConfig出力先ディレクトリを設定します.
	 */
	public static final void removeOutCdi(
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
		String[] javaSrcs = QuinaCTConstants.OUTPUT_SOURCE_ARRAY;
		String outDir = outSourceDirectory + "/" + QuinaCTConstants.CDI_DIRECTORY_NAME + "/";
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
				outSourceDirectory + "/" + QuinaCTConstants.CDI_PROXY_DIRECTORY_NAME);
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
