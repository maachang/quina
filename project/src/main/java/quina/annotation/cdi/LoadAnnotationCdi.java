package quina.annotation.cdi;

import java.lang.reflect.Field;

import quina.CdiManager;
import quina.Quina;
import quina.annotation.log.LoadAnnotationLog;
import quina.exception.QuinaException;

/**
 * CDIに対する処理を行います.
 */
public class LoadAnnotationCdi {
	private LoadAnnotationCdi() {}
	
	/**
	 * Annotationに定義されてるServiceScopeが定義されてるか取得.
	 * @param c コンポーネントを設定します.
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
	 * @param c コンポーネントクラスを設定します.
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
	 * @param man サービスマネージャを設定します.
	 * @param o 対象のオブジェクトを設定します.
	 * @return Object 反映されたオブジェクトが返却されます.
	 */
	public static final Object loadInject(CdiManager man, Object o) {
		if(man == null || o == null) {
			throw new QuinaException("The specified argument is null.");
		}
		Inject inj;
		String clazz;
		Field targetField;
		Object serviceObject;
		final CdiReflectElement list = Quina.get().getCdiReflectManager().get(o);
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
					targetField.set(o, serviceObject);
				} catch(QuinaException qe) {
					throw qe;
				} catch(Exception e) {
					throw new QuinaException(e);
				}
			}
		}
		return o;
	}
}
