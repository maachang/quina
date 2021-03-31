package quina.promise;

/**
 * PromiseAwait待ちの代わりのコールバック処理.
 */
public interface PromiseAwaitCall {

	/**
	 * awaitコールバック.
	 * @param action PromiseActionを設定します.
	 * @param status Promiseステータスを設定します.
	 * @param value 返却Valueを設定します.
	 */
	public void call(PromiseActionImpl action, PromiseStatus status, Object value);
}
