package quina.promise;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import quina.net.nio.tcp.NioAtomicValues.Bool;
import quina.net.nio.tcp.NioAtomicValues.Number32;
import quina.util.Flag;
import quina.worker.Wait;

/**
 * Promiseワーカーマネージャ.
 */
public class PromiseWorkerManager {
	private static final PromiseWorkerManager INST = new PromiseWorkerManager();

	/**
	 * PromiseWorkerManagerを取得.
	 * @return PromiseWorkerManager PromiseWorkerManagerが返却されます.
	 */
	public static final PromiseWorkerManager getInstance() {
		return INST;
	}

	// ワーカーコンフィグ.
	private PromiseWorkerConfig config = new PromiseWorkerConfig();

	// ワーカースレッド群.
	private PromiseWorkerThread[] threads;

	// 初期化フラグ.
	private final Flag initFlag = new Flag(false);

	// 次のワーカースレッド割当ID.
	private final Number32 nextWorkerId = new Number32(0);

	// 開始処理実行フラグ.
	private final Flag startFlag = new Flag(false);

	// 停止処理実行フラグ.
	private final Flag stopFlag = new Flag(false);

	/**
	 * コンストラクタ.
	 */
	protected PromiseWorkerManager() {
	}

	/**
	 * コンフィグ情報を取得.
	 * @return PromiseWorkerConfig コンフィグ情報が返却されます.
	 */
	public PromiseWorkerConfig getConfig() {
		return config;
	}

	/**
	 * 初期化処理.
	 * @return boolean [true]の場合、初期化に成功しました.
	 */
	public boolean init() {
		return init(null);
	}

	/**
	 * 初期化処理.
	 * @param len 生成するワーカースレッド数を設定します.
	 * @return boolean [true]の場合、初期化に成功しました.
	 */
	public boolean init(Integer len) {
		if(initFlag.setToGetBefore(true)) {
			return false;
		}
		// lenが設定されていない場合.
		if(len == null) {
			// コンフィグからワーカースレッド数を取得.
			len = config.getWorkerLength();
		// lenが指定されている場合.
		} else if(PromiseConstants.MIN_WORKER_THREAD_LENGTH < len) {
			len = PromiseConstants.MIN_WORKER_THREAD_LENGTH;
		} else if(PromiseConstants.MAX_WORKER_THREAD_LENGTH > len) {
			len = PromiseConstants.MAX_WORKER_THREAD_LENGTH;
		}
		this.threads = new PromiseWorkerThread[len];
		for(int i = 0; i < len; i ++) {
			threads[i] = new PromiseWorkerThread(i);
		}
		// スレッド開始.
		startThread();
		return true;
	}

	/**
	 * 初期化されているか取得.
	 * @return boolean trueの場合、初期化されています.
	 */
	public boolean isInit() {
		return initFlag.get();
	}

	/**
	 * ワーカースレッドを開始.
	 */
	protected void startThread() {
		if(!startFlag.setToGetBefore(true)) {
			final int len = threads.length;
			for(int  i = 0; i < len; i ++) {
				threads[i].startThread();
			}
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

	/**
	 * 要素の追加.
	 * @param wem Nioワーカー要素を設定します.
	 * @return int 割り当てられたワーカーNoが返却されます.
	 */
	public int push(PromiseWorkerCall wem) {
		// init処理が行われていない場合は実行.
		init();
		// 割り当てるワーカースレッドを算出.
		int no, nextNo;
		final int len = threads.length;
		while(true) {
			// 前の設定されたワーカーNoを取得.
			no = nextWorkerId.get();
			// 次のワーカーNoを作成.
			nextNo = no + 1;
			// ワーカースレッド数の上限.
			if(nextNo >= len) {
				nextNo = 0;
			}
			// この条件で更新可能な場合は割当可能.
			if(nextWorkerId.compareAndSet(no, nextNo)) {
				break;
			}
		}
		// 対象ワーカースレッドに登録.
		threads[no].push(wem);
		return no;
	}

	/**
	 * 管理されてるワーカースレッド数を取得.
	 * @return int ワーカースレッド数が返却されます.
	 */
	public int size() {
		return threads.length;
	}

	// ワーカースレッド.
	public static final class PromiseWorkerThread extends Thread {
		// waitタイムアウト値.
		private static final int TIMEOUT = 1000;

		// ワーカースレッドNo.
		private final int no;

		// Nioワーカー要素キュー.
		private final Queue<PromiseWorkerCall> queue;

		// wait管理.
		private final Wait wait;

		// スレッド停止フラグ.
		private volatile boolean stopFlag = true;

		// スレッド開始完了フラグ.
		private final Bool startThreadFlag = new Bool(false);

		// スレッド終了フラグ.
		private final Bool endThreadFlag = new Bool(false);

		/**
		 * コンストラクタ.
		 * @param no ワーカーNoを設定します.
		 * @param handle ワーカースレッドハンドラーが設定されます.
		 */
		public PromiseWorkerThread(int no) {
			this.no = no;
			this.queue = new ConcurrentLinkedQueue<PromiseWorkerCall>();
			this.wait = new Wait();
		}

		/**
		 * このワーカースレッドのワーカーNoを取得.
		 * @return このワーカースレッドのワーカーNoが返却されます.
		 */
		public int getWorkerNo() {
			return no;
		}

		/**
		 * ワーカースレッドに処理を登録.
		 *
		 * @param em 登録するワーカを設定します.
		 * @throws IOException
		 */
		public void push(PromiseWorkerCall em) {
			queue.offer(em);
			wait.signal();
		}

		/**
		 * 現在登録され待機中のワーカー数を取得.
		 * @return int 登録され待機中のワーカー数が返却されます.
		 */
		public int length() {
			return queue.size();
		}

		/**
		 * ワーカーを開始する.
		 */
		public void startThread() {
			stopFlag = false;
			startThreadFlag.set(false);
			endThreadFlag.set(false);
			setDaemon(true);
			start();
		}

		/**
		 * ワーカーを停止する.
		 */
		public void stopThread() {
			stopFlag = true;
		}

		/**
		 * ワーカーが停止命令が既に出されているかチェック.
		 * @return
		 */
		public boolean isStopThread() {
			return stopFlag;
		}

		/**
		 * ワーカーが開始しているかチェック.
		 * @return
		 */
		public boolean isStartupThread() {
			return startThreadFlag.get();
		}

		/**
		 * ワーカーが終了しているかチェック.
		 * @return
		 */
		public boolean isExitThread() {
			return endThreadFlag.get();
		}

		/**
		 * スレッド実行.
		 */
		public void run() {
			final ThreadDeath td = execute();
			// スレッド終了完了.
			endThreadFlag.set(true);
			if (td != null) {
				throw td;
			}
		}

		/**
		 * ワーカースレッド実行処理.
		 */
		protected final ThreadDeath execute() {
			PromiseWorkerCall pcall = null;
			ThreadDeath ret = null;
			boolean endFlag = false;
			// スレッド開始完了.
			startThreadFlag.set(true);
			while (!endFlag && !stopFlag) {
				try {
					while (!endFlag && !stopFlag) {
						// 実行ワーカー要素を取得.
						if ((pcall = queue.poll()) == null) {
							wait.await(TIMEOUT);
							continue;
						// 対象ワーカーを実行.
						} else {
							// 対象ワーカー要素を実行.
							pcall.call();
							pcall = null;
						}
					}
				} catch (Throwable to) {
					// スレッド中止.
					if (to instanceof InterruptedException) {
						endFlag = true;
					// threadDeathが発生した場合.
					} else if (to instanceof ThreadDeath) {
						endFlag = true;
						ret = (ThreadDeath) to;
					}
					pcall = null;
				}
			}
			// ワーカースレッド処理後の後始末.
			queue.clear();
			return ret;
		}
	}
}
