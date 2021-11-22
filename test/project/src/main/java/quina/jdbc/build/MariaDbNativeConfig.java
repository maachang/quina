package quina.jdbc.build;

import quina.annotation.nativeimage.NativeBuildStep;
import quina.annotation.nativeimage.NativeConfigScoped;
import quina.nativeimage.InitializeBuildItem;
import quina.nativeimage.ReflectionItem;

/**
 * MariaDb用のGraalVM用Native-Imageコンフィグ定義.
 */
@NativeConfigScoped("org.mariadb.jdbc.Driver")
public class MariaDbNativeConfig {
	
	/**
	 * この定義が正常に動作するバージョン.
	 * 未検証.
	 */
	public static final String VERSION = "none";
	
	/**
	 * リフレクション定義.
	 */
	@NativeBuildStep
	public void reflectionConfig() {
		ReflectionItem.get()
		.addItem("org.mariadb.jdbc.Driver",true)
		.addItem("org.mariadb.jdbc.util.Options",true);
	}
	
	/**
	 * ビルド時に初期化実行.
	 */
	@NativeBuildStep
	public void InitializeBuildConfig() {
		InitializeBuildItem.get().addItem(
			"org.mariadb.jdbc.internal.failover.impl.MastersReplicasListener");
	}

}
