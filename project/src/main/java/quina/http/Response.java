package quina.http;

import java.io.Closeable;
import java.io.IOException;

/**
 * Httpレスポンス.
 */
@SuppressWarnings("unchecked")
public interface Response<T> extends Closeable {
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
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public T setContentType(String contentType);

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
	 * クロスドメインを許可するかセット.
	 * @param mode trueの場合クロスドメインを許可します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public T setCrossDomain(boolean mode);

	/**
	 * クロスドメインを許可するか取得.
	 * @return boolean trueの場合クロスドメインを許可します.
	 */
	public boolean isCrossDomain();
}
