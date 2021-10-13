package quina.jdbc;

import quina.exception.CoreException;
import quina.exception.QuinaException;
import quina.http.HttpStatus;

public class QuinaJDBCException extends QuinaException {
	private static final long serialVersionUID = -5261699680742622910L;
	
	public QuinaJDBCException(HttpStatus status) {
		super(status.getState(), status.getMessage());
	}

	public QuinaJDBCException(HttpStatus status, String message) {
		super(status.getState(), message);
	}

	public QuinaJDBCException(HttpStatus status, Throwable e) {
		super(status.getState(), status.getMessage(), e);
	}

	public QuinaJDBCException(HttpStatus status, String message, Throwable e) {
		super(status.getState(), message, e);
	}

	public QuinaJDBCException(int status) {
		this(HttpStatus.getHttpStatus(status));
	}

	public QuinaJDBCException(int status, String message) {
		super(status, message);
	}

	public QuinaJDBCException(int status, Throwable e) {
		this(HttpStatus.getHttpStatus(status), e);
	}

	public QuinaJDBCException(int status, String message, Throwable e) {
		super(status, message, e);
	}

	public QuinaJDBCException() {
		this(HttpStatus.InternalServerError);
	}

	public QuinaJDBCException(String m) {
		this(HttpStatus.InternalServerError, m);
	}

	public QuinaJDBCException(Throwable e) {
		this(_getStatus(e), e);
	}

	public QuinaJDBCException(String m, Throwable e) {
		this(_getStatus(e), m, e);
	}

	private static final int _getStatus(Throwable e) {
		if(e instanceof CoreException) {
			return ((CoreException)e).getStatus();
		}
		return 500;
	}

}
