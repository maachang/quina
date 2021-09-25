package quina.http.server.furnishing;

import quina.http.HttpStatus;

public interface BaseSendResponse<T>
	extends CoreSendResponse<T> {
	/**
	 * フォワード処理.
	 * @param path フォワード先のパスを設定します.
	 * @return T オブジェクトが返却されます.
	 */
	public T forward(String path);

	/**
	 * リダイレクト処理.
	 * @param url リダイレクトURLを設定します.
	 * @return T オブジェクトが返却されます.
	 */
	public T redirect(String url);

	/**
	 * リダイレクト処理.
	 * @param status HTTPステータスを設定します.
	 * @param url リダイレクトURLを設定します.
	 * @return T オブジェクトが返却されます.
	 */
	public T redirect(int status, String url);

	/**
	 * リダイレクト処理.
	 * @param status HTTPステータスを設定します.
	 * @param url リダイレクトURLを設定します.
	 * @return T オブジェクトが返却されます.
	 */
	public T redirect(HttpStatus status, String url);

}
