package quina.util.collection.mc;

/**
 * 返却値なしのJsCollection用Function.
 */
public interface VoidFunction<V> {
	/**
	 * 実行処理.
	 * @param value １つの要素が設定されます.
	 */
	public void call(V value);

}
