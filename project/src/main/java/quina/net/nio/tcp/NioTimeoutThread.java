package quina.net.nio.tcp;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import quina.QuinaServiceThread;

/**
 * Nioタイムアウト監視.
 */
public class NioTimeoutThread
	extends QuinaServiceThread<NioElement> {
	
	// タイムアウト値.
	private long timeout;
	
	// TimeoutHandler.
	private NioTimeoutHandler handle;
	
	// ワーカー要素キュー.
	private final Queue<NioElement> queue =
		new ConcurrentLinkedQueue<NioElement>();
	
	// タイムアウト監視キュー.
	private final Queue<NioElement> timeoutQueue =
		new LinkedList<NioElement>();
	
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
	 * Nio要素のタイムアウト監視登録.
	 *
	 * @param em 登録するワーカを設定します.
	 * @throws IOException
	 */
	@Override
	public void offer(NioElement em) {
		// 登録されてない場合のみ１度登録.
		if(!em.regIoTimeout()) {
			queue.offer(em);
		}
	}
	
	@Override
	protected NioElement poll() {
		// Iteratorで取得するので、ここでは取得しない.
		return null;
	}
	
	/**
	 * ワーカースレッド実行処理.
	 * @param em null が設定されます.
	 */
	@Override
	protected void executeCall(NioElement em)
		throws Throwable {
		long nextTime = 0L;
		// 監視キュー情報が存在しない.
		if(queue.size() == 0) {
			// Timeout監視キューの実行条件でない場合.
			if(!(timeoutQueue.size() > 0 &&
				System.currentTimeMillis() > nextTime)) {
				// 存在しない場合.
				Thread.sleep(50L);
				return;
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
		while(!stopFlag && it.hasNext()) {
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
		if(!stopFlag) {
			Thread.sleep(50L);
		}
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
		while(!stopFlag && it.hasNext()) {
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
		if(!stopFlag) {
			Thread.sleep(50L);
		}
		// 0以下の場合かLong.MAX_VALUEの場合は0を返却.
		if(ret < 0 || ret == Long.MAX_VALUE) {
			return 0L;
		}
		return ret;
	}
	
	/**
	 * 後始末実行.
	 */
	protected void cleanUpCall() {
		Iterator<NioElement> it = timeoutQueue.iterator();
		while(it.hasNext()) {
			try {
				it.next().close();
			} catch(Exception e) {}
		}
		it = queue.iterator();
		while(it.hasNext()) {
			try {
				it.next().close();
			} catch(Exception e) {}
		}
	}
}
