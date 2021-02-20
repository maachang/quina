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
	 * Httpステータスをセット.
	 * @param state 対象のHttpステータスを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public Response setStatus(HttpStatus state);

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
	 * 送信処理.
	 * @param state Httpステータスを設定します.
	 * @param value 送信データを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	default Response send(int state, byte[] value) {
		return setStatus(state).send(value);
	}

	/**
	 * 送信処理.
	 * @param state Httpステータスを設定します.
	 * @param value 送信データを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	default Response send(HttpStatus state, byte[] value) {
		return setStatus(state).send(value);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	public Response send(byte[] value);

	/**
	 * 送信処理.
	 * @param state Httpステータスを設定します.
	 * @param value 送信データを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	default Response send(int state, String value) {
		return setStatus(state).send(value);
	}

	/**
	 * 送信処理.
	 * @param state Httpステータスを設定します.
	 * @param value 送信データを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	default Response send(HttpStatus state, String value) {
		return setStatus(state).send(value);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	public Response send(InputStream value);

	/**
	 * 送信処理.
	 * @param state Httpステータスを設定します.
	 * @param value 送信データを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	default Response send(int state, InputStream value) {
		return setStatus(state).send(value);
	}

	/**
	 * 送信処理.
	 * @param state Httpステータスを設定します.
	 * @param value 送信データを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	default Response send(HttpStatus state, InputStream value) {
		return setStatus(state).send(value);
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	public Response send(String value);

	/**
	 * 送信処理.
	 * @param state Httpステータスを設定します.
	 * @param name 送信するファイル名を設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	default Response sendFile(int state, String name) {
		return setStatus(state).sendFile(name);
	}

	/**
	 * 送信処理.
	 * @param state Httpステータスを設定します.
	 * @param name 送信するファイル名を設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	default Response sendFile(HttpStatus state, String name) {
		return setStatus(state).sendFile(name);
	}

	/**
	 * 送信処理.
	 * @param name 送信するファイル名を設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	public Response sendFile(String name);

	/**
	 * 送信処理.
	 * @param state Httpステータスを設定します.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	default Response sendJSON(int state, Object value) {
		return setStatus(state).sendJSON(value);
	}

	/**
	 * 送信処理.
	 * @param state Httpステータスを設定します.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	default Response sendJSON(HttpStatus state, Object value) {
		return setStatus(state).sendJSON(value);
	}

	/**
	 * 送信処理.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	public Response sendJSON(Object value);
}
