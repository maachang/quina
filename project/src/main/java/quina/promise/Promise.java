package quina.promise;

import quina.exception.QuinaException;
import quina.net.nio.tcp.Wait;
import quina.util.AtomicNumber;
import quina.util.Flag;

/**
 * Quina専用のPromise処理.
 *
 * javascriptのPromiseを模倣した形での実装で、内容は最低限の実装
 * となります.
 *
 * 使い方はこんな感じです.
 * Router router = Quina.router();
 * router.route("/", (RESTfulGetSync)(req, res, params) -> {
 *   Promise promise = new Promise("hoge");
 *   promise.then((action, value) -> {
 *     action.resolve(value + " moge");
 *   });
 *   promise.then((action, value) -> {
 *     response.setContentType("text/html");
 *     response.send(value);
 *   });
 *   promise.error((action, error) -> {
 *     response.sendError(error);
 *   });
 *   promise.start();
 * }
 *
 * 基本的にはコールバック地獄を回避するための
 * 最低限のPromise実装を提供します.
 *
 * またPromiseはコンポーネントは、基本的に「非同期モード」のものしか
 * 実行出来ません.
 *
 * ですが、以下のように行う事で、同期モードのコンポーネントでも実装可能です.
 * <例>
 *
 * Quina.getRouter().route("/promiseSync",
 * (SyncComponent)(request, response) -> {
 *   Promise p = Promise.form("abc")
 *     .then((action, value) -> {
 *       action.resolve(value + " def");
 *   })
 *   .start();
 *   return p.await();
 * })
 * .start()
 * .await();
 */
public class Promise {
	// 初期起動用コール.
	protected PromiseFromEndWorker firstCall = null;
	// promiseアクション.
	protected PromiseActionImpl action;
	
	/**
	 * コンストラクタ.
	 */
	public Promise() {
		this.action = new PromiseActionImpl(null);
	}

	/**
	 * コンストラクタ.
	 * @param param パラメータを設定します.
	 */
	public Promise(Object param) {
		this.action = new PromiseActionImpl(param);
	}

	/**
	 * コンストラクタ.
	 * @param call 初期実行処理を設定します.
	 */
	public Promise(PromiseFromEndCall call) {
		this.action = new PromiseActionImpl(null);
		this.firstCall = new PromiseFromEndWorker(this.action, call);
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
	 * インスタンス生成.
	 * @return Promise Promiseオブジェクトが返却されます.
	 */
	public static final Promise form() {
		return new Promise();
	}
	
	/**
	 * インスタンス生成.
	 * @param param パラメータを設定します.
	 * @return Promise Promiseオブジェクトが返却されます.
	 */
	public static final Promise form(Object param) {
		return new Promise(param);
	}
	
	/**
	 * インスタンス生成.
	 * @param call 初期実行処理を設定します.
	 * @return Promise Promiseオブジェクトが返却されます.
	 */
	public static final Promise form(PromiseFromEndCall call) {
		return new Promise(call);
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
		return new Promise((action) -> {
			int i;
			// 既に実行されている場合は例外返却.
			checkStartPromiseList(list, len);
			// 設定されたPromise群を順次実行.
			for(i = 0; i < len; i ++) {
				list[i].start();
			}
			final Object[] params = new Object[len];
			// 実行されたPromise群の待機.
			for(i = 0; i < len; i ++) {
				// それぞれの処理結果を取得.
				params[i] = list[i].await();
				// reject条件を検知した場合はリジェクト内容を返却.
				if(list[i].getStatus() == PromiseStatus.Rejected) {
					action.reject(params[i]);
					return;
				}
			}
			// 全てが正常な場合はリスト返却.
			action.resolve(params);
		});
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
				list[i].start();
			}
			final PromiseValue[] params = new PromiseValue[len];
			// 実行されたPromise群の待機.
			for(i = 0; i < len; i ++) {
				// 待機結果をリスト取得.
				params[i] = new PromiseValue(
					list[i].getStatus(), list[i].await());
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
		final AnyCallback anyCallback = new AnyCallback(len);
		// 新しいPromiseを生成して、そこで登録されたPromise群を実行.
		Promise ret = new Promise((action) -> {
			int i;
			// 既に実行されている場合は例外返却.
			checkStartPromiseList(list, len);
			// Anyコールバックでの非同期実行.
			Promise p;
			for(i = 0; i < len; i ++) {
				p = list[i];
				// anyCallbackをセット.
				p.action.setAwaitCall(anyCallback);
				// anyCallbackで管理してるWaitオブジェクトをsignalAllで定義.
				p.action.setWaitObject(anyCallback.getWait());
				// 非同期実行.
				list[i].start();
			}
			// anyCallbackが正常終了、もしくは全部エラー終了の場合
			// awaitが解かれるので待機する.
			list[0].await();
			// 全てがエラー終了の場合.
			if(!anyCallback.isSuccess()) {
				// 返却可能なresolve条件が存在しない場合.
				throw new QuinaException("No Promise in Promise.any was resolved.");
			}
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
				p.action.setWaitObject(w);
			}
			p = null;
			// 非同期実行.
			for(i = 0; i < len; i ++) {
				list[i].start();
			}
			// 終了したものを対象として返却.
			for(i = 0; i < len; i ++) {
				p = list[i];
				// 終了まで待機して、対象Promiseが終了している場合は
				// その内容を返却する.
				if(p.action.await(null, -1L)) {
					if(p.isExit()) {
						if(p.getStatus() == PromiseStatus.Fulfilled) {
							action.resolve(p.await());
						} else {
							action.reject(p.await());
						}
						break;
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
	public Promise any(PromiseCall call) {
		action.any(call);
		return this;
	}

	/**
	 * 最後に必ず呼び出される処理を設定します.
	 * @param call Promise終了呼び出しコールを設定します.
	 * @return Promise Promiseオブジェクトが返却されます.
	 */
	public Promise finalize(PromiseFromEndCall call) {
		action.finalize(call);
		return this;
	}

	/**
	 * Promiseを開始.
	 * こちらでの呼び出しの場合は[action.send(...)]系の処理が実行出来ません.
	 * @return Promise Promiseオブジェクトが返却されます.
	 */
	public Promise start() {
		// 初期実行が定義されている場合.
		if(firstCall != null) {
			final PromiseWorkerCall call = firstCall;
			firstCall = null;
			// actionを自動実行させずに起動.
			action.start(false);
			// firstCallのワーカー実行.
			PromiseWorkerManager.getInstance().push(call);
		// 初期実行が定義されていない場合.
		} else {
			// actionを自動実行で起動.
			action.start(true);
		}
		return this;
	}

	/**
	 * Promise処理終了まで待機する.
	 * @return Object 処理結果が返却されます.
	 */
	public Object await() {
		return action.await();
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

	// any用の処理終了コールバック.
	private static final class AnyCallback implements PromiseAwaitCall {
		private final int allAnyPromiseLength;
		private final Wait waitObject = new Wait();
		private final Flag successFlag = new Flag(false);
		private final AtomicNumber rejectCounter = new AtomicNumber(0);

		/**
		 * コンストラクタ.
		 * @param len Anyで実施するPromiseの数を設定します.
		 */
		public AnyCallback(int len) {
			allAnyPromiseLength = len;
		}

		@Override
		public void call(PromiseActionImpl action, PromiseStatus status, Object value) {
			// 正常終了の場合.
			// 初めての正常終了のみresolve実行.
			if(status == PromiseStatus.Fulfilled &&
				!successFlag.setToGetBefore(true)) {
				// 正常処理.
				action.resolve(value);
				// 待機処理終了.
				waitObject.signalAll();
			// 正常終了でない場合.
			} else {
				// リジェクトカウンターをセット.
				rejectCounter.inc();
				// 全てが失敗の場合.
				if(rejectCounter.get() >= allAnyPromiseLength) {
					// 待機処理終了.
					waitObject.signalAll();
				}
			}
		}

		/**
		 * anyで設定されたPromise群の一斉Waitを取得.
		 * action.setWaitObject(thisObject.getWait())の感じで設定します.
		 * thisObject = このオブジェクト.
		 *
		 * @return Wait Anyに対する処理停止用のWaitオブジェクトが返却されます.
		 *              この処理はanyにおいて以下のようにanyで定義します.
		 *              // anyCallbackをセット.
		 *              p.action.setAwaitCall(anyCallback);
		 *              // anyCallbackで管理してるWaitオブジェクトをsignalAllで定義.
		 *              p.action.setWaitObject(anyCallback.getWait());
		 */
		public Wait getWait() {
			return waitObject;
		}

		/**
		 * 処理結果が正常終了の場合は trueが返却されます.
		 * @return boolean 正常の場合 trueが返却されます.
		 */
		public boolean isSuccess() {
			return successFlag.get();
		}
	}
}
