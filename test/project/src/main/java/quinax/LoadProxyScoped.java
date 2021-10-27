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
			new quina.annotation.proxy.QuinaProxy() {
				public Class<?> getProxyClass() {
					return quinax.proxy.AutoProxyQuinaProxyStatement.class;
				}
				public Object newInstance(
					quina.annotation.proxy.ProxySettingArgs args) {
					quinax.proxy.AutoProxyQuinaProxyStatement ret = new
						quinax.proxy.AutoProxyQuinaProxyStatement();
					ret.__initialSetting(args);
					return ret;
				}
			}
		);
		
		// Register the "quina.jdbc.QuinaProxyResultSet"
		// object in the @ProxyScoped.
		prxManager.put("quina.jdbc.QuinaProxyResultSet",
			new quina.annotation.proxy.QuinaProxy() {
				public Class<?> getProxyClass() {
					return quinax.proxy.AutoProxyQuinaProxyStatement.class;
				}
				public Object newInstance(
					quina.annotation.proxy.ProxySettingArgs args) {
					quinax.proxy.AutoProxyQuinaProxyResultSet ret = new
						quinax.proxy.AutoProxyQuinaProxyResultSet();
					ret.__initialSetting(args);
					return ret;
				}
			}
		);
		
		// Register the "quina.jdbc.QuinaProxyConnection"
		// object in the @ProxyScoped.
		prxManager.put("quina.jdbc.QuinaProxyConnection",
			new quina.annotation.proxy.QuinaProxy() {
				public Class<?> getProxyClass() {
					return quinax.proxy.AutoProxyQuinaProxyStatement.class;
				}
				public Object newInstance(
					quina.annotation.proxy.ProxySettingArgs args) {
					quinax.proxy.AutoProxyQuinaProxyConnection ret = new
						quinax.proxy.AutoProxyQuinaProxyConnection();
					ret.__initialSetting(args);
					return ret;
				}
			}
		);
		
		// Register the "quina.jdbc.QuinaProxyCallableStatement"
		// object in the @ProxyScoped.
		prxManager.put("quina.jdbc.QuinaProxyCallableStatement",
			new quina.annotation.proxy.QuinaProxy() {
				public Class<?> getProxyClass() {
					return quinax.proxy.AutoProxyQuinaProxyStatement.class;
				}
				public Object newInstance(
					quina.annotation.proxy.ProxySettingArgs args) {
					quinax.proxy.AutoProxyQuinaProxyCallableStatement ret = new
						quinax.proxy.AutoProxyQuinaProxyCallableStatement();
					ret.__initialSetting(args);
					return ret;
				}
			}
		);
		
		// Register the "quina.jdbc.QuinaProxyPreparedStatement"
		// object in the @ProxyScoped.
		prxManager.put("quina.jdbc.QuinaProxyPreparedStatement",
			new quina.annotation.proxy.QuinaProxy() {
				public Class<?> getProxyClass() {
					return quinax.proxy.AutoProxyQuinaProxyStatement.class;
				}
				public Object newInstance(
					quina.annotation.proxy.ProxySettingArgs args) {
					quinax.proxy.AutoProxyQuinaProxyPreparedStatement ret = new
						quinax.proxy.AutoProxyQuinaProxyPreparedStatement();
					ret.__initialSetting(args);
					return ret;
				}
			}
		);
	}
}
