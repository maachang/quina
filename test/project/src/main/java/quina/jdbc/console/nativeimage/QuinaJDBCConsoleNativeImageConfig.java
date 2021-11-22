package quina.jdbc.console.nativeimage;

import quina.annotation.nativeimage.NativeBuildStep;
import quina.annotation.nativeimage.NativeConfigScoped;
import quina.nativeimage.ResourceItem;

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
