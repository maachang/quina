package quinax;

import quina.Quina;
import quina.CdiReflectManager;
import quina.annotation.cdi.CdiReflectElement;

/**
 * Reads the CDI reflection information.
 */
public final class LoadCdiReflect {
	private LoadCdiReflect() {}
	
	/**
	 * Reads the CDI reflection information.
	 *
	 * @exception Exception If the cdi reflect registration fails.
	 */
	public static final void load() throws Exception {
		
		// Get the Cdi Reflect to be registered.
		final CdiReflectManager refManager = Quina.get().getCdiReflectManager();
		
		Object o = null;
		Class<?> cls = null;
		CdiReflectElement element = null;
		
		// Register the field group of the target class
		// "quina.test.service.GreetingService"
		o = new quina.test.service.GreetingService();
		element = refManager.register(o);
		if(element != null) {
			cls = o.getClass();
			element.add(cls.getDeclaredField("uppercaseService"));
			cls = null; element = null;
		}
		o = null;
		
		// Register the field group of the target class
		// "quina.test.route.GreetingJsonGet"
		o = new quina.test.route.GreetingJsonGet();
		element = refManager.register(o);
		if(element != null) {
			cls = o.getClass();
			element.add(cls.getDeclaredField("log"));
			element.add(cls.getDeclaredField("service"));
			cls = null; element = null;
		}
		o = null;
	}
}