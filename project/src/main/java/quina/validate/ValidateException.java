package quina.validate;

import quina.exception.CoreException;

/**
 * Validate例外.
 */
public class ValidateException extends CoreException {
	private static final long serialVersionUID = 1986130764272672301L;
	public ValidateException(int status) {
		super(status);
	}

	public ValidateException(int status, String message) {
		super(status, message);
	}

	public ValidateException(int status, Throwable e) {
		super(status, e);
	}

	public ValidateException(int status, String message, Throwable e) {
		super(status, message, e);
	}

	public ValidateException() {
		super();
	}

	public ValidateException(String m) {
		super(m);
	}

	public ValidateException(Throwable e) {
		super(e);
	}

	public ValidateException(String m, Throwable e) {
		super(m, e);
	}

	public int getStatus() {
		return super.getStatus();
	}

	public void setMessage(String msg) {
		super.setMessage(msg);
	}

	public String getMessage() {
		return super.getMessage();
	}

	public String getLocalizedMessage() {
		return super.getLocalizedMessage();
	}
}
