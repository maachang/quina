package quina.jdbc.build;

import quina.annotation.nativeimage.NativeBuildStep;
import quina.annotation.nativeimage.NativeConfigScoped;
import quina.nativeimage.ReflectionItem;

/**
 * PostgresSql用のGraalVM用Native-Imageコンフィグ定義.
 */
@NativeConfigScoped("org.postgresql.Driver")
public class PostgresSqlNativeConfig {
	
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
		.addItem("org.postgresql.Driver",true);
	}
}
