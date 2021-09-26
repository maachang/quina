package quina.util.collection;

/**
 * 読み込み専用のオブジェクトリスト.
 */
public interface ReadList<V> extends TypesValue<V> {

	/**
	 * 現在の情報数を取得.
	 *
	 * @return size 対象のサイズが返却されます.
	 */
	public int size();

	/**
	 * オブジェクト配列情報を取得.
	 * @return Object[] 配列情報として取得します.
	 */
	public Object[] toArray();

	/**
	 * 検索処理.
	 * @param n 検索対象の要素を設定します.
	 * @return int 検索結果の位置が返却されます. [-1]の場合は情報は存在しません.
	 */
	public int search(V n);
}
