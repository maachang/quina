package quina.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import quina.util.collection.AbstractEntryIterator;
import quina.util.collection.AbstractKeyIterator;
import quina.util.collection.IndexKeyValueList;

/**
 * Http送信用ヘッダ.
 */
public class HttpSendHeader implements Header {
	private IndexKeyValueList<String, String> list = new IndexKeyValueList<String, String>();

	/**
	 * コンストラクタ.
	 */
	public HttpSendHeader() {
		list = new IndexKeyValueList<String, String>();
	}

	/**
	 * コンストラクタ.
	 * @param list
	 */
	public HttpSendHeader(IndexKeyValueList<String, String> list) {
		if (list == null) {
			list = new IndexKeyValueList<String, String>();
		}
		this.list = list;
	}

	/**
	 * コンストラクタ.
	 * @param v
	 */
	public HttpSendHeader(final Map<String, String> v) {
		list = new IndexKeyValueList<String, String>(v);
	}

	/**
	 * コンストラクタ.
	 * @param args
	 */
	public HttpSendHeader(final Object... args) {
		list = new IndexKeyValueList<String, String>(args);
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public String put(String name, String value) {
		if (name == null) {
			return null;
		}
		return list.put(name, value);
	}

	public HttpSendHeader putAll(Object... args) {
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
		return list.containsKey((String)key);
	}

	@Override
	public String get(Object key) {
		if (key == null) {
			return null;
		}
		return list.get((String)key);
	}

	@Override
	public String remove(Object key) {
		if (key == null) {
			return null;
		}
		return list.remove((String)key);
	}

	@Override
	public boolean isEmpty() {
		return list.size() == 0;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void putAll(Map toMerge) {
		if (toMerge == null) {
			return;
		}
		Iterator it = toMerge.keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			Object value = toMerge.get(key);
			if (key != null) {
				put((String)key, (String)value);
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
		StringBuilder buf = new StringBuilder();
		int len = size();
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				buf.append("\n");
			}
			buf.append(getKey(i)).append(": ").append(getValue(i));
		}
		return buf.toString();
	}

	@Override
	public Collection<String> values() {
		ArrayList<String> ret = new ArrayList<String>();
		int len = list.size();
		for (int i = 0; i < len; i++) {
			ret.add(list.valueAt(i));
		}
		return ret;
	}

	@Override
	public Set<String> keySet() {
		return new AbstractKeyIterator.Set<String>(this);
	}

	@Override
	public Set<Entry<String, String>> entrySet() {
		return new AbstractEntryIterator.Set<String, String>(this);
	}

	@Override
	public String getKey(int no) {
		return list.keyAt(no);
	}

	@Override
	public String getValue(int no) {
		return list.valueAt(no);
	}

	/**
	 * このオブジェクトと同じ内容のオブジェクトを生成.
	 * @return
	 */
	public HttpSendHeader copy() {
		HttpSendHeader ret = new HttpSendHeader();
		list.copy(ret.list);
		return ret;
	}
}
