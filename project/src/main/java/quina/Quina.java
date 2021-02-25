package quina;

import quina.http.server.HttpServerInfo;
import quina.http.server.HttpServerService;
import quina.http.worker.HttpWorkerInfo;
import quina.http.worker.HttpWorkerService;
import quina.util.FileUtil;

/**
 * Quina.
 */
public class Quina {
	// シングルトン.
	private static final Quina SNGL = new Quina();

	// ルータオブジェクト.
	private Router router = new Router();

	// コンフィグディレクトリ.
	private String configDir = "./";

	// HttpServerService.
	private HttpServerService httpServerService;

	// httpWorkerService.
	private HttpWorkerService httpWorkerService;

	// コンストラクタ.
	private Quina() {
		this.httpWorkerService = new HttpWorkerService();
		this.httpServerService = new HttpServerService(this.httpWorkerService);
	}

	/**
	 * quinaを取得.
	 * @return Quina quinaが返却されます.
	 */
	public static final Quina get() {
		return SNGL;
	}

	/**
	 * routerを取得.
	 * @return Router ルータが返却されます.
	 */
	public static final Router router() {
		return SNGL.getRouter();
	}

	/**
	 * ルータを取得.
	 * @return Router ルータが返却されます.
	 */
	public Router getRouter() {
		return router;
	}

	/**
	 * 既にサービスが稼働/停止している場合はエラー返却.
	 * @param flg [true]の場合、開始中 [false]の場合、停止中の場合、
	 *            エラーが発生します.
	 */
	protected void check(boolean flg) {
		httpWorkerService.check(flg);
		httpServerService.check(flg);
	}

	/**
	 * コンフィグディレクトリを設定.
	 * @param configDir コンフィグディレクトリを設定します.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina setConfigDirectory(String configDir) {
		check(true);
		try {
			configDir = FileUtil.getFullPath(configDir);
			if(!configDir.endsWith("/")) {
				configDir = configDir + "/";
			}
			this.configDir = configDir;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
		return this;
	}

	/**
	 * コンフィグディレクトリを取得.
	 * @return String 設定されているコンフィグディレクトリが返却されます.
	 */
	public String getConfigDirectory() {
		return configDir;
	}

	/**
	 * コンフィグ情報を読み込む.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina loadConfig() {
		return loadConfig(null);
	}

	/**
	 * コンフィグ情報を読み込む.
	 * @param configDir コンフィグディレクトリを設定します.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina loadConfig(String configDir) {
		check(true);
		if(configDir != null && !configDir.isEmpty()) {
			setConfigDirectory(configDir);
		}
		httpWorkerService.readConfig(this.configDir);
		httpServerService.readConfig(this.configDir);
		return this;
	}

	/**
	 * HttpWorkerInfoを取得.
	 * @return HttpWorkerInfo HttpWorkerInfoが返却されます.
	 */
	public HttpWorkerInfo getHttpWorkerInfo() {
		return (HttpWorkerInfo)httpWorkerService.getInfo();
	}

	/**
	 * HttpServerInfoを取得.
	 * @return HttpServerInfo HttpServerInfoが返却されます.
	 */
	public HttpServerInfo getHttpServerInfo() {
		return (HttpServerInfo)httpServerService.getInfo();
	}

	/**
	 * Quinaの開始処理が呼ばれたかチェック.
	 * @return boolean [true]の場合開始しています.
	 */
	public boolean isStart() {
		return httpServerService.isStartService() &&
			httpWorkerService.isStartService();
	}

	/**
	 * Quina開始処理.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina start() {
		check(true);
		try {
			httpWorkerService.startService();
			httpWorkerService.waitToStartup();
			httpServerService.startService();
			httpServerService.waitToStartup();
			return this;
		} catch(QuinaException qe) {
			try {
				stop();
			} catch(Exception e) {}
			throw qe;
		}
	}

	/**
 	 * Quinaのサービスがすべて開始済みかチェック.
	 * @return boolean trueの場合、全てのサービスが開始しています.
	 */
	public boolean isStartup() {
		return httpWorkerService.isStartup() && httpServerService.isStartup();
	}

	/**
	 * Quina終了処理.
	 * @return Quina Quinaオブジェクトが返却されます.
	 */
	public Quina stop() {
		httpServerService.stopService();
		httpWorkerService.waitToExit();
		httpWorkerService.stopService();
		httpServerService.waitToExit();
		return this;
	}

	/**
	 * Quinaのサービスがすべて終了したかチェック.
	 * @return boolean trueの場合、全てのサービスが終了しています.
	 */
	public boolean isExit() {
		return httpWorkerService.isExit() && httpServerService.isExit();
	}
}
