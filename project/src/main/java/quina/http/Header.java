package quina.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import quina.util.collection.AbstractEntryIterator;
import quina.util.collection.AbstractKeyIterator;
import quina.util.collection.TypesKeyValue;

/**
 * Httpヘッダ.
 */
public interface Header extends AbstractKeyIterator.Base<String>,
	AbstractEntryIterator.Base<String, String>,
	Map<String, String>,
	TypesKeyValue<String, String> {

	@Override
	default boolean isEmpty() {
		return size() == 0;
	}

	@Override
	default boolean containsValue(Object value) {
		final int len = size();
		if(value == null) {
			for(int i = 0; i < len; i ++) {
				if(getValue(i) == null) {
					return true;
				}
			}
		} else {
			for(int i = 0; i < len; i ++) {
				if(value.equals(getValue(i))) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	default String put(String key, String value) {
		return null;
	}

	@Override
	default String remove(Object key) {
		return null;
	}

	@Override
	default void putAll(Map<? extends String, ? extends String> m) {
	}

	@Override
	default void clear() {
	}

	@Override
	default Set<String> keySet() {
		return new AbstractKeyIterator.Set<String>(this);
	}

	@Override
	default Collection<String> values() {
		final int len = size();
		final List<String> ret = new ArrayList<String>(len);
		for(int i = 0; i < len; i ++) {
			ret.add(getValue(i));
		}
		return ret;
	}

	@Override
	default Set<Entry<String, String>> entrySet() {
		return new AbstractEntryIterator.Set<String, String>(this);
	}

	@Override
	public boolean containsKey(Object key);

	@Override
	public String get(Object key);

	@Override
	public String getValue(int no);

	@Override
	public String getKey(int no);

	@Override
	public int size();
}
