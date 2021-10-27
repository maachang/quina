package quina.command.generateCdi;

import quina.annotation.nativeimage.AnnotationNativeImage;
import quina.annotation.nativeimage.ExecuteNativeBuildStep;
import quina.exception.QuinaException;
import quina.nativeimage.InitializeBuildItem;
import quina.nativeimage.InitializeRunItem;
import quina.nativeimage.JniItem;
import quina.nativeimage.NativeImageConfig;
import quina.nativeimage.ProxyItem;
import quina.nativeimage.ReflectionItem;
import quina.nativeimage.ResourceItem;
import quina.util.FileUtil;

/**
 * NativeImage用のコンフィグファイルを生成.
 */
public class NativeImages {
	private NativeImages() {}
	
	/**
	 * NativeConfigScoped定義されていて、クラス指定が
	 * 行われてた場合はそれが、クラスローダ内に
	 * ローディングされていて、その中のNativeBuildStep
	 * アノテーションが定義されてるMethod群を実行します.
	 * @param c 対象のクラスを設定します.
	 * @param params GCdiParamsを設定します.
	 * @return boolean trueの場合実行出来ました.
	 */
	public static final boolean executeExecuteStep(
		Class<?> c, GCdiParams params) {
		ExecuteNativeBuildStep step =
			AnnotationNativeImage.nativeConfigScoped(
				c, params.cl);
		if(step != null) {
			step.execute();
			return true;
		}
		return false;
	}
	
	// 出力リスト.
	private static final NativeImageConfig[] LIST = 
		new NativeImageConfig[] {
			ReflectionItem.get(),     // reflection.
			ResourceItem.get(),       // jar-resource.
			JniItem.get(),            // jni.
			ProxyItem.get(),          // proxy.
			InitializeBuildItem.get(),// InitializeBuildTimeItem.
			InitializeRunItem.get()   // InitializeRunTimeItem.
	};
	
	/**
	 * ファイル情報の出力.
	 * @param directory 出力先フォルダを設定します.
	 * @param charset コンフィグ出力対象の文字コードを設定します.
	 */
	public static final void outputNativeConfig(
		String directory, String charset) {
		// ディレクトリ名が存在しない.
		if(directory == null || directory.isEmpty()) {
			throw new QuinaException(
				"The specified Native Image definition file " +
				"output destination directory has not been set. ");
		}
		// 出力ファイル用の文字コードが存在しない.
		if(charset == null || charset.isEmpty()) {
			charset = "UTF8";
		}
		try {
			// ディレクトリ名が存在しない場合.
			if(!FileUtil.isDir(directory)) {
				// ディレクトリを作成.
				FileUtil.mkdirs(directory);
			}
			// NativeImage用のコンフィグ定義を出力.
			final int len = LIST.length;
			for(int i = 0; i < len; i ++) {
				// ファイル出力.
				FileUtil.setFileString(
					true, directory + "/" + LIST[i].getConfigName(),
					LIST[i].outString(), charset);
				// データクリア.
				LIST[i].clear();
			}
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
}
