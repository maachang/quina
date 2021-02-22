package quina.net.nio.tcp;

/**
 * Nio例外.
 */
public class NioException extends RuntimeException {
	private static final long serialVersionUID = -1494397605392187603L;
	protected int status;
	protected String msg;

	public NioException(int status) {
		super();
		this.status = status;
	}

	public NioException(int status, String message) {
		super(message);
		this.status = status;
	}

	public NioException(int status, Throwable e) {
		super(e);
		this.status = status;
	}

	public NioException(int status, String message, Throwable e) {
		super(message, e);
		this.status = status;
	}

	public NioException() {
		this(500);
	}

	public NioException(String m) {
		this(500, m);
	}

	public NioException(Throwable e) {
		this(500, e);
	}

	public NioException(String m, Throwable e) {
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
