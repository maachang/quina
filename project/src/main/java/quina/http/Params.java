 package quina.http;

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
	 * @param args
	 */
	public Params(final Object... args) {
		super(args);
	}

	@Override
	public void setIndexMap(IndexMap<String, Object> m) {
	}

	@Override
	public IndexMap<String, Object> getIndexMap() {
		return null;
	}
}
