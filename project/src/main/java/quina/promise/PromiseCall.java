package quina.promise;

/**
 * Promise実行処理.
 */
public interface PromiseCall {
	/**
	 * コール呼び出し実装.
	 * @param action PromiseActionを設定します.
	 * @param value パラメータが設定されます.
	 * @throws Exception 例外.
	 */
	public void call(PromiseAction action, Object value)
		throws Exception;
}
