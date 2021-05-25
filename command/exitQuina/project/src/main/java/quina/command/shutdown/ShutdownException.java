package quina.command.shutdown;

/**
 * シャットダウン例外.
 */
public class ShutdownException extends RuntimeException {
	private static final long serialVersionUID = -731486070955051391L;

	/**
	 * コンストラクタ.
	 */
	public ShutdownException() {
		super();
	}

	/**
	 * コンストラクタ.
	 * @param message メッセージを設定します.
	 */
	public ShutdownException(String message) {
		super(message);
	}

	/**
	 * コンストラクタ.
	 * @param t 例外を設定します.
	 */
	public ShutdownException(Throwable t) {
		super(t);
	}
}
