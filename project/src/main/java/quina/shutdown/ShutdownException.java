package quina.shutdown;

import quina.exception.CoreException;

/**
 * シャットダウン例外.
 */
public class ShutdownException extends CoreException {
	private static final long serialVersionUID = 2451048062021273370L;

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
