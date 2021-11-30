package quina.worker.timeout;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import quina.QuinaThreadStatus;
import quina.exception.QuinaException;
import quina.worker.QuinaLoopElement;

/**
 * TimeoutLoop処理要素.
 */
public class TimeoutLoopElement
	implements QuinaLoopElement {
	
	// タイムアウト値.
	private long timeout;
	
	// タイムアウト監視に移行するタイム.
	private long doubtTime;
	
	// 次にタイムアウトキューの監視を行う時間.
	private long nextTime;
	
	// TimeoutHandler.
	private TimeoutHandler handle;
	
	// ワーカー要素キュー.
	private final Queue<TimeoutElement> queue =
		new ConcurrentLinkedQueue<TimeoutElement>();
	
	// タイムアウト監視キュー.
	private final Queue<TimeoutElement> timeoutQueue =
		new LinkedList<TimeoutElement>();
	
	/**
	 * コンストラクタ.
	 * @param timeout I/Oタイムアウト値（ミリ秒）を設定します.
	 * @param doubtTime タイムアウト監視に移行する時間（ミリ秒）を
	 *                  設定します.
	 * @param handle Timeoutハンドラを設定します.
	 */
	public TimeoutLoopElement(long timeout, long doubtTime,
		TimeoutHandler handle) {
		if(handle == null) {
			throw new QuinaException(
				"TimeoutHandle is not set.");
		}
		if(timeout > TimeoutConstants.MAX_TIMEOUT) {
			timeout = TimeoutConstants.MAX_TIMEOUT;
		} else if(timeout < TimeoutConstants.MIN_TIMEOUT) {
			timeout = TimeoutConstants.MIN_TIMEOUT;
		}
		if(doubtTime > timeout) {
			doubtTime = timeout / 10L;
		}
		if(doubtTime > TimeoutConstants.MAX_DOUBT_TIMEOUT) {
			doubtTime = TimeoutConstants.MAX_DOUBT_TIMEOUT;
		} else if(doubtTime < TimeoutConstants.MIN_DOUBT_TIMEOUT) {
			doubtTime = TimeoutConstants.MIN_DOUBT_TIMEOUT;
		}
		this.timeout = timeout;
		this.doubtTime = doubtTime;
		this.nextTime = 0L;
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
	 * タイムアウト監視に移行するタイム値（ミリ秒）を取得.
	 * @return long タイムアウト監視に移行するタイム値（ミリ秒）を取得します.
	 */
	public long getDoubtTime() {
		return doubtTime;
	}

	
	/**
	 * 要素のタイムアウト監視登録.
	 *
	 * @param em 登録するTimeoutElementを設定します.
	 * @throws IOException
	 */
	public void offer(TimeoutElement em) {
		// 登録されてない場合登録.
		if(!em.regTimeout()) {
			queue.offer(em);
		}
	}
	
	/**
	 * Loop実行.
	 * @param status QuinaThreadステータスが設定されます.
	 * @exception Throwable 例外.
	 */
	@Override
	public void execute(QuinaThreadStatus status)
		throws Throwable {
		boolean check = false;
		// 監視キューに情報が存在する場合.
		if(queue.size() > 0) {
			// 監視キューの処理実行.
			executeElementQueue(status);
			check = true;
		}
		// Timeout監視キューの実行が可能な場合.
		if(timeoutQueue.size() > 0 &&
			System.currentTimeMillis() > nextTime) {
			// Timeout監視キューの実行処理.
			nextTime = executeTimeoutQueue(status);
			// 次に実行される監視キューの時間を設定.
			nextTime = (nextTime >> 1L) +
				System.currentTimeMillis();
			check = true;
		}
		// 何も処理されない場合.
		if(!check) {
			sleep();
		}
	}
	
	// nio要素監視キューの処理.
	private final void executeElementQueue(
		QuinaThreadStatus status)
		throws Throwable {
		final long currentTime = System.currentTimeMillis();
		// 一覧を取得.
		TimeoutElement em;
		Iterator<TimeoutElement> it = queue.iterator();
		while(!status.isStopThread() && it.hasNext()) {
			em = null;
			try {
				// クローズされてる場合.
				if(handle.isCloseTimeoutElement(em = it.next())) {
					// 要素監視キューから除外.
					it.remove();
					continue;
				// タイムアウトの可能性がある要素の場合.
				} else if(
					em.getTime() + doubtTime < currentTime) {
					// 要素監視キューから除外.
					it.remove();
					// Timeout監視キューにセット.
					timeoutQueue.offer(em);
					continue;
				}
			} catch(Throwable e) {
				//e.printStackTrace();
				// 通信を破棄.
				handle.closeTimeoutElement(em);
				try {
					// Timeout監視キューから除外.
					it.remove();
				} catch(Exception ee) {}
				throw e;
			}
		}
		// 一定期間待機.
		if(!status.isStopThread()) {
			sleep();
		}
	}
	
	// Timeout監視キューの処理.
	private final long executeTimeoutQueue(
		QuinaThreadStatus status)
		throws Throwable {
		long time = -1;
		long ret = Long.MAX_VALUE;
		final long currentTime = System.currentTimeMillis();
		// 一覧を取得.
		TimeoutElement em;
		Iterator<TimeoutElement> it = timeoutQueue.iterator();
		while(!status.isStopThread() && it.hasNext()) {
			em = null;
			try {
				// クローズされてる場合.
				if(handle.isCloseTimeoutElement(em = it.next())) {
					// Timeout監視キューから除外.
					it.remove();
					continue;
				}
				// 現在の要素に対するタイムアウト値を取得.
				time = em.getTime();
				// タイムアウトの可能性が無い要素の場合.
				if(time + doubtTime > currentTime) {
					// Timeout監視キューから除外.
					it.remove();
					queue.offer(em);
					continue;
				// タイムアウトの場合.
				} else if(time + timeout < currentTime) {
					// タイムアウト監視が不要な場合.
					if(!handle.isMonitoredTimeout(em)) {
						// タイムアウトチェックから除外.
						it.remove();
						// 要素をクローズ
						handle.closeTimeoutElement(em);
						continue;
					// タイムアウト監視が必要な場合.
					} else {
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
					}
				}
				// タイムアウトしてない条件で一番短い
				// タイムアウト値を取得.
				time = (time + timeout) - currentTime;
				if(ret > time) {
					ret = time;
				}
			} catch(Throwable e) {
				// 要素をクローズ
				handle.closeTimeoutElement(em);
				try {
					// Timeout監視キューから除外.
					it.remove();
				} catch(Exception ee) {}
				throw e;
			}
		}
		// 一定期間待機.
		if(!status.isStopThread()) {
			sleep();
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
	@Override
	public void cleanUpCall() {
		// タイムアウト監視キューをクリアー.
		Iterator<TimeoutElement> it =
			timeoutQueue.iterator();
		while(it.hasNext()) {
			try {
				// 要素をクローズ
				handle.closeTimeoutElement(it.next());
			} catch(Exception e) {}
		}
		// 監視キューをクリアー.
		it = queue.iterator();
		while(it.hasNext()) {
			try {
				// 要素をクローズ
				handle.closeTimeoutElement(it.next());
			} catch(Exception e) {}
		}
	}
	
	// 一定期間待機.
	private static final void sleep() {
		try {
			Thread.sleep(5L);
		} catch(Exception e) {}
	}
}
