package quina.http;

import java.io.IOException;

import quina.net.nio.tcp.NioElement;
import quina.net.nio.tcp.NioRecvBody;
import quina.util.AtomicNumber;

/**
 * Http要素.
 */
public class HttpElement extends NioElement {
	/** ステータス. **/
	private HttpElementState state = 
		HttpElementState.STATE_RECEIVING_HEADER;

	/** Httpモード. **/
	private CsMode httpMode = null;

	/** MimeTypes. **/
	private MimeTypes mimeTypes = null;

	/** Httpリクエスト. **/
	private Request request = null;

	/** Httpレスポンス. **/
	private Response<?> response = null;

	/** 受信ヘッダチェック済みデータポジション. **/
	private int receiveHeaderPosition = 0;

	/** 受信Body情報. **/
	private NioRecvBody receiveBody;

	/** chunked受信用. **/
	private HttpReceiveChunked recvChunked;
	
	/** threadScope. **/
	private final AtomicNumber threadScope = new AtomicNumber(0);

	/**
	 * コンストラクタ.
	 * @param httpMode httpMode を設定します.
	 * @param mimeTypes mimeTypes を設定します.
	 */
	public HttpElement(CsMode httpMode, MimeTypes mimeTypes) {
		this.httpMode = httpMode;
		this.mimeTypes = mimeTypes;
	}

	/**
	 * オブジェクトクローズ.
	 */
	@Override
	public void close() throws IOException {
		super.close();
		closeReceiveBody();
		if(request != null) {
			request.close();
			request = null;
		}
		if(response != null) {
			response.close();
			response = null;
		}
		receiveHeaderPosition = 0;
		threadScope.set(0);
	}

	/**
	 * ReceiveBodyをクリア.
	 */
	public void closeReceiveBody() {
		if(receiveBody != null) {
			try {
				receiveBody.close();
			} catch(Exception e) {}
			receiveBody = null;
		}
		if(recvChunked != null) {
			recvChunked.clear();
			recvChunked = null;
		}
	}

	/**
	 * Http要素ステータスを設定.
	 * @param state
	 */
	public void setState(HttpElementState state) {
		this.state = state;
	}

	/**
	 * Http要素ステータスを取得.
	 * @return
	 */
	public HttpElementState getState() {
		return state;
	}

	/**
	 * MimeTypesを取得.
	 * @return
	 */
	public MimeTypes getMimeTypes() {
		return mimeTypes;
	}

	/**
	 * HTTPモードを取得.
	 * @return
	 */
	public CsMode getHttpMode() {
		return httpMode;
	}

	/**
	 * 受信ヘッダポジションをリセット.
	 */
	public void resetReceiveHeaderPosition() {
		receiveHeaderPosition = 0;
	}

	/**
	 * リクエストを設定.
	 * @param req
	 * @return
	 */
	public HttpElement setRequest(Request req) {
		this.request = req;
		return this;
	}

	/**
	 * リクエストを取得.
	 * @return
	 */
 	public Request getRequest() {
		return request;
	}

 	/**
 	 * レスポンスを設定.
 	 * @param res
 	 * @return
 	 */
 	public HttpElement setResponse(Response<?> res) {
 		this.response = res;
 		return this;
 	}

 	/**
 	 * レスポンスを取得.
 	 * @return
 	 */
	public Response<?> getResponse() {
		return response;
	}

	/**
	 * RequestやResponseヘッダの読み込み時のポジション値を設定.
	 * @param o
	 * @return
	 */
	public HttpElement setReceiveHeaderPosition(int o) {
		receiveHeaderPosition = o;
		return this;
	}

	/**
	 * RequestやResponseヘッダの読み込み時のポジション値を取得.
	 * @return
	 */
	public int getReceiveHeaderPosition() {
		return receiveHeaderPosition;
	}


	/**
	 * NioRecvBodyをセット.
	 * @param b
	 */
	public void setReceiveBody(NioRecvBody b) {
		receiveBody = b;
	}

	/**
	 * NioRecvBodyを取得.
	 * @return
	 */
	public NioRecvBody getReceiveBody() {
		return receiveBody;
	}

	/**
	 * 受信Bodyのチャンク受信オブジェクトをセット.
	 * @param rc
	 */
	public void setReceiveChunked(HttpReceiveChunked rc) {
		recvChunked = rc;
	}

	/**
	 * 受信Bodyのチャンク受信オブジェクトを取得.
	 * @return
	 */
	public HttpReceiveChunked getReceiveChunked() {
		return recvChunked;
	}
	
	/**
	 * スレッドスコープ値を設定.
	 * @param threadScope 対象のスレッドスコープ値を
	 *                    設定します.
	 */
	public void setThreadScope(int threadScope) {
		this.threadScope.set(threadScope);
	}
	
	/**
	 * 新しいスレッドスコープを実行.
	 */
	public void startThreadScope() {
		threadScope.inc();
	}
	
	/**
	 * 現在のスレッドスコープを終了.
	 */
	public void exitThreadScope() {
		threadScope.dec();
	}
	
	/**
	 * スレッドスコープ値を取得.
	 * @return int スレッドスコープ値が返却されます.
	 */
	public int getThreadScope() {
		return threadScope.get();
	}
}
