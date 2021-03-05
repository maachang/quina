package quina.shutdown;

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
		return send(-1, -1, -1);
	}

	/**
	 * シャットダウン通知.
	 *
	 * @param port 対象のポート番号を設定します.
	 */
	public static final boolean send(int port) {
		return send(port, -1, -1);
	}

	/**
	 * シャットダウン通知.
	 *
	 * @param port 対象のポート番号を設定します.
	 * @param timeout タイムアウト値を設定します.
	 */
	public static final boolean send(int port, int timeout) {
		return send(port, timeout, -1);
	}

	/**
	 * シャットダウン通知.
	 *
	 * @param port 対象のポート番号を設定します.
	 * @param timeout タイムアウト値を設定します.
	 * @param retry リトライカウントを設定します.
	 */
	public static final boolean send(int port, int timeout, int retry) {
		if(retry <= 0 || retry >= ShutdownConstants.MAX_RETRY) {
			retry = ShutdownConstants.getRetry();
		}
		for (int i = 0; i < retry; i++) {
			if (sendShutdownConnection(port, timeout)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * シャットダウンコネクションを送信.
	 *
	 * @param port シャットダウン先のポート番号を設定します.
	 * @return boolean 停止完了が成立した場合[true]が返されます.
	 */
	protected static boolean sendShutdownConnection(int port, int timeout) {
		if (port <= 0 || port > 65535) {
			port = ShutdownConstants.getPort();
		}
		if (timeout <= 0 || timeout >= ShutdownConstants.MAX_TIMEOUT) {
			timeout = ShutdownConstants.getTimeout();
		}
		DatagramSocket s = null;
		try {
			// 規定条件を送信.
			s = new DatagramSocket(null);
			s.setReuseAddress(true);
			s.bind(new InetSocketAddress(InetAddress.getByName(ShutdownConstants.LOCAL_ADDRESS), 0));
			s.send(new DatagramPacket(ShutdownConstants.SHUTDOWN_BINARY, 0,
				ShutdownConstants.SHUTDOWN_BINARY.length, InetAddress.getByName(
					ShutdownConstants.LOCAL_ADDRESS),
				port));
			// 停止完了を待つ.
			s.setSoTimeout(timeout);
			DatagramPacket p = new DatagramPacket(new byte[512], 512);
			s.receive(p);
			// 同じデータが返却された場合は正常終了.
			if(ShutdownManager.eqShutdownConnection(p)) {
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

}
