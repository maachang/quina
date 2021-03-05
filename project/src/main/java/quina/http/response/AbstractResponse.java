package quina.http.response;

import java.io.IOException;

import quina.http.EditMimeTypes;
import quina.http.Header;
import quina.http.HttpConstants;
import quina.http.HttpElement;
import quina.http.HttpSendHeader;
import quina.http.HttpStatus;
import quina.http.MimeTypes;
import quina.http.Response;
import quina.http.server.CreateResponseHeader;
import quina.http.server.HttpServerConstants;
import quina.net.nio.tcp.NioAtomicValues.Bool;
import quina.net.nio.tcp.NioSendData;

/**
 * 基本レスポンス定義.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractResponse<T> implements Response<T>{
	/** HTTP要素. **/
	protected HttpElement element = null;
	/** MimeTypes. **/
	protected MimeTypes mimeTypes = null;
	/** 送信ヘッダ. **/
	protected HttpSendHeader header = null;
	/** ステータス. **/
	protected HttpStatus state = HttpStatus.OK;
	/** ステータスメッセージ. **/
	protected String message = null;
	/** コンテンツタイプ. **/
	protected String contentType = null;
	/** 文字コード. **/
	protected String charset = HttpConstants.getCharset();
	/** キャッシュモード. **/
	protected boolean cacheMode = !HttpServerConstants.isNoCacheMode();
	/** クロスドメインモード. **/
	protected boolean crossDomain = HttpServerConstants.isCrossDomainMode();
	/** 送信済みフラグ. **/
	protected Bool sendFlag = new Bool(false);

	/**
	 * クローズ処理.
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException {
		element = null;
		mimeTypes = null;
		header = null;
		state = null;
		message = null;
		contentType = null;
		charset = null;
	}

	/**
	 * Httpステータスを取得.
	 * @return HttpStatus Httpステータスが返却されます.
	 */
	@Override
	public HttpStatus getStatus() {
		if(state == null) {
			return HttpStatus.OK;
		}
		return state;
	}

	/**
	 * Httpステータスのメッセージを取得.
	 * @return String Httpステータスメッセージが返却されます.
	 */
	public String getMessage() {
		if(message != null && !message.isEmpty()) {
			return message;
		} else if(state != null) {
			return state.getMessage();
		}
		return "UNKNOWN";
	}

	/**
	 * コンテンツタイプを取得.
	 * @return String コンテンツタイプが返却されます.
	 */
	@Override
	public String getContentType() {
		if(contentType == null || contentType.isEmpty()) {
			return EditMimeTypes.UNKNONW_MIME_TYPE;
		}
		return contentType;
	}

	/**
	 * コンテンツの文字コードを取得.
	 * @return Strig 文字コードが返却されます.
	 */
	@Override
	public String getCharset() {
		if(charset == null || charset.isEmpty()) {
			return HttpConstants.getCharset();
		}
		return charset;
	}


	/**
	 * キャッシュモードを取得.
	 * @return boolean trueの場合はキャッシュモードはONです.
	 */
	public boolean isCacheMode() {
		return cacheMode;
	}

	/**
	 * クロスドメインを許可するか取得.
	 * @return boolean trueの場合クロスドメインを許可します.
	 */
	public boolean isCrossDomain() {
		return crossDomain;
	}

	/**
	 * Httpヘッダ情報を取得.
	 * @return Header Httpヘッダが返却されます.
	 */
	@Override
	public Header getHeader() {
		if(header == null) {
			header = new HttpSendHeader();
		}
		return header;
	}

	/**
	 * Httpステータスをセット.
	 * @param state 対象のHttpステータスを設定します.
	 * @return T レスポンスオブジェクトが返却されます.
	 */
	@Override
	public T setStatus(HttpStatus state) {
		this.state = state;
		return (T)this;
	}

	/**
	 * Httpステータスのメッセージをセット.
	 * @param msg 対象のHttpステータスメッセージを設定します.
	 * @return T レスポンスオブジェクトが返却されます.
	 */
	public T setMessage(String msg) {
		this.message = msg;
		return (T)this;
	}

	/**
	 * コンテンツタイプを設定.
	 * @param contentType コンテンツタイプを設定します.
	 * @return T レスポンスオブジェクトが返却されます.
	 */
	@Override
	public T setContentType(String contentType) {
		this.contentType = contentType;
		return (T)this;
	}

	/**
	 * 文字コードを設定.
	 * @param charset 文字コードを設定します.
	 * @return T レスポンスオブジェクトが返却されます.
	 */
	@Override
	public T setCharset(String charset) {
		this.charset = charset;
		return (T)this;
	}

	/**
	 * キャッシュモードをセット.
	 * @param mode tureの場合キャッシュモードがONになります.
	 * @return T レスポンスオブジェクトが返却されます.
	 */
	public T setCacheMode(boolean mode) {
		this.cacheMode = mode;
		return (T)this;
	}

	/**
	 * クロスドメインを許可するかセット.
	 * @param mode trueの場合クロスドメインを許可します.
	 * @return T レスポンスオブジェクトが返却されます.
	 */
	public T setCrossDomain(boolean mode) {
		this.crossDomain = mode;
		return (T)this;
	}


	/**
	 * 対象のHTTPヘッダを生成します.
	 * @param bodyLength
	 * @param charset
	 * @return
	 */
	protected final NioSendData createHeader(long bodyLength, String charset) {
		return CreateResponseHeader.createHeader(
			state.getState(), getMessage(), mimeTypes, header,
			getContentType(), ResponseUtil.lastCharset(charset),
			!cacheMode, crossDomain, bodyLength);
	}
}
