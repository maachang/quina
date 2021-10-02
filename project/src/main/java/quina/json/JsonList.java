package quina.json;

import java.util.AbstractList;
import java.util.List;

import quina.util.collection.ObjectList;

/**
 * JsonList.
 * 
 * add などの追加処理は行えません.
 */
public class JsonList extends AbstractList<Object> {
	private final Object[] array;
	
	/**
	 * コンストラクタ.
	 */
	protected JsonList() {
		array = new Object[0];
	}
	
	/**
	 * JsonList作成処理.
	 * @param args リスト情報を設定します.
	 * @return JsonList JsonListが返却されます..
	 */
	public static final JsonList of(Object... args) {
		return new JsonList(args);
	}
	
	/**
	 * コンストラクタ.
	 * @param list List情報を設定します.
	 */
	public JsonList(List<?> list) {
		final int len = list == null ? 0 : list.size();
		array = new Object[len];
		for(int i = 0; i < len; i ++) {
			array[i] = list.get(i);
		}
	}
	
	/**
	 * コンストラクタ.
	 * @param list List情報を設定します.
	 */
	public JsonList(ObjectList<?> list) {
		final int len = list == null ?
			0 : list.size();
		array = new Object[len];
		for(int i = 0; i < len; i ++) {
			array[i] = list.get(i);
		}
	}
	
	/**
	 * コンストラクタ.
	 * @param array リスト情報を設定します.
	 */
	public JsonList(Object... array) {
		final int len = array == null ?
			0 : array.length;
		this.array = new Object[len];
		System.arraycopy(array, 0, this.array,
			0, len);
	}

	@Override
	public Object get(int index) {
		return array[index];
	}

	@Override
	public int size() {
		return array.length;
	}

}
