package quina.jdbc.build;

import quina.annotation.nativeimage.NativeBuildStep;
import quina.annotation.nativeimage.NativeConfigScoped;
import quina.nativeimage.ReflectionItem;

/**
 * MariaDb用のGraalVM用Native-Imageコンフィグ定義.
 */
@NativeConfigScoped("org.mariadb.jdbc.Driver")
public class MariaDbNativeConfig {
	
	/**
	 * この定義が正常に動作するバージョン.
	 */
	public static final String VERSION = "2.7.4";
	
	/**
	 * リフレクション定義.
	 */
	@NativeBuildStep
	public void reflectionConfig() {
		ReflectionItem.get()
		.addItem("org.mariadb.jdbc.Driver",true)
		.addItem("org.mariadb.jdbc.util.Options",true, false, true)
		;
	}

}
