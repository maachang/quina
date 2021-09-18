package quina.annotation.cdi;

import java.lang.reflect.InvocationTargetException;

import quina.exception.QuinaException;
import quina.util.collection.IndexKeyValueList;

/**
 * CDI(Contexts and Dependency Injection)
 * リフレクション代替え管理.
 * 
 * graalvmのnative-image では フィールド一覧の取得や
 * メソッド一覧の取得や、変数指定でのリフレクション取得
 * は、ワーニングが発生します.
 * 
 * ＜例＞
 * // OK
 * Class.forName("hoge.moge.Abc");
 * 
 * // NG
 * String clazzName = "hoge.moge.Abc";
 * Class.forName(clazzName); // NG.
 * 
 * // OK.
 * Class.forName("hoge.moge.Abc").getDeclaredField("hoge");
 * 
 * // NG.
 * Class.forName("hoge.moge.Abc").getDeclaredFields();
 * 
 * これらNGの条件が含まれてる場合はnative-image中に fallback
 * が発生し、これらが発生するとJDKが無いと動作しないNative
 * イメージが作成されてしまいます.
 * 
 * 特に getDeclaredFields 等は現在のnative-imageではサポート外
 * であるため、fieldに@Inject等ができない等の問題があります.
 * 
 * それらを回避するため、qRouteOut 実行でComponent関連と@ServiceScoped
 * アノテーション定義オブジェクトの呼び出しの条件を洗い出して、
 * それらのフィールド情報をこのオブジェクトに設定するロードオブジェクトを
 * 作成し、ここにフィールド群を管理します.
 */
public class CdiReflectManager {
	/**
	 * CDIRefect自動読み込み実行用クラス名.
	 */
	public static final String AUTO_READ_CDI_REFLECT_CLASS = "LoadCdiReflect";

	/**
	 * CDIRefect自動読み込み実行用メソッド名.
	 */
	public static final String AUTO_READ_CDI_REFLECT_METHOD = "load";
	
	// Componentや@ServiceScopedアノテーション単位で管理するマネージャ.
	private IndexKeyValueList<String, CdiReflectElement> manager = new
		IndexKeyValueList<String, CdiReflectElement>();
	
	/**
	 * コンストラクタ.
	 */
	public CdiReflectManager() {
		
	}
	
	/**
	 * オブジェクトを登録してCdiReflectElementを
	 * @param c 対象のクラスを設定します.
	 * @return CdiReflectElement 要素が返却されます.
	 */
	public CdiReflectElement register(Class<?> c) {
		if(c == null) {
			throw new QuinaException(
				"The specified argument is Null.");
		}
		String name = c.getName();
		CdiReflectElement ret = manager.get(name);
		if(ret == null) {
			ret = new CdiReflectElement();
			manager.put(name, ret);
		}
		return ret;
	}
	
	/**
	 * CdiReflectElementを取得.
	 * @param o 対象のオブジェクトを設定します.
	 * @return CdiReflectElement 要素が返却されます.
	 */
	public CdiReflectElement get(Object o) {
		if(o == null) {
			throw new QuinaException(
				"The specified argument is Null.");
		}
		return get(o.getClass());
	}
	
	/**
	 * CdiReflectElementを取得.
	 * @param c 対象ののクラスを設定します.
	 * @return CdiReflectElement 要素が返却されます.
	 */
	public CdiReflectElement get(Class<?> c) {
		if(c == null) {
			throw new QuinaException(
				"The specified argument is Null.");
		}
		String name = c.getName();
		return manager.get(name);
	}
	
	/**
	 * 登録オブジェクト数を取得.
	 * @return int 登録オブジェクト数が返却されます.
	 */
	public int size() {
		return manager.size();
	}
	
	/**
	 * 指定項番の登録クラス名が返却されます.
	 * @param no 対象の項番を設定します.
	 * @return String パッケージ名＋クラス名が返却されます.
	 */
	public String getClassName(int no) {
		return manager.keyAt(no);
	}
	
	
	/**
	 * autoCdiService実行.
	 * @return CdiManager このオブジェクトが返却されます.
	 */
	public CdiReflectManager autoCdiReflect() {
		java.lang.Class<?> clazz;
		java.lang.reflect.Method method;
		try {
			// AutoRoute実行用のクラスを取得.
			clazz = Class.forName(
				AnnotationCdiConstants.CDI_PACKAGE_NAME + "." +
				AUTO_READ_CDI_REFLECT_CLASS);
			// 実行メソッドを取得.
			method = clazz.getMethod(AUTO_READ_CDI_REFLECT_METHOD);
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
