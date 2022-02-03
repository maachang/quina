package quina.jdbc.build;

import quina.compile.graalvm.item.ReflectionItem;
import quina.compile.graalvm.item.annotation.NativeBuildStep;
import quina.compile.graalvm.item.annotation.NativeConfigScoped;

/**
 * PostgresSql用のGraalVM用Native-Imageコンフィグ定義.
 */
@NativeConfigScoped("org.postgresql.Driver")
public class PostgresSqlNativeConfig {
	
	/**
	 * この定義が正常に動作するバージョン.
	 */
	public static final String VERSION = "42.3.1";
	
	/**
	 * リフレクション定義.
	 */
	@NativeBuildStep
	public void reflectionConfig() {
		ReflectionItem.get()
		.addItem("org.postgresql.Driver",true)
		.addItem("org.postgresql.PGProperty",true, false, true)
		;
	}
}
