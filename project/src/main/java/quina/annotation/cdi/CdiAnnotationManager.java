package quina.annotation.cdi;

import quina.Quina;
import quina.annotation.log.AnnotationLog;
import quina.exception.QuinaException;
import quina.util.Flag;
import quina.util.collection.ObjectList;

/**
 * Cdi(Contexts and Dependency Injection)をロードする
 * マネージャー.
 */
public class CdiAnnotationManager {
	// QuinaAnnotationHandle管理リスト.
	private ObjectList<CdiAnnotationHandle> list =
		new ObjectList<CdiAnnotationHandle>();
	
	// fixフラグ.
	private final Flag fixFlag = new Flag(false);
	
	/**
	 * コンストラクタ.
	 */
	public CdiAnnotationManager() {
	}
	
	// 対象Handleが存在する場合、項番返却.
	private int search(CdiAnnotationHandle handle) {
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
	 * カスタムハンドルを設定.
	 * @param handle 対象のハンドルを設定します.
	 * @return boolean true の場合、正しく追加されました.
	 */
	public boolean add(CdiAnnotationHandle handle) {
		if(fixFlag.get()) {
			throw new QuinaException("Already completed.");
		} else if(handle == null) {
			throw new QuinaException("The specified component is Null.");
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
	public boolean remove(CdiAnnotationHandle handle) {
		if(fixFlag.get()) {
			throw new QuinaException("Already completed.");
		} else if(handle == null) {
			throw new QuinaException("The specified component is Null.");
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
		load(Quina.get().getCdiReflectManager(), o, o.getClass());
	}
	
	/**
	 * AnnotationのCdi定義を反映されます.
	 * @param c Cdi定義の読み込みクラスを設定します.
	 * @exception Exception 例外.
	 */
	public void load(Class<?> c) {
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
