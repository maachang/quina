package quina.net.nio.tcp;

/**
 * NioTimeoutThreadに対するハンドル定義.
 */
public interface NioTimeoutHandler {
	
	/**
	 * タイムアウトを行ってよいかチェック.
	 * @param element Nio要素が設定されます.
	 * @param timeout タイムアウト値が設定されます.
	 * @return boolean true の場合、タイムアウト処理が
	 *                 行われます.
	 */
	public boolean isExecuteTimeout(
		NioElement element, long timeout);
	
	/**
	 * タイムアウトが発生した場合の実行処理を行います.
	 * @param element Nio要素が設定されます.
	 * @param timeout タイムアウト値が設定されます.
	 */
	public void executeTimeout(
		NioElement element, long timeout);

}
