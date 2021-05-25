package quina.util;

import quina.exception.CoreException;

/**
 * BooleanException.
 */
public class BooleanException extends CoreException {
	private static final long serialVersionUID = -238943056702354406L;
	public BooleanException(int status) {
		super(status);
	}

	public BooleanException(int status, String message) {
		super(status, message);
	}

	public BooleanException(int status, Throwable e) {
		super(status, e);
	}

	public BooleanException(int status, String message, Throwable e) {
		super(status, message, e);
	}

	public BooleanException() {
		super();
	}

	public BooleanException(String m) {
		super(m);
	}

	public BooleanException(Throwable e) {
		super(e);
	}

	public BooleanException(String m, Throwable e) {
		super(m, e);
	}
}
