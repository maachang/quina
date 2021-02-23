 package quina.http;

import java.util.Map;

import quina.util.collection.BinarySearchMap;
import quina.util.collection.IndexMap;

/**
 * Httpパラメータ.
 */
public class Params extends BinarySearchMap<String, Object> {
	/**
	 * コンストラクタ.
	 */
	protected Params() {
		super();
	}

	@SuppressWarnings("unchecked")
	public Params(BinarySearchMap<String, Object> map) {
		super(map.getIndexMap());
	}

	/**
	 * コンストラクタ.
	 * @param list
	 */
	public Params(IndexMap<String, Object> list) {
		super(list);
	}

	/**
	 * コンストラクタ.
	 */
	public Params(final Map<String, Object> v) {
		super(v);
	}

	/**
	 * コンストラクタ.
	 * @param args
	 */
	public Params(final Object... args) {
		super(args);
	}

	@Override
	public void setIndexMap(IndexMap<String, Object> m) {
		super.setIndexMap(m);
	}

	@Override
	public IndexMap<String, Object> getIndexMap() {
		return null;
	}
}
