package quina.util.collection;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * スレッドセーフなIndexKeyValueList.
 */
public class IndexKeyValueLockList<K, V>
	extends IndexKeyValueList<K, V> {
	
	// Read-Writeロックオブジェクト.
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	/**
	 * コンストラクタ.
	 */
	public IndexKeyValueLockList() {
		super();
	}

	/**
	 * コンストラクタ.
	 * @param args args.length == 1 で args[0]にMap属性を設定すると、
	 *             その内容がセットされます.
	 *             また、 key, value .... で設定することも可能です.
	 */
	public IndexKeyValueLockList(Object... args) {
		super(args);
	}
	
	/**
	 * データクリア.
	 */
	@Override
	public void clear() {
		lock.writeLock().lock();
		try {
			super.clear();
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * データセット.
	 * @param key
	 * @param value
	 * @return
	 */
	@Override
	public V put(K key, V value) {
		lock.writeLock().lock();
		try {
			return super.put(key, value);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * 指定データ群の設定.
	 * @param args args.length == 1 で args[0]にMap属性を設定すると、その内容がセットされます.
	 *     また、 key, value .... で設定することも可能です.
	 */
	@Override
	public void putAll(Object... args) {
		lock.writeLock().lock();
		try {
			super.putAll(args);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * データ取得.
	 * @param key
	 * @return
	 */
	@Override
	public V get(Object key) {
		lock.readLock().lock();
		try {
			return super.get(key);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * データ確認.
	 * @param key
	 * @return
	 */
	@Override
	public boolean containsKey(K key) {
		lock.readLock().lock();
		try {
			return super.containsKey(key);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * データ削除.
	 * @param key
	 * @return
	 */
	@Override
	public V remove(K key) {
		lock.writeLock().lock();
		try {
			return super.remove(key);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * データ数を取得.
	 * @return
	 */
	@Override
	public int size() {
		lock.readLock().lock();
		try {
			return super.size();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * キー名一覧を取得.
	 * @return
	 */
	@Override
	public Object[] names() {
		lock.readLock().lock();
		try {
			return super.names();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * 指定項番でキー情報を取得.
	 * @param no
	 * @return
	 */
	@Override
	public K keyAt(int no) {
		lock.readLock().lock();
		try {
			return super.keyAt(no);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * 指定項番で要素情報を取得.
	 * @param no
	 * @return
	 */
	@Override
	public V valueAt(int no) {
		lock.readLock().lock();
		try {
			return super.valueAt(no);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int hashCode() {
		lock.readLock().lock();
		try {
			return super.hashCode();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * 対象オブジェクトと一致するかチェック.
	 * @param o
	 * @return
	 */
	@Override
	public boolean equals(Object o) {
		lock.readLock().lock();
		try {
			return super.equals(o);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * 文字列として出力.
	 * @return
	 */
	@Override
	public String toString() {
		lock.readLock().lock();
		try {
			return super.toString();
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * 同じ内容で別のオブジェクトを作成.
	 * @return IndexKeyValueLockList コピーされた内容が返却されます.
	 */
	@SuppressWarnings("rawtypes")
	public IndexKeyValueLockList copy() {
		return copy(null);
	}

	/**
	 * 同じ内容で別のオブジェクトを作成.
	 * @param out 指定した場合、このオブジェクトに格納されます.
	 * @return IndexKeyValueLockList コピーされた内容が返却されます.
	 */
	@SuppressWarnings("rawtypes")
	public IndexKeyValueLockList copy(IndexKeyValueLockList<K, V> out) {
		lock.readLock().lock();
		try {
			IndexKeyValueLockList<K, V> ret = out == null ?
				new IndexKeyValueLockList<K, V>(this.size()) :
				out;
			ret.clear();
			ObjectList<Entry<K, V>> srcList = this.list;
			ObjectList<Entry<K, V>> retList = ret.list;
			int len = srcList.size();
			for(int i = 0; i < len; i ++) {
				retList.add(srcList.get(i).copy());
			}
			return ret;
		} finally {
			lock.readLock().unlock();
		}
	}
}
