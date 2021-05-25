package quina.util;

import quina.exception.CoreException;

/**
 * StringException.
 */
public class StringException extends CoreException {
	private static final long serialVersionUID = -4288350208273210318L;
	public StringException(int status) {
		super(status);
	}

	public StringException(int status, String message) {
		super(status, message);
	}

	public StringException(int status, Throwable e) {
		super(status, e);
	}

	public StringException(int status, String message, Throwable e) {
		super(status, message, e);
	}

	public StringException() {
		super();
	}

	public StringException(String m) {
		super(m);
	}

	public StringException(Throwable e) {
		super(e);
	}

	public StringException(String m, Throwable e) {
		super(m, e);
	}
}
