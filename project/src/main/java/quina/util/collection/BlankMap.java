package quina.util.collection;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * 空のMapオブジェクト.
 */
public class BlankMap<K, V> extends AbstractMap<K, V>
	implements QuinaMap<K, V> {
	@SuppressWarnings("rawtypes")
	private final class BlankIterator implements Iterator {
		@Override
		public boolean hasNext() {
			return false;
		}
		@Override
		public Object next() {
			throw new NoSuchElementException();
		}
	}
	@SuppressWarnings("rawtypes")
	private final class BlankSet extends AbstractSet {
		@Override
		public Iterator iterator() {
			return new BlankIterator();
		}

		@Override
		public int size() {
			return 0;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<Entry<K, V>> entrySet() {
		// TODO 自動生成されたメソッド・スタブ
		return new BlankSet();
	}
	
	public V put(K key, V value) {
		return null;
	}

	@Override
	public K keyAt(int no) {
		return null;
	}

	@Override
	public V valueAt(int no) {
		return null;
	}
}
