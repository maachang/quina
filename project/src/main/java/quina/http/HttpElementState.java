package quina.http;

/**
 * Http要素ステータス.
 */
public enum HttpElementState {
	/** ステータス : ヘッダ受信中. **/
	STATE_RECEIVING_HEADER(0, "receivingHeader"),
	/** ステータス : ヘッダ受信完了. **/
	STATE_END_RECV_HTTP_HEADER(1, "endRecvHttpHeader"),
	/** ステータス : ContentLengthによるBody受信中. **/
	STATE_RECV_BODY(2, "stateRecvBody"),
	/** ステータス : chunkedによるBody受信中. **/
	STATE_RECV_CHUNKED_BODY(3, "recvChunkedBody"),
	/** ステータス : 受信終了. **/
	STATE_END_RECV(9, "endRecv");

	private int state;
	private String name;

	/**
	 * コンストラクタ.
	 * @param state
	 * @param name
	 */
	private HttpElementState(int state, String name) {
		this.state = state;
		this.name = name;
	}

	/**
	 * ステータスをコードで取得.
	 * @return
	 */
	public int getState() {
		return state;
	}

	/**
	 * Http要素ステータスを文字列で取得.
	 * @return
	 */
	public String getName() {
		return name;
	}
}
