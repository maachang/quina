package quina.net.nio.tcp;

import quina.exception.CoreException;

/**
 * Nio例外.
 */
public class NioException extends CoreException {
	private static final long serialVersionUID = -1494397605392187603L;
	public NioException(int status) {
		super(status);
	}

	public NioException(int status, String message) {
		super(status, message);
	}

	public NioException(int status, Throwable e) {
		super(status, e);
	}

	public NioException(int status, String message, Throwable e) {
		super(status, message, e);
	}

	public NioException() {
		super();
	}

	public NioException(String m) {
		super(m);
	}

	public NioException(Throwable e) {
		super(e);
	}

	public NioException(String m, Throwable e) {
		super(m, e);
	}
}
