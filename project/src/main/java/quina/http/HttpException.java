package quina.http;

import quina.QuinaException;

/**
 * HttpException.
 */
public class HttpException extends QuinaException {
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
		this(HttpStatus.getHttpStatus(status), message);
	}

	public HttpException(int status, Throwable e) {
		this(HttpStatus.getHttpStatus(status), e);
	}

	public HttpException(int status, String message, Throwable e) {
		this(HttpStatus.getHttpStatus(status), message, e);
	}

	public HttpException() {
		this(HttpStatus.InternalServerError);
	}

	public HttpException(String m) {
		this(HttpStatus.InternalServerError, m);
	}

	public HttpException(Throwable e) {
		this(getStatus(e), e);
	}

	public HttpException(String m, Throwable e) {
		this(getStatus(e), m, e);
	}

	private static final int getStatus(Throwable e) {
		if(e instanceof QuinaException) {
			return ((QuinaException)e).getStatus();
		}
		return 500;
	}
}
