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

import quina.net.nio.tcp.NioAtomicValues.Bool;
import quina.net.nio.tcp.NioException;
import quina.net.nio.tcp.SslCacerts;

/**
 * NioClientSocket生成.
 */
final class NioClientCreateSocket {
	protected NioClientCreateSocket() {
	}

	private static final Object sync = new Object();
	private static Bool sslFactoryFlag = new Bool(false);
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

	/** Socket基本オプション. **/
	private static final int LINGER = 0;
	private static final int SENDBUF = 8192;
	private static final int RECVBUF = 32767;
	private static final boolean TCP_NODELAY = false;
	private static final boolean KEEP_ALIVE = false;

	/** Httpソケットオプションをセット. **/
	private static final void setSocketOption(Socket soc, int timeout) {
		try {
			soc.setReuseAddress(true);
			soc.setSoLinger(true, LINGER);
			soc.setSendBufferSize(SENDBUF);
			soc.setReceiveBufferSize(RECVBUF);
			soc.setKeepAlive(KEEP_ALIVE);
			soc.setTcpNoDelay(TCP_NODELAY);
			soc.setSoTimeout(timeout);
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
	public static final Socket create(boolean ssl, String addr, int port, int timeout) {
		if (ssl) {
			return createSSLSocket(addr, port, timeout);
		} else {
			return createSocket(addr, port, timeout);
		}
	}

	/** SSLSocket生成. **/
	private static final Socket createSSLSocket(String addr, int port, int timeout) {
		SSLSocket ret = null;
		try {
			SSLSocketFactory factory = (SSLSocketFactory) getSSLSocketFactory();
			ret = (SSLSocket) factory.createSocket();
			setSocketOption(ret, timeout);
			ret.connect(new InetSocketAddress(addr, port), timeout);
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
	private static final Socket createSocket(String addr, int port, int timeout) {
		Socket ret = new Socket();
		try {
			setSocketOption(ret, timeout);
			ret.connect(new InetSocketAddress(addr, port), timeout);
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
