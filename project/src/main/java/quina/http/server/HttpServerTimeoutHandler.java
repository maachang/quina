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
	public boolean isExecuteTimeout(NioElement element) {
		return true;
	}

	// タイムアウト実行を行う.
	@Override
	public void executeTimeout(NioElement element) {
		HttpElement em = (HttpElement)element;
		try {
			// HttpError408(Request Timeout)を返却.
			HttpServerUtil.sendError(408, em);
		} catch(Exception e) {}
	}
}
