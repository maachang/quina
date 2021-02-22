package quina.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Httpレスポンス.
 */
public interface Response extends Closeable {
	/**
	 * レスポンスをクリア.
	 */
	public void close() throws IOException;

	/**
	 * Httpステータスを取得.
	 * @return HttpStatus Httpステータスが返却されます.
	 */
	public HttpStatus getStatus();

	/**
	 * Httpステータスのメッセージを取得.
	 * @return String Httpステータスメッセージが返却されます.
	 */
	public String getMessage();

	/**
	 * コンテンツタイプを取得.
	 * @return String コンテンツタイプが返却されます.
	 */
	public String getContentType();

	/**
	 * コンテンツの文字コードを取得.
	 * @return Strig 文字コードが返却されます.
	 */
	public String getCharset();

	/**
	 * Httpヘッダ情報を取得.
	 * @return Header Httpヘッダが返却されます.
	 */
	public Header getHeader();

	/**
	 * Httpステータスをセット.
	 * @param state 対象のHttpステータスを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	default Response setStatus(int state) {
		return setStatus(HttpStatus.getHttpStatus(state));
	}

	/**
	 * Httpステータスとメッセージをセット.
	 * @param state 対象のHttpステータスを設定します.
	 * @param message 対象のHttpステータスメッセージを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	default Response setStatus(int state, String message) {
		this.setStatus(HttpStatus.getHttpStatus(state));
		this.setMessage(message);
		return this;
	}

	/**
	 * Httpステータスとメッセージをセット.
	 * @param state 対象のHttpステータスを設定します.
	 * @param message 対象のHttpステータスメッセージを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	default Response setStatus(HttpStatus state, String message) {
		this.setStatus(state);
		this.setMessage(message);
		return this;
	}

	/**
	 * Httpステータスをセット.
	 * @param state 対象のHttpステータスを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public Response setStatus(HttpStatus state);

	/**
	 * Httpステータスのメッセージをセット.
	 * @param meesage 対象のHttpステータスメッセージを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public Response setMessage(String meesage);

	/**
	 * コンテンツタイプを設定.
	 * @param contentType コンテンツタイプを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public Response setContentType(String contentType);

	/**
	 * 文字コードを設定.
	 * @param charset 文字コードを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public Response setCharset(String charset);

	/**
	 * キャッシュモードをセット.
	 * @param mode tureの場合キャッシュモードがONになります.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public Response setCacheMode(boolean mode);

	/**
	 * キャッシュモードを取得.
	 * @return boolean trueの場合はキャッシュモードはONです.
	 */
	public boolean isCacheMode();

	/**
	 * クロスドメインを許可するかセット.
	 * @param mode trueの場合クロスドメインを許可します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public Response setCrossDomain(boolean mode);

	/**
	 * クロスドメインを許可するか取得.
	 * @return boolean trueの場合クロスドメインを許可します.
	 */
	public boolean isCrossDomain();

	/**
	 * 送信処理.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	public Response send();

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	default Response send(byte[] value) {
		return send(value, HttpConstants.getCharset());
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param charset 変換文字コードが設定されます.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	public Response send(byte[] value, String charset);

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	default Response send(InputStream value) {
		return send(value, -1L, HttpConstants.getCharset());
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	default Response send(InputStream value, long length) {
		return send(value, length, HttpConstants.getCharset());
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	default Response send(InputStream value, int length) {
		return send(value, (long)length, HttpConstants.getCharset());
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @param charset 変換文字コードが設定されます.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	default Response send(InputStream value, int length, String charset) {
		return send(value, (long)length, charset);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @param charset 変換文字コードが設定されます.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	public Response send(InputStream value, long length, String charset);

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	default Response send(String value) {
		return send(value, HttpConstants.getCharset());
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param charset 変換文字コードが設定されます.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	public Response send(String value, String charset);

	/**
	 * 送信処理.
	 * @param name 送信するファイル名を設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	default Response sendFile(String name) {
		return sendFile(name, HttpConstants.getCharset());
	}

	/**
	 * 送信処理.
	 * @param name 送信するファイル名を設定します.
	 * @param charset 変換文字コードが設定されます.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	public Response sendFile(String name, String charset);

	/**
	 * 送信処理.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	default Response sendJSON(Object value) {
		return sendJSON(value, HttpConstants.getCharset());
	}

	/**
	 * 送信処理.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @param charset 変換文字コードが設定されます.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	public Response sendJSON(Object value, String charset);

}
