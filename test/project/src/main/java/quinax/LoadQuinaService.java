package quinax;

import quina.Quina;
import quina.QuinaServiceManager;

/**
 * QuinaServiceScoped Annotation Registers the defined service object.
 */
public final class LoadQuinaService {
	private LoadQuinaService() {}
	
	/**
	 * QuinaServiceScoped Annotation Registers the defined service object.
	 *
	 * @exception Exception If the service registration fails.
	 */
	public static final void load() throws Exception {
		
		// Get the Quina Service Manager to be registered.
		final QuinaServiceManager qsrvManager = Quina.get().getQuinaServiceManager();
		
		// Register the "quina.test.QuinaServiceTest"
		// object in the @QuinaServiceScoped.
		qsrvManager.put(new quina.test.QuinaServiceTest());
	}
}
