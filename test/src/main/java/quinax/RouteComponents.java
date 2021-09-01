package quinax;

import quina.Quina;
import quina.Router;
import quina.component.Component;

/**
 * @Route Registers the configured Component group as a Router.
 */
public final class RouteComponents {
	private RouteComponents() {}
	
	/**
	 * @Route Performs Router registration processing for the set Component group.
	 */
	public static final void load() throws Exception {
		
		// Get the Router to be registered.
		final Router router = Quina.get().getRouter();
		
		// Register the "quina.test.route.JsonGet" component in the Router. 
		router.route((Component)(Class.forName("quina.test.route.JsonGet")
			.getDeclaredConstructor().newInstance()
		));
	}
}
