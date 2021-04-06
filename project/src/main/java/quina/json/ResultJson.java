package quina.json;

import quina.util.collection.IndexMap;
import quina.util.collection.IndexKeyValueList;

/**
 * JSON返却処理.
 */
public class ResultJson extends IndexMap<String, Object> {
	/**
	 * コンストラクタ.
	 */
	protected ResultJson() {
		super();
	}

	@SuppressWarnings("unchecked")
	public ResultJson(IndexMap<String, Object> map) {
		super(map.getIndexMap());
	}

	/**
	 * コンストラクタ.
	 * @param list
	 */
	public ResultJson(IndexKeyValueList<String, Object> list) {
		super(list);
	}

	/**
	 * コンストラクタ.
	 * @param args
	 */
	public ResultJson(final Object... args) {
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
