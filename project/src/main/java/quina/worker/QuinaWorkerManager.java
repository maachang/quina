package quina.worker;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import quina.QuinaServiceThread;
import quina.exception.QuinaException;
import quina.util.AtomicNumber;
import quina.util.Flag;
import quina.util.collection.IndexKeyValueList;

/**
 * QuinaWorkerマネージャ.
 */
final class QuinaWorkerManager {
	// ワーカースレッド群.
	private final QuinaWorkerThread[] threads;
	
	// ワーカースレッド長.
	private final int threadLength;
	
	// ワーカーハンドラ.
	private final QuinaWorkerHandler handle;

	// 次のワーカースレッド割当ID.
	private final AtomicNumber nextWorkerId = new AtomicNumber(0);

	// 開始処理実行フラグ.
	private final Flag startFlag = new Flag(false);

	// 停止処理実行フラグ.
	private final Flag stopFlag = new Flag(false);

	/**
	 * コンストラクタ.
	 * @param len 生成するワーカースレッド数を設定します.
	 * @param handle QuinaWorkerHandleを設定します.
	 * @param callHandles QuinaWorkerCallHandler群を設定します.
	 */
	public QuinaWorkerManager(int threadLength, QuinaWorkerHandler handle,
		QuinaWorkerCallHandler... callHandles) {
		if(handle == null) {
			throw new QuinaException("QuinaWorkerHandle is not set.");
		}
		if(threadLength < QuinaWorkerConstants.MIN_WORKER_LENGTH) {
			threadLength = QuinaWorkerConstants.MIN_WORKER_LENGTH;
		} else if(threadLength >= QuinaWorkerConstants.MAX_WORKER_LENGTH) {
			threadLength = QuinaWorkerConstants.MAX_WORKER_LENGTH;
		}
		// CallHandleをインデックス変換.
		IndexKeyValueList<Integer, QuinaWorkerCallHandler> indexCallHandles =
			createIndexHandler(callHandles);
		final int len = callHandles.length;
		// ハンドル初期化.
		handle.initWorkerCall(threadLength);
		// CallHandleの初期化.
		for(int i = 0; i < len; i ++) {
			callHandles[i].initWorkerCall(threadLength);
		}
		// スレッド生成.
		this.threads = new QuinaWorkerThread[threadLength];
		for(int i = 0; i < threadLength; i ++) {
			threads[i] = new QuinaWorkerThread(i, handle, indexCallHandles);
		}
		// スレッド長を設定.
		this.threadLength = threadLength;
		// ハンドルを設定.
		this.handle = handle;
	}
	
	// 指定されたいQuinaWorkerElementHandle群をインデックス化.
	private static final IndexKeyValueList<Integer, QuinaWorkerCallHandler>
		createIndexHandler(QuinaWorkerCallHandler[] handles) {
		final IndexKeyValueList<Integer, QuinaWorkerCallHandler> ret = new
			IndexKeyValueList<Integer, QuinaWorkerCallHandler>();
		final int len = handles.length;
		for(int i = 0; i < len; i ++) {
			if(handles[i].targetId() == null) {
				throw new QuinaException(
					"The target call for the target handle " +
						handles[i].getClass().getName() + " is null.");
			}
			ret.put(handles[i].targetId(), handles[i]);
		}
		return ret;
	}

	/**
	 * ワーカースレッドを開始.
	 */
	public void startThread() {
		startFlag.set(true);
		final int len = threads.length;
		for(int  i = 0; i < len; i ++) {
			threads[i].startThread();
		}
	}

	/**
	 * ワーカースレッドを全終了.
	 */
	public void stopThread() {
		stopFlag.set(true);
		final int len = threads.length;
		for(int i = 0; i < len; i ++) {
			threads[i].stopThread();
		}
	}

	/**
	 * startThreadが実行されたかチェック.
	 * @return boolean trueの場合startThreadが呼び出さてます.
	 */
	public boolean isStartCall() {
		return startFlag.get();
	}

	/**
	 * stopThreadが実行されたかチェック.
	 * @return boolean trueの場合stopThreadが呼び出さてます.
	 */
	public boolean isStopCall() {
		return stopFlag.get();
	}

	/**
	 * ワーカーがすべて開始しているかチェック.
	 * @return
	 */
	public boolean isStartupThread() {
		final int len = threads.length;
		for(int i = 0; i < len; i ++) {
			if(!threads[i].isStartupThread()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * ワーカーがすべて終了しているかチェック.
	 * @return
	 */
	public boolean isExitThread() {
		final int len = threads.length;
		for(int i = 0; i < len; i ++) {
			if(!threads[i].isExitThread()) {
				return false;
			}
		}
		return true;
	}

	// 開始・終了完了待機処理.
	private static final boolean await(
		QuinaWorkerThread[] threads, boolean startup, long timeout) {
		int i;
		int count;
		final int len = threads.length;
		long first = -1L;
		if(timeout > 0L) {
			first = System.currentTimeMillis() + timeout;
		}
		while(true) {
			count = 0;
			if(startup) {
				for(i = 0; i < len; i ++) {
					if(threads[i].isStartupThread()) {
						count ++;
						continue;
					}
					break;
				}
			} else {
				for(i = 0; i < len; i ++) {
					if(threads[i].isExitThread()) {
						count ++;
						continue;
					}
					break;
				}
			}
			if(count == len) {
				return true;
			} else if(first != -1L && first < System.currentTimeMillis()) {
				return false;
			}
			try {
				Thread.sleep(50L);
			} catch(Exception e) {}
		}
	}

	/**
	 * スレッド開始完了まで待機.
	 * @return boolean [true]の場合、正しく完了しました.
	 */
	public boolean awaitStartup() {
		return await(threads, true, -1L);
	}

	/**
	 * スレッド開始完了まで待機.
	 * @param timeout タイムアウトのミリ秒を設定します.
	 *                0以下を設定した場合、無限に待ちます.
	 * @return boolean [true]の場合、正しく完了しました.
	 */
	public boolean awaitStartup(long timeout) {
		return await(threads, true, timeout);
	}

	/**
	 * スレッド終了まで待機.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean awaitExit() {
		return await(threads, false, -1L);
	}

	/**
	 * スレッド終了まで待機.
	 * @param timeout タイムアウトのミリ秒を設定します.
	 *                0以下を設定した場合、無限に待ちます.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean awaitExit(long timeout) {
		return await(threads, false, timeout);
	}

	/**
	 * 要素の追加.
	 * @param em Quinaワーカー要素を設定します.
	 * @return int 割り当てられたワーカーNoが返却されます.
	 */
	public void push(QuinaWorkerCall em) {
		int no;
		// 既にワーカーIDが設定されている場合.
		if((no = em.getWorkerNo()) > 0) {
			// Pushコール実行.
			handle.pushCall(no, em);
			// そのまま登録.
			threads[no].offer(em);
		// ワーカーIDが設定されていない場合.
		} else {
			// 新しいIDをセット.
			no = nextWorkerId.inc();
			// スレッド数を超えてる場合.
			if(no + 1 >= threadLength) {
				nextWorkerId.set(0);
			}
			// Pushコール実行.
			handle.pushCall(no, em);
			// ワーカー登録.
			em.setWorkerNo(no);
			threads[no].offer(em);
		}
	}

	/**
	 * 管理されてるワーカースレッド数を取得.
	 * @return int ワーカースレッド数が返却されます.
	 */
	public int size() {
		return threads.length;
	}
	
	// QuinaWorkerThread.
	protected static final class QuinaWorkerThread
		extends QuinaServiceThread<QuinaWorkerCall> {
		// waitタイムアウト値.
		private static final int TIMEOUT = 1000;

		// ワーカーNo.
		private final int no;

		// ワーカーハンドラー.
		private final QuinaWorkerHandler handle;
		
		// QuinaWorkerCallに対するハンドル群.
		private IndexKeyValueList<Integer, QuinaWorkerCallHandler>
			callHandles;

		// ワーカー要素キュー.
		private final Queue<QuinaWorkerCall> queue;

		// wait管理.
		private final QuinaWait wait;
		
		// ワーカーコール.
		private QuinaWorkerCallHandler callHandle = null;
		
		// 全体ワーカー共通開始処理.
		private boolean startCommonCallFlag = false;
		
		// 全体ワーカー開始処理.
		private boolean startCallFlag = false;

		/**
		 * コンストラクタ.
		 * @param no ワーカーNoを設定します.
		 * @param handle ワーカースレッドハンドラーが設定されます.
		 * @param callHandles 実行要素群を設定します.
		 */
		public QuinaWorkerThread(int no, QuinaWorkerHandler handle,
			IndexKeyValueList<Integer, QuinaWorkerCallHandler> callHandles) {
			this.no = no;
			this.handle = handle;
			this.callHandles = callHandles;
			this.queue = new ConcurrentLinkedQueue<QuinaWorkerCall>();
			this.wait = new QuinaWait();
		}

		/**
		 * このワーカースレッドのワーカーNoを取得.
		 * @return このワーカースレッドのワーカーNoが返却されます.
		 */
		public int getWorkerNo() {
			return no;
		}
		
		/**
		 * 現在登録され待機中のワーカー数を取得.
		 * @return int 登録され待機中のワーカー数が返却されます.
		 */
		public int length() {
			return queue.size();
		}

		/**
		 * 新しいワーカーコールをセット.
		 */
		@Override
		public void offer(QuinaWorkerCall em) {
			queue.offer(em);
			wait.signal();
		}
		
		/**
		 * ワーカーコールを取得.
		 */
		@Override
		public QuinaWorkerCall poll() {
			QuinaWorkerCall ret = null;
			// ワーカーCallを取得.
			if ((ret = queue.poll()) == null) {
				wait.await(TIMEOUT);
			}
			return ret;
		}

		/**
		 * 開始スレッドコール.
		 */
		@Override
		protected void startThreadCall() {
			final int len = callHandles.size();
			
			// スレッド開始呼び出し.
			try {
				handle.startThreadCall(no);
			} catch(Exception e) {
			}
			for(int i = 0; i < len; i ++) {
				try {
					callHandles.valueAt(i)
						.startThreadCall(no);
				} catch(Exception e) {
				}
			}
		}
		
		/**
		 * 終了スレッドコール.
		 */
		@Override
		protected void endThreadCall() {
			final int len = callHandles.size();
			
			// スレッド終了呼び出し.
			for(int i = 0; i < len; i ++) {
				try {
					callHandles.valueAt(i)
						.endThreadCall(no);
				} catch(Exception e) {
				}
			}
			try {
				handle.endThreadCall(no);
			} catch(Exception e) {
			}
			
			// スレッド終了完了.
			endThreadFlag.set(true);
		}
		
		/**
		 * 実行処理.
		 */
		@Override
		protected void executeCall(QuinaWorkerCall workerCall)
			throws Throwable {
			if(workerCall == null) {
				return;
			}
			int id = this.no;
			
			// 変数初期化.
			callHandle = null;
			startCommonCallFlag = false;
			startCallFlag = false;

			// スレッド開始完了.
			startThreadFlag.set(true);
			// QuinaWorkerCallを処理する
			// 対象ワーカーハンドルを取得.
			callHandle = callHandles.get(workerCall.getId());
			// 対象ワーカーハンドルが存在する場合.
			if(callHandle == null) {
				// 既に破棄されてる場合.
				if(handle.isDestroy(id, workerCall)) {
					handle.destroy(id, workerCall);
					return;
				}
				// 全体ワーカー共通開始処理.
				handle.startCommonCall(id, workerCall);
				startCommonCallFlag = true;
				
				// 全体ワーカー開始処理.
				handle.startCall(id, workerCall);
				startCallFlag = true;
				
				// 全体ワーカー要素を実行.
				if(!handle.executeCall(id, workerCall)) {
					// ワーカー要素を破棄.
					handle.destroy(id, workerCall);
				}
				
				// 全体ワーカー要素の終了処理.
				handle.endCall(id, workerCall);
				startCallFlag = false;
				// 全体ワーカー共通終了処理.
				handle.endCommonCall(id, workerCall);
				startCommonCallFlag = false;
			// 対象ワーカーハンドルが存在しない場合.
			} else {
				// 既に破棄されてる場合.
				if(callHandle.isDestroy(id, workerCall)) {
					callHandle.destroy(id, workerCall);
					return;
				}
				
				// 全体ワーカー共通開始処理.
				handle.startCommonCall(id, workerCall);
				startCommonCallFlag = true;
				
				// 対象ワーカー開始処理.
				callHandle.startCall(id, workerCall);
				startCallFlag = true;
				
				// 対象ワーカー要素を実行.
				if(!callHandle.executeCall(id, workerCall)) {
					// ワーカー要素を破棄.
					callHandle.destroy(id, workerCall);
				}
				
				// 対象ワーカー要素の終了処理.
				callHandle.endCall(id, workerCall);
				startCallFlag = false;
				
				// 全体ワーカー共通終了処理.
				handle.endCommonCall(id, workerCall);
				startCommonCallFlag = false;
			}
		}
		
		/**
		 * 後始末処理.
		 */
		@Override
		protected void cleanUpCall() {
			int id = this.no;
			QuinaWorkerCall workerCall;
			// ワーカースレッド処理後の後始末.
			while(true) {
				workerCall = null;
				callHandle = null;
				try {
					// 実行ワーカーを取得して破棄.
					if ((workerCall = queue.poll()) == null) {
						break;
					}
					// QuinaWorkerCallを処理する
					// 対象ワーカーハンドルを取得.
					callHandle = callHandles.get(workerCall.getId());
					// 対象ワーカーハンドルが存在する場合.
					if(callHandle != null) {
						try {
							callHandle.destroy(id, workerCall);
						} catch(Exception e) {}
					// 対象ワーカーハンドルが存在しない場合.
					} else {
						try {
							handle.destroy(id, workerCall);
						} catch(Exception e) {
							try {
								workerCall.destroy(id);
							} catch(Exception ee) {}
						}
					}
				} catch (Throwable to) {
				}
			}
		}
		
		/**
		 * エラーコール.
		 */
		@Override
		protected void errorThreadCall(
			QuinaWorkerCall workerCall, Throwable td) {
			if(workerCall == null) {
				return;
			}
			int id = this.no;
			
			// 例外処理用ハンドラを呼び出す.
			try {
				handle.errorCall(id, workerCall, td);
			} catch(Exception e) {}
			// 対象ワーカーハンドルが存在する場合.
			if(callHandle != null) {
				// startCallが呼ばれてる場合.
				if(startCallFlag) {
					try {
						// ワーカー要素の利用終了の場合.
						callHandle.endCall(id, workerCall);
					} catch(Exception e) {}
				}
				// startCommonCallが呼ばれてる場合.
				if(startCommonCallFlag) {
					try {
						// 全体ワーカー共通終了処理.
						handle.endCommonCall(id, workerCall);
					} catch(Exception e) {}
				}
				try {
					// 破棄処理.
					callHandle.destroy(id, workerCall);
				} catch(Exception e) {}
			// 対象ワーカーハンドルが存在しない場合.
			} else {
				// startCallが呼ばれてる場合.
				if(startCallFlag) {
					try {
						// ワーカー要素の利用終了の場合.
						handle.endCall(id, workerCall);
					} catch(Exception e) {}
				}
				// startCommonCallが呼ばれてる場合.
				if(startCommonCallFlag) {
					try {
						// 全体ワーカー共通終了処理.
						handle.endCommonCall(id, workerCall);
					} catch(Exception e) {}
				}
				try {
					handle.destroy(id, workerCall);
				} catch(Exception e) {
					try {
						// 破棄処理.
						workerCall.destroy(id);
					} catch(Exception ee) {}
				}
			}
		}
	}
}
