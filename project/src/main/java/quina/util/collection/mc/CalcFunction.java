package quina.util.collection.mc;

/**
 * 計算処理用JsCollection用Function.
 */
public interface CalcFunction<V> {
	/**
	 * 実行処理.
	 * @param all 前回の返却結果が設定されます.
	 * @param value 今回の要素が設定されます.
	 * @return Object 今回の処理結果が返却されます.
	 */
	public Object call(Object all, V value);
}
