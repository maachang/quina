package quina.annotation.proxy;

/**
 * ProxyScoped定義.
 */
public class AnnotationProxyScopedConstants {
	private AnnotationProxyScopedConstants() {}
	
	/** 自動生成するProxyソース出力先パッケージ名. **/
	public static final String OUTPUT_AUTO_SOURCE_PROXY_PACKAGE_NAME =
		"quinax.proxy";
	
	/** 自動生成されるProxyクラスのヘッダ名. **/
	public static final String HEAD_PROXY_CLASS_NAME = "AutoProxy";

	/** 初期設定を行うメソッド名. **/
	public static final String INITIAL_SETTING_METHOD = "__initialSetting";
}
