package quina.http.controll.ipv4;

import java.net.InetSocketAddress;

import quina.http.Request;
import quina.http.Response;
import quina.http.controll.HttpControll;
import quina.http.server.HttpServerRequest;

/**
 * IPアドレスがLocalHostを許可.
 */
public class IpLocalHostControll
	implements HttpControll {
	
	// シングルトン.
	private static final IpLocalHostControll SNGL =
		new IpLocalHostControll();
	
	/**
	 * オブジェクトを取得.
	 * @return IpLocalHostAccessControll オブジェクトが返却されます.
	 */
	public static final IpLocalHostControll getInstance() {
		return SNGL;
	}
	
	// コンストラクタ.
	protected IpLocalHostControll() {}
	
	@Override
	public boolean isAccess(Request req, Response<?> res) {
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
