package quina.jdbc.nativeimage;

import quina.annotation.nativeimage.NativeBuildStep;
import quina.annotation.nativeimage.NativeConfigScoped;
import quina.nativeimage.ReflectionItem;

/**
 * H2用のGraalVM用Native-Imageコンフィグ定義.
 */
@NativeConfigScoped("org.h2.Driver")
public class H2NativeConfig {
	/**
	 * リフレクション定義.
	 */
	@NativeBuildStep
	public void reflectionConfig() {
		ReflectionItem.get()
		.addItem("org.h2.Driver",true);
	}
}
