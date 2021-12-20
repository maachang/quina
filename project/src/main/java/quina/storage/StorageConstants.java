package quina.storage;

import quina.util.AtomicNumber64;

/**
 * Storage定義.
 */
public class StorageConstants {
	
	/**
	 * サービス名.
	 */
	public static final String SERVICE_NAME = "storage";
	
	/**
	 * コンフィグ名.
	 */
	public static final String CONFIG_NAME = "storage";
	
	
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
	 * タイムアウトコンフィグ定義名.
	 */
	public static final String TIMEOUT = "timeout";
	
	/**
	 * タイムアウト値を設定.
	 * @param time タイムアウト値を設定します.
	 */
	public static final void setTimeout(long time) {
		timeout.set(getTimeout(time));
	}
	
	/**
	 * タイムアウト値を取得.
	 * @param timeout タイムアウト値を設定します.
	 * @return long 整形された内容が返却されます.
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
	 * @return long タイムアウト値が返却されます.
	 */
	public static final long getTimeout() {
		return timeout.get();
	}
	
	// デフォルトタイムアウトチェックタイミング.
	// 15秒.
	private static final long DEF_CHECK_TIMING = 15000L;
	
	// 最小タイムアウトチェックタイミング.
	// 5秒.
	private static final long MIN_CHECK_TIMING = 5000L;

	// 最大タイムアウトチェックタイミング.
	// 60秒.
	private static final long MAX_CHECK_TIMING = 60000L;
	
	// チェックタイミング.
	private static final AtomicNumber64 checkTiming =
		new AtomicNumber64(DEF_CHECK_TIMING);
	
	/**
	 * チェックタイミングコンフィグ定義名.
	 */
	public static final String TIMING = "timing";

	/**
	 * チェックタイミング値を設定.
	 * @param time チェックタイミング値を設定します.
	 */
	public static final void setCheckTiming(long time) {
		checkTiming.set(getCheckTiming(time));
	}
	
	/**
	 * チェックタイミング値を取得.
	 * @param timeout チェックタイミング値を設定します.
	 * @return long 整形された内容が返却されます.
	 */
	public static final long getCheckTiming(long time) {
		if(time < MIN_CHECK_TIMING) {
			time = MIN_CHECK_TIMING;
		} else if(time > MAX_CHECK_TIMING) {
			time = MAX_CHECK_TIMING;
		}
		return time;
	}

	/**
	 * チェックタイミング値を取得.
	 * @return long チェックタイミング値が返却されます.
	 */
	public static final long getCheckTiming() {
		return timeout.get();
	}
}
