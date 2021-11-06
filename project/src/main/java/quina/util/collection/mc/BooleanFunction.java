package quina.util.collection.mc;

/**
 * Boolean返却用JsCollection用Function.
 */
public interface BooleanFunction<V> {
	/**
	 * 実行処理.
	 * @param value １つの要素が設定されます.
	 * @return boolean trueを返却した場合Valueの評価が
	 *                 正しいものとして処理されます.
	 */
	public boolean call(V value);
}
