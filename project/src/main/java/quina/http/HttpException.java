package quina.http;

import quina.exception.CoreException;

/**
 * HttpException.
 */
public class HttpException extends CoreException {
	private static final long serialVersionUID =
		-6152362160134736688L;

	public HttpException(HttpStatus status) {
		super(status.getState(), status.getMessage());
	}

	public HttpException(HttpStatus status, String message) {
		super(status.getState(), message);
	}

	public HttpException(HttpStatus status, Throwable e) {
		super(status.getState(), status.getMessage(), e);
	}

	public HttpException(HttpStatus status, String message, Throwable e) {
		super(status.getState(), message, e);
	}

	public HttpException(int status) {
		this(HttpStatus.getHttpStatus(status));
	}

	public HttpException(int status, String message) {
		super(status, message);
	}

	public HttpException(int status, Throwable e) {
		this(HttpStatus.getHttpStatus(status), e);
	}

	public HttpException(int status, String message, Throwable e) {
		super(status, message, e);
	}

	public HttpException() {
		this(HttpStatus.InternalServerError);
	}

	public HttpException(String m) {
		this(HttpStatus.InternalServerError, m);
	}

	public HttpException(Throwable e) {
		this(_getStatus(e), e);
	}

	public HttpException(String m, Throwable e) {
		this(_getStatus(e), m, e);
	}
}
