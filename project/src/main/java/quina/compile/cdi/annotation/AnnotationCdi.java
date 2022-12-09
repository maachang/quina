package quina.compile.cdi.annotation;

import java.lang.reflect.Field;

import quina.Quina;
import quina.exception.QuinaException;

/**
 * Cdi(Contexts and Dependency Injection)に対する
 * アノテーション処理を行います.
 */
public class AnnotationCdi {
	private AnnotationCdi() {}
	
	/**
	 * Annotationに定義されてるServiceScopeが定義されてるか取得.
	 * @param c オブジェクトを設定します.
	 * @return boolean ServiceScopeが定義されてる場合 true が返却されます.
	 */
	public static final boolean loadServiceScoped(Object c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		return loadServiceScoped(c.getClass());
	}
	
	/**
	 * Annotationに定義されてるServiceScopeが定義されてるか取得.
	 * @param クラスを設定します.
	 * @return boolean ServiceScopeが定義されてる場合 true が返却されます.
	 */
	public static final boolean loadServiceScoped(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		return c.isAnnotationPresent(ServiceScoped.class);
	}
	
	/**
	 * 対象オブジェクトに対してInjectアノテーションを反映.
	 * @param o 対象のオブジェクトを設定します.
	 */
	public static final void inject(Object o) {
		if(o == null) {
			throw new QuinaException("The specified argument is null.");
		}
		inject(o, o.getClass());
	}
	
	/**
	 * 対象オブジェクトのstaticフィールドに対してInjectアノテーションを反映.
	 * @param c 対象のクラスを設定します.
	 */
	public static final void inject(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is null.");
		}
		inject(null, c);
	}
	
	// 対象オブジェクトに対してInjectアノテーションを反映.
	private static final void inject(Object o, Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is null.");
		}
		Inject inj;
		String clazz;
		Field targetField;
		Object serviceObject;
		final CdiServiceManager man = Quina.get().getCdiServiceManager();
		final CdiInjectFieldElement list = Quina.get()
			.getCdiInjectFieldManager().get(c);
		if(list == null) {
			return;
		}
		final int len = list.size();
		for(int i = 0; i < len; i ++) {
			targetField = list.get(i);
			// フィールドからInjectアノテーションを取得.
			if((inj = targetField.getAnnotation(Inject.class)) != null) {
				// Injectで定義されてるパッケージ名＋クラス名を取得.
				clazz = inj.value();
				// 存在しない場合は、対象フィールドのクラス名を取得.
				if(clazz == null || clazz.isEmpty()) {
					// フィールドのクラス名で検索.
					clazz = targetField.getType().getName();
				}
				// 存在する場合、フィールドにサービスオブジェクトを注入.
				try {
					// 対象サービスを取得.
					serviceObject = man.getService(clazz);
					// 存在しない場合.
					if(serviceObject == null) {
						// 例外.
						throw new QuinaException(
							"The target service class definition (" + clazz +
							") does not exist.");
					}
					// セットする.
					list.setValue(i, o, serviceObject);
				} catch(QuinaException qe) {
					throw qe;
				} catch(Exception e) {
					throw new QuinaException(e);
				}
			}
		}
	}
}
