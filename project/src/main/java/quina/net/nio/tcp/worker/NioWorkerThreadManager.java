package quina.net.nio.tcp.worker;

import quina.net.nio.tcp.NioAtomicValues.Bool;
import quina.net.nio.tcp.NioAtomicValues.Number32;
import quina.net.nio.tcp.NioElement;

/**
 * Nioワーカースレッド管理.
 */
public class NioWorkerThreadManager {
	// ワーカースレッド群.
	private final NioWorkerThread[] threads;

	// 次のワーカースレッド割当ID.
	private final Number32 nextWorkerId = new Number32(0);

	// 開始処理実行フラグ.
	private final Bool startFlag = new Bool(false);

	// 停止処理実行フラグ.
	private final Bool stopFlag = new Bool(false);

	/**
	 * コンストラクタ.
	 * @param handle ワーカースレッドハンドラーを設定します.
	 */
	public NioWorkerThreadManager(NioWorkerThreadHandler handle) {
		this(NioWorkerConstants.getWorkerThreadLength(), handle);
	}

	/**
	 * コンストラクタ.
	 * @param len 生成するワーカースレッド数を設定します.
	 * @param handle ワーカースレッドハンドラーを設定します.
	 */
	public NioWorkerThreadManager(int len, NioWorkerThreadHandler handle) {
		if(NioWorkerConstants.MIN_WORKER_THREAD_LENGTH > len) {
			len = NioWorkerConstants.MIN_WORKER_THREAD_LENGTH;
		} else if(NioWorkerConstants.MAX_WORKER_THREAD_LENGTH < len) {
			len = NioWorkerConstants.MAX_WORKER_THREAD_LENGTH;
		}
		if(handle != null) {
			handle.initWorkerThreadManager(len);
		}
		this.threads = new NioWorkerThread[len];
		for(int i = 0; i < len; i ++) {
			threads[i] = new NioWorkerThread(i, handle);
		}
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
		NioWorkerThread[] threads, boolean startup, long timeout) {
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
	 * @param wem Nioワーカー要素を設定します.
	 * @return int 割り当てられたワーカーNoが返却されます.
	 */
	public int push(WorkerElement wem) {
		return push(null, wem);
	}

	/**
	 * 要素の追加.
	 * @param em Nio要素を設定します.
	 * @param wem Nioワーカー要素を設定します.
	 * @return int 割り当てられたワーカーNoが返却されます.
	 */
	public int push(NioElement em, WorkerElement wem) {
		// nio要素に登録されたワーカーNoを取得.
		int no = em == null ?
			NioElement.NON_WORKER_NO :
			em.getWorkerNo();
		// １度登録されたワーカースレッド番号が存在しない場合.
		if(no == NioElement.NON_WORKER_NO) {
			// 割り当てるワーカースレッドを算出.
			int nextNo;
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
					// nio要素に引き続き利用するワーカーNoを登録.
					if(em != null) {
						em.setWorkerNo(no);
					}
					break;
				}
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

	/**
	 * 指定番号のNioWorkerThreadを取得.
	 * @param no ワーカースレッド番号を設定します.
	 * @return NioWorkerThread NioWorkerThreadが返却されます.
	 */
	public NioWorkerThread getWorkerThread(int no) {
		return threads[no];
	}
}
