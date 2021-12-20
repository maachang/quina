package quina.net.nio.tcp.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import quina.net.nio.tcp.NioException;
import quina.net.nio.tcp.SslCacerts;
import quina.util.Flag;

/**
 * NioClientSocket生成.
 */
public final class NioClientSocket {
	protected NioClientSocket() {
	}

	private static final Object sync = new Object();
	private static Flag sslFactoryFlag = new Flag(false);
	private static SocketFactory sslFactory = null;

	/** SSLSocketFactory作成. **/
	protected static final SocketFactory getSSLSocketFactory() {
		if (!sslFactoryFlag.get()) {
			synchronized (sync) {
				if(!sslFactoryFlag.get()) {
					InputStream in = null;
					try {
						// キーストアを生成.
						in = new ByteArrayInputStream(SslCacerts.get());
						final KeyStore t = KeyStore.getInstance("JKS");
						t.load(in, SslCacerts.TRUST_PASSWORD);
						in.close();
						in = null;
						// トラストストアマネージャを生成.
						final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
						tmf.init(t);
						// SSLソケットを生成する
						final SSLContext ctx = SSLContext.getInstance("TLS");
						ctx.init(null, tmf.getTrustManagers(), null);
						SSLSocketFactory s = ctx.getSocketFactory();
						// SSLSocketFactoryをセット.
						sslFactory = s;
						sslFactoryFlag.set(true);
					} catch (NioException ne) {
						throw ne;
					} catch (Exception e) {
						throw new NioException(e);
					} finally {
						if (in != null) {
							try {
								in.close();
							} catch (Exception e) {
							}
						}
					}

				}
			}
		}
		return sslFactory;
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
			SSLSocketFactory factory = (SSLSocketFactory) getSSLSocketFactory();
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
