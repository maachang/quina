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
	 * タイムアウト監視が必要かチェック.
	 * @param element Timeout要素が設定されます.
	 * @return boolean trueの場合タイムアウト監視が必要です.
	 */
	public boolean isMonitoredTimeout(
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
	
}