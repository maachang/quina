package quina.shutdown;

/**
 * シャットダウン例外.
 */
public class ShutdownException extends RuntimeException {
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
