package quina.exception;

/**
 * 基底例外.
 */
public class CoreException extends RuntimeException {
	private static final long serialVersionUID = 3031571979036707289L;
	protected int status;
	protected String msg;

	/**
	 * コンストラクタ.
	 * @param status
	 */
	public CoreException(int status) {
		super();
		this.status = status;
	}

	/**
	 * コンストラクタ.
	 * @param status
	 * @param message
	 */
	public CoreException(int status, String message) {
		super(message);
		this.status = status;
	}

	/**
	 * コンストラクタ.
	 * @param status
	 * @param e
	 */
	public CoreException(int status, Throwable e) {
		super(_getMessage(null, e), e);
		this.status = status;
	}

	/**
	 * コンストラクタ.
	 * @param status
	 * @param message
	 * @param e
	 */
	public CoreException(int status, String message, Throwable e) {
		super(_getMessage(message, e), e);
		this.status = status;
	}

	/**
	 * コンストラクタ.
	 */
	public CoreException() {
		this(500);
	}

	public CoreException(String m) {
		this(500, m);
	}

	/**
	 * コンストラクタ.
	 * @param e
	 */
	public CoreException(Throwable e) {
		this(_getStatus(e), e);
	}

	/**
	 * コンストラクタ.
	 * @param m
	 * @param e
	 */
	public CoreException(String m, Throwable e) {
		this(_getStatus(e), m, e);
	}

	/**
	 * ステータスを取得.
	 * @return int ステータスが返却されます.
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * メッセージを設定.
	 * @param msg 対象のメッセージを設定します.
	 * @return CoreException 例外.
	 */
	public CoreException setMessage(String msg) {
		this.msg = msg;
		return this;
	}
	
	/**
	 * メッセージを取得.
	 * @return String メッセージが返却されます.
	 */
	public String getMessage() {
		return msg == null ? super.getMessage() : msg;
	}

	/**
	 * メッセージを取得.
	 * @return String メッセージが返却されます.
	 */
	public String getLocalizedMessage() {
		return msg == null ? super.getLocalizedMessage() : msg;
	}
	
	/**
	 * ステータスを取得.
	 * @param e 例外を設定します.
	 * @return int ステータスが返却されます.
	 */
	protected static final int _getStatus(Throwable e) {
		if(e instanceof CoreException) {
			return ((CoreException)e).getStatus();
		}
		return 500;
	}
	
	/**
	 * メッセージを取得.
	 * @param msg メッセージを設定します.
	 * @param e 例外を設定します.
	 * @return メッセージが返却されます.
	 */
	protected static final String _getMessage(String msg, Throwable e) {
		if(msg == null || msg.isEmpty()) {
			return e.getMessage();
		}
		return msg;
	}
}

