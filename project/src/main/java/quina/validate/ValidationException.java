package quina.validate;

import quina.exception.CoreException;

/**
 * Validate例外.
 */
public class ValidationException extends CoreException {
	private static final long serialVersionUID = 1986130764272672301L;
	public ValidationException(int status) {
		super(status);
	}

	public ValidationException(int status, String message) {
		super(status, message);
	}

	public ValidationException(int status, Throwable e) {
		super(status, e);
	}

	public ValidationException(int status, String message, Throwable e) {
		super(status, message, e);
	}

	public ValidationException() {
		super();
	}

	public ValidationException(String m) {
		super(m);
	}

	public ValidationException(Throwable e) {
		super(e);
	}

	public ValidationException(String m, Throwable e) {
		super(m, e);
	}
}
