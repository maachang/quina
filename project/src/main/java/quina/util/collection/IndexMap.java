package quina.util.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * バイナリサーチマップ.
 *
 * IndexMapを内部的に利用.
 *
 * IndexMapでは、BinarySearchを使って、データの追加、削除、取得を行います.
 * HashMapと比べると速度は１０倍ぐらいは遅いですが、リソースは
 * Listと同じぐらいしか食わないので、リソースを重視する場合は、
 * こちらを利用することをおすすめします.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class IndexMap<K, V> implements AbstractKeyIterator.Base<K>,
	AbstractEntryIterator.Base<K, V>,
	Map<K, V>,
	TypesKeyValue<K, V> {

	private IndexKeyValueList<K, V> list;

	/**
	 * コンストラクタ.
	 */
	public IndexMap() {
		list = new IndexKeyValueList<K, V>();
	}

	/**
	 * コンストラクタ.
	 */
	public IndexMap(IndexKeyValueList<K, V> list) {
		if (list == null) {
			list = new IndexKeyValueList<K, V>();
		}
		this.list = list;
	}

	/**
	 * コンストラクタ.
	 */
	public IndexMap(final Map<K, V> v) {
		list = new IndexKeyValueList<K, V>(v);
	}

	/**
	 * コンストラクタ.
	 */
	public IndexMap(final Object... args) {
		list = new IndexKeyValueList<K, V>(args);
	}

	/**
	 * IndexMapをセット.
	 */
	protected void setIndexMap(IndexKeyValueList<K, V> list) {
		this.list = list;
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public V put(K name, V value) {
		if (name == null) {
			return null;
		}
		return list.put(name, value);
	}

	public IndexMap putAll(Object... args) {
		if(args == null || args.length ==0) {
			return this;
		}
		list.putAll(args);
		return this;
	}

	@Override
	public boolean containsKey(Object key) {
		if (key == null) {
			return false;
		}
		return list.containsKey((K)key);
	}

	@Override
	public V get(Object key) {
		if (key == null) {
			return null;
		}
		return list.get((K)key);
	}

	@Override
	public V remove(Object key) {
		if (key == null) {
			return null;
		}
		return list.remove((K)key);
	}

	@Override
	public boolean isEmpty() {
		return list.size() == 0;
	}

	@Override
	public void putAll(Map toMerge) {
		if (toMerge == null) {
			return;
		}
		Iterator it = toMerge.keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			Object value = toMerge.get(key);
			if (key != null) {
				put((K)key, (V)value);
			}
		}
	}

	@Override
	public boolean containsValue(Object value) {
		final int len = list.size();
		if (value == null) {
			for (int i = 0; i < len; i++) {
				if (list.valueAt(i) == null) {
					return true;
				}
			}
		} else {
			for (int i = 0; i < len; i++) {
				if (value.equals(list.valueAt(i))) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public String toString() {
		return list.toString();
	}

	@Override
	public Collection<V> values() {
		ArrayList<V> ret = new ArrayList<V>();
		int len = list.size();
		for (int i = 0; i < len; i++) {
			ret.add(list.valueAt(i));
		}
		return ret;
	}

	public IndexKeyValueList getIndexMap() {
		return list;
	}

	@Override
	public Set<K> keySet() {
		return new AbstractKeyIterator.Set<K>(this);
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return new AbstractEntryIterator.Set<K, V>(this);
	}

	@Override
	public K getKey(int no) {
		return list.keyAt(no);
	}

	@Override
	public V getValue(int no) {
		return list.valueAt(no);
	}

	public K keyAt(int no) {
		return list.keyAt(no);
	}

	public V valueAt(int no) {
		return list.valueAt(no);
	}
}
