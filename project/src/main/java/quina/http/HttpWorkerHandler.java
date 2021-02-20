package quina.http;

import quina.logger.Log;
import quina.logger.LogFactory;
import quina.net.nio.tcp.NioCall;
import quina.net.nio.tcp.NioConstants;
import quina.net.nio.tcp.client.NioClientCall;
import quina.net.nio.tcp.server.NioServerCall;
import quina.net.nio.tcp.worker.NioReceiveWorkerElement;
import quina.net.nio.tcp.worker.NioWorkerElement;
import quina.net.nio.tcp.worker.NioWorkerPoolingManager;
import quina.net.nio.tcp.worker.NioWorkerThreadHandler;

/**
 * Http用ワーカーハンドラ.
 */
public class HttpWorkerHandler implements NioWorkerThreadHandler {
	// ログ出力.
	private static final Log LOG = LogFactory.getInstance().get();

	// nioサーバ用プーリングマネージャ.
	private final NioWorkerPoolingManager serverPooling;

	// nioクライアント用プーリングマネージャ.
	private final NioWorkerPoolingManager clientPooling;

	// 受信用テンポラリバッファ長.
	private int tmpBufLen;

	// ワーカースレッドNoに紐づくオブジェクト群.
	private Object[] workersObject;

	/**
	 * コンストラクタ.
	 * @param serverPooling nioサーバ用プーリングマネージャを設定します.
	 * @param clientPooling nioクライアント用プーリングマネージャを設定します.
	 */
	public HttpWorkerHandler(NioWorkerPoolingManager serverPooling,
		NioWorkerPoolingManager clientPooling, int tmpBufLen) {
		this.serverPooling = serverPooling;
		this.clientPooling = clientPooling;
		if(tmpBufLen <= 0) {
			tmpBufLen = NioConstants.getByteBufferLength();
		}
		this.tmpBufLen = tmpBufLen;
	}

	/**
	 * ワーカースレッド初期化時の呼び出し処理。
	 * @param len ワーカースレッド数が設定されます.
	 */
	@Override
	public void initWorkerThreadManager(int len) {
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
			LOG.info("*** start worker(" + no + ") thread");
		}
	}

	/**
	 * １つのワーカースレッド終了.
	 * @param no 対象のワーカースレッド番号が設定されます.
	 */
	@Override
	public void endThreadCall(int no) {
		if(LOG.isInfoEnabled()) {
			LOG.info("*** end worker(" + no + ") thread");
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
	public void endWorkerElement(NioWorkerElement em) {
		// 対象要素が「nio受信用ワーカ要素」の場合.
		if(em instanceof NioReceiveWorkerElement) {
			// プーリングにセット.
			final NioReceiveWorkerElement rem = (NioReceiveWorkerElement)em;
			try {
				NioCall call = rem.getCall();
				// NioCallの型がサーバ用の場合.
				if(call instanceof NioServerCall) {
					rem.close();
					serverPooling.offer(rem);
				// NioCallの型がクライアントの場合.
				} else if(call instanceof NioClientCall) {
					rem.close();
					clientPooling.offer(rem);
				}
			} catch(Exception e) {}
			return;
		}
	}
}
