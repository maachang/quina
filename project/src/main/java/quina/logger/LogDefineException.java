package quina.logger;

import quina.exception.CoreException;

/**
 * ログ定義例外.
 */
public class LogDefineException extends CoreException {
	private static final long serialVersionUID = -5683305474945524974L;

	/**
	 * コンストラクタ.
	 */
	public LogDefineException() {
		super();
	}

	/**
	 * コンストラクタ.
	 * @param msg
	 */
	public LogDefineException(String msg) {
		super(msg);
	}
}
