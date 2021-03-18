package quina.promise;

/**
 * Promise実行時に最初に呼び出されるコール実行.
 */
public interface PromiseFromEndCall {
	/**
	 * コール呼び出し実装.
	 * @param action PromiseActionを設定します.
	 * @throws Exception 例外.
	 */
	public void call(PromiseAction action)
		throws Exception;
}
