package quinax;

import quina.Quina;
import quina.Router;

/**
 * Route Annotation Registers the configured Component group as a Router.
 */
public final class RouteComponents {
	private RouteComponents() {}
	
	/**
	 * Route Annotation Performs Router registration processing for the
	 * set Component group.
	 *
	 * @exception Exception When Router registration fails.
	 */
	public static final void load() throws Exception {
		
		// Get the Router to be registered.
		final Router router = Quina.get().getRouter();
		
		// Register the "quina.test.route.JsonGet" component in the Router.
		router.route(new quina.test.route.JsonGet());
		
		// Register the "quina.test.route.AnyJsonGet" component in the Any.
		router.any(new quina.test.route.AnyJsonGet());
	}
}
