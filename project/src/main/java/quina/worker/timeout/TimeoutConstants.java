package quina.worker.timeout;

/**
 * タイムアウト定数定義.
 */
final class TimeoutConstants {
	private TimeoutConstants() {
	}
	
	/**
	 * 最小タイムアウト.
	 * 5秒.
	 */
	public static final long MIN_TIMEOUT = 5L * 1000L;
	
	/**
	 * 最大タイムアウト.
	 * １週間.
	 */
	public static final long MAX_TIMEOUT = 60L * 60L * 24L * 7L * 1000L;
	
	/**
	 * 最小タイムアウト監視キュー移行タイム.
	 * 500ミリ秒.
	 */
	public static final long MIN_DOUBT_TIMEOUT = 500L;
	
	/**
	 * 最大タイムアウト監視キュー移行タイム.
	 * 5秒.
	 */
	public static final long MAX_DOUBT_TIMEOUT = 5L * 1000L;
	
	/**
	 * タイムアウトキューを監視する最大値.
	 * 15秒に１度.
	 */
	public static final long MAX_TIMEOUT_QUEUE_TIMEOUT_CHECK = 15000L;
	

}
