package quina.jdbc.console;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.Quina;
import quina.QuinaConfig;
import quina.QuinaService;
import quina.QuinaUtil;
import quina.annotation.log.LogDefine;
import quina.annotation.quina.QuinaServiceScoped;
import quina.http.controll.AccessControll;
import quina.http.controll.AnyAccessControll;
import quina.http.controll.IpLocalHostAccessControll;
import quina.http.controll.IpPrivateAccessControll;
import quina.http.controll.NoneAccessControll;
import quina.logger.Log;
import quina.util.Flag;
import quina.util.collection.IndexMap;
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
	private AccessControll ipAccessControll;
	
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
	
	@Override
	public boolean loadConfig(String configDir) {
		wlock();
		try {
			boolean ret = QuinaService.super.loadConfig(configDir);
			// アクセス制御条件を取得.
			boolean localHost = config.getBoolean("localhostOnly");
			boolean privateAddress = config.getBoolean("privateAddress");
			boolean globalAddress = config.getBoolean("globalAddress");
			// フルアクセス許可.
			if(globalAddress && privateAddress && localHost) {
				ipAccessControll = new AnyAccessControll();
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
		} finally {
			wulock();
		}
	}
	
	/**
	 * アクセスコントロールオブジェクトを取得.
	 * @return AccessControll アクセスコントロール
	 *                          オブジェクトが返却されます.
	 */
	public AccessControll getAccessControll() {
		rlock();
		try {
			return ipAccessControll;
		} finally {
			rulock();
		}
	}
	
	@Override
	public ReadWriteLock getLock() {
		return lock;
	}
	
	@Override
	public QuinaConfig getConfig() {
		rlock();
		try {
			return config;
		} finally {
			rulock();
		}
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