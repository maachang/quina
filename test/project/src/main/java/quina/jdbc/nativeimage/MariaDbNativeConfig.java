package quina.jdbc.nativeimage;

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
