package quinax;

import java.lang.reflect.Field;

import quina.Quina;
import quina.annotation.cdi.CdiReflectManager;
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
		
		// Register the field group of the target class
		// "quina.test.service.GreetingService"
		quina_test_service_GreetingService();
		
		// Register the field group of the target class
		// "quina.test.QuinaTest"
		quina_test_QuinaTest();
		
		// Register the field group of the target class
		// "quina.test.route.GreetingJsonGet"
		quina_test_route_GreetingJsonGet();
	}

	// Register the field group of the target class "quina.test.service.GreetingService"
	private static final void quina_test_service_GreetingService()
		throws Exception {
		CdiReflectManager refManager = Quina.get().getCdiReflectManager();
		Class<?> cls = quina.test.service.GreetingService.class;
		CdiReflectElement element = refManager.register(cls);
		Field field = null;
		boolean staticFlag = false;
		
		field = cls.getDeclaredField("uppercaseService");
		staticFlag = false;
		element.add(staticFlag, field);
	}

	// Register the field group of the target class "quina.test.QuinaTest"
	private static final void quina_test_QuinaTest()
		throws Exception {
		CdiReflectManager refManager = Quina.get().getCdiReflectManager();
		Class<?> cls = quina.test.QuinaTest.class;
		CdiReflectElement element = refManager.register(cls);
		Field field = null;
		boolean staticFlag = false;
		
		field = cls.getDeclaredField("log");
		staticFlag = true;
		element.add(staticFlag, field);
	}

	// Register the field group of the target class "quina.test.route.GreetingJsonGet"
	private static final void quina_test_route_GreetingJsonGet()
		throws Exception {
		CdiReflectManager refManager = Quina.get().getCdiReflectManager();
		Class<?> cls = quina.test.route.GreetingJsonGet.class;
		CdiReflectElement element = refManager.register(cls);
		Field field = null;
		boolean staticFlag = false;
		
		field = cls.getDeclaredField("log");
		staticFlag = false;
		element.add(staticFlag, field);
		
		field = cls.getDeclaredField("service");
		staticFlag = false;
		element.add(staticFlag, field);
	}
}
