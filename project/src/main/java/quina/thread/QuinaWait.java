package quina.thread;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import quina.util.AtomicNumber;
import quina.util.Flag;

/**
 * QuinaWaitオブジェクト.
 */
public class QuinaWait {

	// ロックオブジェクト.
	private final Lock sync = new ReentrantLock();
	private final Condition con = sync.newCondition();
	private final AtomicNumber awaitFlag = new AtomicNumber(0);

	/**
	 * コンストラクタ.
	 */
	public QuinaWait() {
	}

	/**
	 * 指定時間待機.
	 * @return boolean
	 */
	public final boolean await() {
		sync.lock();
		try {
			awaitFlag.inc(); // セット.
			con.await();
		} catch(Exception e) {
			return false;
		} finally {
			sync.unlock();
			awaitFlag.dec(); // 解除.
		}
		return true;
	}

	/**
	 * 指定時間待機.
	 *
	 * @param timeout
	 *            ミリ秒での待機時間を設定します. [0]を設定した場合、無限待機となります.
	 * @return boolean [true]が返された場合、復帰条件が設定されました.
	 */
	public final boolean await(long time) {
		if (time <= 0L) {
			await();
			return true;
		}
		sync.lock();
		try {
			awaitFlag.inc(); // セット.
			return con.await(time, TimeUnit.MILLISECONDS);
		} catch(Exception e) {
			return false;
		} finally {
			sync.unlock();
			awaitFlag.dec(); // 解除.
		}
	}

	/**
	 * 待機中のスレッドを１つ起動.
	 * @return boolean
	 */
	public final boolean signal() {
		if (awaitFlag.get() > 0) {
			sync.lock();
			try {
				con.signal();
			} catch(Exception e) {
				return false;
			} finally {
				sync.unlock();
			}
			return true;
		}
		return true;
	}

	/**
	 * 待機中のスレッドを全て起動.
	 * @return boolean
	 */
	public final boolean signalAll() {
		if (awaitFlag.get() > 0) {
			sync.lock();
			try {
				con.signalAll();
			} catch(Exception e) {
				return false;
			} finally {
				sync.unlock();
			}
		}
		return true;
	}

	/**
	 * 現在待機中かチェック.
	 *
	 * @return boolean [true]の場合、待機中です.
	 */
	public final boolean isWait() {
		return awaitFlag.get() > 0;
	}

	/**
	 * ロックオブジェクトの取得.
	 *
	 * @return Lock ロックオブジェクトが返却されます.
	 */
	public final Lock getLock() {
		return sync;
	}
	
	/**
	 * フラグがtrueになるまで待機処理.
	 * @param timeout タイムアウト値を設定します.
	 * @param flg 判別するフラグオブジェクトを設定します.
	 * @return boolean [false]の場合、タイムアウトが発生しました.
	 */
	public static final boolean await(long timeout, Flag flg) {
		if(!flg.get()) {
			long first = -1L;
			if(timeout > 0L) {
				first = System.currentTimeMillis() + timeout;
			}
			while(!flg.get()) {
				if(first != -1L && first < System.currentTimeMillis()) {
					return false;
				}
				try {
					Thread.sleep(30L);
				} catch(Exception e) {}
			}
		}
		return true;
	}
}