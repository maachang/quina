package quina.http.server;

import java.io.IOException;

import quina.component.ExecuteComponent;
import quina.exception.QuinaException;
import quina.http.HttpAnalysis;
import quina.http.HttpElement;
import quina.logger.Log;
import quina.logger.LogFactory;
import quina.worker.QuinaWorkerCall;
import quina.worker.QuinaWorkerCallHandler;

/**
 * HttpServer用のWorkerCallハンドラ.
 */
public class HttpServerWorkerCallHandler
	extends QuinaWorkerCallHandler {
	// ログオブジェクト.
	protected final Log log = LogFactory.getInstance().get();
	
	// テンポラリバイナリサイズ.
	private int tmpBinaryLength;
	
	// スレッド単位のテンポラリバイナリ.
	private Object[] tmpBinaryThreadList;
	
	/**
	 * 紐付けたいQuinaCall.getId()と同じIdを取得.
	 * @return Integer 対象となるQuinaCallのIdが返却されます.
	 */
	@Override
	public Integer targetId() {
		return HttpServerConstants.WORKER_CALL_ID;
	}
	
	/**
	 * ワーカーマネージャ初期化時の呼び出し処理。
	 * @param len ワーカースレッド数が設定されます.
	 */
	@Override
	public void initWorkerCall(int len) {
		// スレッド毎のテンポラリバイナリを設定.
		final Object[] lst = new Object[len];
		for(int i = 0; i < len; i ++) {
			lst[i] = new byte[tmpBinaryLength];
		}
		this.tmpBinaryThreadList = lst;
	}
	
	/**
	 * 対象要素の開始時の呼び出し.
	 * @param no ワーカースレッドNoが設定されます.
	 * @param em QuinaWorkerElementを設定します.
	 */
	@Override
	public void startCall(int no, QuinaWorkerCall em) {
		HttpServerWorkerCall wem = (HttpServerWorkerCall)em;
		// 対象要素にこのハンドルをセット.
		wem.setHandler(this);
		// 対象要素にスレッド番号をセット.
		wem.setWorkerNo(no);
		// 開始処理実行.
		wem.startCall(no);
	}
	
	/**
	 * 要素を破棄.
	 * @param no ワーカースレッドNoが設定されます.
	 * @param em QuinaWorkerElementを設定します.
	 */
	@Override
	public void destroy(int no, QuinaWorkerCall em) {
		// destroy処理でContextをクリアする.
		em.setContext(null);
		HttpServerContext.clear();
		// 破棄処理を実施.
		super.destroy(no, em);
	}

	/**
	 * 要素が既に破棄されているかチェック.
	 * @param no ワーカースレッドNoが設定されます.
	 * @param em QuinaWorkerElementを設定します.
	 * @return boolean [true]の場合は既に破棄されています.
	 */
	@Override
	public boolean isDestroy(int no, QuinaWorkerCall em) {
		return super.isDestroy(no, em);
	}
	
	/**
	 * テンポラリバイナリ長を設定.
	 * @param tmpBinaryLength テンポラリバイナリ長を設定します.
	 */
	public void setTmpBinaryLength(int tmpBinaryLength) {
		if(tmpBinaryThreadList != null) {
			throw new QuinaException(
				"Temporary binaries have already been created.");
		}
		this.tmpBinaryLength = tmpBinaryLength;
	}
	
	/**
	 * テンポラリバイナリ長を取得.
	 * @return int テンポラリバイナリ長が返却されます.
	 */
	public int getTmpBinaryLength() {
		return tmpBinaryLength;
	}
	/**
	 * スレッド毎のテンポラリバイナリを取得.
	 * @param threadNo 対象のワーカースレッド番号を設定します.
	 * @return byte[] テンポラリバイナリが返却されます.
	 */
	protected final byte[] getTmpBinary(int threadNo) {
		return (byte[])tmpBinaryThreadList[threadNo];
	}
	
	/**
	 * Http向けのデータ受信処理を実装.
	 * @param recvBin 今回受信されたバイナリ情報を取得します.
	 * @param element 対象のHttp要素を設定します.
	 * @param threadNo 対象のワーカースレッド番号を設定します.
	 * @return boolean trueの場合、正常に処理されました.
	 */
	protected final boolean executeReceive(
		byte[] recvBin, HttpElement element, int threadNo)
		throws IOException {
		while(true) {
			switch(element.getState()) {
			// リクエストヘッダを受信中.
			case STATE_RECEIVING_HEADER:
				// リクエストオブジェクトを作成.
				if(HttpServerAnalysis.getRequest(
					element, null, recvBin)) {
					// リクエストオブジェクト作成完了.
					continue;
				}
				// 完了していないので、次の受信処理で行う.
				return true;
			// リクエストヘッダ受信完了.
			case STATE_END_RECV_HTTP_HEADER:
			// contentLengthに準じたBody受信.
			case STATE_RECV_BODY:
			// chunkedに準じたBody受信.
			case STATE_RECV_CHUNKED_BODY:
				// Body読み込み.
				if(HttpAnalysis.receiveBody(
					getTmpBinary(threadNo),
					element,
					element.getRequest().getContentLength(),
					recvBin)) {
					// 受診処理が完了したので次の処理に移行.
					continue;
				}
				// body読み込みが完了してない場合はfalseが返却されるので、
				// 次の受信処理を引き続き行う.
				return true;
			// 受信完了.
			case STATE_END_RECV:
				// コンポーネント実行.
				ExecuteComponent.getInstance()
					.execute(element);
				// 処理終了.
				return true;
			}
		}
	}
}
