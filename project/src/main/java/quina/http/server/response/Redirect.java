package quina.http.server.response;

import quina.QuinaException;
import quina.http.HttpStatus;

/**
 * Redirect.
 */
public class Redirect extends QuinaException {
	// リダイレクト先URL.
	private String url;
	private HttpStatus status;

	/**
	 * コンストラクタ.
	 * @param url リダイレクトURLを設定します.
	 */
	public Redirect(String url) {
		super(301);
		this.status = HttpStatus.MovedPermanently;
		this.url = url;
	}

	/**
	 * コンストラクタ.
	 * @param status HTTPステータスを設定します.
	 * @param url リダイレクトURLを設定します.
	 */
	public Redirect(int status, String url) {
		super(status);
		this.status = HttpStatus.getHttpStatus(status);
		this.url = url;
	}

	/**
	 * コンストラクタ.
	 * @param status HTTPステータスを設定します.
	 * @param url リダイレクトURLを設定します.
	 */
	public Redirect(HttpStatus status, String url) {
		super(status.getState());
		this.status = status;
		this.url = url;
	}

	/**
	 * リダイレクト先URLを取得.
	 * @return Stirng リダイレクト先URLが返却されます.
	 */
	public String getLocation() {
		return url;
	}

	/**
	 * Httpステータスを取得.
	 * @return HttpStatus Httpステータスが返却されます.
	 */
	public HttpStatus getHttpStatus() {
		return this.status;
	}
}
