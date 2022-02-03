package quina.compile.cdi.annotation.proxy;

import java.lang.reflect.InvocationTargetException;

import quina.compile.cdi.annotation.AnnotationCdiConstants;
import quina.exception.QuinaException;
import quina.util.Flag;
import quina.util.collection.IndexKeyValueList;

/**
 * ProxyScoped定義クラスの生成管理.
 */
public class ProxyScopedManager {
	
	/**
	 * ProxyScopedアノテーション自動読み込み実行用クラス名.
	 */
	public static final String AUTO_READ_PROXY_SCOPED_CLASS =
		"LoadProxyScoped";

	/**
	 * ProxyScopedアノテーション自動読み込み実行用メソッド名.
	 */
	public static final String AUTO_READ_PROXY_SCOPED_METHOD =
		"load";
	
	// Proxyクラス管理.
	// key  : ＠ProxyScoped定義のクラス名.
	// value: ＠ProxyScoped定義を継承された
	//        自動生成Javaコードのクラスオブジェクト.
	private final IndexKeyValueList<String, QuinaProxy> manager =
		new IndexKeyValueList<String, QuinaProxy>();
	
	// fixフラグ.
	private final Flag fixFlag = new Flag(false);
	
	/**
	 * コンストラクタ.
	 */
	public ProxyScopedManager() {
		
	}
	
	/**
	 * 追加・削除処理を確定する.
	 */
	public void fix() {
		fixFlag.set(true);
	}
	
	/**
	 * 確定されているかチェック.
	 * @return boolean true の場合確定しています.
	 */
	public boolean isFix() {
		return fixFlag.get();
	}
	
	/**
	 * Proxyクラスを追加セット.
	 * @param srcClass ＠ProxyScoped定義のクラス.
	 * @param QuinaProxy ＠ProxyScoped定義を継承された
	 *                   オブジェクトを生成できるオブジェクトが
	 *                   設定されます..
	 */
	public void put(
		Class<?> srcClass, QuinaProxy proxyObject) {
		if(fixFlag.get()) {
			throw new QuinaException("Already completed.");
		} else if(srcClass == null) {
			throw new QuinaException(
				"The specified argument is null.");
		}
		manager.put(srcClass.getName(), proxyObject);
	}
	
	/**
	 * Proxyクラスを追加セット.
	 * @param srcClassName ＠ProxyScoped定義のクラス名.
	 * @param QuinaProxy ＠ProxyScoped定義を継承された
	 *                   オブジェクトを生成できるオブジェクトが
	 *                   設定されます..
	 */
	public void put(
		String srcClassName, QuinaProxy proxyObject) {
		if(fixFlag.get()) {
			throw new QuinaException("Already completed.");
		} else if(srcClassName == null || srcClassName.isEmpty()) {
			throw new QuinaException(
				"The specified argument is null.");
		}
		manager.put(srcClassName, proxyObject);
	}
	
	/**
	 * Proxyクラスを削除.
	 * @param srcClass ＠ProxyScoped定義のクラス.
	 */
	public void remove(Class<?> srcClass) {
		if(fixFlag.get()) {
			throw new QuinaException("Already completed.");
		} else if(srcClass == null) {
			throw new QuinaException(
				"The specified argument is null.");
		}
		manager.remove(srcClass.getName());
	}
	
	/**
	 * Proxyクラスを削除.
	 * @param srcClassName ＠ProxyScoped定義のクラス名.
	 */
	public void remove(String srcClassName) {
		if(fixFlag.get()) {
			throw new QuinaException("Already completed.");
		} else if(srcClassName == null || srcClassName.isEmpty()) {
			throw new QuinaException(
				"The specified argument is null.");
		}
		manager.remove(srcClassName);
	}
	
	/**
	 * Proxyクラスを取得.
	 * @param srcClass ＠ProxyScoped定義のクラス.
	 * @return Class<?> ＠ProxyScoped定義を継承された自動生成
	 *                  Javaコードのクラスオブジェクト.
	 */
	public Class<?> get(Class<?> srcClass) {
		if(srcClass == null) {
			throw new QuinaException(
				"The specified argument is null.");
		}
		return get(srcClass.getName());
	}
	
	/**
	 * Proxyクラスを取得.
	 * @param srcClassName ＠ProxyScoped定義のクラス名.
	 * @return Class<?> ＠ProxyScoped定義を継承された自動生成
	 *                  Javaコードのクラスオブジェクト.
	 */
	public Class<?> get(String srcClassName) {
		return _get(srcClassName).getProxyClass();
	}
	
	// QuinaProxyオブジェクトを取得.
	public QuinaProxy _get(String srcClassName) {
		if(srcClassName == null || srcClassName.isEmpty()) {
			throw new QuinaException(
				"The specified argument is null.");
		}
		QuinaProxy qp = manager.get(srcClassName);
		if(qp == null) {
			throw new QuinaException(
				"Proxy object with specified class name \"" +
				srcClassName + "\" does not exist.");
		}
		return qp;
	}

	
	/**
	 * Proxyオブジェクトを取得.
	 * @param srcClass ＠ProxyScoped定義のクラス.
	 * @return Object ＠ProxyScoped定義を継承された自動生成
	 *                Javaコードのオブジェクト.
	 */
	public Object getObject(Class<?> srcClass,
		Object[] args) {
		if(srcClass == null) {
			throw new QuinaException(
				"The specified argument is null.");
		}
		return getObject(srcClass.getName(), args);
	}
	
	/**
	 * Proxyオブジェクトを取得.
	 * @param srcClassName ＠ProxyScoped定義のクラス名.
	 * @return Object ＠ProxyScoped定義を継承された自動生成
	 *                Javaコードのオブジェクト.
	 */
	public Object getObject(String srcClassName,
		Object... args) {
		return _get(srcClassName)
			.newInstance(new ProxySettingArgs(args));
	}
	
	/**
	 * autoProxyScopedManager実行.
	 * @return ProxyManager このオブジェクトが返却されます.
	 */
	public final ProxyScopedManager autoProxyScopedManager() {
		if(fixFlag.get()) {
			throw new QuinaException("Already completed.");
		}
		java.lang.Class<?> clazz;
		java.lang.reflect.Method method;
		try {
			// AutoRoute実行用のクラスを取得.
			clazz = Class.forName(
				AnnotationCdiConstants.CDI_PACKAGE_NAME + "." +
					AUTO_READ_PROXY_SCOPED_CLASS);
			// 実行メソッドを取得.
			method = clazz.getMethod(AUTO_READ_PROXY_SCOPED_METHOD);
		} catch(Exception e) {
			// クラスローディングやメソッド読み込みに失敗した場合は処理終了.
			return this;
		}
		try {
			// Methodをstatic実行.
			method.invoke(null);
		} catch(InvocationTargetException it) {
			// メソッド実行でエラーの場合はエラー返却.
			throw new QuinaException(it.getCause());
		} catch(Exception e) {
			// メソッド実行でエラーの場合はエラー返却.
			throw new QuinaException(e);
		}
		return this;
	}
}
