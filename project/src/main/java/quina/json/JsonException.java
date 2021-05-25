package quina.json;

import quina.exception.CoreException;

/**
 * JSON例外.
 */
public class JsonException extends CoreException {
	private static final long serialVersionUID = -2856761833799946840L;

	public JsonException() {
		super();
	}

	public JsonException(String m) {
		super(m);
	}

	public JsonException(Throwable e) {
		super(e);
	}

	public JsonException(String m, Throwable e) {
		super(m, e);
	}
}
