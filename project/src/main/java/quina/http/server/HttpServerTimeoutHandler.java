package quina.http.server;

import quina.http.HttpElement;
import quina.net.nio.tcp.NioUtil;
import quina.worker.timeout.TimeoutElement;
import quina.worker.timeout.TimeoutHandler;

/**
 * HttpServerTimeoutHandler.
 */
final class HttpServerTimeoutHandler
	implements TimeoutHandler {
	
	/**
	 * タイムアウト要素をクローズ.
	 * @param element Timeout要素が設定されます.
	 */
	@Override
	public void closeTimeoutElement(TimeoutElement element) {
		NioUtil.closeNioElement((HttpElement)element);
	}
	
	/**
	 * タイムアウト要素がクローズ済みかチェック.
	 * @param element Timeout要素が設定されます.
	 * @return boolean trueの場合、クローズ済みです.
	 */
	@Override
	public boolean isCloseTimeoutElement(TimeoutElement element) {
		return !((HttpElement)element).isConnection();
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
		// タイムアウト処理を行う.
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
		HttpElement em = (HttpElement)element;
		try {
			// HttpError408(Request-Timeout)を返却.
			HttpServerCore.sendError(408, em);
		} catch(Exception e) {}
	}
	
	
	/**
	 * タイムアウト監視から除外するかチェック.
	 * @param element Timeout要素が設定されます.
	 * @return boolean trueが返却した場合、タイムアウト監視から
	 *                 外されます.
	 */
	@Override
	public boolean comeOffTimeout(TimeoutElement element) {
		return false;
	}
}
