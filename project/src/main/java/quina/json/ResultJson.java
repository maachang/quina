package quina.json;

import quina.util.collection.BinarySearchMap;
import quina.util.collection.IndexMap;

/**
 * JSON返却処理.
 */
public class ResultJson extends BinarySearchMap<String, Object> {
	/**
	 * コンストラクタ.
	 */
	protected ResultJson() {
		super();
	}

	@SuppressWarnings("unchecked")
	public ResultJson(BinarySearchMap<String, Object> map) {
		super(map.getIndexMap());
	}

	/**
	 * コンストラクタ.
	 * @param list
	 */
	public ResultJson(IndexMap<String, Object> list) {
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
	public void setIndexMap(IndexMap<String, Object> m) {
		super.setIndexMap(m);
	}

	@Override
	public IndexMap<String, Object> getIndexMap() {
		return null;
	}
}
