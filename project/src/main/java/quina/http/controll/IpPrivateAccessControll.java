package quina.http.controll;

import quina.http.Request;
import quina.http.server.HttpServerRequest;
import quina.net.ThisMachineAddress;
import quina.util.NumberUtil;
import quina.util.collection.ObjectList;

/**
 * Ipアドレスが対象のPrivateアドレスのみ許可.
 *
 */
public class IpPrivateAccessControll
	implements AccessControll {
	String[] addrList;
	boolean[] ip172Flags;
	
	/**
	 * コンストラクタ.
	 */
	public IpPrivateAccessControll() {
		int p;
		int len = ThisMachineAddress.size();
		String ip;
		ObjectList<Boolean> ip172 = new ObjectList<Boolean>();
		ObjectList<String> ipList = new ObjectList<String>();
		for(int i = 0; i < len; i ++) {
			ip = ThisMachineAddress.get(i);
			if(ip.startsWith("192.168.")) {
				ip = "192.168.";
			} else if(ip.startsWith("172.")) {
				p = ip.indexOf(".", 4);
				p = NumberUtil.parseInt(ip.substring(4, p));
				if(p >= 16 && p <= 31) {
					ip = "172.";
				} else {
					continue;
				}
			} else if(ip.startsWith("10.")) {
				ip = "10.";
			} else {
				continue;
			}
			ipList.add(ip);
			ip172.add("172.".equals(ip));
		}
		len = ipList.size();
		addrList = new String[len];
		ip172Flags = new boolean[len];
		for(int i = 0; i < len; i ++) {
			addrList[i] = ipList.get(i);
			ip172Flags[i] = ip172.get(i);
		}
	}
	
	@Override
	public boolean isAccess(Request req) {
		String ip =((HttpServerRequest)req).getElement()
			.getRemoteAddress().getAddress().getHostAddress();
		final int len = addrList.length;
		for(int i = 0; i < len; i ++) {
			if(ip.startsWith(addrList[i])) {
				// 172. の時だけ 172.16. ～ 172.31. を調べる.
				if(ip172Flags[i]) {
					int p = ip.indexOf(".", 4);
					p = NumberUtil.parseInt(ip.substring(4, p));
					if(p >= 16 && p <= 31) {
						return true;
					}
					continue;
				}
				return true;
			}
		}
		return false;
	}
}