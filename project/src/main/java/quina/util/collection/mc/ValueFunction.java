package quina.util.collection.mc;

/**
 * Value返却用JsCollection用Function.
 */
public interface ValueFunction<V> {
	/**
	 * 実行処理.
	 * @param value １つの要素が設定されます.
	 * @return V 処理されたValue内容が返却されます.
	 */
	public V call(V value);

}
