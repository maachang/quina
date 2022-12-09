package quina.net.nio.tcp.client;

import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import quina.net.nio.tcp.NioException;
import quina.net.nio.tcp.SslCacerts;

/**
 * NioClientSocket生成.
 */
public final class NioClientSocket {
	protected NioClientSocket() {
	}
	
	/** Httpソケットオプションをセット. **/
	private static final void setSocketOption(Socket soc, long timeout) {
		try {
			soc.setReuseAddress(true);
			soc.setSoLinger(true, 0);
			soc.setSendBufferSize(NioClientConstants.getSendBuffer());
			soc.setReceiveBufferSize(NioClientConstants.getRecvBuffer());
			soc.setKeepAlive(NioClientConstants.isKeepAlive());
			soc.setTcpNoDelay(NioClientConstants.isTcpNoDeley());
			soc.setSoTimeout((int)timeout);
		} catch(Exception e) {
			throw new NioException(e);
		}
	}

	/**
	 * Socket作成.
	 *
	 * @param ssl
	 *            [true]の場合、SSLで接続します.
	 * @param addr
	 *            接続先アドレス(domain)を設定します.
	 * @param port
	 *            対象のポート番号を設定します.
	 * @param timeout
	 *            通信タイムアウト値を設定します.
	 */
	public static final Socket create(boolean ssl, String addr, int port, long timeout) {
		if (ssl) {
			return createSSLSocket(addr, port, timeout);
		} else {
			return createSocket(addr, port, timeout);
		}
	}

	/** SSLSocket生成. **/
	private static final Socket createSSLSocket(String addr, int port, long timeout) {
		SSLSocket ret = null;
		try {
			SSLSocketFactory factory = SslCacerts.getSSLSocketFactory();
			ret = (SSLSocket) factory.createSocket();
			setSocketOption(ret, timeout);
			ret.connect(new InetSocketAddress(addr, port), (int)timeout);
			ret.startHandshake();
		} catch (Exception e) {
			if (ret != null) {
				try {
					ret.close();
				} catch (Exception ee) {
				}
			}
			throw new NioException(e);
		}
		return ret;
	}

	/** Socket生成. **/
	private static final Socket createSocket(String addr, int port, long timeout) {
		Socket ret = new Socket();
		try {
			setSocketOption(ret, timeout);
			ret.connect(new InetSocketAddress(addr, port), (int)timeout);
		} catch (Exception e) {
			try {
				ret.close();
			} catch (Exception ee) {
			}
			throw new NioException(e);
		}
		return ret;
	}
}
