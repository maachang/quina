package quina.http.server;

import quina.exception.CoreException;
import quina.exception.QuinaException;
import quina.http.HttpElement;
import quina.net.nio.tcp.NioWorkerCall;
import quina.worker.QuinaWorkerConstants;

/**
 * HttpServer用のWorker実行.
 */
public class HttpServerWorkerCall extends NioWorkerCall {
	// WorkerCallHandler.
	protected HttpServerWorkerCallHandler handle;
	
	/**
	 * ワーカー要素用のユニークIDを取得.
	 * @return int ユニークIDを取得します.
	 */
	public int getId() {
		return QuinaWorkerConstants.HTTP_SERVER_WORKER_CALL_ID;
	}
	
	/**
	 * WorkerCallハンドルを設定.
	 * @param handler 対象のWorkerCallハンドルを設定します.
	 */
	protected void setWorkerCallHandler(
		HttpServerWorkerCallHandler handle) {
		this.handle = handle;
	}
	
	/**
	 * 対象要素実行時でエラーが発生した場合の呼び出し.
	 * @param no 対象のスレッドNoを設定します.
	 * @param t 例外(Throwable)が設定されます.
	 */
	@Override
	public void errorCall(int no, Throwable t) {
		if(handle.log.isErrorEnabled()) {
			handle.log.error("*** error threadNo: " + no, t);
		}
	}
	
	/**
	 * 対象要素の実行時の呼び出し.
	 * @param no 対象のスレッドNoを設定します.
	 * @return boolean falseの場合実行処理は失敗しました.
	 */
	@Override
	public boolean executeCall(int no) {
		try {
			// HttpElementを取得.
			final HttpElement httpElement = 
				(HttpElement)super.getNioElement();
			// HttpElementが利用出来ない場合.
			if(httpElement == null ||
				!httpElement.isConnection()) {
				// 処理失敗.
				return false;
			}
			// 受信処理を実行.
			return handle.executeReceive(
				super.getReceiveData(), httpElement, no);
		} catch(CoreException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}

	/**
	 * 要素を破棄.
	 */
	@Override
	public void destroy(int no) {
		super.destroy(no);
		handle = null;
	}

	/**
	 * 要素が既に破棄されているかチェック.
	 * @return boolean [true]の場合は既に破棄されています.
	 */
	@Override
	public boolean isDestroy(int no) {
		return super.isDestroy(no);
	}
}
