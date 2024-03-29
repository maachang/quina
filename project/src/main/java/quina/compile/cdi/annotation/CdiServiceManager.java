package quina.compile.cdi.annotation;

import java.lang.reflect.InvocationTargetException;

import quina.exception.QuinaException;
import quina.util.collection.IndexKeyValueList;

/**
 * CDI(Contexts and Dependency Injection)サービス管理.
 */
public class CdiServiceManager {
	/**
	 * CDIServiceアノテーション自動読み込み実行用クラス名.
	 */
	public static final String AUTO_READ_CDI_SERVICE_CLASS = "LoadCdiService";

	/**
	 * CDIServiceアノテーション自動読み込み実行用メソッド名.
	 */
	public static final String AUTO_READ_CDI_SERVICE_METHOD = "load";
	
	// package + class名で、対象サービスを管理.
	private IndexKeyValueList<String, Object> manager =
		new IndexKeyValueList<String, Object>();
	
	public CdiServiceManager() {
	}
	
	/**
	 * サービスオブジェクトを設定.
	 * @param service サービスオブジェクトを設定します.
	 * @return CidServiceManager このオブジェクトが返却されます.
	 */
	public CdiServiceManager put(Object service) {
		return put(service.getClass(), service);
	}
	
	/**
	 * サービスオブジェクトを設定.
	 * @param c クラスオブジェクトを設定します.
	 * @param service サービスオブジェクトを設定します.
	 * @return CidServiceManager このオブジェクトが返却されます.
	 */
	public synchronized CdiServiceManager put(Class<?> c, Object service) {
		if(!AnnotationCdi.loadServiceScoped(service)) {
			throw new QuinaException(
				"The specified object is not a service object.");
		}
		return put(c.getName(), service);
	}
	
	/**
	 * サービスオブジェクトを設定.
	 * @param name パッケージ名＋クラス名を設定します.
	 * @param service サービスオブジェクトを設定します.
	 * @return CidServiceManager このオブジェクトが返却されます.
	 */
	public synchronized CdiServiceManager put(String name, Object service) {
		if(!AnnotationCdi.loadServiceScoped(service)) {
			throw new QuinaException(
				"The specified object is not a service object.");
		}
		manager.put(name, service);
		return this;
	}
	
	/**
	 * 管理されてるサービスオブジェクトを取得.
	 * @param c クラスオブジェクトを設定します.
	 * @return Object 一致するサービスオブジェクトが返却されます.
	 */
	public synchronized Object getService(Class<?> c) {
		return getService(c.getName());
	}
	
	/**
	 * 管理されてるサービスオブジェクトを取得.
	 * @param name パッケージ名＋クラス名を設定します.
	 * @return Object 一致するサービスオブジェクトが返却されます.
	 */
	public synchronized Object getService(String name) {
		return manager.get(name);
	}
	
	/**
	 * 登録サービス数を取得.
	 * @return int 登録サービス数が返却されます.
	 */
	public synchronized int size() {
		return manager.size();
	}
	
	/**
	 * 番号を指定して登録サービスクラス名を取得.
	 * @param no 番号を指定します.
	 * @return String サービスクラス名が返却されます.
	 */
	public synchronized String getClassName(int no) {
		return manager.keyAt(no);
	}
	
	/**
	 * 番号を指定して登録サービスクラスを取得.
	 * @param no 番号を指定します.
	 * @return Object サービスクラスが返却されます.
	 */
	public synchronized Object getService(int no) {
		return manager.valueAt(no);
	}
	
	/**
	 * autoCdiService実行.
	 * @return CdiManager このオブジェクトが返却されます.
	 */
	public CdiServiceManager autoCdiService() {
		java.lang.Class<?> clazz;
		java.lang.reflect.Method method;
		try {
			// AutoRoute実行用のクラスを取得.
			clazz = Class.forName(
				AnnotationCdiConstants.CDI_PACKAGE_NAME + "." +
				AUTO_READ_CDI_SERVICE_CLASS);
			// 実行メソッドを取得.
			method = clazz.getMethod(AUTO_READ_CDI_SERVICE_METHOD);
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
