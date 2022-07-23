package quina.thread;

/**
 * QuinaThreadステータス.
 */
public interface QuinaThreadStatus {
	
	/**
	 * 停止命令が既に出されているかチェック.
	 * @return
	 */
	public boolean isStopThread();

	/**
	 * 開始しているかチェック.
	 * @return
	 */
	public boolean isStartupThread();

	/**
	 * 終了しているかチェック.
	 * @return
	 */
	public boolean isExitThread();
}
