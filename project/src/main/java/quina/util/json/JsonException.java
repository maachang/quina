package quina.util.json;

import quina.QuinaException;

/**
 * JSON例外.
 */
public class JsonException extends QuinaException {
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
