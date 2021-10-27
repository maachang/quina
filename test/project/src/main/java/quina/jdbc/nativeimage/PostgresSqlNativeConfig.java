package quina.jdbc.nativeimage;

import quina.annotation.nativeimage.NativeBuildStep;
import quina.annotation.nativeimage.NativeConfigScoped;
import quina.nativeimage.ReflectionItem;

/**
 * PostgresSql用のGraalVM用Native-Imageコンフィグ定義.
 */
@NativeConfigScoped("org.postgresql.Driver")
public class PostgresSqlNativeConfig {
	/**
	 * リフレクション定義.
	 */
	@NativeBuildStep
	public void reflectionConfig() {
		ReflectionItem.get()
		.addItem("org.postgresql.Driver",true);
	}
}
