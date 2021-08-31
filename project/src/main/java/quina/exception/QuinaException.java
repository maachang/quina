package quina.exception;

/**
 * QuinaException.
 */
public class QuinaException extends CoreException {
	private static final long serialVersionUID = 3811602491396883951L;
	public QuinaException(int status) {
		super(status);
	}

	public QuinaException(int status, String message) {
		super(status, message);
	}

	public QuinaException(int status, Throwable e) {
		super(status, e);
	}

	public QuinaException(int status, String message, Throwable e) {
		super(status, message, e);
	}

	public QuinaException() {
		super();
	}

	public QuinaException(String m) {
		super(m);
	}

	public QuinaException(Throwable e) {
		super(e);
	}

	public QuinaException(String m, Throwable e) {
		super(m, e);
	}
}
