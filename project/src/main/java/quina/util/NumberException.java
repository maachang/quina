package quina.util;

import quina.exception.CoreException;

/**
 * NumberException.
 */
public class NumberException extends CoreException {
	private static final long serialVersionUID = 3746230071673497893L;
	public NumberException(int status) {
		super(status);
	}

	public NumberException(int status, String message) {
		super(status, message);
	}

	public NumberException(int status, Throwable e) {
		super(status, e);
	}

	public NumberException(int status, String message, Throwable e) {
		super(status, message, e);
	}

	public NumberException() {
		super();
	}

	public NumberException(String m) {
		super(m);
	}

	public NumberException(Throwable e) {
		super(e);
	}

	public NumberException(String m, Throwable e) {
		super(m, e);
	}
}
