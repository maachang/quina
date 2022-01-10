package quina.http;

import quina.exception.CoreException;

/**
 * 空のレスポンス返却を行う場合に利用する例外.
 */
public class HttpEmptySendResponse extends CoreException {
	private static final long serialVersionUID = 4172694969503538788L;
	
	/**
	 * コンストラクタ.
	 * この呼び出しの場合、レスポンスに設定されてる
	 * ステータスが有効となります.
	 */
	public HttpEmptySendResponse() {
		// HTTPステータス０を設定.
		// この設定の場合、既に設定されているレスポンスステータスを
		// 利用する.
		super(0);
	}
	
	/**
	 * コンストラクタ.
	 * @param status HTTPステータスを設定します.
	 */
	public HttpEmptySendResponse(HttpStatus status) {
		super(status.getState(), status.getMessage());
	}

	/**
	 * コンストラクタ.
	 * @param status HTTPステータスを設定します.
	 * @param message HTTPステータスメッセージを設定します.
	 */
	public HttpEmptySendResponse(HttpStatus status, String message) {
		super(status.getState(), message);
	}
	
	/**
	 * コンストラクタ.
	 * @param status HTTPステータスを設定します.
	 */
	public HttpEmptySendResponse(int status) {
		this(HttpStatus.getHttpStatus(status));
	}

	/**
	 * コンストラクタ.
	 * @param status HTTPステータスを設定します.
	 * @param message HTTPステータスメッセージを設定します.
	 */
	public HttpEmptySendResponse(int status, String message) {
		this(HttpStatus.getHttpStatus(status), message);
	}
}
