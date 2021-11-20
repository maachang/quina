package quina.json;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import quina.util.collection.IndexKeyValueList;
import quina.util.collection.ObjectList;
import quina.util.collection.QuinaMap;

/**
 * JsonMap.
 * 
 * put などの追加処理は行えません.
 */
public class JsonMap
	extends AbstractMap<String, Object>
	implements QuinaMap<String, Object> {
	private final JsonSet jsonSet = new JsonSet();
	
	/**
	 * コンストラクタ.
	 */
	protected JsonMap() {
		super();
	}
	
	/**
	 * JsonMap作成処理.
	 * @param args JsonMap作成内容を設定します.
	 *             key, value, key, value ... のように設定します.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final JsonMap of(Object... args) {
		if(args == null || args.length <= 0) {
			return new JsonMap();
		} else if(args.length == 1) {
			Object o = args[0];
			if(o instanceof Map) {
				return new JsonMap((Map<String, Object>)o);
			} else if(o instanceof IndexKeyValueList) {
				return new JsonMap(
					(IndexKeyValueList<String, Object>)o);
			}
			return new JsonMap(o, null);
		} else {
			return new JsonMap(args);
		}
	}
	
	/**
	 * コンストラクタ.
	 * @param map
	 */
	public JsonMap(Map<String, Object> map) {
		if(map == null) {
			return;
		}
		Entry<String, Object> e;
		Iterator<Entry<String, Object>> it = map.entrySet().iterator();
		while(it.hasNext()) {
			e = it.next();
			jsonSet.put(e.getKey(), e.getValue());
		}
		jsonSet.fix();
	}
	
	/**
	 * コンストラクタ.
	 * @param list
	 */
	public JsonMap(IndexKeyValueList<String, Object> list) {
		if(list == null) {
			return;
		}
		final int len = list.size();
		for(int i = 0; i < len; i ++) {
			jsonSet.put(list.keyAt(i), list.valueAt(i));
		}
		jsonSet.fix();
	}

	/**
	 * コンストラクタ.
	 * @param args JsonMap作成内容を設定します.
	 *             key, value, key, value ... のように設定します.
	 */
	public JsonMap(final Object... args) {
		if(args == null || args.length == 0) {
			return;
		}
		final int len = args.length;
		for(int i = 0; i < len; i += 2) {
			jsonSet.put((String)args[i], args[i + 1]);
		}
		jsonSet.fix();
	}
	
	@Override
	public Object get(Object key) {
		return jsonSet.get((String)key);
	}
	
	@Override
	public boolean containsKey(Object key) {
		return jsonSet.containsKey((String)key);
	}
	
	@Override
	public int size() {
		return jsonSet.size();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return jsonSet;
	}
	
	// JsonEntry.
	private static final class JsonEntry
		implements Entry<String, Object>,
			Comparable<Object> {
		private String key;
		private Object value;
		
		/**
		 * コンストラクタ.
		 * @param key 対象のKeyを設定します.
		 * @param value 対象のValueを設定します.
		 */
		protected JsonEntry(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public Object getValue() {
			return value;
		}

		@Override
		public Object setValue(Object value) {
			Object ret = this.value;
			this.value = value;
			return ret;
		}

		@Override
		public int compareTo(Object o) {
			if(o instanceof Entry) {
				return key.compareTo(
					(String)((Entry<?, ?>)o).getKey());
			}
			return key.compareTo((String)o);
		}
	}
	
	// jsonSet.
	private static final class JsonSet
		extends AbstractSet<Entry<String, Object>> {
		private ObjectList<JsonEntry> list = new ObjectList<JsonEntry>();
		private JsonEntry[] searchList = null;
		
		/**
		 * 対象のEntryを追加.
		 * @param key 対象のキーを設定します.
		 * @param value 対象の要素を設定します.
		 */
		protected void put(String key, Object value) {
			if(key == null) {
				throw new NullPointerException();
			}
			list.add(new JsonEntry(key, value));
		}
		
		/**
		 * Entry追加を完了させる.
		 */
		protected void fix() {
			final int len = list.size();
			searchList = new JsonEntry[len];
			System.arraycopy(list.rawArray(), 0, searchList, 0, len);
			Arrays.sort(searchList);
			list = null;
		}
		
		// バイナリサーチ.
		private static final int binarySearch(
			final JsonEntry[] keys, final Object n) {
			int low = 0;
			int high = keys.length - 1;
			int mid, cmp;
			while (low <= high) {
				mid = (low + high) >>> 1;
				if ((cmp = keys[mid].compareTo(n)) < 0) {
					low = mid + 1;
				} else if (cmp > 0) {
					high = mid - 1;
				} else {
					return mid; // key found
				}
			}
			return -1;
		}
		
		// 指定キーで検索.
		private final int search(String key) {
			if(key == null) {
				return -1;
			}
			return binarySearch(searchList, key);
		}
		
		/**
		 * キーを指定して要素を取得.
		 * @param key 対象のキーを設定します.
		 * @return Object 対象の要素が返却されます.
		 */
		public Object get(String key) {
			int n = search(key);
			if(n == -1) {
				return null;
			}
			return searchList[n].getValue();
		}
		
		/**
		 * 対象のキーが存在するかチェック.
		 * @param key 対象のキーを設定します.
		 * @return boolean true の場合、存在します.
		 */
		public boolean containsKey(String key) {
			return search(key) != -1;
		}

		@Override
		public Iterator<Entry<String, Object>> iterator() {
			return new JsonIterator(searchList);
		}

		@Override
		public int size() {
			return searchList.length;
		}
	}
	
	// JsonEntry.
	private static final class JsonIterator
		implements Iterator<Entry<String, Object>> {
		private JsonEntry[] list;
		private int position;
		private JsonEntry entry;
		
		/**
		 * コンストラクタ.
		 * @param list 対象のJsonEntryリストを設定します.
		 */
		protected JsonIterator(JsonEntry[] list) {
			this.list = list;
			this.position = 0;
			this.entry = null;
		}
		
		// 次の情報を取得.
		private boolean nextValue(boolean hasNext) {
			if(list == null) {
				if(hasNext) {
					return false;
				}
				throw new NoSuchElementException();
			} else if(entry != null) {
				return true;
			} else if(position >= list.length) {
				if(hasNext) {
					return false;
				}
				throw new NoSuchElementException();
			}
			entry = list[position ++];
			return true;
		}

		@Override
		public boolean hasNext() {
			return nextValue(true);
		}

		@Override
		public Entry<String, Object> next() {
			nextValue(false);
			JsonEntry ret = entry;
			entry = null;
			return ret;
		}
	}

	@Override
	public String keyAt(int no) {
		JsonEntry e = jsonSet.list.get(no);
		return e == null ? null : e.getKey();
	}

	@Override
	public Object valueAt(int no) {
		JsonEntry e = jsonSet.list.get(no);
		return e == null ? null : e.getValue();
	}
}
