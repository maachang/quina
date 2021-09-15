package quina.http.worker;

import quina.logger.Log;
import quina.logger.LogFactory;
import quina.net.nio.tcp.NioConstants;
import quina.net.nio.tcp.worker.NioReceiveWorkerElement;
import quina.net.nio.tcp.worker.NioWorkerThreadHandler;
import quina.net.nio.tcp.worker.WorkerElement;

/**
 * Http用ワーカーハンドラ.
 */
public class HttpWorkerHandler implements NioWorkerThreadHandler {
	// ログ出力.
	private static final Log LOG = LogFactory.getInstance().get();

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
	public HttpWorkerHandler(int tmpBufLen) {
		setTmpRecvBufLength(tmpBufLen);
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
			if(LOG.isDebugEnabled()) {
				LOG.debug("*** start worker(" + no + ") thread.");
			}
		}
	}

	/**
	 * １つのワーカースレッド終了.
	 * @param no 対象のワーカースレッド番号が設定されます.
	 */
	@Override
	public void endThreadCall(int no) {
		if(LOG.isInfoEnabled()) {
			if(LOG.isDebugEnabled()) {
				LOG.debug("*** end worker(" + no + ") thread.");
			}
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
		try {
			em.close();
		} catch(Exception e) {}
	}
}
