package quina.exception;

/**
 * QuinaException.
 */
public class QuinaException extends CoreException {
	private static final long serialVersionUID = 3811602491396883951L;
	
	/**
	 * コンストラクタ.
	 * @param status
	 */
	public QuinaException(int status) {
		super(status);
	}

	/**
	 * コンストラクタ.
	 * @param status
	 * @param message
	 */
	public QuinaException(int status, String message) {
		super(status, message);

	}

	/**
	 * コンストラクタ.
	 * @param status
	 * @param e
	 */
	public QuinaException(int status, Throwable e) {
		super(status, e);
	}

	/**
	 * コンストラクタ.
	 * @param status
	 * @param message
	 * @param e
	 */
	public QuinaException(int status, String message, Throwable e) {
		super(status, message, e);
	}

	/**
	 * コンストラクタ.
	 */
	public QuinaException() {
		super();
	}

	public QuinaException(String m) {
		super(m);
	}

	/**
	 * コンストラクタ.
	 * @param e
	 */
	public QuinaException(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ.
	 * @param m
	 * @param e
	 */
	public QuinaException(String m, Throwable e) {
		super(m, e);
	}

}
