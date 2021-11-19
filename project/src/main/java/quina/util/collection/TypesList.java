package quina.util.collection;

import java.util.AbstractList;
import java.util.List;

import quina.json.JsonList;

/**
 * TypesList.
 */
public class TypesList<T> extends AbstractList<T>
	implements QuinaList<T> {
	private final ObjectList<T> list;
	
	/**
	 * コンストラクタ.
	 */
	protected TypesList() {
		list = new ObjectList<T>();
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
	public TypesList(List<T> list) {
		final int len = list == null ? 0 : list.size();
		this.list = new ObjectList<T>(len);
		for(int i = 0; i < len; i ++) {
			this.list.add(list.get(i));
		}
	}
	
	/**
	 * コンストラクタ.
	 * @param list List情報を設定します.
	 */
	public TypesList(ObjectList<T> list) {
		final int len = list == null ? 0 : list.size();
		this.list = new ObjectList<T>(len);
		for(int i = 0; i < len; i ++) {
			this.list.add(list.get(i));
		}
	}
	
	/**
	 * コンストラクタ.
	 * @param array リスト情報を設定します.
	 */
	@SuppressWarnings("unchecked")
	public TypesList(Object... array) {
		final int len = array == null ? 0 : array.length;
		this.list = new ObjectList<T>(len);
		for(int i = 0; i < len; i ++) {
			this.list.add((T)array[i]);
		}
	}
	
	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public T get(int index) {
		return list.get(index);
	}

	@Override
	public int size() {
		return list.size();
	}
	
	@Override
	public boolean add(T value) {
		list.add(value);
		return true;
	}
	
	@Override
	public void add(int index, T value) {
		list.add(index, value);
	}
	
	@Override
	public T set(int index, T value) {
		return list.set(index, value);
	}
	
	@Override
	public T remove(int index) {
		return list.remove(index);
	}
}