package quina.http.controll.ipv4;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import quina.http.Request;
import quina.http.controll.AccessControll;
import quina.http.server.HttpServerRequest;
import quina.net.ThisMachineAddress;
import quina.util.Flag;
import quina.util.NumberUtil;
import quina.util.collection.ObjectList;

/**
 * Ipアドレスが対象のPrivateアドレスを許可.
 *
 */
public class IpPrivateAccessControll
	implements AccessControll {
	
	// アドレスリスト
	protected String[] addrList;
	// 172で始まるアドレスのフラグ.
	protected boolean[] ip172Flags;
	
	// init用ロック.
	protected Lock initLock = new ReentrantLock();
	
	// init完了フラグ.
	protected Flag initFlag = new Flag(false);
	
	// シングルトン.
	private static final IpPrivateAccessControll SNGL =
		new IpPrivateAccessControll();
	
	/**
	 * オブジェクトを取得.
	 * @return IpPrivateAccessControll オブジェクトが返却されます.
	 */
	public static final IpPrivateAccessControll getInstance() {
		return SNGL;
	}
	
	// コンストラクタ.
	protected IpPrivateAccessControll() {}
	
	// 初期化処理.
	protected void init() {
		if(initFlag.get()) {
			return;
		}
		initLock.lock();
		try {
			if(initFlag.get()) {
				return;
			}
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
			initFlag.set(true);
		} finally {
			initLock.unlock();
		}
	}
	
	@Override
	public boolean isAccess(Request req) {
		if(req instanceof HttpServerRequest) {
			init();
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
		}
		return false;
	}
}