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
	 * この定義が正常に動作するバージョン.
	 */
	public static final String SUCCESS_VERSION = "1.4.199";
	
	/**
	 * リフレクション定義.
	 */
	@NativeBuildStep
	public void reflectionConfig() {
		ReflectionItem.get()
		.addItem("org.h2.Driver", true)
		.addItem("org.h2.store.fs.FilePathDisk", true)
		.addItem("org.h2.store.fs.FilePathMem", true)
		.addItem("org.h2.store.fs.FilePathMemLZF", true)
		.addItem("org.h2.store.fs.FilePathNioMem", true)
		.addItem("org.h2.store.fs.FilePathNioMemLZF", true)
		.addItem("org.h2.store.fs.FilePathSplit", true)
		.addItem("org.h2.store.fs.FilePathNio", true)
		.addItem("org.h2.store.fs.FilePathNioMapped", true)
		.addItem("org.h2.store.fs.FilePathAsync", true)
		.addItem("org.h2.store.fs.FilePathZip", true)
		.addItem("org.h2.store.fs.FilePathRetryOnInterrupt", true)
		.addItem("org.h2.mvstore.db.MVTableEngine", true)
		;
	}
}
