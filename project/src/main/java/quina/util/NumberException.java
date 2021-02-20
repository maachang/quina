package quina.util;

/**
 * NumberException.
 */
public class NumberException extends RuntimeException {
	protected int status;
	protected String msg;

	public NumberException(int status) {
		super();
		this.status = status;
	}

	public NumberException(int status, String message) {
		super(message);
		this.status = status;
	}

	public NumberException(int status, Throwable e) {
		super(e);
		this.status = status;
	}

	public NumberException(int status, String message, Throwable e) {
		super(message, e);
		this.status = status;
	}

	public NumberException() {
		this(500);
	}

	public NumberException(String m) {
		this(500, m);
	}

	public NumberException(Throwable e) {
		this(500, e);
	}

	public NumberException(String m, Throwable e) {
		this(500, m, e);
	}

	public int getStatus() {
		return status;
	}

	public void setMessage(String msg) {
		this.msg = msg;
	}

	public String getMessage() {
		return msg == null ? super.getMessage() : msg;
	}

	public String getLocalizedMessage() {
		return msg == null ? super.getLocalizedMessage() : msg;
	}
}
