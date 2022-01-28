package quina.jdbc.console.build;

import quina.compile.tool.graalvm.annotation.NativeBuildStep;
import quina.compile.tool.graalvm.annotation.NativeConfigScoped;
import quina.compile.tool.graalvm.nativeimage.ResourceItem;

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
