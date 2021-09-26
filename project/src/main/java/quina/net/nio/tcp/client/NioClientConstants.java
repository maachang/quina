package quina.net.nio.tcp.client;

import java.net.InetAddress;

import quina.net.nio.tcp.NioAtomicValues.Bool;
import quina.net.nio.tcp.NioAtomicValues.Number32;
import quina.net.nio.tcp.NioAtomicValues.Number64;
import quina.net.nio.tcp.NioAtomicValues.Value;

public class NioClientConstants {
	private NioClientConstants() {}

	/**
	 * TCPクライアント側送信バッファ.
	 */
	private static final int SEND_BUFFER = 65535;

	/**
	 * TCPクライアント側受信バッファ.
	 */
	private static final int RECV_BUFFER = 32767;

	/**
	 * TCPクライアント側KeepAlive.
	 */
	private static final boolean KEEP_ALIVE = false;

	/**
	 * TCPクライアント側TcpNoDeley.
	 */
	private static final boolean TCP_NODELEY = true;

	/**
	 * TCPクライアントバインドアドレス.
	 */
	private static final InetAddress BIND_ADDRESS = null;

	/**
	 * TCPクライアントバインドポート.
	 */
	private static final int BIND_PORT = -1;

	/**
	 * 受信タイムアウト値.
	 */
	private static final int TIMEOUT = 30000;

	/**
	 * リトライ許容回数.
	 */
	private static final int MAX_RETRY = 16;

	// TCPクライアント用送信バッファ.
	private static final Number32 sendBuf = new Number32(NioClientConstants.SEND_BUFFER);

	// TCPクライアント用受信バッファ.
	private static final Number32 recvBuf = new Number32(NioClientConstants.RECV_BUFFER);

	// TCPクライアント用keepAlive.
	private static final Bool keepAlive = new Bool(NioClientConstants.KEEP_ALIVE);

	// TCPクライアント用NoDeley.
	private static final Bool tcpNoDeley = new Bool(NioClientConstants.TCP_NODELEY);

	// TCPクライアント用バインドアドレス.
	private static final Value<InetAddress> bindAddress = new Value<InetAddress>(BIND_ADDRESS);

	// TCPクライアント用バインドポート.
	private static final Number32 bindPort = new Number32(BIND_PORT);

	// 受信タイムアウト(同期モードのみ).
	private static final Number64 timeout = new Number64(TIMEOUT);

	// リトライ許容回数.
	private static final Number32 maxRetry = new Number32(MAX_RETRY);

	/**
	 * TCPクライアント用送信バッファ.
	 * @return sendBuf
	 */
	public static final int getSendBuffer() {
		return sendBuf.get();
	}

	/**
	 * TCPクライアント用送信バッファ.
	 * @param sendBuf セットする sendBuf
	 */
	public static final void setSendBuffer(int sendBuf) {
		NioClientConstants.sendBuf.set(sendBuf);
	}

	/**
	 * TCPクライアント用受信バッファ.
	 * @return recvBuf
	 */
	public static final int getRecvBuffer() {
		return recvBuf.get();
	}

	/**
	 * TCPクライアント用受信バッファ.
	 * @param recvBuf セットする recvBuf
	 */
	public static final void setRecvBuffer(int recvBuf) {
		NioClientConstants.recvBuf.set(recvBuf);
	}

	/**
	 * TCPクライアント用keepAlive.
	 * @return keepAlive
	 */
	public static final boolean isKeepAlive() {
		return keepAlive.get();
	}

	/**
	 * TCPクライアント用keepAlive.
	 * @param keepAlive セットする keepAlive
	 */
	public static final void setKeepAlive(boolean keepAlive) {
		NioClientConstants.keepAlive.set(keepAlive);
	}

	/**
	 * TCPクライアント用NoDeley.
	 * @return tcpNoDeley
	 */
	public static final boolean isTcpNoDeley() {
		return tcpNoDeley.get();
	}

	/**
	 * TCPクライアント用NoDeley.
	 * @param tcpNoDeley セットする tcpNoDeley
	 */
	public static final void setTcpNoDeley(boolean tcpNoDeley) {
		NioClientConstants.tcpNoDeley.set(tcpNoDeley);
	}

	/**
	 * TCPクライアント用バインドアドレス.
	 * @return bindAddress
	 */
	public static final InetAddress getBindAddress() {
		return bindAddress.get();
	}

	/**
	 * TCPクライアント用バインドアドレス.
	 * @param bindAddress セットする bindAddress
	 */
	public static final void setBindPort(InetAddress bindAddress) {
		NioClientConstants.bindAddress.set(bindAddress);
	}

	/**
	 * TCPクライアント用バインドポート.
	 * @return bindPort
	 */
	public static final int getBindPort() {
		return bindPort.get();
	}

	/**
	 * TCPクライアント用バインドポート.
	 * @param bindPort セットする bindPort
	 */
	public static final void setBindPort(int bindPort) {
		NioClientConstants.bindPort.set(bindPort);
	}

	/**
	 * タイムアウト値(同期モードのみ)を取得します.
	 * @return タイムアウト値が返却されます.
	 */
	public static final long getTimeout() {
		return timeout.get();
	}

	/**
	 * タイムアウト値(同期モードのみ)を設定します.
	 * @param timeout タイムアウト値を設定します.
	 *                0 の場合、タイムアウトを無効にできます.
	 */
	public static final void setTimeout(long timeout) {
		if(timeout <= 0L) {
			timeout = 0L;
		}
		NioClientConstants.timeout.set(timeout);
	}

	/**
	 * リトライ許容範囲の回数を取得.
	 * @return int リトライ許容範囲の回数が返却されます.
	 */
	public static final int getMaxRetry() {
		return maxRetry.get();
	}

	/**
	 * リトライ許容範囲の回数を設定します.
	 * @param maxRetry リトライ許容範囲の回数を設定します.
	 */
	public static final void setMaxRetry(int maxRetry) {
		if(maxRetry <= 0) {
			maxRetry = 1;
		}
		NioClientConstants.maxRetry.set(maxRetry);
	}



}
