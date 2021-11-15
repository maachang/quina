package quina.jdbc.console;

import quina.Quina;
import quina.QuinaConfig;
import quina.QuinaService;
import quina.annotation.log.LogDefine;
import quina.annotation.quina.QuinaServiceScoped;
import quina.logger.Log;
import quina.util.Flag;
import quina.util.collection.TypesClass;

/**
 * QuinaJDBCConsoleService.
 */
@QuinaServiceScoped("jdbcConsole")
public class QuinaJDBCConsoleService
	implements QuinaService {
	
	@LogDefine
	private Log log;
	
	// QuinaConfig.
	private QuinaConfig config = new QuinaConfig(
		// jdbcConsole.jsonで定義.
		"jdbcConsole"
		// コンソールログインユーザー名.
		,"user", TypesClass.String, "root"
		// コンソールログインパスワード.
		,"password", TypesClass.String, ""
		// ローカルホストからのアクセスのみ有効.
		,"localhostOnly", TypesClass.Boolean, true
		// 同一ローカルネットワークからのアクセス許可.
		,"localAddress", TypesClass.Boolean, false
		// グローバルネットワークからのアクセス許可.
		,"globalAddress", TypesClass.Boolean, false
		// ログインタイムアウト(30分).
		,"loginTimeout", TypesClass.Long, 1800000
	);
	
	// サービス開始フラグ.
	private final Flag startFlag = new Flag(false);
	
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
			Quina.get().getQuinaServiceManager().get("jdbcConsole");
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
			log.info("@ startService " + this.getClass().getName());
		}
	}

	@Override
	public void stopService() {
		if(startFlag.setToGetBefore(false)) {
			log.info("@ stopService " + this.getClass().getName());
		}
	}
}