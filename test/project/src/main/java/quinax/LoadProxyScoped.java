package quinax;

import quina.Quina;
import quina.annotation.proxy.ProxyScopedManager;

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
		
		// Register the "quina.jdbc.QuinaProxyStatement"
		// object in the @ProxyScoped.
		prxManager.put("quina.jdbc.QuinaProxyStatement", 
			quinax.proxy.AutoProxyQuinaProxyStatement.class);
		
		// Register the "quina.jdbc.QuinaProxyConnection"
		// object in the @ProxyScoped.
		prxManager.put("quina.jdbc.QuinaProxyConnection", 
			quinax.proxy.AutoProxyQuinaProxyConnection.class);
		
		// Register the "quina.jdbc.QuinaProxyCallableStatement"
		// object in the @ProxyScoped.
		prxManager.put("quina.jdbc.QuinaProxyCallableStatement", 
			quinax.proxy.AutoProxyQuinaProxyCallableStatement.class);
		
		// Register the "quina.jdbc.QuinaProxyPreparedStatement"
		// object in the @ProxyScoped.
		prxManager.put("quina.jdbc.QuinaProxyPreparedStatement", 
			quinax.proxy.AutoProxyQuinaProxyPreparedStatement.class);
	}
}
