package quinax;

import quina.Quina;
import quina.annotation.cdi.CdiServiceManager;

/**
 * ServiceScoped Annotation Registers the defined service object.
 */
public final class LoadCdiService {
	private LoadCdiService() {}
	
	/**
	 * ServiceScoped Annotation Registers the defined service object.
	 *
	 * @exception Exception If the service registration fails.
	 */
	public static final void load() throws Exception {
		
		// Get the Service Manager to be registered.
		final CdiServiceManager cdiManager = Quina.get().getCdiServiceManager();
		
		// Register the "quina.test.service.UppercaseService"
		// object in the @ServiceScoped.
		cdiManager.put(new quina.test.service.UppercaseService());
		
		// Register the "quina.test.service.GreetingService"
		// object in the @ServiceScoped.
		cdiManager.put(new quina.test.service.GreetingService());
	}
}
