package quina;

import quina.net.nio.tcp.NioException;

/**
 * QuinaException.
 */
public class QuinaException extends NioException {
	//protected int status;
	//protected String msg;

	public QuinaException(int status) {
		super();
		this.status = status;
	}

	public QuinaException(int status, String message) {
		super(message);
		this.status = status;
	}

	public QuinaException(int status, Throwable e) {
		super(e);
		this.status = status;
	}

	public QuinaException(int status, String message, Throwable e) {
		super(message, e);
		this.status = status;
	}

	public QuinaException() {
		this(500);
	}

	public QuinaException(String m) {
		this(500, m);
	}

	public QuinaException(Throwable e) {
		this(500, e);
	}

	public QuinaException(String m, Throwable e) {
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
