package quina.http.server;

import quina.http.HttpElement;
import quina.net.nio.tcp.NioElement;
import quina.net.nio.tcp.NioTimeoutHandler;

/**
 * HttpServerTimeoutHandler.
 */
public class HttpServerTimeoutHandler
	implements NioTimeoutHandler {
	
	// 対象要素はタイムアウト実行が可能かチェック.
	@Override
	public boolean isExecuteTimeout(
		NioElement element, long timeout) {
		// ThreadScopeが０の場合はタイムアウト実行が可能.
		// 基本的に実行中の場合は、タイムアウト処理としない.
		if(((HttpElement)element).getThreadScope() <= 0) {
			return true;
		}
		return false;
	}

	// タイムアウト実行を行う.
	@Override
	public void executeTimeout(
		NioElement element, long timeout) {
		HttpElement em = (HttpElement)element;
		try {
			// HttpError408(Request-Timeout)を返却.
			HttpServerUtil.sendError(408, em);
		} catch(Exception e) {}
	}
}
