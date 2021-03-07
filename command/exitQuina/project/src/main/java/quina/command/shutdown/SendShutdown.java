package quina.command.shutdown;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * シャットダウンコネクションを送信.
 */
public class SendShutdown {
	/**
	 * コンストラクタ.
	 */
	protected SendShutdown() {
	}

	// 詳細表示.
	private static volatile boolean verbose = false;

	/**
	 * 詳細表示を行うかチェックします.
	 * @param m
	 */
	public static final void setVerbose(boolean m) {
		verbose = m;
	}

	/**
	 * 詳細表示モードかチェックします.
	 * @return
	 */
	public static final boolean isVerbose() {
		return verbose;
	}

	/**
	 * シャットダウン通知.
	 */
	public static final boolean send() {
		return send(null, -1, -1, ShutdownConstants.getRetry());
	}

	/**
	 * シャットダウン通知.
	 *
	 * @param port 対象のポート番号を設定します.
	 */
	public static final boolean send(int port) {
		return send(null, port, -1, ShutdownConstants.getRetry());
	}

	/**
	 * シャットダウン通知.
	 *
	 * @param port 対象のポート番号を設定します.
	 * @param timeout タイムアウト値を設定します.
	 */
	public static final boolean send(int port, int timeout) {
		return send(null, port, timeout, ShutdownConstants.getRetry());
	}

	/**
	 * シャットダウン通知.
	 *
	 * @param port 対象のポート番号を設定します.
	 * @param timeout タイムアウト値を設定します.
	 * @param retry リトライカウントを設定します.
	 */
	public static final boolean send(int port, int timeout, int retry) {
		return send(null, port,timeout,retry);
	}

	/**
	 * シャットダウン通知.
	 */
	public static final boolean send(String token) {
		return send(token, -1, -1, ShutdownConstants.getRetry());
	}

	/**
	 * シャットダウン通知.
	 *
	 * @param port 対象のポート番号を設定します.
	 */
	public static final boolean send(String token, int port) {
		return send(token, port, -1, ShutdownConstants.getRetry());
	}

	/**
	 * シャットダウン通知.
	 *
	 * @param port 対象のポート番号を設定します.
	 * @param timeout タイムアウト値を設定します.
	 */
	public static final boolean send(String token, int port, int timeout) {
		return send(token, port, timeout, ShutdownConstants.getRetry());
	}

	/**
	 * シャットダウン通知.
	 *
	 * @param port 対象のポート番号を設定します.
	 * @param timeout タイムアウト値を設定します.
	 * @param retry リトライカウントを設定します.
	 */
	public static final boolean send(String token, int port, int timeout, int retry) {
		if(retry <= 0 || retry > ShutdownConstants.MAX_RETRY) {
			if(retry <= 0) {
				retry = 1;
			} else {
				retry = ShutdownConstants.MAX_RETRY;
			}
		}
		if (port <= 0 || port > 65535) {
			port = ShutdownConstants.getPort();
		}
		if(timeout <= 0 || timeout > ShutdownConstants.MAX_TIMEOUT) {
			if(timeout <= 0) {
				timeout = ShutdownConstants.getTimeout();
			} else {
				timeout = ShutdownConstants.MAX_TIMEOUT;
			}
		}
		final byte[] t = token == null ?
			ShutdownConstants.getShutdownToken() :
			ShutdownConstants.createShutdownToken(token);
		if(verbose) {
			StringBuilder buf = new StringBuilder();
			if(ShutdownConstants.DEFAULT_TOKEN.equals(token)) {
				buf.append(" Token [DEFAULT]");
			} else {
				buf.append(" Token \"").append(token).append("\"");
			}
			buf.append(" with a timeout value of ").append(timeout)
				.append(" milliseconds and ").append(retry).append(" retries.");
			System.out.println(buf.toString());
		}
		for (int i = 0; i < retry; i++) {
			if(verbose) {
				System.out.println(" Sends the " + (i + 1) + "th shutdown command.");
			}
			if (sendShutdownConnection(t, port, timeout)) {
				if(verbose) {
					System.out.println(" The shutdown command was successfully sent.");
					System.out.println();
				}
				return true;
			}
			if(verbose) {
				System.out.println(" Failed to send the shutdown command.");
				System.out.println();
			}
		}
		return false;
	}

	/**
	 * シャットダウンコネクションを送信.
	 *
	 * @parma token 対象のトークンを設定します.
	 * @param port シャットダウン先のポート番号を設定します.
	 * @return boolean 停止完了が成立した場合[true]が返されます.
	 */
	protected static boolean sendShutdownConnection(byte[] token, int port, int timeout) {
		if (port <= 0 || port > 65535) {
			port = ShutdownConstants.getPort();
		}
		if(timeout <= 0 || timeout > ShutdownConstants.MAX_TIMEOUT) {
			if(timeout <= 0) {
				timeout = ShutdownConstants.getTimeout();
			} else {
				timeout = ShutdownConstants.MAX_TIMEOUT;
			}
		}
		token = token == null ?
			ShutdownConstants.getShutdownToken() :
			token;
		DatagramSocket s = null;
		try {
			// 規定条件を送信.
			s = new DatagramSocket(null);
			s.setReuseAddress(true);
			s.bind(new InetSocketAddress(InetAddress.getByName(
				ShutdownConstants.LOCAL_ADDRESS), 0));
			s.send(new DatagramPacket(token, 0,token.length,
				InetAddress.getByName(ShutdownConstants.LOCAL_ADDRESS),
				port));
			// 停止完了を待つ.
			s.setSoTimeout(timeout);
			DatagramPacket p = new DatagramPacket(new byte[512], 512);
			s.receive(p);
			// 同じデータが返却された場合は正常終了.
			if(eqShutdownConnection(p, token)) {
				return true;
			}
		} catch (Exception e) {
		} finally {
			if(s != null) {
				try {
					s.close();
				} catch (Exception e) {
				}
			}
		}
		return false;
	}

	/**
	 * シャットダウンコネクションデータが受信されたかチェック.
	 */
	protected static final boolean eqShutdownConnection(
		final DatagramPacket packet, final byte[] token) {
		if (packet.getLength() == token.length) {
			final int len = packet.getLength();
			final byte[] bin = packet.getData();
			for (int i = 0; i < len; i++) {
				if ((token[i] & 0x000000ff) != (bin[i] & 0x000000ff)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
