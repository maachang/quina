package quina.net.nio.tcp;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import quina.net.nio.tcp.NioAtomicValues.Bool;
import quina.net.nio.tcp.client.NioClientConstants;
import quina.net.nio.tcp.server.NioServerConstants;
import quina.util.Flag;

public final class NioUtil {
	/**
	 * ネットワーク初期定義.
	 */
	public static final void initNet() {
		// guiをoff.
		System.setProperty("java.awt.headless", "true");
		// IPV4で処理.
		System.setProperty("java.net.preferIPv4Stack", "" + NioConstants.NET_IPV4_FLAG);
		// DNSキャッシュは300秒.
		System.setProperty("networkaddress.cache.ttl", "" + NioConstants.NET_DNS_CACHE_SECOND);
		// DNS解決失敗した場合のキャッシュ保持しない.
		System.setProperty("networkaddress.cache.negative.ttl", "" + NioConstants.ERROR_DNS_CACHE_TIME);
	}

	/**
	 * Channelをクローズ.
	 * @param ch 対象のSocketChannelを設定します.
	 */
	public static final void closeChannel(Channel ch) {
		if (ch instanceof SocketChannel) {
			try {
				((SocketChannel) ch).socket().close();
			} catch (Throwable e) {
			}
			try {
				ch.close();
			} catch (Throwable e) {
			}
		} else {
			try {
				ch.close();
			} catch (Throwable e) {
			}
		}
	}

	/**
	 * SelectionKeyの破棄.
	 * @param key
	 *            破棄対象のSelectionKeyを設定します.
	 */
	public static final void destroyKey(SelectionKey key) {
		if (key != null) {
			try {
				NioElement em = (NioElement) key.attachment();
				if (em != null) {
					closeNioElement(em);
					return;
				}
			} catch (Exception e) {
			}
			try {
				key.cancel();
			} catch (Exception e) {
			}
			closeChannel(key.channel());
		}
	}

	/**
	 * NioElementをクローズ.
	 * @param em
	 */
	public static final void closeNioElement(NioElement em) {
		try {
			em.close();
		} catch(Exception e) {}
	}

	/**
	 * サーバーソケット作成.
	 * @param addr
	 * @param port
	 * @exception Exception
	 *                例外.
	 */
	public static final ServerSocketChannel createServerSocketChannel(
		String addr, int port) throws Exception {
		return createServerSocketChannel(addr, port
			, NioServerConstants.getBacklog()
			, NioServerConstants.getRecvBuffer());
	}

	/**
	 * サーバーソケット作成.
	 *
	 * @param addr
	 * @param port
	 * @param backlog
	 * @param recvBuf
	 * @exception Exception
	 *                例外.
	 */
	public static final ServerSocketChannel createServerSocketChannel(
		String addr, int port, int backlog, int recvBuf) throws Exception {
		// nio:サーバーソケット作成.
		ServerSocketChannel ch = ServerSocketChannel.open();
		ch.configureBlocking(false);
		ch.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		ch.setOption(StandardSocketOptions.SO_RCVBUF, recvBuf);

		// サーバーソケットBind.
		if (addr == null || (addr = addr.trim()).length() == 0) {
			ch.bind(new InetSocketAddress("0.0.0.0", port), backlog);
		} else {
			ch.bind(new InetSocketAddress(addr, port), backlog);
		}
		return ch;
	}

	/**
	 * Client接続のSocketChannelを初期化.
	 * @param channel
	 *            SocketChannelを設定します.
	 * @return boolean [true]の場合初期化成功です.
	 */
	public static final boolean initClientChannel(SocketChannel channel) {
		return initSocketChannel(channel
				, NioClientConstants.getSendBuffer()
				, NioClientConstants.getRecvBuffer()
				, NioClientConstants.isKeepAlive()
				, NioClientConstants.isTcpNoDeley());
	}

	/**
	 * ServerSocketChannelからacceptされたSocketChannelを初期化.
	 * @param channel
	 *            SocketChannelを設定します.
	 * @return boolean [true]の場合初期化成功です.
	 */
	public static final boolean initAcceptSocket(SocketChannel channel) {
		return initSocketChannel(channel
				, NioServerConstants.getSendBuffer()
				, NioServerConstants.getRecvBuffer()
				, NioServerConstants.isKeepAlive()
				, NioServerConstants.isTcpNoDeley());
	}

	/**
	 * Client接続のSocketChannelを初期化.
	 * @param channel SocketChannelを設定します.
	 * @param sendBuf 送信バッファを設定します.
	 * @param recvBuf 受信バッファを設定します.
	 * @param keepAlive keepAliveモードを設定します.
	 * @param tcpNoDeley tcpNoDeleyモードを設定します.
	 * @return boolean [true]の場合初期化成功です.
	 */
	public static final boolean initSocketChannel(
		SocketChannel channel, int sendBuf, int recvBuf,
		boolean keepAlive, boolean tcpNoDeley) {
		try {
			// ノンブロッキングモードセット.
			channel.configureBlocking(false);
			// ソケット送信バッファのサイズ.
			channel.setOption(StandardSocketOptions.SO_SNDBUF, sendBuf);
			// ソケット受信バッファのサイズ.
			channel.setOption(StandardSocketOptions.SO_RCVBUF, recvBuf);
			// 接続をキープアライブにします.
			channel.setOption(StandardSocketOptions.SO_KEEPALIVE, keepAlive);
			// アドレスを再利用します.
			channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			// Nagleアルゴリズムを無効にします.
			channel.setOption(StandardSocketOptions.TCP_NODELAY, tcpNoDeley);
		} catch (Exception e) {
			try {
				channel.close();
			} catch (Throwable tw) {
				return false;
			}
		}
		return true;
	}

	/**
	 * オブジェクトをInetAddressに変換.
	 * @param addr
	 * @return
	 * @exception IOException
	 */
	public static final InetAddress getInetAddress(Object addr)
		throws IOException {
		return (addr instanceof InetAddress) ?
				(InetAddress)addr : InetAddress.getByName(""+addr);
	}

	/**
	 * InetAddress を文字列変換.
	 * @param addr
	 * @return
	 */
	public static final String getStrAddr(InetAddress addr) {
		// ローカルアドレスはIPアドレス(v4)で
		if(IpV4Util.isLocalIp(addr.getHostAddress())) {
			return addr.getHostAddress();
		// グローバルアドレスではドメイン名で
		} else {
			return addr.getHostName();
		}
	}

	/**
	 * SendData内容をByteBufferにセットする.
	 * @param em NioElementを設定.
	 * @param buf 送信データ受け取り先のByteBufferをセット.
	 * @return trueの場合、送信可能な状態です.
	 * @throws IOException
	 */
	public static final boolean sendDataToWriteByteBuffer(NioElement em, ByteBuffer buf)
		throws IOException {
		NioSendData in = em.getSendData();
		// 送信データが存在し、ByteBufferに設定可能な領域が有る場合.
		while (in != null && buf.hasRemaining()) {
			// 終端を検知.
			if (in.read(buf) == -1) {
				// 現在の sendData をクローズして次の情報を取得.
				em.removeSendData();
				try {
					in.close();
				} catch (Exception e) {
				}
				in = em.getSendData();
			}
		}
		// ByteBufferに情報が格納されている場合は true 返却.
		return buf.remaining() > 0;
	}

	// ランダム名テーブル.
	private static final char[] RANDOM_NAME_CODE = new char[] { (char) 'A', (char) 'B', (char) 'C', (char) 'D', (char) 'E',
			(char) 'F', (char) 'G', (char) 'H', (char) 'I', (char) 'J', (char) 'K', (char) 'L', (char) 'M', (char) 'N',
			(char) 'O', (char) 'P', (char) 'Q', (char) 'R', (char) 'S', (char) 'T', (char) 'U', (char) 'V', (char) 'W',
			(char) 'X', (char) 'Y', (char) 'Z', (char) 'a', (char) 'b', (char) 'c', (char) 'd', (char) 'e', (char) 'f',
			(char) 'g', (char) 'h', (char) 'i', (char) 'j', (char) 'k', (char) 'l', (char) 'm', (char) 'n', (char) 'o',
			(char) 'p', (char) 'q', (char) 'r', (char) 's', (char) 't', (char) 'u', (char) 'v', (char) 'w', (char) 'x',
			(char) 'y', (char) 'z', (char) '0', (char) '1', (char) '2', (char) '3', (char) '4', (char) '5', (char) '6',
			(char) '7', (char) '8', (char) '9', (char) '-', (char) '_' };

	/**
	 * ランダム名を取得.
	 * @param rand ランダムオブジェクトを設定します.
	 * @param nameCount ランダムで作成する文字列長を設定します.
	 * @return String ランダムな文字列が返却されます.
	 */
	public static final String getRandomName(NioRand rand, int randNameSize) {
		StringBuilder buf = new StringBuilder();
		int code = rand.nextInt();
		int cnt = 0;
		while (cnt < randNameSize) {
			buf.append(RANDOM_NAME_CODE[code & 0x003f]);
			cnt ++;
			code = code >> 6;
			if (code == 0) {
				code = rand.nextInt();
			}
		}
		return buf.toString();
	}

	/**
	 * 指定ディレクトリ以下に一意の名前の空のファイルを作成.
	 * @param rand ランダムオブジェクトを設定します.
	 * @param dirName 対象ディレクトリ名を設定します.
	 * @param headName 作成するファイルのヘッダ名を設定します.
	 * @param extension 作成するファイルの拡張子を設定します.
	 * @param randNameSize ランダムファイル名の長さを設定します.
	 * @return String 作成されたファイル名（拡張子抜き）が返却されます.
	 *    headName が存在する場合は headNem + "_" + returnName が返却されます.
	 *    headName が存在しない場合は returnName のみ返却されます.
	 * @throws IOException I/O例外.
	 */
	public static final String createRandomFile(NioRand rand, String dirName
		, String headName, String extension, int randNameSize)
			throws IOException {
		File f = new File(dirName);
		if(!f.isDirectory() || !f.exists() || !f.canRead() || !f.canWrite()) {
			throw new IOException(
				"The specified directory '" + dirName + "' is invalid.");
		}
		f = null;
		if (!dirName.endsWith(File.pathSeparator)) {
			dirName += File.pathSeparator;
		}
		headName = (headName == null || headName.isEmpty()) ? "" : headName;
		extension = (extension == null || extension.isEmpty()) ? "" : extension;
		if(!extension.isEmpty() && !extension.startsWith(".")) {
			extension = "." + extension;
		}
		String name;
		String fpath;
		String dirAndHead = dirName + headName;
		while (true) {
			// ファイル名を取得.
			name = getRandomName(rand, randNameSize);
			fpath = dirAndHead + name + extension;
			// 作成したランダム名のファイルが既に存在する場合.
			if ((f = new File(fpath)).exists()) {
				f = null;
				// 再作成.
				continue;
			// ランダム名のファイルが存在しない場合は空のファイルを作成.
			} else if (f.createNewFile()) {
				f = null;
				// 作成成功.
				break;
			}
			// 作成失敗.
			f = null;
		}
		return headName + name;
	}

	/**
	 * フラグがtrueになるまで待機処理.
	 * @param timeout タイムアウト値を設定します.
	 * @param flg 判別するフラグオブジェクトを設定します.
	 * @return boolean [false]の場合、タイムアウトが発生しました.
	 */
	public static final boolean waitTo(long timeout, Flag flg) {
		if(!flg.get()) {
			long first = -1L;
			if(timeout > 0L) {
				first = System.currentTimeMillis() + timeout;
			}
			while(!flg.get()) {
				if(first != -1L && first < System.currentTimeMillis()) {
					return false;
				}
				try {
					Thread.sleep(30L);
				} catch(Exception e) {}
			}
		}
		return true;
	}

	/**
	 * フラグがtrueになるまで待機処理.
	 * @param timeout タイムアウト値を設定します.
	 * @param flg 判別するフラグオブジェクトを設定します.
	 * @return boolean [false]の場合、タイムアウトが発生しました.
	 */
	public static final boolean waitTo(long timeout, Bool flg) {
		if(!flg.get()) {
			long first = -1L;
			if(timeout > 0L) {
				first = System.currentTimeMillis() + timeout;
			}
			while(!flg.get()) {
				if(first != -1L && first < System.currentTimeMillis()) {
					return false;
				}
				try {
					Thread.sleep(30L);
				} catch(Exception e) {}
			}
		}
		return true;
	}
}
