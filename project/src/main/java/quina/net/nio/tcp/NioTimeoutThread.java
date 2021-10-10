package quina.net.nio.tcp;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import quina.util.Flag;
import quina.worker.QuinaWait;

/**
 * Nioタイムアウト監視.
 */
public class NioTimeoutThread
	extends Thread {
	
	// タイムアウト値.
	private long timeout;
	
	// TimeoutHandler.
	private NioTimeoutHandler handle;
	
	// ワーカー要素キュー.
	private final Queue<NioElement> queue =
		new ConcurrentLinkedQueue<NioElement>();
	
	// スレッド停止フラグ.
	private volatile boolean stopFlag = true;
	
	// スレッド開始完了フラグ.
	private final Flag startThreadFlag = new Flag(false);
	
	// スレッド終了フラグ.
	private final Flag endThreadFlag = new Flag(false);
	
	/**
	 * コンストラクタ.
	 * @param timeout I/Oタイムアウト値（ミリ秒）を
	 *                設定します.
	 * @param handle Timeoutハンドラを設定します.
	 */
	public NioTimeoutThread(long timeout,
		NioTimeoutHandler handle) {
		if(timeout > 0L) {
			this.timeout = timeout;
		} else {
			this.timeout = 0L;
		}
		this.handle = handle;
	}
	
	/**
	 * タイムアウト値（ミリ秒）を取得.
	 * @return long タイムアウト値（ミリ秒）を取得します.
	 */
	public long getTimeout() {
		return timeout;
	}
	
	/**
	 * ワーカースレッドに処理を登録.
	 *
	 * @param em 登録するワーカを設定します.
	 * @throws IOException
	 */
	public void push(NioElement em) {
		// 登録されてない場合のみ１度登録.
		if(!em.regIoTimeout()) {
			queue.offer(em);
		}
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
		return QuinaWait.await(-1L, startThreadFlag);
	}

	/**
	 * スレッド開始完了まで待機.
	 * @param timeout タイムアウトのミリ秒を設定します.
	 *                0以下を設定した場合、無限に待ちます.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean awaitStartup(long timeout) {
		return QuinaWait.await(timeout, startThreadFlag);
	}

	/**
	 * スレッド終了まで待機.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean awaitExit() {
		return QuinaWait.await(-1L, endThreadFlag);
	}

	/**
	 * スレッド終了まで待機.
	 * @param timeout タイムアウトのミリ秒を設定します.
	 *                0以下を設定した場合、無限に待ちます.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean awaitExit(long timeout) {
		return QuinaWait.await(timeout, endThreadFlag);
	}

	/**
	 * スレッド実行.
	 */
	public void run() {
		// スレッド実行.
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
		ThreadDeath ret = null;
		boolean endFlag = false;
		long nextTime = 0L;
		Queue<NioElement> timeoutQueue =
			new LinkedList<NioElement>();
		
		// スレッド開始完了.
		startThreadFlag.set(true);
		while (!endFlag && !stopFlag) {
			try {
				while (!endFlag && !stopFlag) {
					// 監視キュー情報が存在しない.
					if(queue.size() == 0) {
						// Timeout監視キューの実行条件でない場合.
						if(!(timeoutQueue.size() > 0 &&
							System.currentTimeMillis() > nextTime)) {
							// 存在しない場合.
							Thread.sleep(50L);
							continue;
						}
					}
					
					// NioElement監視キューの実行.
					if(queue.size() > 0) {
						executeNioElementQueue(timeoutQueue);
					}
					
					// Timeout監視キューの実行が可能な場合.
					if(timeoutQueue.size() > 0 &&
						System.currentTimeMillis() > nextTime) {
						// Timeout監視キューの実行処理.
						nextTime = executeTimeoutQueue(timeoutQueue);
						// 次に実行される監視キューの時間を設定.
						nextTime = (nextTime / 2) +
							System.currentTimeMillis();
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
			}
		}
		return ret;
	}
	
	// タイムアウト疑い値.
	private static final long DOUBT_TIMEOUT = 2500L;
	
	// nio要素監視キューの処理.
	private final void executeNioElementQueue(
		Queue<NioElement> timeoutQueue)
		throws Throwable {
		final long currentTime = System.currentTimeMillis();
		// 一覧を取得.
		NioElement em;
		Iterator<NioElement> it = queue.iterator();
		while(it.hasNext()) {
			em = null;
			try {
				// クローズされてる場合.
				if(!(em = it.next()).isConnection()) {
					// Nio要素監視キューから除外.
					it.remove();
					continue;
				// タイムアウトの可能性があるNio要素の場合.
				} else if(
					em.getIoTimeout() + DOUBT_TIMEOUT < currentTime) {
					// Nio要素監視キューから除外.
					it.remove();
					// Timeout監視キューにセット.
					timeoutQueue.offer(em);
					continue;
				}
			} catch(Throwable e) {
				//e.printStackTrace();
				// 通信を破棄.
				NioUtil.closeNioElement(em);
				try {
					// Timeout監視キューから除外.
					it.remove();
				} catch(Exception ee) {}
				throw e;
			}
		}
		// 一定期間待機.
		Thread.sleep(50L);
	}
	
	// Timeout監視キューの処理.
	private final long executeTimeoutQueue(
		Queue<NioElement> timeoutQueue)
		throws Throwable {
		long time = -1;
		long ret = Long.MAX_VALUE;
		final long currentTime = System.currentTimeMillis();
		// 一覧を取得.
		NioElement em;
		Iterator<NioElement> it = timeoutQueue.iterator();
		while(it.hasNext()) {
			em = null;
			try {
				// クローズされてる場合.
				if(!(em = it.next()).isConnection()) {
					// Timeout監視キューから除外.
					it.remove();
					continue;
				}
				// 現在の要素に対するタイムアウト値を取得.
				time = em.getIoTimeout();
				// タイムアウトの可能性が無い要素の場合.
				if(time + DOUBT_TIMEOUT > currentTime) {
					// Timeout監視キューから除外.
					it.remove();
					queue.offer(em);
					continue;
				// タイムアウトの場合.
				} else if(time + timeout < currentTime) {
					// 既に送信済みの場合.
					if(em.isSendData()) {
						// タイムアウトチェックから除外.
						it.remove();
						// エラーの再送は出来ないので
						// 通信を破棄.
						NioUtil.closeNioElement(em);
						continue;
					// ハンドルが設定されてる場合.
					} else if(handle != null) {
						// タイムアウト実行が可能かチェック.
						if(handle.isExecuteTimeout(
							em, timeout)) {
							// Timeout監視キューから除外.
							it.remove();
							// タイムアウト実行処理.
							handle.executeTimeout(
								em, timeout);
							continue;
						}
					// ハンドルが設定されていない場合.
					} else {
						// タイムアウトチェックから除外.
						it.remove();
						// 通信を破棄.
						NioUtil.closeNioElement(em);
						continue;
					}
				}
				// タイムアウトしてない条件で一番短い
				// タイムアウト値を取得.
				time = (time + timeout) - currentTime;
				if(ret > time) {
					ret = time;
				}
			} catch(Throwable e) {
				//e.printStackTrace();
				// 通信を破棄.
				NioUtil.closeNioElement(em);
				try {
					// Timeout監視キューから除外.
					it.remove();
				} catch(Exception ee) {}
				throw e;
			}
		}
		// 一定期間待機.
		Thread.sleep(50L);
		// 0以下の場合かLong.MAX_VALUEの場合は0を返却.
		if(ret < 0 || ret == Long.MAX_VALUE) {
			return 0L;
		}
		return ret;
	}

}
