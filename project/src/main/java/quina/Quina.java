package quina;

import quina.http.server.HttpServerService;
import quina.net.nio.tcp.worker.NioWorkerService;

/**
 * Quina.
 */
public class Quina {
	// コンストラクタ.
	private Quina() {}

	// シングルトン.
	private static final Quina SNGL = new Quina();

	// ルータオブジェクト.
	private Router router = new Router();

	// コンフィグディレクトリ.
	private String configDir = "./";

	// HttpServerService.
	private HttpServerService httpServerService = new HttpServerService();

	// NioWorkerService.
	private NioWorkerService nioWorkerService = new NioWorkerService();

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
}
