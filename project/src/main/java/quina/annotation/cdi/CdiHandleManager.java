package quina.annotation.cdi;

import java.lang.reflect.InvocationTargetException;

import quina.Quina;
import quina.annotation.log.AnnotationLog;
import quina.exception.QuinaException;
import quina.util.Flag;
import quina.util.collection.ObjectList;

/**
 * Cdi(Contexts and Dependency Injection)をロードする
 * マネージャー.
 */
public class CdiHandleManager {
	/**
	 * CdiAnnotationScopedアノテーション自動読み込み実行用クラス名.
	 */
	public static final String AUTO_READ_CDI_HANDLE_CLASS =
		"LoadCdiAnnotationHandle";

	/**
	 * CdiAnnotationScopedアノテーション自動読み込み実行用メソッド名.
	 */
	public static final String AUTO_READ_CDI_HANDLE_METHOD =
		"load";
	
	// QuinaAnnotationHandle管理リスト.
	private ObjectList<CdiHandle> list =
		new ObjectList<CdiHandle>();
	
	// fixフラグ.
	private final Flag fixFlag = new Flag(false);
	
	/**
	 * コンストラクタ.
	 */
	public CdiHandleManager() {
	}
	
	// 対象Handleが存在する場合、項番返却.
	private int search(CdiHandle handle) {
		final int len = list.size();
		for(int i = 0; i < len; i ++) {
			if(list.get(i) == handle) {
				return i;
			}
		}
		return -1;
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
	 * autoCdiAnnotationHandleManager実行.
	 * @return CdiAnnotationHandleManager このオブジェクトが返却されます.
	 */
	public final CdiHandleManager autoCdiHandleManager() {
		if(fixFlag.get()) {
			throw new QuinaException("Already completed.");
		}
		java.lang.Class<?> clazz;
		java.lang.reflect.Method method;
		try {
			// AutoRoute実行用のクラスを取得.
			clazz = Class.forName(
				AnnotationCdiConstants.CDI_PACKAGE_NAME + "." +
					AUTO_READ_CDI_HANDLE_CLASS);
			// 実行メソッドを取得.
			method = clazz.getMethod(AUTO_READ_CDI_HANDLE_METHOD);
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
	
	/**
	 * カスタムハンドルを設定.
	 * @param handle 対象のハンドルを設定します.
	 * @return boolean true の場合、正しく追加されました.
	 */
	public boolean add(CdiHandle handle) {
		if(fixFlag.get()) {
			throw new QuinaException("Already completed.");
		} else if(handle == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		if(search(handle) != -1) {
			return false;
		}
		list.add(handle);
		return true;
	}
	
	/**
	 * カスタムハンドルを削除.
	 * @param handle 対象のハンドルを設定します.
	 * @return boolean true の場合、正しく削除されました.
	 */
	public boolean remove(CdiHandle handle) {
		if(fixFlag.get()) {
			throw new QuinaException("Already completed.");
		} else if(handle == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		int p;
		if((p = search(handle)) == -1) {
			return false;
		}
		list.remove(p);
		return true;
	}
	
	/**
	 * サイズが返却されます.
	 * @return
	 */
	public int size() {
		return list.size();
	}
	
	/**
	 * AnnotationのCdi定義を反映されます.
	 * @param o Cdi定義を読み込むオブジェクトを設定します.
	 * @exception Exception 例外.
	 */
	public void load(Object o) {
		if(o == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		load(Quina.get().getCdiReflectManager(), o, o.getClass());
	}
	
	/**
	 * AnnotationのCdi定義を反映されます.
	 * @param c Cdi定義の読み込みクラスを設定します.
	 * @exception Exception 例外.
	 */
	public void load(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		load(Quina.get().getCdiReflectManager(), null, c);
	}
	
	/**
	 * AnnotationのCdi定義を反映されます.
	 * @param man CdiReflectManagerが設定されます.
	 * @param o Cdi定義を読み込むオブジェクトを設定します.
	 *          null の場合 Cdi対象のフィールドはstatic のみ
	 *          ローディングされます.
	 * @param c Cdi定義の読み込みクラスを設定します.
	 */
	private void load(CdiReflectManager man, Object o, Class<?> c) {
		// Objectが存在する場合.
		if(o != null) {
			// Cdiの条件を読み込む.
			AnnotationCdi.loadInject(o);
			// Logの条件を読み込む.
			AnnotationLog.loadLogDefine(o);
		// Objectが存在しない場合.
		} else {
			// Cdiの条件を読み込む.
			AnnotationCdi.loadInject(c);
			// Logの条件を読み込む.
			AnnotationLog.loadLogDefine(c);
		}
		final int len = list.size();
		try {
			// カスタム読み込み.
			for(int i = 0; i < len; i ++) {
				list.get(i).load(man, o, c);
			}
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
}
