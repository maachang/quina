package quina.command.shutdown;

import java.security.MessageDigest;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
	 * デフォルトシャットダウン送信リトライ数.
	 */
	private static final int DEFAULT_RETRY = 3;

	/**
	 * 最大シャットダウン送信リトライ数.
	 */
	public static final int MAX_RETRY = 15;

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
	 * シャットダウントークンヘッダ.
	 */
	private static final String TOKEN_HEADER = "0x0035, 0x0071, 0x0033, 0x0060";

	/**
	 * 基本的なQuinaShutdownToken.
	 */
	public static final String DEFAULT_TOKEN = "@aniuq";

	/**
	 * シャットダウン送信リトライ回数.
	 */
	private static final AtomicInteger retry = new AtomicInteger(DEFAULT_RETRY);

	/**
	 * シャットダウン通知用の受信ポートバインド番号.
	 */
	private static final AtomicInteger port = new AtomicInteger(DEFAULT_PORT);

	/**
	 * 受信管理タイムアウト値.
	 */
	private static final AtomicInteger timeout = new AtomicInteger(DEF_TIMEOUT);

	/**
	 * シャットダウントークン情報.
	 */
	private static final AtomicReference<byte[]> token =
		new AtomicReference<byte[]>(createShutdownToken(DEFAULT_TOKEN));

	/**
	 * シャットダウン用の相互通信用トークンの作成.
	 * @param token
	 * @return
	 */
	protected static final byte[] createShutdownToken(String token) {
		if(token == null || token.isEmpty()) {
			token = DEFAULT_TOKEN;
		}
		try {
			return MessageDigest.getInstance("SHA-1").
				digest((TOKEN_HEADER + token).getBytes("UTF8"));
		} catch(Exception e) {
			throw new ShutdownException(e);
		}
	}

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
		if(r <= 0) {
			r = 1;
		} else if(r > MAX_RETRY) {
			r = MAX_RETRY;
		}
		while(!retry.compareAndSet(retry.get(), r));
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
	 * @return
	 */
	public static final boolean setPort(int p) {
		if(p > 0 && p < 65535) {
			while(!port.compareAndSet(port.get(), p));
			return true;
		}
		return false;
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
		if(t < 0) {
			t = DEF_TIMEOUT;
		} else if(t > MAX_TIMEOUT) {
			t = MAX_TIMEOUT;
		}
		while(!timeout.compareAndSet(timeout.get(), t));
	}

	/**
	 * シャットダウントークンを設定.
	 * @param t
	 */
	public static final void setShutdownToken(String t) {
		final byte[] b = createShutdownToken(t);
		while(!token.compareAndSet(token.get(), b));
	}

	/**
	 * シャットダウントークンを取得.
	 * @return
	 */
	public static final byte[] getShutdownToken() {
		return token.get();
	}
}
