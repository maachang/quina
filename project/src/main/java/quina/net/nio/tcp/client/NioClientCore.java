package quina.net.nio.tcp.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import quina.net.nio.tcp.NioAtomicValues.Bool;
import quina.net.nio.tcp.NioConstants;
import quina.net.nio.tcp.NioElement;
import quina.net.nio.tcp.NioSelector;
import quina.net.nio.tcp.NioSendData;
import quina.net.nio.tcp.NioSendLess;
import quina.net.nio.tcp.NioUtil;
import quina.net.nio.tcp.NioWorkerCall;
import quina.worker.QuinaWorkerService;

/**
 * NioClientCore.
 * 基本的な使い方として、クライアント側は「送信」して
 * 「送信内容の返信を受け取る」仕組みで設計されています.
 */
public class NioClientCore extends Thread {
	// セレクタータイムアウト.
	private static final int SELECTOR_TIMEOUT = NioConstants.SELECTOR_TIMEOUT;

	// 送信命令管理キュー.
	private final Queue<NioClientSendOrder> queue = new ConcurrentLinkedQueue<NioClientSendOrder>();

	// クライアント処理.
	private NioClientCall call;

	// ByteBufferサイズ.
	private int byteBufferLength;

	// スレッド開始、終了管理フラグ.
	private volatile boolean stopFlag = true;

	// スレッド開始完了フラグ.
	private final Bool startupFlag = new Bool(false);

	// スレッド終了完了フラグ.
	private final Bool exitFlag = new Bool(false);

	// ワーカースレッドマネージャ.
	private QuinaWorkerService workerService;

	/**
	 * コンストラクタ.
	 * @param call NioClientコール処理を設定します.
	 * @param workerService QuinaWorkerServiceを設定します.
	 */
	public NioClientCore(NioClientCall call,QuinaWorkerService workerService) {
		this(NioConstants.getByteBufferLength(), call, workerService);
	}

	/**
	 * コンストラクタ.
	 * @param byteBufferLength Nioで利用するByteBufferのサイズを設定します.
	 * @param call NioClientコール処理を設定します.
	 * @param workerService QuinaWorkerServiceを設定します.
	 */
	public NioClientCore(int byteBufferLength, NioClientCall call,
		QuinaWorkerService workerService) {
		this.byteBufferLength = byteBufferLength;
		this.call = call;
		this.workerService = workerService;
	}

	/**
	 * スレッド開始.
	 */
	public void startThread() {
		stopFlag = false;
		startupFlag.set(false);
		exitFlag.set(false);
		setDaemon(true);
		start();
	}

	/**
	 * スレッド終了.
	 */
	public void stopThread() {
		stopFlag = true;
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
	public boolean awaitStartup() {
		return awaitStartup(-1L);
	}

	/**
	 * スレッド開始完了まで待機.
	 * @param timeout タイムアウトのミリ秒を設定します.
	 *                0以下を設定した場合、無限に待ちます.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean awaitStartup(long timeout) {
		return NioUtil.await(timeout, startupFlag);
	}

	/**
	 * スレッド終了まで待機.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean awaitExit() {
		return awaitExit(-1L);
	}

	/**
	 * スレッド終了まで待機.
	 * @param timeout タイムアウトのミリ秒を設定します.
	 *                0以下を設定した場合、無限に待ちます.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean awaitExit(long timeout) {
		return NioUtil.await(timeout, exitFlag);
	}

	/**
	 * Nioで利用するByteBufferのサイズを取得.
	 * @return int
	 */
	public int getByteBufferLength() {
		return byteBufferLength;
	}

	/**
	 * 送信命令管理キューにクライアント送信命令をセット.
	 * @param channel SocketChannelを設定します.
	 * @param datas 送信データオブジェクトを設定します.
	 */
	public void push(SocketChannel channel, NioSendData... datas) {
		queue.offer(new NioClientSendOrder(channel, datas));
	}

	/**
	 * 送信命令管理キューにクライアント送信命令をセット.
	 * @param sem NioClientSendElementオブジェクトを設定します.
	 */
	public void push(NioClientSendOrder sem) {
		queue.offer(sem);
	}

	/**
	 * 送信命令管理キューに格納されている未送送信数を取得.
	 * @return int 未処理件数を返却します.
	 */
	public int size() {
		return queue.size();
	}

	/**
	 * スレッド実行.
	 */
	public void run() {
		NioSelector selector = null;
		ThreadDeath d = null;
		try {
			// [call] 開始処理.
			if (call.startNio()) {
				// スレッド開始.
				try {
					selector = new NioSelector();
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
			// 未処理のNioクライアント送信条件をクリア.
			NioClientSendOrder nse;
			while((nse = queue.poll()) != null) {
				try {
					nse.clear();
				} catch (Exception e) {
				}
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
		final int OP_READ = SelectionKey.OP_READ;
		final int OP_WRITE = SelectionKey.OP_WRITE;
		final ByteBuffer buf = ByteBuffer.allocateDirect(byteBufferLength);
		final NioClientCall nc = call;
		ThreadDeath ret = null;
		boolean endFlag = false;
		int ops;
		Iterator<SelectionKey> it;
		SelectionKey key = null;
		SocketChannel ch = null;
		NioElement em = null;
		NioSendLess sl = null;
		NioClientSendOrder sendEm = null;
		NioWorkerCall wem = null;
		byte[] rb = null;

		// スレッド開始完了.
		startupFlag.set(true);
		while (!endFlag && !stopFlag) {
			sendEm = null;
			key = null;
			em = null;
			sl = null;
			ch = null;
			wem = null;
			rb = null;
			try {
				while (!endFlag && !stopFlag) {
					// 送信キューにクライアント送信情報が存在する場合.
					while((sendEm = queue.poll()) != null) {
						try {
							// Socketを取得.
							ch = sendEm.getChannel();
							// SocketChannelの初期化処理.
							if(nc.initSocket(ch)) {
								sendEm.destroy();
								continue;
							}
							// 要素情報生成失敗の場合.
							if((em = nc.createElement()) == null) {
								sendEm.destroy();
								continue;
							}
							// 送信オーダから送信データをNio要素にセット.
							em.setSendData(sendEm.getDatas());
							// クライアント送信要素をクリア.
							sendEm.clear(); sendEm = null;
							// selectorに登録.
							em.registor(selector, ch, OP_READ);
							// [call] register 処理コール.
							if (!nc.register(em)) {
								// [call] のconnect 失敗の場合.
								NioUtil.closeNioElement(em);
								NioUtil.closeChannel(ch);
								continue;
							}
							// 送信対象のデータが存在する場合.
							if(em.isSendData()) {
								// 送信開始.
								em.startWrite();
							}
						} catch(Throwable se) {
							if (sendEm != null) {
								sendEm.destroy();
							}
							errorToClean(null, em, ch);
						} finally {
							sendEm = null;
							em = null;
							ch = null;
						}
					}
					sendEm = null;
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
						key = null; em = null; sl = null;
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
										// I/Oタイムアウトを更新.
										em.setIoTimeout();
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
									buf.flip();
									// 受信情報が存在する場合.
									if(buf.remaining() > 0) {
										rb = new byte[buf.remaining()];
										buf.get(rb);
										wem = nc.createNioWorkerCall();
										// ワーカー要素に受信データをセット.
										wem.setReceiveData(em, rb);
										rb = null;
										// I/Oタイムアウトを更新.
										em.setIoTimeout();
										// ワーカーサービスに登録.
										workerService.push(wem);
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
				if (sendEm != null) {
					sendEm.destroy();
					sendEm = null;
				}
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
