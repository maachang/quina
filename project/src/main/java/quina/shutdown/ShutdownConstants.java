package quina.shutdown;

import quina.util.AtomicNumber;

/**
 * シャットダウン定義.
 */
public class ShutdownConstants {
	private ShutdownConstants() {}

	/**
	 * ローカルアドレス.
	 */
	public static final String LOCAL_ADDRESS = "127.0.0.1";

	/**
	 * シャットダウン送信内容.
	 */
	public static final byte[] SHUTDOWN_BINARY = {
		(byte) 0x90, (byte) 0x17, (byte) 0x36, (byte) 0x39
	};

	/**
	 * デフォルトシャットダウン送信リトライ数.
	 */
	private static final int DEFAULT_RETRY = 12;

	/**
	 * 最大シャットダウン送信リトライ数.
	 */
	public static final int MAX_RETRY = 99;

	/**
	 * デフォルトポート番号.
	 */
	private static final int DEFAULT_PORT = 3332;

	/**
	 * デフォルトタイムアウト.
	 */
	private static final int DEF_TIMEOUT = 5000;

	/**
	 * 最大タイムアウト値.
	 */
	public static final int MAX_TIMEOUT = 30000;

	/**
	 * シャットダウン送信リトライ回数.
	 */
	private static final AtomicNumber retry = new AtomicNumber(DEFAULT_RETRY);

	/**
	 * シャットダウン通知用の受信ポートバインド番号.
	 */
	private static final AtomicNumber port = new AtomicNumber(DEFAULT_PORT);

	/**
	 * 受信管理タイムアウト値.
	 */
	private static final AtomicNumber timeout = new AtomicNumber(DEF_TIMEOUT);

	/**
	 * シャットダウン送信リトライ回数を取得.
	 * @return
	 */
	public static final int getRetry() {
		return retry.get();
	}

	/**
	 * シャットダウン送信リトライ回数を設定.
	 * @param r
	 */
	public static final void setRetry(int r) {
		retry.set(r);
	}

	/**
	 * シャットダウンバインドポートを取得.
	 * @return
	 */
	public static final int getPort() {
		return port.get();
	}

	/**
	 * シャットダウンバインドポートを設定.
	 * @param p
	 */
	public static final void setPort(int p) {
		port.set(p);
	}

	/**
	 * 受信タイムアウト値を取得.
	 * @return
	 */
	public static final int getTimeout() {
		return timeout.get();
	}

	/**
	 * 受信タイムアウト値を設定.
	 * @param t
	 */
	public static final void setTimeout(int t) {
		timeout.set(t);
	}
}
