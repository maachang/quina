package quina.jdbc.nativeimage;

import quina.annotation.nativeimage.NativeBuildStep;
import quina.annotation.nativeimage.NativeConfigScoped;
import quina.nativeimage.InitializeBuildItem;
import quina.nativeimage.ReflectionItem;
import quina.nativeimage.ResourceItem;

/**
 * MsSql用のGraalVM用Native-Imageコンフィグ定義.
 */
@NativeConfigScoped("com.microsoft.sqlserver.jdbc.SQLServerDriver")
public class MsSqlNativeConfig {
	/**
	 * リフレクション定義.
	 */
	@NativeBuildStep
	public void reflectionConfig() {
		ReflectionItem.get()
		.addItem("com.microsoft.sqlserver.jdbc.SQLServerDriver",true);
	}

	/**
	 * リソース定義.
	 */
	@NativeBuildStep
	public void resourceConfig() {
		ResourceItem.get()
			.addBundleItem("com.microsoft.sqlserver.jdbc.SQLServerResource");
	}
	
	/**
	 * ビルド時に初期化実行.
	 */
	@NativeBuildStep
	public void InitializeBuildConfig() {
		InitializeBuildItem.get()
			.addItem("com.microsoft.sqlserver.jdbc.KerbAuthentication");
	}
}