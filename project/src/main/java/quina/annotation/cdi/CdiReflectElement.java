package quina.annotation.cdi;

import java.lang.reflect.Field;

import quina.exception.QuinaException;
import quina.util.collection.ObjectList;

/**
 * Cdi(Contexts and Dependency Injection)フィールド群を
 * 管理する要素.
 */
public class CdiReflectElement {
	private CdiObjectType type;
	private ObjectList<Field> list;
	
	/**
	 * コンストラクタ.
	 * @param type
	 */
	public CdiReflectElement(CdiObjectType type) {
		this.type = type;
		this.list = new ObjectList<Field>();
	}
	
	/**
	 * このオブジェクトのタイプを取得.
	 * @return CdiObjectType CdiObjectTypeが返却されます.
	 */
	public CdiObjectType getCdiType() {
		return type;
	}
	
	/**
	 * フィールド追加.
	 * @param f
	 * @return
	 */
	public CdiReflectElement add(Field f) {
		if(f == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		f.setAccessible(true);
		list.add(f);
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
		return list.get(no);
	}
	
	/**
	 * 項番を指定してフィールドのクラスを取得.
	 * @param no 項番を設定します.
	 * @return Class<?> フィールドのクラスが返却されます.
	 */
	public Class<?> getType(int no) {
		return list.get(no).getType();
	}
	
	/**
	 * 対象フィールドに新しい内容をセット.
	 * @param no 項番を設定します.
	 * @param target 設定対象のオブジェクトを設定します
	 * @param value フィールドに設定する内容を設定します.
	 */
	public void setField(int no, Object target, Object value) {
		try {
			list.get(no).set(target, value);
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
	
}

