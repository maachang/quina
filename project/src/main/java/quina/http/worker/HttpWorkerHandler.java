package quina.http.worker;

import quina.logger.Log;
import quina.logger.LogFactory;
import quina.net.nio.tcp.NioCall;
import quina.net.nio.tcp.NioConstants;
import quina.net.nio.tcp.client.NioClientCall;
import quina.net.nio.tcp.server.NioServerCall;
import quina.net.nio.tcp.worker.NioReceiveWorkerElement;
import quina.net.nio.tcp.worker.WorkerElement;
import quina.net.nio.tcp.worker.NioWorkerPoolingManager;
import quina.net.nio.tcp.worker.NioWorkerThreadHandler;

/**
 * Http用ワーカーハンドラ.
 */
public class HttpWorkerHandler implements NioWorkerThreadHandler {
	// ログ出力.
	private static final Log LOG = LogFactory.getInstance().get();

	// nioサーバ用プーリングマネージャ.
	private NioWorkerPoolingManager serverPooling;

	// nioクライアント用プーリングマネージャ.
	private NioWorkerPoolingManager clientPooling;

	// 受信用テンポラリバッファ長.
	private int tmpBufLen;

	// ワーカースレッドNoに紐づくオブジェクト群.
	private Object[] workersObject;

	/**
	 * コンストラクタ.
	 * @param tmpBufLen 受信テンポラリバッファサイズを設定します.
	 * @param serverPooling nioサーバ用プーリングマネージャを設定します.
	 * @param clientPooling nioクライアント用プーリングマネージャを設定します.
	 */
	public HttpWorkerHandler(int tmpBufLen, NioWorkerPoolingManager serverPooling,
			NioWorkerPoolingManager clientPooling) {
		setTmpRecvBufLength(tmpBufLen);
		setPoolingManagers(serverPooling, clientPooling);
	}


	/**
	 * プーリングマネージャを設定.
	 * @param serverPooling nioサーバ用プーリングマネージャを設定します.
	 * @param clientPooling nioクライアント用プーリングマネージャを設定します.
	 */
	public void setPoolingManagers(NioWorkerPoolingManager serverPooling,
		NioWorkerPoolingManager clientPooling) {
		this.serverPooling = serverPooling;
		this.clientPooling = clientPooling;
	}

	/**
	 * 受信用テンポラリバッファサイズを設定します.
	 * @param tmpBufLen 受信テンポラリバッファサイズを設定します.
	 */
	public void setTmpRecvBufLength(int tmpBufLen) {
		if(tmpBufLen <= 0) {
			tmpBufLen = NioConstants.getByteBufferLength();
		}
		this.tmpBufLen = tmpBufLen;
	}

	/**
	 * 受信用テンポラリバッファサイズを取得.
	 * @return
	 */
	public int getTmpRecvBufLength() {
		return tmpBufLen;
	}

	/**
	 * ワーカースレッド初期化時の呼び出し処理。
	 * @param len ワーカースレッド数が設定されます.
	 */
	@Override
	public void initWorkerThreadManager(int len) {
		// ワーカスレッド毎のテンポラリバッファを生成.
		Object[] lst = new Object[len];
		for(int i = 0; i < len; i ++) {
			lst[i] = new byte[tmpBufLen];
		}
		this.workersObject = lst;
	}

	/**
	 * １つのワーカースレッド開始.
	 * @param no 対象のワーカースレッド番号が設定されます.
	 */
	@Override
	public void startThreadCall(int no) {
		if(LOG.isInfoEnabled()) {
			LOG.info("*** start worker(" + no + ") thread.");
		}
	}

	/**
	 * １つのワーカースレッド終了.
	 * @param no 対象のワーカースレッド番号が設定されます.
	 */
	@Override
	public void endThreadCall(int no) {
		if(LOG.isInfoEnabled()) {
			LOG.info("*** end worker(" + no + ") thread.");
		}
	}

	/**
	 * １つのワーカースレッドでエラーが発生した時のコールバック.
	 * @param no 対象のワーカースレッド番号が設定されます.
	 * @param t 例外処理オブジェクトが設定されます.
	 */
	@Override
	public void errorCall(int no, Throwable t) {
		LOG.error("*** error worker(" + no + ")", t);
	}

	/**
	 * ワーカースレッドNoに紐づくオブジェクトを取得.
	 * @param no 対象のワーカスレッドの項番が設定されます.
	 * @return
	 */
	@Override
	public Object getWorkerThreadObject(int no) {
		return workersObject[no];
	}

	/**
	 * 対象のワーカー処理が終了する場合に呼び出されます.
	 * @param em 終了予定のワーカーオブジェクトが設定されます.
	 */
	@Override
	public void endWorkerElement(WorkerElement em) {
		// 対象要素が「nio受信用ワーカ要素」の場合.
		if(em instanceof NioReceiveWorkerElement) {
			// プーリングにセット.
			final NioReceiveWorkerElement rem = (NioReceiveWorkerElement)em;
			try {
				NioCall call = rem.getCall();
				// NioCallの型がサーバ用の場合.
				if(call instanceof NioServerCall) {
					rem.close();
					if(serverPooling != null) {
						serverPooling.offer(rem);
					}
				// NioCallの型がクライアントの場合.
				} else if(call instanceof NioClientCall) {
					rem.close();
					if(clientPooling != null) {
						clientPooling.offer(rem);
					}
				}
			} catch(Exception e) {}
			return;
		}
	}
}
