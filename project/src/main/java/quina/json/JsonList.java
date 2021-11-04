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
	private final ObjectList<Object> list;
	
	/**
	 * コンストラクタ.
	 */
	protected JsonList() {
		list = new ObjectList<Object>();
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
	 * @param array Object配列を設定します.
	 */
	public JsonList(List<?> list) {
		final int len = list == null ? 0 : list.size();
		this.list = new ObjectList<Object>(len);
		for(int i = 0; i < len; i ++) {
			this.list.add(list.get(i));
		}
	}
	
	/**
	 * コンストラクタ.
	 * @param list List情報を設定します.
	 */
	public JsonList(ObjectList<?> list) {
		final int len = list == null ? 0 : list.size();
		this.list = new ObjectList<Object>(len);
		for(int i = 0; i < len; i ++) {
			this.list.add(list.get(i));
		}
	}
	
	/**
	 * コンストラクタ.
	 * @param array リスト情報を設定します.
	 */
	public JsonList(Object... array) {
		final int len = array == null ? 0 : array.length;
		this.list = new ObjectList<Object>(len);
		for(int i = 0; i < len; i ++) {
			this.list.add(array[i]);
		}
	}
	
	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public Object get(int index) {
		return list.get(index);
	}

	@Override
	public int size() {
		return list.size();
	}
	
	@Override
	public boolean add(Object value) {
		list.add(value);
		return true;
	}
	
	@Override
	public void add(int index, Object value) {
		list.add(index, value);
	}
	
	@Override
	public Object set(int index, Object value) {
		return list.set(index, value);
	}
	
	@Override
	public Object remove(int index) {
		return list.remove(index);
	}
}
