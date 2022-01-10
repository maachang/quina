package quina.http.controll.ipv4;

import java.net.InetSocketAddress;

import quina.http.Request;
import quina.http.controll.AccessControll;
import quina.http.server.HttpServerRequest;

/**
 * IPアドレスがLocalHostを許可.
 */
public class IpLocalHostAccessControll
	implements AccessControll {
	
	// シングルトン.
	private static final IpLocalHostAccessControll SNGL =
		new IpLocalHostAccessControll();
	
	/**
	 * オブジェクトを取得.
	 * @return IpLocalHostAccessControll オブジェクトが返却されます.
	 */
	public static final IpLocalHostAccessControll getInstance() {
		return SNGL;
	}
	
	// コンストラクタ.
	protected IpLocalHostAccessControll() {}
	
	@Override
	public boolean isAccess(Request req) {
		if(req instanceof HttpServerRequest) {
			InetSocketAddress addr =
				((HttpServerRequest)req).getElement()
				.getRemoteAddress();
			return "127.0.0.1".equals(
				addr.getAddress().getHostAddress());
		}
		return false;
	}
}
