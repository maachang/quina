package quina.annotation.nativeimage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import quina.exception.QuinaException;
import quina.util.collection.ObjectList;

/**
 * GraalVMのNative-Imageのコンパイルに対する
 * コンフィグ定義を行うアノテーションを提供します.
 */
public class AnnotationNativeImage {
	private AnnotationNativeImage() {}
	
	/**
	 * 対象のオブジェクトがNativeCConfigScopedが定義されているかチェック.
	 * @param c 対象のクラスを設定します.
	 * @param cl 対象のクラスローダを設定します.
	 * @return ExecuteNativeBuildStep BuildStep実行メソッドが返却されます.
	 */
	public static final ExecuteNativeBuildStep nativeConfigScoped(
		Class<?> c, ClassLoader cl) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		if(c.isAnnotationPresent(NativeConfigScoped.class)) {
			String[] value = c.getAnnotation(NativeConfigScoped.class).value();
			String n;
			boolean loadFlag = false;
			int len = value == null ? 0 : value.length;
			ObjectList<Method> list;
			// 指定クラスがローディングできる場合に
			// このNativeConfigScopedを有効にします.
			if(len > 0) {
				for(int i = 0; i < len; i ++) {
					// 対象クラス名が空の場合エラー.
					n = value[i];
					if(n == null || (n = n.trim()).isEmpty()) {
						throw new QuinaException(
							"One class definition name in the NativeConfigScoped " +
							"argument is empty: " + c);
					}
					try {
						// 指定されたクラスがローディングされてる場合.
						Class.forName(n, true, cl);
						loadFlag = true;
						break;
					} catch(Exception e) {
					}
				}
			// クラス指定ないNativeConfigScopedの場合は
			// 必ず処理対象とする.
			} else {
				loadFlag = true;
			}
			// NativeBuildStepメソッドの実行が必要な場合.
			if(loadFlag) {
				try {
					// 空のコンストラクタが設定可能かチェック.
					c.getDeclaredConstructor().newInstance();
					// コンストラクタで実行可能なStepメソッドを検出.
					list = getNativeBuildeStepMethods(c);
					// Stepメソッドが存在する場合.
					if(list != null) {
						return new ExecuteNativeBuildStep(c, list);
					}
				} catch(QuinaException qe) {
					throw qe;
				} catch(InvocationTargetException ite) {
					throw new QuinaException(ite.getCause());
				} catch(Exception e) {
					throw new QuinaException(e);
				}
			}
		}
		return null;
	}
	
	/**
	 * NativeBuildeStep定義されメソッドを取得
	 * @param cs 対象クラスを設定します.
	 * @return int NativeBuildeSteアノテーションが定義されてる
	 *             メソッド数が返却されます
	 */
	private static final ObjectList<Method> getNativeBuildeStepMethods(
		Class<?> cs) {
		// 対象クラスのメソッド群を取得.
		final Method[] array = cs.getDeclaredMethods();
		final int len = array.length;
		ObjectList<Method> ret = null;
		try {
			for(int i = 0; i < len; i ++) {
				// NativeBuildStepアノテーション設定のMethod()が存在する場合.
				if(array[i].isAnnotationPresent(NativeBuildStep.class)) {
					// メソッドのパラメータは0である必要がある.
					if(array[i].getParameterCount() == 0) {
						if(ret == null) {
							ret = new ObjectList<Method>();
						}
						ret.add(array[i]);
					// メソッドパラメータが0以外の場合エラー.
					} else {
						throw new QuinaException(
							"The argument parameter of the method \"" +
							array[i].getName() + "\" of the NativeBuildeStep " +
							"annotation definition of the target class \"" +
							cs.getName() + "\" is not 0. ");
					}
				}
			}
		} catch(Exception e) {
			throw new QuinaException(e);
		}
		return ret;
	}
}
