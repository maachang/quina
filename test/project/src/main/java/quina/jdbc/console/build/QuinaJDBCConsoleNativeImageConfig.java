package quina.jdbc.console.build;

import quina.compile.graalvm.item.ResourceItem;
import quina.compile.graalvm.item.annotation.NativeBuildStep;
import quina.compile.graalvm.item.annotation.NativeConfigScoped;

/**
 * QuinaJDBCコンソールのリソースに対するNativeImage対応.
 */
@NativeConfigScoped()
public class QuinaJDBCConsoleNativeImageConfig {
	
	/**
	 * リソース定義.
	 */
	@NativeBuildStep
	public void resourceConfig() {
		ResourceItem.get()
			.addIncludeItem("^quina/jdbc/console/resource/.*")
			;
	}
}
