package quina;

import quina.component.file.EtagManagerInfo;
import quina.exception.QuinaException;
import quina.http.controll.ipv4.IpPermissionAccessControllService;
import quina.http.server.HttpServerService;
import quina.logger.LogFactory;
import quina.route.Router;
import quina.route.annotation.AnnotationRoute;
import quina.shutdown.ShutdownManager;
import quina.worker.QuinaWorkerService;

/**
 * Quinaスタートアップ処理.
 */
final class QuinaStartup {
	
	// QuinaMembers.
	private final QuinaMembers quinaMembers;
	
	/**
	 * コンストラクタ.
	 * @param quinaMembers QuinaMembersを設定します.
	 */
	QuinaStartup(QuinaMembers quinaMembers) {
		this.quinaMembers = quinaMembers;
	}
	
	
	/**
	 * 全てのQuinaサービスを開始処理してシャットダウン待機処理.
	 */
	public void startAwait() {
		start();
		await();
	}
	
	/**
	 * 全てのQuinaサービスを開始処理.
	 */
	public void start() {
		quinaMembers.checkService(true);
		
		// Quinaルータ.
		final Router router = quinaMembers.router;
		// Quinaワーカーサービス.
		final QuinaWorkerService quinaWorkerService =
			quinaMembers.quinaWorkerService;
		// Quinaサービスマネージャ.
		final QuinaServiceManager quinaServiceManager =
			quinaMembers.quinaServiceManager;
		// Httpサーバーサービス.
		final HttpServerService httpServerService =
			quinaMembers.httpServerService;
		// IpV4パーミッションアクセスコントロールサービス.
		final IpPermissionAccessControllService
			ipPermissionAccessControllService =
				quinaMembers.ipPermissionAccessControllService;
		
		try {
			// AutoRouter読み込みを実行.
			AnnotationRoute.autoRoute();
			// Etag管理情報を取得.
			final EtagManagerInfo etagManagerInfo =
				router.getEtagManagerInfo();
			// Etag管理情報が確定されていない場合は確定する.
			if(!etagManagerInfo.isDone()) {
				etagManagerInfo.done();
			}
			// QuinaServiceManagerを完了させる.
			quinaServiceManager.fix();
			// 登録されたQuinaServiceを起動.
			QuinaService qs;
			// 登録ID順にソート処理.
			quinaServiceManager.sort();
			// オリジナルで登録されたサービスの実行.
			final int len = quinaServiceManager.size();
			for(int i = 0; i < len; i ++) {
				qs = quinaServiceManager.get(i);
				// サービス開始.
				qs.startService();
				// 開始が完了するまで待機.
				qs.awaitStartup();
				qs = null;
			}
			// 確定しなかったサービス定義を削除.
			quinaServiceManager.clearDefine();
			// 基本サービスを起動.
			// ワーカー起動.
			quinaWorkerService.startService();
			// 開始が完了するまで待機.
			quinaWorkerService.awaitStartup();
			// IpV4パーミッションアクセスコントロールサービス起動.
			ipPermissionAccessControllService.startService();
			// 開始が完了するまで待機.
			ipPermissionAccessControllService.awaitStartup();
			// HTTPサーバー起動.
			httpServerService.startService();
			// 開始が完了するまで待機.
			httpServerService.awaitStartup();
		} catch(QuinaException qe) {
			try {
				stop();
			} catch(Exception e) {}
			throw qe;
		}
	}
	
	/**
	 * 全てのQuinaサービスを終了開始処理.
	 */
	public void stop() {
		quinaMembers.checkNoneExecuteInit();
		
		// Quinaワーカーサービス.
		final QuinaWorkerService quinaWorkerService =
			quinaMembers.quinaWorkerService;
		// Quinaサービスマネージャ.
		final QuinaServiceManager quinaServiceManager =
			quinaMembers.quinaServiceManager;
		// Httpサーバーサービス.
		final HttpServerService httpServerService =
			quinaMembers.httpServerService;
		// IpV4パーミッションアクセスコントロールサービス.
		final IpPermissionAccessControllService
			ipPermissionAccessControllService =
				quinaMembers.ipPermissionAccessControllService;
		
		// Httpサーバーサービスを停止.
		try {
			httpServerService.stopService();
			httpServerService.awaitExit();
		} catch(Exception e) {
			LogFactory.getInstance().get().error(
				"## error stop service: httpServerService", e);
		}
		// IpV4パーミッションアクセスコントロールサービスを停止.
		try {
			ipPermissionAccessControllService.stopService();
			ipPermissionAccessControllService.awaitExit();
		} catch(Exception e) {
			LogFactory.getInstance().get().error(
				"## error stop service: ipPermissionAccessControllService", e);
		}
		// ワーカー停止.
		try {
			quinaWorkerService.stopService();
			quinaWorkerService.awaitExit();
		} catch(Exception e) {
			LogFactory.getInstance().get().error(
				"## error stop service: quinaWorkerService", e);
		}
		// 登録されたサービスを後ろから停止.
		QuinaService qs = null;
		final int len = quinaServiceManager.size();
		for(int i = len - 1; i >= 0; i --) {
			qs = null;
			// エラーが発生しても続行させる.
			try {
				qs = quinaServiceManager.get(i);
				qs.stopService();
				qs.awaitExit();
			} catch(Exception e) {
				LogFactory.getInstance().get().error(
					"## error stop service: " +
					quinaServiceManager.nameAt(i), e);
			}
		}
		// ログの停止.
		try {
			LogFactory.getInstance().stopLogWriteWorker();
		} catch(Exception e) {
			LogFactory.getInstance().get().error(
				"## error stop service: Log", e);
		}
	}

	/**
	 * Quinaサービス開始処理[start()]が呼ばれたかチェック.
	 * @return boolean [true]の場合開始しています.
	 */
	public boolean isStart() {
		quinaMembers.checkNoneExecuteInit();
		
		// Quinaワーカーサービス.
		final QuinaWorkerService quinaWorkerService =
			quinaMembers.quinaWorkerService;
		// Quinaサービスマネージャ.
		final QuinaServiceManager quinaServiceManager =
			quinaMembers.quinaServiceManager;
		// Httpサーバーサービス.
		final HttpServerService httpServerService =
			quinaMembers.httpServerService;
		// IpV4パーミッションアクセスコントロールサービス.
		final IpPermissionAccessControllService
			ipPermissionAccessControllService =
				quinaMembers.ipPermissionAccessControllService;
		
		// 基本サービスの開始処理[start()]が呼び出された場合.
		if(ipPermissionAccessControllService.isStartService() &&
			httpServerService.isStartService() &&
			quinaWorkerService.isStartService()) {
			// 登録サービスの開始処理[start()]が呼び出されたかチェック.
			final int len = quinaServiceManager.size();
			for(int i = 0; i < len; i ++) {
				if(!quinaServiceManager.get(i).isStartService()) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 全てのQuinaサービスが起動済みかチェック.
	 * @return boolean trueの場合、全てのQuinaサービスが起動しています.
	 */
	public boolean isStarted() {
		quinaMembers.checkNoneExecuteInit();
		
		// Quinaワーカーサービス.
		final QuinaWorkerService quinaWorkerService =
			quinaMembers.quinaWorkerService;
		// Quinaサービスマネージャ.
		final QuinaServiceManager quinaServiceManager =
			quinaMembers.quinaServiceManager;
		// Httpサーバーサービス.
		final HttpServerService httpServerService =
			quinaMembers.httpServerService;
		// IpV4パーミッションアクセスコントロールサービス.
		final IpPermissionAccessControllService
			ipPermissionAccessControllService =
				quinaMembers.ipPermissionAccessControllService;
		
		// 基本サービスが起動している場合.
		if(ipPermissionAccessControllService.isStarted() &&
			httpServerService.isStarted() &&
			quinaWorkerService.isStarted()) {
			// 登録サービスが起動しているかチェック.
			final int len = quinaServiceManager.size();
			for(int i = 0; i < len; i ++) {
				if(!quinaServiceManager.get(i).isStarted()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 全てのQuinaサービスが起動済みになるまで待機.
	 */
	public void awaitStarted() {
		quinaMembers.checkNoneExecuteInit();
		// 全てのQuinaサービスが起動済みになるまで待機.
		while(!isExit()) {
			QuinaUtil.sleep(50L);
		}
	}

	/**
	 * 全てのQuinaのサービスが終了完了したかチェック.
	 * @return boolean trueの場合、全てのサービスが終了完了しています.
	 */
	public boolean isExit() {
		quinaMembers.checkNoneExecuteInit();
		
		// Quinaワーカーサービス.
		final QuinaWorkerService quinaWorkerService =
			quinaMembers.quinaWorkerService;
		// Quinaサービスマネージャ.
		final QuinaServiceManager quinaServiceManager =
			quinaMembers.quinaServiceManager;
		// Httpサーバーサービス.
		final HttpServerService httpServerService =
			quinaMembers.httpServerService;
		// IpV4パーミッションアクセスコントロールサービス.
		final IpPermissionAccessControllService
			ipPermissionAccessControllService =
				quinaMembers.ipPermissionAccessControllService;
		
		// 登録サービスの停止チェック.
		final int len = quinaServiceManager.size();
		for(int i = 0; i < len; i ++) {
			if(!quinaServiceManager.get(i).isExit()) {
				return false;
			}
		}
		// 基本サービスの停止チェック.
		boolean ret = quinaWorkerService.isExit() &&
			ipPermissionAccessControllService.isExit() &&
			httpServerService.isExit() &&
			LogFactory.getInstance().isExitLogWriteWorker();
		return ret;
	}
	
	/**
	 * 全てのQuinaサービスが停止完了するまで待機.
	 */
	public void await() {
		quinaMembers.checkNoneExecuteInit();
		
		// シャットダウンマネージャ.
		final ShutdownManager shutdownManager =
			quinaMembers.shutdownManager;
		
		// シャットダウンマネージャが開始されていない場合.
		if(!shutdownManager.getInfo().isStart()) {
			// シャットダウンマネージャを開始.
			shutdownManager.startShutdown();
			LogFactory.getInstance().get()
				.info("### start ShutdownManager");
			// すべてのサービスが終了するまで待機.
			while(!isExit()) {
				QuinaUtil.sleep();
			}
		}
	}
}
