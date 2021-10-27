package quina.annotation.proxy;

/**
 * QuinaProxyオブジェクト.
 */
public interface QuinaProxy {
	/**
	 * Proxyクラスオブジェクトを取得.
	 * @return Class<?> Proxyクラスオブジェクトが返却されます.
	 */
	public Class<?> getProxyClass();
	
	/**
	 * Proxyクラスオブジェクトを生成.
	 * @param args 初期設定値を設定します.
	 * @return Object Proxyクラスオブジェクトが返却されます.
	 */
	public Object newInstance(ProxySettingArgs args);
}
