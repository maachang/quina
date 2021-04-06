 package quina.http;

import quina.util.collection.IndexMap;
import quina.util.collection.IndexKeyValueList;

/**
 * Httpパラメータ.
 */
public class Params extends IndexMap<String, Object> {
	/**
	 * コンストラクタ.
	 */
	protected Params() {
		super();
	}

	@SuppressWarnings("unchecked")
	public Params(IndexMap<String, Object> map) {
		super(map.getIndexMap());
	}

	/**
	 * コンストラクタ.
	 * @param list
	 */
	public Params(IndexKeyValueList<String, Object> list) {
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
	public void setIndexMap(IndexKeyValueList<String, Object> m) {
		super.setIndexMap(m);
	}

	@Override
	public IndexKeyValueList<String, Object> getIndexMap() {
		return null;
	}
}
