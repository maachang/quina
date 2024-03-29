package quina.http;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

import quina.http.server.furnishing.BaseSendResponse;

/**
 * Httpレスポンス.
 */
@SuppressWarnings("unchecked")
public interface Response<T>
	extends BaseSendResponse<T>, Closeable {
	/**
	 * レスポンスをクリア.
	 */
	public void close() throws IOException;

	/**
	 * レスポンス情報をリセット.
	 */
	public void reset();

	/**
	 * Httpステータスを取得.
	 * @return HttpStatus Httpステータスが返却されます.
	 */
	public HttpStatus getStatus();

	/**
	 * Httpステータスを取得.
	 * @return int Httpステータスが返却されます.
	 */
	public int getStatusNo();

	/**
	 * Httpステータスのメッセージを取得.
	 * @return String Httpステータスメッセージが返却されます.
	 */
	public String getMessage();

	/**
	 * コンテンツタイプが設定されているかチェック.
	 * @return boolean trueの場合コンテンツタイプは設定されています.
	 */
	public boolean isContentType();

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
	 * @return T レスポンスオブジェクトが返却されます.
	 */
	default T setStatus(int state) {
		return setStatus(HttpStatus.getHttpStatus(state));
	}

	/**
	 * Httpステータスとメッセージをセット.
	 * @param state 対象のHttpステータスを設定します.
	 * @param message 対象のHttpステータスメッセージを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	default T setStatus(int state, String message) {
		this.setStatus(HttpStatus.getHttpStatus(state));
		this.setMessage(message);
		return (T)this;
	}

	/**
	 * Httpステータスとメッセージをセット.
	 * @param state 対象のHttpステータスを設定します.
	 * @param message 対象のHttpステータスメッセージを設定します.
	 * @return T レスポンスオブジェクトが返却されます.
	 */
	default T setStatus(HttpStatus state, String message) {
		this.setStatus(state);
		this.setMessage(message);
		return (T)this;
	}

	/**
	 * Httpステータスをセット.
	 * @param state 対象のHttpステータスを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public T setStatus(HttpStatus state);

	/**
	 * Httpステータスのメッセージをセット.
	 * @param meesage 対象のHttpステータスメッセージを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public T setMessage(String meesage);

	/**
	 * コンテンツタイプを設定.
	 * @param contentType コンテンツタイプを設定します.
	 * @param charset 文字コードを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	default T setContentType(String contentType, String charset) {
		setCharset(charset);
		return setContentType(contentType);
	}

	/**
	 * コンテンツタイプを設定.
	 * @param contentType コンテンツタイプを設定します.
	 * @param charset 文字コードを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	default T setContentType(String contentType, Charset charset) {
		setCharset(charset);
		return setContentType(contentType);
	}

	/**
	 * コンテンツタイプを設定.
	 * @param contentType コンテンツタイプを設定します.
	 * @param charset 文字コードを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	default T setContentType(String contentType, HttpCharset charset) {
		setCharset(charset);
		return setContentType(contentType);
	}

	/**
	 * コンテンツタイプを設定.
	 * @param contentType コンテンツタイプを設定します.
	 * @param charset 文字コードを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	default T setContentType(MediaType contentType, String charset) {
		setCharset(charset);
		return setContentType(contentType);
	}

	/**
	 * コンテンツタイプを設定.
	 * @param contentType コンテンツタイプを設定します.
	 * @param charset 文字コードを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	default T setContentType(MediaType contentType, Charset charset) {
		setCharset(charset);
		return setContentType(contentType);
	}

	/**
	 * コンテンツタイプを設定.
	 * @param contentType コンテンツタイプを設定します.
	 * @param charset 文字コードを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	default T setContentType(MediaType contentType, HttpCharset charset) {
		setCharset(charset);
		return setContentType(contentType);
	}

	/**
	 * コンテンツタイプを設定.
	 * @param contentType コンテンツタイプを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	default T setContentType(MediaType contentType) {
		return setContentType(contentType.getMimeType());
	}

	/**
	 * コンテンツタイプを設定.
	 * @param contentType コンテンツタイプを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public T setContentType(String contentType);

	/**
	 * 文字コードを設定.
	 * @param charset 文字コードを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	default T setCharset(Charset charset) {
		return setCharset(charset.displayName());
	}

	/**
	 * 文字コードを設定.
	 * @param charset 文字コードを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	default T setCharset(HttpCharset charset) {
		if(charset != HttpCharset.NONE) {
			// 未設定の場合は、デフォルトのCharsetを設定.
			return setCharset(HttpConstants.getCharset());
		} else {
			// 設定されてる場合はその値をセット.
			return setCharset(charset.getCharset());
		}
	}

	/**
	 * 文字コードを設定.
	 * @param charset 文字コードを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public T setCharset(String charset);

	/**
	 * キャッシュモードをセット.
	 * @param mode tureの場合キャッシュモードがONになります.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public T setCacheMode(boolean mode);

	/**
	 * キャッシュモードを取得.
	 * @return boolean trueの場合はキャッシュモードはONです.
	 */
	public boolean isCacheMode();

	/**
	 * gzip圧縮モードをセット.
	 * @param mode trueの場合Requestで許可されている場合はGZIP圧縮して返却します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public T setGzip(boolean mode);

	/**
	 * gzip圧縮モードを取得.
	 * @return boolean trueの場合Requestで許可されている場合はGZIP圧縮して返却します.
	 */
	public boolean isGzip();

	/**
	 * クロスドメインを許可するかセット.
	 * @param mode trueの場合クロスドメインを許可します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public T setCors(boolean mode);

	/**
	 * クロスドメインを許可するか取得.
	 * @return boolean trueの場合クロスドメインを許可します.
	 */
	public boolean isCors();
}
