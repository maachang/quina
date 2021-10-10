package quinax;

import quina.Quina;
import quina.annotation.reflection.ProxyScopedManager;

/**
 * ProxyScoped Annotation Registers the defined service object.
 */
public final class LoadProxyScoped {
	private LoadProxyScoped() {}
	
	/**
	 * ProxyScoped Annotation Registers the define object.
	 *
	 * @exception Exception If the registration fails.
	 */
	public static final void load() throws Exception {
		
		// Proxy Scoped Manager to be registered.
		final ProxyScopedManager prxManager = Quina.get().getProxyScopedManager();
		
		// Register the "quina.test.proxy.ProxyConnection"
		// object in the @ProxyScoped.
		prxManager.put("quina.test.proxy.ProxyConnection", 
			quinax.proxy.AutoProxyProxyConnection.class);
	}
}
