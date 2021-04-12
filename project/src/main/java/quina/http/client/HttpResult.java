package quina.http.client;

import java.io.Closeable;
import java.io.InputStream;

import quina.http.Header;
import quina.http.HttpStatus;

/**
 * HttpClient処理結果.
 */
public interface HttpResult extends Closeable {

	/**
	 * HTTPステータス取得.
	 *
	 * @return HttpStatus HTTPステータスが返却されます.
	 */
	public HttpStatus getStatus();

	/**
	 * メッセージを取得.
	 * @return String HTTPメッセージが返却サれます.
	 */
	public String getMessage();

	/**
	 * HTTPヘッダを取得.
	 *
	 * @param key
	 *            キー名を設定します.
	 * @return String 要素が返却されます.
	 */
	public Header getHeader();

	/**
	 * 受信条件がGZIPか取得.
	 *
	 * @return boolean [true]の場合GZIP圧縮されています.
	 */
	public boolean isGzip();

	/**
	 * コンテンツタイプを取得.
	 *
	 * @return String コンテンツタイプが返却されます.
	 */
	public String getContentType();

	/**
	 * コンテンツ長を取得.
	 *
	 * @return long コンテンツ長が返却されます.
	 */
	public long getContentLength();

	/**
	 * Bodyに対する文字コードを取得.
	 * @return String 文字コードが返却されます.
	 */
	public String getCharset();

	/**
	 * レスポンスボディInputStreamを取得.
	 *
	 * @return InputStream レスポンスボディInputStreamが返却されます.
	 */
	public InputStream getInputStream();

	/**
	 * レスポンスボディバイナリを取得.
	 *
	 * @return byte[] レスポンスボディバイナリが返却されます.
	 */
	public byte[] getBody();

	/**
	 * レスポンスボディを取得.
	 *
	 * @return String レスポンスボディが返却されます.
	 */
	public String getText();

	/**
	 * JSONオブジェクトを取得.
	 * @return
	 */
	public Object getJson();
}
