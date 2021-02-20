package quina.util;

/**
 * BooleanException.
 */
public class DateException extends RuntimeException {
	protected int status;
	protected String msg;

	public DateException(int status) {
		super();
		this.status = status;
	}

	public DateException(int status, String message) {
		super(message);
		this.status = status;
	}

	public DateException(int status, Throwable e) {
		super(e);
		this.status = status;
	}

	public DateException(int status, String message, Throwable e) {
		super(message, e);
		this.status = status;
	}

	public DateException() {
		this(500);
	}

	public DateException(String m) {
		this(500, m);
	}

	public DateException(Throwable e) {
		this(500, e);
	}

	public DateException(String m, Throwable e) {
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
