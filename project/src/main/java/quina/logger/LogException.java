package quina.logger;

import quina.exception.CoreException;

/**
 * ログ例外.
 */
public class LogException extends CoreException {
	private static final long serialVersionUID = -5683305474945524974L;

	/**
	 * コンストラクタ.
	 */
	public LogException() {
		super();
	}

	/**
	 * コンストラクタ.
	 * @param msg
	 */
	public LogException(String msg) {
		super(msg);
	}
	
	/**
	 * コンストラクタ.
	 * @param e
	 */
	public LogException(Throwable e) {
		super(e);
	}
}
