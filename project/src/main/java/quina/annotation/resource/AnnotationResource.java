package quina.annotation.resource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import quina.exception.QuinaException;
import quina.util.collection.ObjectList;

/**
 * リソースファイルをコピーするAnnotation.
 */
public class AnnotationResource {
	private AnnotationResource() {}
	
	/**
	 * 対象のオブジェクトがResourceScopedが定義されているかチェック.
	 * @param c 対象のクラスを設定します.
	 * @return ExecuteCopyResource リソースファイルコピー処理が返却されます.
	 */
	public static final ExecuteCopyResource resourceScoped(
		Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		if(c.isAnnotationPresent(ResourceScoped.class)) {
			try {
				// 空のコンストラクタが設定可能かチェック.
				c.getDeclaredConstructor().newInstance();
				// コンストラクタで実行可能なStepメソッドを検出.
				return getExecuteCopyResource(c);
			} catch(QuinaException qe) {
				throw qe;
			} catch(InvocationTargetException ite) {
				throw new QuinaException(ite.getCause());
			} catch(Exception e) {
				throw new QuinaException(e);
			}
		}
		return null;
	}
	
	/**
	 * ExecuteCopyResource定義されメソッドを取得
	 * @param cs 対象クラスを設定します.
	 * @return ExecuteCopyResource リソースファイルコピー処理が返却されます.
	 */
	private static final ExecuteCopyResource getExecuteCopyResource(
		Class<?> cs) {
		BuildResource rb;
		// 対象クラスのメソッド群を取得.
		final Method[] array = cs.getDeclaredMethods();
		final int len = array.length;
		ObjectList<Method> list = null;
		try {
			for(int i = 0; i < len; i ++) {
				// BuildResourceアノテーション設定のMethod()が存在する場合.
				if((rb = array[i].getAnnotation(BuildResource.class)) != null) {
					// メソッドのパラメータは0である必要で、
					// 戻り値はString[]である必要がある.
					if(array[i].getParameterCount() == 0 ||
						String[].class.equals(array[i].getReturnType())) {
						if(list == null) {
							list = new ObjectList<Method>();
						}
						list.add(array[i]);
					// メソッドパラメータが0以外の場合エラー.
					} else {
						throw new QuinaException(
							"The argument parameter of the method \"" +
							array[i].getName() + "\" of the BuildResource " +
							"annotation definition of the target class \"" +
							cs.getName() + "\" is not 0 or is not return String[]. ");
					}
					// BuildResourceのsrcが設定されていない場合.
					if(rb.src() == null) {
						throw new QuinaException("The src of the method \""
							+ array[i].getName() + "\" of the BuildResource " +
							"annotation defined in the target class \"" +
							cs.getName() +"\" is not set.");
					// BuildResourceのdestが設定されていない場合.
					} else if(rb.dest() == null) {
						throw new QuinaException("The dest of the method \""
							+ array[i].getName() + "\" of the BuildResource " +
							"annotation defined in the target class \"" +
							cs.getName() +"\" is not set.");
					}
				}
			}
		} catch(Exception e) {
			throw new QuinaException(e);
		}
		return new ExecuteCopyResource(cs, list);
	}
}
