package quina.annotation.nativeimage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import quina.exception.QuinaException;
import quina.util.collection.ObjectList;

/**
 * １つのNativeConfigScoped定義のクラスに
 * 対するNativeBuildStepメソッドを実行する
 * ためのオブジェクト.
 */
public class ExecuteNativeBuildStep {
	// 対象クラス.
	private Class<?> clazz;
	// 対象メソッド.
	private ObjectList<Method> list;
	
	/**
	 * コンストラクタ.
	 * @param clazz 対象クラスを設定します.
	 * @param list 対象BuildStep実行メソッドを設定します.
	 */
	public ExecuteNativeBuildStep(
		Class<?> clazz, ObjectList<Method> list) {
		this.clazz = clazz;
		this.list = list;
	}
	
	/**
	 * NativeBuildStepメソッド群を実行.
	 */
	public void execute() {
		Method m;
		Object o = null;
		try {
			final int len = list.size();
			for(int i = 0; i < len; i ++) {
				m = list.get(i);
				m.setAccessible(true);
				if(Modifier.isStatic(m.getModifiers())) {
					m.invoke(null);
				} else {
					if(o == null) {
						o = clazz.getDeclaredConstructor()
							.newInstance();
					}
					m.invoke(o);
				}
			}
		} catch(InvocationTargetException ite) {
			throw new QuinaException(ite.getCause());
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
}
