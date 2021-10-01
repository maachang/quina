package quina.net.nio.tcp.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import quina.net.nio.tcp.NioSendData;
import quina.net.nio.tcp.NioUtil;

/**
 * Nioクライアント送信ソケット管理.
 */
public class NioClientSendSocket {
	
	private NioClientCore clientCore;
	private int sendBuffer;
	private int recvBuffer;
	private boolean keepAlive;
	private boolean tcpNoDeley;

	private int bindPort;
	private InetAddress bindAddress;

	/**
	 * コンストラクタ.
	 * @param core NioClientCoreオブジェクトを設定します.
	 * @exception IOException I/O例外.
	 */
	public NioClientSendSocket(NioClientCore core)
		throws IOException {
		this(core
			, NioClientConstants.getSendBuffer()
			, NioClientConstants.getRecvBuffer()
			, NioClientConstants.isKeepAlive()
			, NioClientConstants.isTcpNoDeley()
			, null, -1);
	}

	/**
	 * コンストラクタ.
	 * @param core NioClientCoreオブジェクトを設定します.
	 * @param sendBuf 送信バッファを設定します.
	 * @param recvBuf 受信バッファを設定します.
	 * @param keepAlive keepAliveモードを設定します.
	 * @param tcpNoDeley tcpNoDeleyモードを設定します.
	 * @exception IOException I/O例外.
	 */
	public NioClientSendSocket(NioClientCore core, int sendBuf,
		int recvBuf,boolean keepAlive, boolean tcpNoDeley)
		throws IOException {
		this(core, sendBuf, recvBuf, keepAlive, tcpNoDeley,
			null, -1);
	}

	/**
	 * コンストラクタ.
	 * @param core NioClientCoreオブジェクトを設定します.
	 * @param bindAddr バインド先のアドレスを設定します.
	 *                 null の場合はバインドアドレスは設定しません.
	 * @param bindPort バインド先のポート番号を設定します.
	 *                 0以下を設定した場合はバインドポートは設定しません.
	 * @exception IOException I/O例外.
	 */
	public NioClientSendSocket(
		NioClientCore core, Object bindAddr, int bindPort)
		throws IOException {
		this(core
			, NioClientConstants.getSendBuffer()
			, NioClientConstants.getRecvBuffer()
			, NioClientConstants.isKeepAlive()
			, NioClientConstants.isTcpNoDeley()
			, bindAddr, bindPort);
	}

	/**
	 * コンストラクタ.
	 * @param core NioClientCoreオブジェクトを設定します.
	 * @param sendBuf 送信バッファを設定します.
	 * @param recvBuf 受信バッファを設定します.
	 * @param keepAlive keepAliveモードを設定します.
	 * @param tcpNoDeley tcpNoDeleyモードを設定します.
	 * @param bindAddr バインド先のアドレスを設定します.
	 *                 null の場合はバインドアドレスは設定しません.
	 * @param bindPort バインド先のポート番号を設定します.
	 *                 0以下を設定した場合はバインドポートは設定しません.
	 * @exception IOException I/O例外.
	 */
	public NioClientSendSocket(NioClientCore core, int sendBuf,
		int recvBuf, boolean keepAlive, boolean tcpNoDeley,
		Object bindAddr, int bindPort)
		throws IOException {
		this.clientCore = core;
		this.sendBuffer = sendBuf;
		this.recvBuffer = recvBuf;
		this.keepAlive = keepAlive;
		this.tcpNoDeley = tcpNoDeley;
		this.bindAddress = bindAddr != null ?
			NioUtil.getInetAddress(bindAddr) :
			NioClientConstants.getBindAddress();
		this.bindPort = bindPort > 0 ? bindPort :
			NioClientConstants.getBindPort();
	}

	/**
	 * 指定アドレス & ポートに対して、データ送信.
	 * @param addr 接続先のアドレスを設定します.
	 * @param port 接続先のポート番号を設定します.
	 * @param datas 送信データ情報群を設定します.
	 * @exception IOException I/O例外.
	 */
	public void send(Object addr, int port, NioSendData... datas)
		throws IOException {
		send(addr, port, null, -1, datas);
	}

	/**
	 * 指定アドレス & ポートに対して、データ送信.
	 * @param addr 接続先のアドレスを設定します.
	 * @param port 接続先のポート番号を設定します.
	 * @param bindPort バインド先のポートを設定します.
	 * @param datas 送信データ情報群を設定します.
	 * @exception IOException I/O例外.
	 */
	public void send(Object addr, int port, int bindPort,
		NioSendData... datas)
		throws IOException {
		send(addr, port, null, bindPort, datas);
	}

	/**
	 * 指定アドレス & ポートに対して、データ送信.
	 * @param addr 接続先のアドレスを設定します.
	 * @param port 接続先のポート番号を設定します.
	 * @param bindAddr バインド先のアドレスを設定します.
	 * @param datas 送信データ情報群を設定します.
	 * @exception IOException I/O例外.
	 */
	public void send(Object addr, int port, Object bindAddr,
		NioSendData... datas)
		throws IOException {
		send(addr, port, bindAddr, -1, datas);
	}

	/**
	 * 指定アドレス & ポートに対して、データ送信.
	 * @param addr 接続先のアドレスを設定します.
	 * @param port 接続先のポート番号を設定します.
	 * @param bindAddr バインド先のアドレスを設定します.
	 * @param bindPort バインド先のポートを設定します.
	 * @param datas 送信データ情報群を設定します.
	 * @exception IOException I/O例外.
	 */
	public void send(Object addr, int port,
		Object bindAddr, int bindPort, NioSendData... datas)
		throws IOException {
		InetAddress iaddr = NioUtil.getInetAddress(addr);
		InetAddress ibaddr = bindAddr != null ?
				NioUtil.getInetAddress(bindAddr)
				: this.bindAddress;
		bindPort = bindPort > 0 ?
				bindPort : this.bindPort;
		SocketChannel ch = createSocketChannel(
			iaddr, port, ibaddr, bindPort,
			sendBuffer, recvBuffer, keepAlive, tcpNoDeley);
		clientCore.push(ch, datas);
	}
	
	/** SocketChannel生成. **/
	private static final SocketChannel createSocketChannel(
		InetAddress connAddr, int connPort, InetAddress bindAddr,
		int bindPort, int sendBuf, int recvBuf, boolean keepAlive,
		boolean tcpNoDeley)
		throws IOException {
		SocketChannel channel = SocketChannel.open();
		try {
			if(!NioUtil.initSocketChannel(
				channel, sendBuf, recvBuf, keepAlive, tcpNoDeley)) {
				throw new IOException(
					"SocketChannel initialization failed.");
			}
			if(bindAddr != null) {
				if(bindPort > 0) {
					channel.bind(
						new InetSocketAddress(bindAddr, bindPort));
				} else {
					channel.bind(
						new InetSocketAddress(bindAddr, 0));
				}
			} else if(bindPort > 0) {
				channel.bind(new InetSocketAddress(bindPort));
			}
			channel.connect(
				new InetSocketAddress(connAddr, connPort));
		} catch (IOException e) {
			try {
				channel.close();
			} catch (Exception ee) {
			}
			throw e;
		}
		return channel;
	}

	/**
	 * 送信バッファを取得.
	 * @return int
	 */
	public int getSendBuffer() {
		return sendBuffer;
	}

	/**
	 * 受信バッファを取得.
	 * @return int
	 */
	public int getRecvBuffer() {
		return recvBuffer;
	}

	/**
	 * KeepAliveを取得.
	 * @return boolean
	 */
	public boolean isKeepAlive() {
		return keepAlive;
	}

	/**
	 * TcpNoDeleyを取得.
	 * @return boolean
	 */
	public boolean isTcpNoDeley() {
		return tcpNoDeley;
	}

	/**
	 * バインドアドレスを取得.
	 * @return InetAddress
	 */
	public InetAddress getBindAddress() {
		return bindAddress;
	}

	/**
	 * バインドポートを取得.
	 * @return int
	 */
	public int getBindPort() {
		return bindPort;
	}
}
