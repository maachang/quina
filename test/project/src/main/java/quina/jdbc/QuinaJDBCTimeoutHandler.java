package quina.jdbc;

import quina.worker.timeout.TimeoutElement;
import quina.worker.timeout.TimeoutHandler;

/**
 * QuinaJDBCTimeoutHandler.
 */
final class QuinaJDBCTimeoutHandler
	implements TimeoutHandler {
	/**
	 * タイムアウト要素をクローズ.
	 * @param element Timeout要素が設定されます.
	 */
	@Override
	public void closeTimeoutElement(TimeoutElement element) {
		final QuinaConnection conn = (QuinaConnection)element;
		// コネクションを破棄.
		conn.destroy();
	}
	
	/**
	 * タイムアウト要素がクローズ済みかチェック.
	 * @param element Timeout要素が設定されます.
	 * @return boolean trueの場合、クローズ済みです.
	 */
	@Override
	public boolean isCloseTimeoutElement(TimeoutElement element) {
		final QuinaConnection conn = (QuinaConnection)element;
		// コネクションが破棄されているか返却.
		return conn.isDestroy();
	}
	
	/**
	 * タイムアウトを行ってよいかチェック.
	 * @param element Timeout要素が設定されます.
	 * @param timeout タイムアウト値が設定されます.
	 * @return boolean true の場合、タイムアウト処理が
	 *                 行われます.
	 */
	@Override
	public boolean isExecuteTimeout(
		TimeoutElement element, long timeout) {
		// タイムアウトを許可する.
		return true;
	}
	
	/**
	 * タイムアウトが発生した場合の実行処理を行います.
	 * @param element Timeout要素が設定されます.
	 * @param timeout タイムアウト値が設定されます.
	 */
	@Override
	public void executeTimeout(
		TimeoutElement element, long timeout) {
		final QuinaConnection conn = (QuinaConnection)element;
		// 利用中の場合は破棄しない.
		if(conn.getTime() == QuinaConnection.NONE_TIMEOUT) {
			// タイムアウト登録を解除.
			conn.releaseTimeout();
		// 利用中でない場合は破棄する.
		} else {
			// コネクションを破棄.
			conn.destroy();
		}
	}
	
	/**
	 * タイムアウト監視から除外するかチェック.
	 * @param element Timeout要素が設定されます.
	 * @return boolean trueが返却した場合、タイムアウト監視から
	 *                 外されます.
	 */
	@Override
	public boolean comeOffTimeout(TimeoutElement element) {
		final QuinaConnection conn = (QuinaConnection)element;
		// 利用中の場合はタイムアウトから除外.
		if(conn.getTime() == QuinaConnection.NONE_TIMEOUT) {
			// タイムアウト登録を解除.
			conn.releaseTimeout();
			return true;
		}
		return false;
	}
}
