package quina.storage;

import quina.util.AtomicNumber64;

/**
 * Storage定義.
 */
public class StorageConstants {
	// デフォルトタイムアウト.
	// 30分.
	private static final long DEF_TIMEOUT = 30L * 60L * 1000L;

	// 最小タイムアウト.
	// 5分.
	private static final long MIN_TIMEOUT = 5L * 60L * 1000L;

	// 最大タイムアウト.
	// 1週間.
	private static final long MAX_TIMEOUT = 7L * 24 * 60L * 60L * 1000L;

	// タイムアウト.
	private static final AtomicNumber64 timeout =
		new AtomicNumber64(DEF_TIMEOUT);

	/**
	 * タイムアウト値を設定.
	 * @param time タイムアウト値を設定します.
	 */
	public static final void setSessionTimeout(long time) {
		timeout.set(getTimeout(time));
	}
	
	/**
	 * タイムアウト値を取得.
	 * @param timeout タイムアウト値を設定します.
	 */
	public static final long getTimeout(long time) {
		if(time < MIN_TIMEOUT) {
			time = MIN_TIMEOUT;
		} else if(time > MAX_TIMEOUT) {
			time = MAX_TIMEOUT;
		}
		return time;
	}

	/**
	 * タイムアウト値を取得.
	 * @return long セッションタイムアウト値が返却されます.
	 */
	public static final long getTimeout() {
		return timeout.get();
	}
}
