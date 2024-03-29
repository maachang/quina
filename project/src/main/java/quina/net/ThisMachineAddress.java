package quina.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * このマシンが持つIPアドレス(IPV4)であるかチェックするための処理.
 */
public class ThisMachineAddress {
	private static final Map<String,Boolean> MAP;
	private static final String[] LIST;
	static {
		// 自マシンが認識している自IPアドレス群を取得.
		List<String> list = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
			while(n.hasMoreElements()) {
				NetworkInterface e = n.nextElement();
				Enumeration<InetAddress> a = e.getInetAddresses();
				while(a.hasMoreElements()) {
					InetAddress addr = a.nextElement();
					if(addr instanceof Inet4Address &&
						!"127.0.0.1".equals(addr.getHostAddress())) {
						list.add(addr.getHostAddress());
					}
				}
			}
			// ローカルホストアドレスを追加.
			list.add("127.0.0.1");
		} catch(Exception e) {}
		int len = list.size();
		Map<String,Boolean> target = new HashMap<String,Boolean>();
		for(int i = 0; i < len; i ++) {
			target.put(list.get(i), true);
		}
		len = list.size();
		String[] ary = new String[len];
		for(int i = 0; i < len; i ++) {
			ary[i] = list.get(i);
		}
		MAP = target;
		LIST = ary;
	}

	protected ThisMachineAddress() {}

	/**
	 * このマシンのアドレスと一致するかチェック.
	 * @param addr
	 * @return
	 */
	public static final boolean eq(InetAddress addr) {
		if(addr == null) {
			return false;
		}
		return MAP.containsKey(addr.getHostAddress());
	}

	/**
	 * このマシンのアドレスと一致するかチェック.
	 * @param addr
	 * @return
	 */
	public static final boolean eq(String addr) {
		if(addr == null) {
			return false;
		}
		return MAP.containsKey(addr);
	}

	/**
	 * ローカルIP取得.
	 * @param no
	 * @return
	 */
	public static final String get(int no) {
		return LIST[no];
	}

	/**
	 * ローカルIP数を取得.
	 * @return int
	 */
	public static final int size() {
		return LIST.length;
	}
}
