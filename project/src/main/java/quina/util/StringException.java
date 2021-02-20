package quina.util;

/**
 * BooleanException.
 */
public class StringException extends RuntimeException {
	protected int status;
	protected String msg;

	public StringException(int status) {
		super();
		this.status = status;
	}

	public StringException(int status, String message) {
		super(message);
		this.status = status;
	}

	public StringException(int status, Throwable e) {
		super(e);
		this.status = status;
	}

	public StringException(int status, String message, Throwable e) {
		super(message, e);
		this.status = status;
	}

	public StringException() {
		this(500);
	}

	public StringException(String m) {
		this(500, m);
	}

	public StringException(Throwable e) {
		this(500, e);
	}

	public StringException(String m, Throwable e) {
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
