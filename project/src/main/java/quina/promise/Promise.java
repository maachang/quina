package quina.promise;

import quina.Quina;
import quina.QuinaException;
import quina.http.Request;
import quina.http.Response;
import quina.net.nio.tcp.Wait;

/**
 * Quina専用のPromise処理.
 *
 * ベースはjavascriptのPromiseを模倣した形での実装ですが、最低限の実装です.
 * また基本的には、この処理はQuina.get().start() を呼び出さないと利用出来ません.
 *
 * 利用方法としてはQuinaでのComponentオブジェクト実行内で利用が想定されています.
 *
 * 使い方はこんな感じです.
 * Promise promise = new Promise("hoge");
 * promise.then((action, value) -> {
 *   action.resolve(value + " moge");
 * });
 * promise.then((action, value) -> {
 *   action.getResponse().setContentType("text/html");
 *   action.send(value);
 * });
 * promise.exception((action, error) -> {
 *   action.sendError(error);
 * });
 * promise.start(request, response);
 *
 * 基本的にはコールバック地獄を回避するための
 * "最低限"のPromise実装を提供します.
 *
 * またPromiseはコンポーネントは、基本的に「非同期モード」のものしか
 * 実行出来ません.
 *
 * ですが、以下のように行う事で、動機モードのコンポーネントでも実装可能です.
 * <例>
 *
 * Quina.getRouter().route("/promiseSync",
 * (SyncComponent)(request, response) -> {
 *   Promise p = new Promise("abc")
 *     .then((action, value) -> {
 *       action.resolve(value + " def");
 *   })
 *   .start(request, response);
 *   return p.waitTo();
 * })
 * .start()
 * .waitTo();
 */
public class Promise {
	// 初期起動用コール.
	protected PromiseFromEndWorkerElement firstCall = null;
	// promiseアクション.
	protected PromiseAction action;

	/**
	 * コンストラクタ.
	 */
	public Promise() {
		this.action = new PromiseAction(null);
	}

	/**
	 * コンストラクタ.
	 * @param param パラメータを設定します.
	 */
	public Promise(Object param) {
		this.action = new PromiseAction(param);
	}

	/**
	 * コンストラクタ.
	 * @param call 初期実行処理を設定します.
	 */
	public Promise(PromiseFromEndCall call) {
		this.action = new PromiseAction(null);
		this.firstCall = new PromiseFromEndWorkerElement(this.action, call);
	}

	// Promiseが開始している場合はエラー出力.
	private static final void checkStartPromise(Promise p) {
		if(p.isStart()) {
			throw new QuinaException(
				"Promises that have already started cannot be registered.");
		}
	}

	// Promiseリストに対する実行開始チェックを行います.
	private static final void checkStartPromiseList(Promise[] list, int len) {
		for(int i = 0; i < len; i ++) {
			checkStartPromise(list[i]);
		}
	}

	/**
	 * Promise.all実行を行います.
	 * 設定された全てのPromise実行が完了するまで待機します.
	 * ただし実行結果がrejectされたものが存在する場合、最初に検知された
	 * reject結果を返却します.
	 * @param list Promise群を設定します.
	 * @return Promiseが返却されます.
	 */
	public static final Promise all(Promise... list) {
		final int len = list.length;
		// 既に実行されている場合は例外返却.
		checkStartPromiseList(list, len);
		// 新しいPromiseを生成して、そこで登録されたPromise群を実行.
		Promise ret = new Promise((action) -> {
			int i;
			// 既に実行されている場合は例外返却.
			checkStartPromiseList(list, len);
			// 非同期実行.
			for(i = 0; i < len; i ++) {
				list[i].start(action.getRequest(), action.getResponse());
			}
			final Object[] params = new Object[len];
			// 実行されたPromise群の待機.
			for(i = 0; i < len; i ++) {
				// 待機結果をリスト取得.
				params[i] = list[i].waitTo();
				// reject条件を検知した場合はリジェクト内容を返却.
				if(list[i].getStatus() == PromiseStatus.Rejected) {
					action.reject(list[i]);
					return;
				}
			}
			// 全てが正常な場合はリスト返却.
			action.resolve(params);
		});
		return ret;
	}

	/**
	 * Promise.allSettled実行を行います.
	 * 設定された全てのPromise実行が完了するまで待機します.
	 * @param list Promise群を設定します.
	 * @return Promiseが返却されます.
	 */
	public static final Promise allSettled(Promise... list) {
		final int len = list.length;
		// 既に実行されている場合は例外返却.
		checkStartPromiseList(list, len);
		// 新しいPromiseを生成して、そこで登録されたPromise群を実行.
		Promise ret = new Promise((action) -> {
			int i;
			// 既に実行されている場合は例外返却.
			checkStartPromiseList(list, len);
			// 非同期実行.
			for(i = 0; i < len; i ++) {
				list[i].start(action.getRequest(), action.getResponse());
			}
			final Object[] params = new Object[len];
			// 実行されたPromise群の待機.
			for(i = 0; i < len; i ++) {
				// 待機結果をリスト取得.
				params[i] = list[i].waitTo();
			}
			// 処理結果群を次の非同期処理に提供.
			action.resolve(params);
		});
		return ret;
	}

	/**
	 * Promise.any実行を行います.
	 * 設定された全てのPromiseで最初に正常終了したも結果を返却します.
	 * @param list Promise群を設定します.
	 * @return Promiseが返却されます.
	 */
	public static final Promise any(Promise... list) {
		final int len = list.length;
		// 既に実行されている場合は例外返却.
		checkStartPromiseList(list, len);
		// 新しいPromiseを生成して、そこで登録されたPromise群を実行.
		Promise ret = new Promise((action) -> {
			int i;
			Promise p;
			// waitオブジェクト.
			final Wait w = new Wait();
			// 既に実行されている場合は例外返却.
			for(i = 0; i < len; i ++) {
				checkStartPromise(p = list[i]);
				// 共通のWaitオブジェクトを設定して、登録された複数のPrimiseの内
				// 一番最初に処理完了した場合にその他も停止させる.
				p.action.setWaitObject(true, w);
			}
			p = null;
			// 非同期実行.
			for(i = 0; i < len; i ++) {
				list[i].start(action.getRequest(), action.getResponse());
			}
			// 終了したものを対象として返却.
			for(i = 0; i < len; i ++) {
				p = list[i];
				// 終了まで待機して、対象Promiseが終了している場合は
				// その内容を返却する.
				if(p.action.waitTo(null, -1L)) {
					if(p.getStatus() == PromiseStatus.Fulfilled) {
						action.resolve(p.waitTo());
					}
				}
			}
			// 返却可能なresolve条件が存在しない場合.
			throw new QuinaException("No Promise in Promise.any was resolved.");
		});
		return ret;
	}

	/**
	 * Promise.race実行を行います.
	 * 設定された全てのPromise実行で最初に処理された結果を返却します.
	 * @param list Promise群を設定します.
	 * @return Promiseが返却されます.
	 */
	public static final Promise race(Promise... list) {
		final int len = list.length;
		// 既に実行されている場合は例外返却.
		checkStartPromiseList(list, len);
		// 新しいPromiseを生成して、そこで登録されたPromise群を実行.
		Promise ret = new Promise((action) -> {
			int i;
			Promise p;
			// waitオブジェクト.
			final Wait w = new Wait();
			// 既に実行されている場合は例外返却.
			for(i = 0; i < len; i ++) {
				checkStartPromise(p = list[i]);
				// 共通のWaitオブジェクトを設定して、登録された複数のPrimiseの内
				// 一番最初に処理完了した場合にその他も停止させる.
				p.action.setWaitObject(true, w);
			}
			p = null;
			// 非同期実行.
			for(i = 0; i < len; i ++) {
				list[i].start(action.getRequest(), action.getResponse());
			}
			// 終了したものを対象として返却.
			for(i = 0; i < len; i ++) {
				p = list[i];
				// 終了まで待機して、対象Promiseが終了している場合は
				// その内容を返却する.
				if(p.action.waitTo(null, -1L)) {
					if(p.getStatus() == PromiseStatus.Fulfilled) {
						action.resolve(p.waitTo());
					} else {
						action.reject(p.waitTo());
					}
				}
			}
		});
		return ret;
	}

	/**
	 * 正常系実行処理を設定します.
	 * resolveが呼び出された時に呼び出されます.
	 * @param call 実行処理を設定します.
	 * @return Promise Promiseオブジェクトが返却されます.
	 */
	public Promise then(PromiseCall call) {
		action.then(call);
		return this;
	}

	/**
	 * 正常系実行処理を設定します.
	 * resolveが呼び出された時に呼び出されます.
	 * @param call 実行処理を設定します.
	 * @param errorCall 異常系実行処理を設定します.
	 * @return Promise Promiseオブジェクトが返却されます.
	 */
	public Promise then(PromiseCall call, PromiseCall errorCall) {
		action.then(call);
		action.error(errorCall);
		return this;
	}

	/**
	 * 異常系実行処理を設定します.
	 * rejectが呼び出された時に呼び出します.
	 * @param call 実行処理を設定します.
	 * @return Promise Promiseオブジェクトが返却されます.
	 */
	public Promise error(PromiseCall call) {
		action.error(call);
		return this;
	}

	/**
	 * 正常系、異常系に関係なく呼び出される処理を設定します.
	 * @param call 実行処理を設定します.
	 * @return Promise Promiseオブジェクトが返却されます.
	 */
	public Promise allways(PromiseCall call) {
		action.allways(call);
		return this;
	}

	/**
	 * 最後に必ず呼び出される処理を設定します.
	 * @param call Promise終了呼び出しコールを設定します.
	 * @return Promise Promiseオブジェクトが返却されます.
	 */
	public Promise finalyTo(PromiseFromEndCall call) {
		action.finalyTo(call);
		return this;
	}

	/**
	 * Promiseを開始.
	 * @param req HttpRequestを設定します.
	 * @param res HttpResponseを設定します.
	 * @return Promise Promiseオブジェクトが返却されます.
	 */
	public Promise start(Request req, Response<?> res) {
		// 初期実行が定義されている場合.
		if(firstCall != null) {
			// actionを自動実行させずに起動.
			action.start(false, req, res);
			// firstCallのワーカー実行.
			Quina.get().registerWorker(firstCall);
		// 初期実行が定義されていない場合.
		} else {
			// actionを自動実行で起動.
			action.start(true, req, res);
		}
		return this;
	}

	/**
	 * Promise処理終了まで待機する.
	 * @return Object 処理結果が返却されます.
	 */
	public Object waitTo() {
		return action.waitTo();
	}

	/**
	 * Promiseが開始しているか取得.
	 * @return boolean trueの場合は開始しています.
	 */
	public boolean isStart() {
		return action.isStartPromise();
	}

	/**
	 * Promiseが終了しているかチェック.
	 * @return boolean trueの場合は終了しています.
	 */
	public boolean isExit() {
		return action.isExitPromise();
	}

	/**
	 * Promiseステータスを取得.
	 * @return PromiseStatus Promiseのステータスが返却されます.
	 */
	public PromiseStatus getStatus() {
		return action.getStatus();
	}
}
