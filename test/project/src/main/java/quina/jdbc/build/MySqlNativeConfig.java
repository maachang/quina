package quina.jdbc.build;

import quina.annotation.nativeimage.NativeBuildStep;
import quina.annotation.nativeimage.NativeConfigScoped;
import quina.nativeimage.ReflectionItem;
import quina.nativeimage.ResourceItem;

/**
 * MySql用のGraalVM用Native-Imageコンフィグ定義.
 */
@NativeConfigScoped(
	{"com.mysql.cj.jdbc.Driver", "com.mysql.jdbc.Driver"})
public class MySqlNativeConfig {
	
	/**
	 * この定義が正常に動作するバージョン.
	 */
	public static final String SUCCESS_VERSION = "8.0.27";
	
	/**
	 * リフレクション定義.
	 */
	@NativeBuildStep
	public void reflectionConfig() {
		ReflectionItem.get()
		.addItem("com.mysql.cj.conf.url.FailoverConnectionUrl", true)
		.addItem("com.mysql.cj.conf.url.FailoverDnsSrvConnectionUrl", true)
		.addItem("com.mysql.cj.conf.url.LoadBalanceConnectionUrl", true)
		.addItem("com.mysql.cj.conf.url.LoadBalanceDnsSrvConnectionUrl", true)
		.addItem("com.mysql.cj.conf.url.ReplicationConnectionUrl", true)
		.addItem("com.mysql.cj.conf.url.ReplicationDnsSrvConnectionUrl", true)
		.addItem("com.mysql.cj.conf.url.SingleConnectionUrl", true)
		.addItem("com.mysql.cj.conf.url.XDevApiConnectionUrl", true)
		.addItem("com.mysql.cj.conf.url.XDevApiDnsSrvConnectionUrl", true)
		.addItem("com.mysql.cj.exceptions.AssertionFailedException", true)
		.addItem("com.mysql.cj.exceptions.CJCommunicationsException", true)
		.addItem("com.mysql.cj.exceptions.CJConnectionFeatureNotAvailableException", true)
		.addItem("com.mysql.cj.exceptions.CJException", true)
		.addItem("com.mysql.cj.exceptions.CJOperationNotSupportedException", true)
		.addItem("com.mysql.cj.exceptions.CJPacketTooBigException", true)
		.addItem("com.mysql.cj.exceptions.CJTimeoutException", true)
		.addItem("com.mysql.cj.exceptions.ClosedOnExpiredPasswordException", true)
		.addItem("com.mysql.cj.exceptions.ConnectionIsClosedException", true)
		.addItem("com.mysql.cj.exceptions.DataConversionException", true)
		.addItem("com.mysql.cj.exceptions.DataReadException", true)
		.addItem("com.mysql.cj.exceptions.DataTruncationException", true)
		.addItem("com.mysql.cj.exceptions.DeadlockTimeoutRollbackMarker", true)
		.addItem("com.mysql.cj.exceptions.FeatureNotAvailableException", true)
		.addItem("com.mysql.cj.exceptions.InvalidConnectionAttributeException", true)
		.addItem("com.mysql.cj.exceptions.NumberOutOfRange", true)
		.addItem("com.mysql.cj.exceptions.OperationCancelledException", true)
		.addItem("com.mysql.cj.exceptions.PasswordExpiredException", true)
		.addItem("com.mysql.cj.exceptions.PropertyNotModifiableException", true)
		.addItem("com.mysql.cj.exceptions.RSAException", true)
		.addItem("com.mysql.cj.exceptions.SSLParamsException", true)
		.addItem("com.mysql.cj.exceptions.StatementIsClosedException", true)
		.addItem("com.mysql.cj.exceptions.StreamingNotifiable", true)
		.addItem("com.mysql.cj.exceptions.UnableToConnectException", true)
		.addItem("com.mysql.cj.exceptions.UnsupportedConnectionStringException", true)
		.addItem("com.mysql.cj.exceptions.WrongArgumentException", true)
		.addItem("com.mysql.cj.jdbc.Driver", true)
		.addItem("com.mysql.cj.jdbc.ha.NdbLoadBalanceExceptionChecker", true)
		.addItem("com.mysql.cj.jdbc.ha.StandardLoadBalanceExceptionChecker", true)
		.addItem("com.mysql.cj.log.StandardLogger", true)
		.addItem("com.mysql.cj.protocol.NamedPipeSocketFactory", true)
		.addItem("com.mysql.cj.protocol.SocksProxySocketFactory", true)
		.addItem("com.mysql.cj.protocol.StandardSocketFactory", true)
		;
	}
	
	/**
	 * リソース定義.
	 */
	@NativeBuildStep
	public void resourceConfig() {
		ResourceItem.get()
			.addBundleItem("com.mysql.cj.LocalizedErrorMessages")
			.addBundleItem("com.mysql.cj.TlsSettings")
			;
	}
}
