package quina.http.server;

import quina.http.HttpElement;
import quina.net.nio.tcp.NioUtil;
import quina.worker.timeout.TimeoutElement;
import quina.worker.timeout.TimeoutHandler;

/**
 * HttpServerTimeoutHandler.
 */
public class HttpServerTimeoutHandler
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
	 * タイムアウト監視が必要かチェック.
	 * @param element Timeout要素が設定されます.
	 * @return boolean trueの場合タイムアウト監視が必要です.
	 */
	@Override
	public boolean isMonitoredTimeout(TimeoutElement element) {
		// データー送信が完了している場合.
		return !((HttpElement)element).isSendData();
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
		// ThreadScopeが０の場合はタイムアウト実行が可能.
		// 基本的に実行中の場合は、タイムアウト処理としない.
		if(((HttpElement)element).getThreadScope() <= 0) {
			return true;
		}
		return false;
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
}
