package quina.annotation.proxy;

import java.lang.reflect.InvocationTargetException;

import quina.annotation.cdi.AnnotationCdiConstants;
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
	private final IndexKeyValueList<String, Class<?>> manager =
		new IndexKeyValueList<String, Class<?>>();
	
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
	 * @param autoClass ＠ProxyScoped定義を継承された
	 *                  自動生成Javaコードのクラスオブジェクト.
	 */
	public void put(
		Class<?> srcClass, Class<?> autoClass) {
		if(fixFlag.get()) {
			throw new QuinaException("Already completed.");
		} else if(srcClass == null) {
			throw new QuinaException(
				"The specified argument is null.");
		}
		manager.put(srcClass.getName(), autoClass);
	}
	
	/**
	 * Proxyクラスを追加セット.
	 * @param srcClassName ＠ProxyScoped定義のクラス名.
	 * @param autoClass ＠ProxyScoped定義を継承された
	 *                  自動生成Javaコードのクラスオブジェクト.
	 */
	public void put(
		String srcClassName, Class<?> autoClass) {
		if(fixFlag.get()) {
			throw new QuinaException("Already completed.");
		} else if(srcClassName == null || srcClassName.isEmpty()) {
			throw new QuinaException(
				"The specified argument is null.");
		}
		manager.put(srcClassName, autoClass);
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
		if(srcClassName == null || srcClassName.isEmpty()) {
			throw new QuinaException(
				"The specified argument is null.");
		}
		return manager.get(srcClassName);
	}
	
	/**
	 * Proxyオブジェクトを取得.
	 * @param srcClass ＠ProxyScoped定義のクラス.
	 * @return Object ＠ProxyScoped定義を継承された自動生成
	 *                Javaコードのオブジェクト.
	 */
	public Object getObject(Class<?> srcClass,
		Object... args) {
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
		Class<?> src = get(srcClassName);
		if(src == null) {
			return null;
		}
		try {
			// 自動生成されたクラスに自動生成される
			//  public static final Object __newInstance()
			// メソッドを実行する.
			final Object ret = src.getMethod(
				AnnotationProxyScopedConstants.NEW_INSTANCE_METHOD)
				.invoke(null);
			
			// 次に＠ProxyInitialSetting定義された初期処理
			//  public void __initialSetting(Object[] args)
			// メソッドを実行する.
			src.getMethod(
				AnnotationProxyScopedConstants.INITIAL_SETTING_METHOD,
					Object[].class)
				.invoke(ret, args);
			return ret;
		} catch(InvocationTargetException it) {
			throw new QuinaException(it.getCause());
		} catch(Exception e) {
			throw new QuinaException(e);
		}
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
