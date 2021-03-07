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
		final byte[] t = token == null ?
			ShutdownConstants.getShutdownToken() :
			ShutdownConstants.createShutdownToken(token);
		for (int i = 0; i < retry; i++) {
			if (sendShutdownConnection(t, port, timeout)) {
				return true;
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
