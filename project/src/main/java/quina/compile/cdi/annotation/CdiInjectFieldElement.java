package quina.compile.cdi.annotation;

import java.lang.reflect.Field;

import quina.exception.QuinaException;
import quina.util.collection.ObjectList;

/**
 * Cdi(Contexts and Dependency Injection)フィールド群を
 * 管理する要素.
 */
public class CdiInjectFieldElement {
	// Cdi フィールド要素.
	private static final class CdiFieldElement {
		// アクセスがstaticの場合 true.
		protected final boolean staticFlag;
		// 対象フィールド情報.
		protected final Field field;
		
		// コンストラクタ.
		protected CdiFieldElement(boolean staticFlag, Field field) {
			this.staticFlag = staticFlag;
			this.field = field;
		}
	}
	
	// Cdi オブジェクトのフィールド要素群管理.
	private ObjectList<CdiFieldElement> list;
	
	/**
	 * コンストラクタ.
	 */
	public CdiInjectFieldElement() {
		this.list = new ObjectList<CdiFieldElement>();
	}
	
	/**
	 * フィールド追加.
	 * @param staticFlag 対象フィールドが static な場合は true を設定します.
	 * @param field 対象のフィールドオブジェクトを設定します.
	 * @return CdiInjectFieldElement このオブジェクトが返却されます.
	 */
	public CdiInjectFieldElement add(boolean staticFlag, Field field) {
		if(field == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		field.setAccessible(true);
		list.add(new CdiFieldElement(staticFlag, field));
		return this;
	}
	
	/**
	 * 登録フィールド数を取得.
	 * @return int 登録フィールド数が返却されます.
	 */
	public int size() {
		return list.size();
	}
	
	/**
	 * 項番を指定してフィールドを取得.
	 * @param no 項番を設定します.
	 * @return Field 対象のフィールドオブジェクトが設定されます.
	 */
	public Field get(int no) {
		// 対象要素を取得.
		final CdiFieldElement em = list.get(no);
		if(em != null) {
			return em.field;
		}
		// null の場合 例外.
		throw new QuinaException(
			"The specified item number (" +
			no + ") is out of range. ");
	}
	
	/**
	 * 項番を指定してフィールドのクラスを取得.
	 * @param no 項番を設定します.
	 * @return Class<?> フィールドのクラスが返却されます.
	 */
	public Class<?> getType(int no) {
		return get(no).getType();
	}
	
	/**
	 * 項番を指定してフィールドがstatic定義かを取得.
	 * @param no 項番を設定します.
	 * @return boolean の場合static定義です.
	 */
	public boolean isStatic(int no) {
		// 対象要素を取得.
		final CdiFieldElement em = list.get(no);
		if(em != null) {
			return em.staticFlag;
		}
		// null の場合 例外.
		throw new QuinaException(
			"The specified item number (" +
			no + ") is out of range. ");
	}
	
	/**
	 * 対象フィールドの情報を取得.
	 * @param no 項番を設定します.
	 * @param target 設定対象のオブジェクトを設定します
	 * @return Object フィールドの内容が返却されます.
	 */
	public Object getValue(int no, Object target) {
		try {
			final CdiFieldElement em = list.get(no);
			if(em != null) {
				if(em.staticFlag) {
					// static フィールドにアクセス.
					return em.field.get(null);
				} else {
					// オブジェクトフィールドにアクセス.
					return em.field.get(target);
				}
			} else {
				// null の場合 例外.
				throw new QuinaException(
					"The specified item number (" +
					no + ") is out of range. ");
			}
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
	
	/**
	 * 対象フィールドに新しい内容をセット.
	 * @param no 項番を設定します.
	 * @param target 設定対象のオブジェクトを設定します
	 * @param value フィールドに設定する内容を設定します.
	 */
	public void setValue(int no, Object target, Object value) {
		try {
			final CdiFieldElement em = list.get(no);
			if(em != null) {
				if(em.staticFlag) {
					// static フィールドにアクセス.
					em.field.set(null, value);
				} else {
					// オブジェクトフィールドにアクセス.
					em.field.set(target, value);
				}
			} else {
				// null の場合 例外.
				throw new QuinaException(
					"The specified item number (" +
					no + ") is out of range. ");
			}
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
}

