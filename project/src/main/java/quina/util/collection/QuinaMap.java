package quina.util.collection;

import java.util.Map;

/**
 * QuinaMap.
 */
public interface QuinaMap<K, V>
	extends Map<K, V>, TypesKeyValue<K, V> {
	
	/**
	 * 指定項番のKeyを取得します.
	 * @param no 項番を設定します.
	 * @return K 対象Keyが返却されます.
	 */
	public K keyAt(int no);

	/**
	 * 指定項番のValueを取得します.
	 * @param no 項番を設定します.
	 * @return V 対象Valueが返却されます.
	 */
	public V valueAt(int no);
}
