package quinax;

import java.lang.reflect.Field;

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
		
		Class<?> cls = null;
		Field field = null;
		boolean staticFlag = false;
		CdiReflectElement element = null;
		
		// Register the field group of the target class
		// "quina.test.service.GreetingService"
		cls = quina.test.service.GreetingService.class;
		element = refManager.register(cls);
		if(element != null) {
			
			field = cls.getDeclaredField("uppercaseService");
			staticFlag = false;
			element.add(staticFlag, field);
			
			field = cls.getDeclaredField("log");
			staticFlag = false;
			element.add(staticFlag, field);
			cls = null; element = null;
		}
		
		// Register the field group of the target class
		// "quina.test.QuinaTest"
		cls = quina.test.QuinaTest.class;
		element = refManager.register(cls);
		if(element != null) {
			
			field = cls.getDeclaredField("log");
			staticFlag = true;
			element.add(staticFlag, field);
			cls = null; element = null;
		}
		
		// Register the field group of the target class
		// "quina.test.route.GreetingJsonGet"
		cls = quina.test.route.GreetingJsonGet.class;
		element = refManager.register(cls);
		if(element != null) {
			
			field = cls.getDeclaredField("log");
			staticFlag = false;
			element.add(staticFlag, field);
			
			field = cls.getDeclaredField("service");
			staticFlag = false;
			element.add(staticFlag, field);
			cls = null; element = null;
		}
	}
}
