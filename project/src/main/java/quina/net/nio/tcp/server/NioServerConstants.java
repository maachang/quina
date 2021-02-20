package quina.net.nio.tcp.server;

import quina.net.nio.tcp.NioAtomicValues.Bool;
import quina.net.nio.tcp.NioAtomicValues.Number32;

public class NioServerConstants {
	private NioServerConstants() {}

	/**
	 * サーバ側送信バッファ.
	 */
	private static final int SEND_BUFFER = 32767;

	/**
	 * サーバ側受信バッファ.
	 */
	private static final int RECV_BUFFER = 65535;

	/**
	 * サーバ側KeepAlive.
	 */
	private static final boolean KEEP_ALIVE = false;

	/**
	 * サーバ側TcpNoDeley.
	 */
	private static final boolean TCP_NODELEY = true;

	/**
	 * サーバ最大接続数.
	 */
	private static final int BACKLOG = Integer.MAX_VALUE;

	// TCPサーバ用送信バッファ.
	private static final Number32 sendBuf = new Number32(NioServerConstants.SEND_BUFFER);

	// TCPサーバ用受信バッファ.
	private static final Number32 recvBuf = new Number32(NioServerConstants.RECV_BUFFER);

	// TCPサーバ用keepAlive.
	private static final Bool keepAlive = new Bool(NioServerConstants.KEEP_ALIVE);

	// TCPサーバ用NoDeley.
	private static final Bool tcpNoDeley = new Bool(NioServerConstants.TCP_NODELEY);

	// TCPサーバ同時接続数.
	private static final Number32 backlog = new Number32(NioServerConstants.BACKLOG);

	/**
	 * TCPサーバ用送信バッファ.
	 * @return sendBuf
	 */
	public static final int getSendBuffer() {
		return sendBuf.get();
	}

	/**
	 * TCPサーバ用送信バッファ.
	 * @param sendBuf セットする sendBuf
	 */
	public static final void setSendBuffer(int sendBuf) {
		NioServerConstants.sendBuf.set(sendBuf);
	}

	/**
	 * TCPサーバ用受信バッファ.
	 * @return recvBuf
	 */
	public static final int getRecvBuffer() {
		return recvBuf.get();
	}

	/**
	 * TCPサーバ用受信バッファ.
	 * @param recvBuf セットする recvBuf
	 */
	public static final void setRecvBuffer(int recvBuf) {
		NioServerConstants.recvBuf.set(recvBuf);
	}

	/**
	 * TCPサーバ用keepAlive.
	 * @return keepAlive
	 */
	public static final boolean isKeepAlive() {
		return keepAlive.get();
	}

	/**
	 * TCPサーバ用keepAlive.
	 * @param keepAlive セットする keepAlive
	 */
	public static final void setKeepAlive(boolean keepAlive) {
		NioServerConstants.keepAlive.set(keepAlive);
	}

	/**
	 * TCPサーバ用NoDeley.
	 * @return tcpNoDeley
	 */
	public static final boolean isTcpNoDeley() {
		return tcpNoDeley.get();
	}

	/**
	 * TCPサーバ用NoDeley.
	 * @param tcpNoDeley セットする tcpNoDeley
	 */
	public static final void setTcpNoDeley(boolean tcpNoDeley) {
		NioServerConstants.tcpNoDeley.set(tcpNoDeley);
	}

	/**
	 * TCPサーバ同時接続数.
	 * @return backlog
	 */
	public static final int getBacklog() {
		return backlog.get();
	}

	/**
	 * TCPサーバ同時接続数.
	 * @param backlog セットする backlog
	 */
	public static final void setBacklog(int backlog) {
		NioServerConstants.backlog.set(backlog);
	}

}
