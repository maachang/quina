package quina.jdbc.console;

import java.net.InetSocketAddress;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.Quina;
import quina.QuinaConfig;
import quina.QuinaService;
import quina.QuinaUtil;
import quina.annotation.log.LogDefine;
import quina.annotation.quina.QuinaServiceScoped;
import quina.http.Request;
import quina.http.server.HttpServerRequest;
import quina.logger.Log;
import quina.net.ThisMachineAddress;
import quina.util.Flag;
import quina.util.NumberUtil;
import quina.util.collection.IndexMap;
import quina.util.collection.ObjectList;
import quina.util.collection.TypesClass;

/**
 * QuinaJDBCConsoleService.
 */
@QuinaServiceScoped(name=QuinaJDBCConsoleService.SERVICE_AND_CONFIG_NAME)
public class QuinaJDBCConsoleService
	implements QuinaService {
	
	// サービス/コンフィグ名.
	protected static final String SERVICE_AND_CONFIG_NAME = "jdbcConsole";
	
	@LogDefine
	private Log log;
	
	// QuinaConfig.
	private QuinaConfig config = new QuinaConfig(
		// jdbcConsole.jsonで定義.
		SERVICE_AND_CONFIG_NAME
		// コンソールログインユーザー名.
		,"auth", TypesClass.Map, new IndexMap<String, Object>("root", "")
		// ローカルホストからのアクセスのみ有効.
		,"localhostOnly", TypesClass.Boolean, true
		// プライベートアドレスからのアクセス許可.
		,"privateAddress", TypesClass.Boolean, false
		// グローバルネットワークからのアクセス許可.
		,"globalAddress", TypesClass.Boolean, false
		// ログインタイムアウト(30分).
		,"loginTimeout", TypesClass.Long,
			QuinaJDBCConsoleConstants.DEF_LOGIN_TIMEOUT
		// クエリー結果受け取り件数.
		,"resultQuerySize", TypesClass.Integer,
			QuinaJDBCConsoleConstants.DEF_RESULT_QUERY_COUNT
	);
	
	// IPアドレスアクセス制御処理.
	private IpAccessControll ipAccessControll;
	
	// サービス開始フラグ.
	private final Flag startFlag = new Flag(false);
	
	// Read-Writeロックオブジェクト.
	private final ReentrantReadWriteLock lock =
		new ReentrantReadWriteLock();
	
	/**
	 * コンストラクタ.
	 */
	public QuinaJDBCConsoleService() {
		
	}
	
	/**
	 * QuinaJDBCConsoleServiceを取得.
	 * @return QuinaJDBCConsoleService
	 *		QuinaJDBCConsoleServiceが返却されます.
	 */
	public static final QuinaJDBCConsoleService getService() {
		return (QuinaJDBCConsoleService)
			Quina.get().getQuinaServiceManager().get(SERVICE_AND_CONFIG_NAME);
	}
	
	// Ipアクセス制限.
	protected static interface IpAccessControll {
		/**
		 * アクセスが制限されてるかチェック.
		 * @param req HttpRequestを設定します.
		 * @return boolean trueの場合アクセス可能です.
		 */
		public boolean isAccess(Request req);
	}
	
	// 全てのアクセスを不許可.
	private static final class NoneAccessControll
		implements IpAccessControll {
		@Override
		public boolean isAccess(Request req) {
			return false;
		}
	}
	
	// 127.0.0.1のアクセスを許可.
	private static final class IpLocalHostAccessControll
		implements IpAccessControll {
		@Override
		public boolean isAccess(Request req) {
			InetSocketAddress addr =((HttpServerRequest)req).getElement()
				.getRemoteAddress();
			return "127.0.0.1".equals(
				addr.getAddress().getHostAddress());
		}
	}
	
	// このサーバのプライベートIPアクセスでのアクセス許可.
	private static final class IpPrivateAccessControll
		implements IpAccessControll {
		String[] addrList;
		boolean[] ip172Flags;
		IpPrivateAccessControll() {
			int p;
			int len = ThisMachineAddress.size();
			String ip;
			ObjectList<Boolean> ip172 = new ObjectList<Boolean>();
			ObjectList<String> ipList = new ObjectList<String>();
			for(int i = 0; i < len; i ++) {
				ip = ThisMachineAddress.get(i);
				if("127.0.0.1".equals(ip)) {
					continue;
				} else if(ip.startsWith("192.168.")) {
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
			// ローカルホストをチェック.
			if("127.0.0.1".equals(ip)) {
				return true;
			}
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
	
	// 全てのIPを許可.
	private static final class IpGlobalAccessControll
		implements IpAccessControll {
		@Override
		public boolean isAccess(Request req) {
			return true;
		}
	}
	
	@Override
	public boolean loadConfig(String configDir) {
		boolean ret = QuinaService.super.loadConfig(configDir);
		// アクセス制御条件を取得.
		boolean localHost = config.getBoolean("localhostOnly");
		boolean privateAddress = config.getBoolean("privateAddress");
		boolean globalAddress = config.getBoolean("globalAddress");
		// フルアクセス許可.
		if(globalAddress && privateAddress && localHost) {
			ipAccessControll = new IpGlobalAccessControll();
		// イントラネット内のアクセス許可.
		} else if(privateAddress && localHost) {
			ipAccessControll = new IpPrivateAccessControll();
		// 127.0.0.1のみアクセス許可
		} else if(localHost) {
			ipAccessControll = new IpLocalHostAccessControll();
		// アクセス不可.
		} else {
			ipAccessControll = new NoneAccessControll();
		}
		return ret;
	}
	
	/**
	 * Ipアクセスコントロールオブジェクトを取得.
	 * @return IpAccessControll Ipアクセスコントロール
	 *                          オブジェクトが返却されます.
	 */
	public IpAccessControll getIpAccessControll() {
		return ipAccessControll;
	}
	
	@Override
	public ReadWriteLock getLock() {
		return lock;
	}
	
	@Override
	public QuinaConfig getConfig() {
		return config;
	}
	
	@Override
	public boolean isStartService() {
		return startFlag.get();
	}

	@Override
	public void startService() {
		if(!startFlag.setToGetBefore(true)) {
			// 開始ログ出力.
			QuinaUtil.startServiceLog(this);
		}
	}

	@Override
	public void stopService() {
		if(startFlag.setToGetBefore(false)) {
			// 停止ログ出力.
			QuinaUtil.stopServiceLog(this);
		}
	}
}