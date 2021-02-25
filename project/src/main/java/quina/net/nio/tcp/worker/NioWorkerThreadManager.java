package quina.net.nio.tcp.worker;

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
		if(len < NioWorkerConstants.MIN_WORKER_THREAD_LENGTH) {
			len = NioWorkerConstants.MIN_WORKER_THREAD_LENGTH;
		} else if(len > NioWorkerConstants.MAX_WORKER_THREAD_LENGTH) {
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
		final int len = threads.length;
		for(int  i = 0; i < len; i ++) {
			threads[i].startThread();
		}
	}

	/**
	 * ワーカースレッドを全終了.
	 */
	public void stopThread() {
		final int len = threads.length;
		for(int i = 0; i < len; i ++) {
			threads[i].stopThread();
		}
		int stop;
		while(true) {
			stop = 0;
			for(int i = 0; i < len; i ++) {
				if(threads[i].isEndThread()) {
					stop ++;
				}
			}
			if(stop == len) {
				break;
			}
		}
	}

	/**
	 * 要素の追加.
	 * @param wem Nioワーカー要素を設定します.
	 * @return int 割り当てられたワーカーNoが返却されます.
	 */
	public int push(NioWorkerElement wem) {
		return push(null, wem);
	}

	/**
	 * 要素の追加.
	 * @param em Nio要素を設定します.
	 * @param wem Nioワーカー要素を設定します.
	 * @return int 割り当てられたワーカーNoが返却されます.
	 */
	public int push(NioElement em, NioWorkerElement wem) {
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
}
