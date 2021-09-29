package quina;

import quina.http.server.HttpServerWorkerCallHandler;
import quina.promise.PromiseWorkerCallHandler;
import quina.worker.QuinaWorkerCallHandler;

/**
 * Quina定数定義.
 */
public final class QuinaConstants {
	private QuinaConstants() {}

	/** HTTPサーバ名. **/
	public static final String SERVER_NAME = "quina";

	/** HTTPサーババージョン. **/
	public static final String SERVER_VERSION = "0.0.1";
	
	// ワーカーコールハンドラ群.
	protected static final QuinaWorkerCallHandler[] REG_WORKER_CALL_HANDLES =
		new QuinaWorkerCallHandler[] {
			new HttpServerWorkerCallHandler(),
			new PromiseWorkerCallHandler()
	};
}
