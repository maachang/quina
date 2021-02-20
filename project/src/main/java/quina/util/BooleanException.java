package quina.util;

/**
 * BooleanException.
 */
public class BooleanException extends RuntimeException {
	protected int status;
	protected String msg;

	public BooleanException(int status) {
		super();
		this.status = status;
	}

	public BooleanException(int status, String message) {
		super(message);
		this.status = status;
	}

	public BooleanException(int status, Throwable e) {
		super(e);
		this.status = status;
	}

	public BooleanException(int status, String message, Throwable e) {
		super(message, e);
		this.status = status;
	}

	public BooleanException() {
		this(500);
	}

	public BooleanException(String m) {
		this(500, m);
	}

	public BooleanException(Throwable e) {
		this(500, e);
	}

	public BooleanException(String m, Throwable e) {
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
