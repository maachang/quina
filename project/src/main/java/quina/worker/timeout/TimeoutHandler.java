package quina.worker.timeout;

/**
 * Timeoutハンドラ.
 */
public interface TimeoutHandler {
	
	/**
	 * タイムアウト要素をクローズ.
	 * @param element Timeout要素が設定されます.
	 */
	public void closeTimeoutElement(
		TimeoutElement element);
	
	/**
	 * タイムアウト要素がクローズ済みかチェック.
	 * @param element Timeout要素が設定されます.
	 * @return boolean trueの場合、クローズ済みです.
	 */
	public boolean isCloseTimeoutElement(
		TimeoutElement element);
	
	/**
	 * タイムアウトを行ってよいかチェック.
	 * @param element Timeout要素が設定されます.
	 * @param timeout タイムアウト値が設定されます.
	 * @return boolean true の場合、タイムアウト処理が
	 *                 行われます.
	 */
	public boolean isExecuteTimeout(
		TimeoutElement element, long timeout);
	
	/**
	 * タイムアウトが発生した場合の実行処理を行います.
	 * @param element Timeout要素が設定されます.
	 * @param timeout タイムアウト値が設定されます.
	 */
	public void executeTimeout(
		TimeoutElement element, long timeout);
	
	/**
	 * タイムアウト監視から除外するかチェック.
	 * @param element Timeout要素が設定されます.
	 * @return boolean trueが返却した場合、タイムアウト監視から
	 *                 外されます.
	 */
	public boolean comeOffTimeout(
		TimeoutElement element);
}