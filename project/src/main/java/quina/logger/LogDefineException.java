package quina.logger;

/**
 * ログ定義例外.
 */
public class LogDefineException extends RuntimeException {
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
