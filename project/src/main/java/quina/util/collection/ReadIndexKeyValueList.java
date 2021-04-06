package quina.util.collection;

/**
 * 読み込み用IndexMap.
 */
public interface ReadIndexKeyValueList<K, V> extends TypesKeyValue<K, V> {
	/**
	 * データ確認.
	 * @param key
	 * @return
	 */
	public boolean containsKey(K key);

	/**
	 * データ数を取得.
	 * @return
	 */
	public int size();


	/**
	 * キー名一覧を取得.
	 * @return
	 */
	public Object[] names();
}
