package quina.jdbc.nativeimage;

import quina.annotation.nativeimage.NativeBuildStep;
import quina.annotation.nativeimage.NativeConfigScoped;
import quina.nativeimage.InitializeBuildItem;
import quina.nativeimage.ReflectionItem;

/**
 * Oracle用のGraalVM用Native-Imageコンフィグ定義.
 */
@NativeConfigScoped("oracle.jdbc.driver.OracleDriver")
public class OracleNativeConfig {
	/**
	 * リフレクション定義.
	 */
	@NativeBuildStep
	public void reflectionConfig() {
		ReflectionItem.get()
		.addItem("oracle.jdbc.driver.OracleDriver",true);
	}
	
	/**
	 * ビルド時に初期化実行.
	 */
	@NativeBuildStep
	public void InitializeBuildConfig() {
		InitializeBuildItem.get()
		.addItem("oracle.jdbc.driver.OracleDriver")
		.addItem("oracle.jdbc.driver.SQLUtil$XMLFactory")
		.addItem("oracle.jdbc.driver.NamedTypeAccessor$XMLFactory")
		.addItem("oracle.jdbc.driver.OracleTimeoutThreadPerVM")
		.addItem("oracle.jdbc.driver.BlockSource$ThreadedCachingBlockSource")
		.addItem("oracle.jdbc.driver.T4CTTIoauthenticate")
		.addItem("oracle.net.nt.TcpMultiplexer$LazyHolder")
		.addItem("oracle.security.o5logon.O5Logon")
		.addItem("oracle.jdbc.driver.BlockSource$ThreadedCachingBlockSource$BlockReleaser")
		.addItem("oracle.net.nt.TimeoutInterruptHandler")
		.addItem("oracle.net.nt.Clock")
		.addItem("oracle.jdbc.driver.NoSupportHAManager")
		.addItem("oracle.jdbc.driver.LogicalConnection")
		;
	}
}