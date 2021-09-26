package quina.net.nio.tcp;

import java.nio.ByteBuffer;

/**
 * Nioの送信残りバイナリ管理.
 */
public class NioSendLess {
	private byte[] binary = null;
	private int length = 0;

	/**
	 * クリア.
	 */
	public void clear() {
		binary = null;
		length = 0;
	}

	/**
	 * Nioでの送信残りデータをセット.
	 * @param buf 送信できなかった残りのByteBufferを設定します.
	 */
	public void evacuate(ByteBuffer buf) {
		int len = buf.remaining();
		if (len == 0) {
			return;
		} else if (binary == null || binary.length < len) {
			binary = new byte[len + (len >> 1)];
		}
		buf.get(binary, 0, len);
		length = len;
	}

	/**
	 * 前回の送信残り分のデータをセット.
	 * @param buf 前回残り分のデータをセットするByteBufferを設定します.
	 */
	public void setting(ByteBuffer buf) {
		if (length == 0) {
			return;
		}
		buf.put(binary, 0, length);
		length = 0;
	}
}
