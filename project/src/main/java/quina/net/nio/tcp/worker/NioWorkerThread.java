package quina.net.nio.tcp.worker;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import quina.net.nio.tcp.NioAtomicValues.Bool;
import quina.net.nio.tcp.NioUtil;
import quina.net.nio.tcp.Wait;

/**
 * Nioワーカースレッド.
 */
public class NioWorkerThread extends Thread {
	// waitタイムアウト値.
	private static final int TIMEOUT = 1000;

	// ワーカースレッドNo.
	private final int no;

	// ワーカースレッドハンドラー.
	private final NioWorkerThreadHandler handle;

	// Nioワーカー要素キュー.
	private final Queue<WorkerElement> queue;

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
	public NioWorkerThread(int no, NioWorkerThreadHandler handle) {
		this.no = no;
		this.handle = handle;
		this.queue = new ConcurrentLinkedQueue<WorkerElement>();
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
	public void push(WorkerElement em) {
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
	 * スレッド開始完了まで待機.
	 * @param timeout タイムアウトのミリ秒を設定します.
	 *                0以下を設定した場合、無限に待ちます.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean awaitStartup() {
		return NioUtil.await(-1L, startThreadFlag);
	}

	/**
	 * スレッド開始完了まで待機.
	 * @param timeout タイムアウトのミリ秒を設定します.
	 *                0以下を設定した場合、無限に待ちます.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean awaitStartup(long timeout) {
		return NioUtil.await(timeout, startThreadFlag);
	}

	/**
	 * スレッド終了まで待機.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean awaitExit() {
		return NioUtil.await(-1L, endThreadFlag);
	}

	/**
	 * スレッド終了まで待機.
	 * @param timeout タイムアウトのミリ秒を設定します.
	 *                0以下を設定した場合、無限に待ちます.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean awaitExit(long timeout) {
		return NioUtil.await(timeout, endThreadFlag);
	}

	/**
	 * スレッド実行.
	 */
	public void run() {
		if(handle != null) {
			try {
				handle.startThreadCall(no);
			} catch(Exception e) {
//				e.printStackTrace();
			}
		}
		final ThreadDeath td = execute();
		if(handle != null) {
			try {
				handle.endThreadCall(no);
			} catch(Exception e) {
//				e.printStackTrace();
			}
		}
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
		int id = this.no;
		Object o = null;
		WorkerElement wem = null;
		ThreadDeath ret = null;
		boolean endFlag = false;

		// スレッド開始完了.
		startThreadFlag.set(true);
		while (!endFlag && !stopFlag) {
			try {
				while (!endFlag && !stopFlag) {
					o = null; wem = null;
					// 実行ワーカー要素を取得.
					if ((wem = queue.poll()) == null) {
						wait.await(TIMEOUT);
						continue;
					// 対象ワーカー要素が有効な場合は実行.
					} else if(!wem.isDestroy()) {
						if(handle != null) {
							o = handle.getWorkerThreadObject(id);
						}
						// 対象ワーカー要素を実行.
						if(!wem.call(o)) {
							// ワーカー要素を破棄.
							wem.destroy();
						}
					}
					// ワーカー要素の利用終了の場合.
					if(handle != null) {
						handle.endWorkerElement(wem);
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
				// エラーの場合はワーカーを破棄.
				if(wem != null) {
					// ワーカー要素を破棄.
					try {
						wem.destroy();
					} catch(Exception e) {}
					// ワーカー要素の利用終了.
					if(handle != null) {
						try {
							handle.endWorkerElement(wem);
						} catch(Exception e) {}
					}
				}
				// ハンドラーが存在する場合は
				// 例外処理用ハンドラを呼び出す.
				if(handle != null) {
					try {
						handle.errorCall(no, to);
					} catch(Exception e) {}
				}
			}
		}
		// ワーカースレッド処理後の後始末.
		while(true) {
			try {
				// 実行ワーカーを取得して破棄.
				if ((wem = queue.poll()) == null) {
					break;
				} else if(!wem.isDestroy()) {
					wem.destroy();
				}
				wem = null;
			} catch (Throwable to) {
				if(wem != null) {
					try {
						wem.destroy();
					} catch(Throwable tt) {}
				}
				if(queue.size() == 0) {
					break;
				}
			}
		}
		return ret;
	}
}
