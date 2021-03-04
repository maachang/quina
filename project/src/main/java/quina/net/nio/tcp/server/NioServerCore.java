package quina.net.nio.tcp.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import quina.net.nio.tcp.NioAtomicValues.Bool;
import quina.net.nio.tcp.NioConstants;
import quina.net.nio.tcp.NioElement;
import quina.net.nio.tcp.NioSelector;
import quina.net.nio.tcp.NioSendLess;
import quina.net.nio.tcp.NioUtil;
import quina.net.nio.tcp.worker.NioReceiveWorkerElement;
import quina.net.nio.tcp.worker.NioWorkerPoolingManager;
import quina.net.nio.tcp.worker.NioWorkerThreadManager;

/**
 * 基本Nio処理. accept,read,writeの nioイベントを１つのスレッドで処理します。
 */
public class NioServerCore extends Thread {
	// セレクタータイムアウト.
	private static final int SELECTOR_TIMEOUT = NioConstants.SELECTOR_TIMEOUT;

	// ByteBufferサイズ.
	private int byteBufferLength;

	// コネクション済みのSocketに対する定義.
	private int sendBuffer; // Socket送信バッファ長.
	private int recvBuffer; // Socket受信バッファ長
	private boolean keepAlive; // KeepAlive
	private boolean tcpNoDeley; // TcpNoDeley.
	private ServerSocketChannel server; // ServerSocketChannel.
	private NioServerCall call; // サーバ処理.

	// スレッド開始、終了管理フラグ.
	private volatile boolean stopFlag = true;

	// スレッド開始完了フラグ.
	private final Bool startupFlag = new Bool(false);

	// スレッド終了完了フラグ.
	private final Bool exitFlag = new Bool(false);

	// ワーカースレッドマネージャ.
	private NioWorkerThreadManager workerMan;

	// ワーカープーリングマネージャ.
	private NioWorkerPoolingManager pooling;

	/**
	 * コンストラクタ.
	 *
	 * @param server ServerSocketChannelを設定します.
	 * @param call NioServerCallを設定します.
	 * @param pooling ワーカープーリングマネージャを設定します.
	 * @param workerMan ワーカースレッドマネージャを設定します.
	 * @exception IOException I/O例外.
	 */
	public NioServerCore(ServerSocketChannel server, NioServerCall call,
		NioWorkerPoolingManager pooling, NioWorkerThreadManager workerMan)
		throws IOException {
		this(NioConstants.getByteBufferLength()
			, server, call
			, pooling, workerMan);
	}

	/**
	 * コンストラクタ.
	 *
	 * @param byteBufferLength Nioで利用するByteBufferのサイズを設定します.
	 * @param server ServerSocketChannelを設定します.
	 * @param call NioServerCallを設定します.
	 * @param pooling ワーカープーリングマネージャを設定します.
	 * @param workerMan ワーカースレッドマネージャを設定します.
	 * @exception IOException I/O例外.
	 */
	public NioServerCore(int byteBufferLength, ServerSocketChannel server,
		NioServerCall call, NioWorkerPoolingManager pooling, NioWorkerThreadManager workerMan)

		throws IOException {
		this(byteBufferLength
			, NioServerConstants.getSendBuffer()
			, NioServerConstants.getRecvBuffer()
			, NioServerConstants.isKeepAlive()
			, NioServerConstants.isTcpNoDeley()
			, server, call
			, pooling, workerMan);
	}

	/**
	 * コンストラクタ.
	 *
	 * @param byteBufferLength Nioで利用するByteBufferのサイズを設定します.
	 * @param sendBuffer 送信バッファを設定します.
	 * @param recvBuffer 受信バッファを設定します.
	 * @param keepAlive keepAliveモードを設定します.
	 * @param tcpNoDeley tcpNoDeleyモードを設定します.
	 * @param server ServerSocketChannelを設定します.
	 * @param call NioServerCallを設定します.
	 * @param pooling ワーカープーリングマネージャを設定します.
	 * @param workerMan ワーカースレッドマネージャを設定します.
	 * @exception IOException I/O例外.
	 */
	public NioServerCore(int byteBufferLength, int sendBuffer,
		int recvBuffer, boolean keepAlive, boolean tcpNoDeley,
		ServerSocketChannel server, NioServerCall call,
		NioWorkerPoolingManager pooling, NioWorkerThreadManager workerMan)
		throws IOException {
		if(server.isBlocking()) {
			server.configureBlocking(false);
		}
		this.byteBufferLength = byteBufferLength;
		this.sendBuffer = sendBuffer;
		this.recvBuffer = recvBuffer;
		this.keepAlive = keepAlive;
		this.tcpNoDeley = tcpNoDeley;
		this.server = server;
		this.call = call;
		this.workerMan = workerMan;
		this.pooling = pooling;
		call.init();
	}

	/**
	 * スレッド開始.
	 */
	public void startThread() {
		stopFlag = false;
		startupFlag.set(false);
		exitFlag.set(false);
		setDaemon(true);
		call.startThread();
		start();
	}

	/**
	 * スレッド終了.
	 */
	public void stopThread() {
		stopFlag = true;
		call.stopThread();
	}

	/**
	 * スレッドが停止指示されているかチェック.
	 * @return
	 */
	public boolean isStopThread() {
		return stopFlag;
	}

	/**
	 * スレッドが開始完了しているかチェック.
	 * @return
	 */
	public boolean isStartupThread() {
		return startupFlag.get();
	}

	/**
	 * スレッドが終了しているかチェック.
	 * @return
	 */
	public boolean isExitThread() {
		return exitFlag.get();
	}

	/**
	 * スレッド開始完了まで待機.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean waitToStartup() {
		return waitToStartup(-1L);
	}

	/**
	 * スレッド開始完了まで待機.
	 * @param timeout タイムアウトのミリ秒を設定します.
	 *                0以下を設定した場合、無限に待ちます.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean waitToStartup(long timeout) {
		return NioUtil.waitTo(timeout, startupFlag);
	}

	/**
	 * スレッド終了まで待機.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean waitToExit() {
		return waitToExit(-1L);
	}

	/**
	 * スレッド終了まで待機.
	 * @param timeout タイムアウトのミリ秒を設定します.
	 *                0以下を設定した場合、無限に待ちます.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean waitToExit(long timeout) {
		return NioUtil.waitTo(timeout, exitFlag);
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
	 * Nioで利用するByteBufferのサイズを取得.
	 * @return int
	 */
	public int getByteBufferLength() {
		return byteBufferLength;
	}

	// スレッド実行.
	public void run() {
		NioSelector selector = null;
		ThreadDeath d = null;
		try {
			// [call] 開始処理.
			if (call.startNio()) {
				// Selectorの初期化.
				// ServerSocketChannelをSelectorに登録.
				try {
					selector = new NioSelector();
					selector.register(server, SelectionKey.OP_ACCEPT);
					d = executeThread(selector);
				} catch (Exception e) {
					call.error(e);
				}
			}
		} finally {
			// セレクタクローズ.
			if (selector != null) {
				try {
					selector.close();
				} catch (Exception e) {
				}
			}
			// サーバーソケットクローズ.
			try {
				server.close();
			} catch (Exception e) {
			}
			// [call] 終了処理.
			try {
				call.endNio();
			} catch (Exception e) {
			}
			exitFlag.set(true);
		}
		if (d != null) {
			throw d;
		}
	}

	/** 処理スレッド. **/
	private final ThreadDeath executeThread(final NioSelector selector) {
		final int OP_ACCEPT = SelectionKey.OP_ACCEPT;
		final int OP_READ = SelectionKey.OP_READ;
		final int OP_WRITE = SelectionKey.OP_WRITE;
		final int ssb = sendBuffer;
		final int srb = recvBuffer;
		final boolean kpF = keepAlive;
		final boolean tnF = tcpNoDeley;
		final ByteBuffer buf = ByteBuffer.allocateDirect(byteBufferLength);
		final ServerSocketChannel sc = server;
		final NioServerCall nc = call;
		ThreadDeath ret = null;
		boolean endFlag = false;
		int ops;
		Iterator<SelectionKey> it;
		SelectionKey key = null;
		SocketChannel ch = null;
		NioElement em = null;
		NioSendLess sl = null;
		NioReceiveWorkerElement wem = null;
		byte[] rb = null;

		// スレッド開始完了.
		startupFlag.set(true);
		while (!endFlag && !stopFlag) {
			key = null;
			em = null;
			sl = null;
			ch = null;
			wem = null;
			rb = null;
			try {
				while (!endFlag && !stopFlag) {
					key = null;
					em = null;
					sl = null;
					ch = null;
					wem = null;
					rb = null;
					if (!selector.select(SELECTOR_TIMEOUT)) {
						continue;
					}
					it = selector.iterator();
					while (it.hasNext()) {
						key = null;
						em = null;
						sl = null;
						ch = null;
						wem = null;
						rb = null;
						try {
							// 今回処理対象の内容を取得.
							key = it.next();
							it.remove();
							// 対象キーが存在しない場合は処理しない.
							if (key == null || !key.isValid()) {
								// 取得情報が無効な場合は、オブジェクトクローズ.
								NioUtil.destroyKey(key);
								continue;
							}
							// オプション処理を取得.
							ops = key.readyOps();
							// accept(ServerSocketに接続)が検知された場合.
							if ((ops & OP_ACCEPT) == OP_ACCEPT) {
								// accept処理.
								if ((ch = sc.accept()) == null) {
									// 失敗の場合はクローズ.
									NioUtil.destroyKey(key);
									key = null;
									continue;
								}
								// ソケット初期化.
								if (NioUtil.initSocketChannel(ch, ssb, srb, kpF, tnF)) {
									// SocketChannelの初期化処理.
									if(!nc.initSocket(ch)) {
										NioUtil.closeChannel(ch);
										ch = null;
										continue;
									}
									// 要素作成.
									if((em = nc.createElement()) == null) {
										// 接続ソケットをクローズ.
										NioUtil.closeChannel(ch);
										ch = null;
										continue;
									}
									// Nio要素にSocketChannelを登録.
									em.registor(selector, ch, OP_READ);
									// [call] accept処理コール.
									if (!nc.accept(em)) {
										// falseの場合はクローズ.
										NioUtil.closeNioElement(em);
										em = null;
										continue;
									}
								} else {
									// 接続ソケットをクローズ.
									NioUtil.closeChannel(ch);
								}
								// 次の処理.
								continue;
							}
							// (Socket)読み込み処理および書き込み処理.
							if ((ops & OP_WRITE) == OP_WRITE || (ops & OP_READ) == OP_READ) {
								// 必要情報の取得に失敗
								if ((em = (NioElement) key.attachment()) == null) {
									NioUtil.destroyKey(key);
									continue;
								}
								// ソケットチャネルを取得.
								ch = (SocketChannel) key.channel();
								// 書き込み可能処理.
								if ((ops & OP_WRITE) == OP_WRITE) {
									// 送信前処理.
									buf.clear();
									// 前回の送信残りがある場合は設定.
									sl = em.getSendLess();
									sl.setting(buf);
									// [call] 送信データをByteBufferにセット.
									if (!nc.send(em, buf)) {
										// データが無くなったらクローズ.
										NioUtil.closeNioElement(em);
										em = null;
										continue;
									}
									// 書き込み処理後.
									buf.flip();
									// ByteBufferに送信データが存在する場合は送信処理.
									if (buf.hasRemaining()) {
										if (ch.write(buf) == -1) {
											// 通信エラーの場合はクローズ.
											NioUtil.closeNioElement(em);
											em = null;
											continue;
										}
										// 送信の残り（未送信バイナリ）が存在する場合.
										sl.evacuate(buf);
									}
									sl = null;
								}
								// 読み込み可能処理.
								if ((ops & OP_READ) == OP_READ) {
									// 受信処理.
									buf.clear();
									if (ch.read(buf) == -1) {
										// 通信エラーの場合はクローズ.
										NioUtil.closeNioElement(em);
										em = null;
										continue;
									}
									// 受信結果をワーカースレッドに登録.
									buf.flip();
									// 受信情報が存在する場合.
									if(buf.remaining() > 0) {
										rb = new byte[buf.remaining()];
										buf.get(rb);
										// ワーカー要素をプーリングマネージャから取得.
										wem = (NioReceiveWorkerElement)pooling.poll();
										if(wem == null) {
											// 存在しない場合は生成.
											wem = new NioReceiveWorkerElement(nc);
										}
										// ワーカー要素に受信データをセット.
										wem.setReceiveData(em, rb);
										rb = null;
										// ワーカースレッドマネージャに登録.
										workerMan.push(em, wem);
									}
								}
							}
						} catch (IOException e) {
							errorToClean(key, em, ch);
							key = null; em = null; ch = null;
						}
					}
				}
			} catch (Throwable to) {
				errorToClean(key, em, ch);
				key = null; em = null; ch = null;
				if (to instanceof InterruptedException) {
					endFlag = true;
				} else if (to instanceof ThreadDeath) {
					endFlag = true;
					ret = (ThreadDeath) to;
				}
			}
		}
		return ret;
	}

	// エラー時のクリーン処理.
	private static final void errorToClean(SelectionKey key, NioElement em, SocketChannel ch) {
		if (key != null) {
			NioUtil.destroyKey(key);
		}
		if (em != null) {
			NioUtil.closeNioElement(em);
		}
		if (ch != null) {
			NioUtil.closeChannel(ch);
		}
	}
}
