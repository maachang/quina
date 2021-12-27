package quina.http.controll;

import java.net.InetSocketAddress;

import quina.http.Request;
import quina.http.server.HttpServerRequest;

/**
 * IPアドレスがLocalHostのみ許可.
 */
public class IpLocalHostAccessControll
	implements AccessControll {
	@Override
	public boolean isAccess(Request req) {
		InetSocketAddress addr =
			((HttpServerRequest)req).getElement()
			.getRemoteAddress();
		return "127.0.0.1".equals(
			addr.getAddress().getHostAddress());
	}
}
