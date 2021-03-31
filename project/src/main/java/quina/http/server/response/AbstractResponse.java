package quina.http.server.response;

import java.io.IOException;

import quina.component.ComponentType;
import quina.http.EditMimeTypes;
import quina.http.Header;
import quina.http.HttpConstants;
import quina.http.HttpElement;
import quina.http.HttpException;
import quina.http.HttpSendHeader;
import quina.http.HttpStatus;
import quina.http.MimeTypes;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.CreateResponseHeader;
import quina.http.server.HttpServerConstants;
import quina.http.server.furnishing.BaseSendResponse;
import quina.net.nio.tcp.NioAtomicValues.Bool;
import quina.net.nio.tcp.NioSendData;

/**
 * 基本レスポンス定義.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractResponse<T>
	implements Response<T>, BaseSendResponse<T> {
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
	/** gzip圧縮モード. **/
	protected boolean gzipMode = HttpServerConstants.isGzipMode();
	/** クロスドメインモード. **/
	protected boolean crossDomain = HttpServerConstants.isCrossDomainMode();
	/** 送信済みフラグ. **/
	protected Bool sendFlag = new Bool(false);
	/** SendData処理フラグ. **/
	protected Bool execSendDataFlag = new Bool(false);

	/**
	 * レスポンスの内容をセット.
	 * @param res セット元のResponseを設定します.
	 */
	public void setting(AbstractResponse<?> res) {
		element = res.element;
		mimeTypes = res.mimeTypes;
		header = res.header;
		state = res.state;
		message = res.message;
		contentType = res.contentType;
		charset = res.charset;
		cacheMode = res.cacheMode;
		gzipMode = res.gzipMode;
		crossDomain = res.crossDomain;
		sendFlag.set(res.sendFlag.get());
		execSendDataFlag.set(res.execSendDataFlag.get());
	}

	/**
	 * 送信が開始された場合に呼び出します.
	 */
	public void startSend() {
		startSend(true);
	}

	/**
	 * 送信が開始された場合に呼び出します.
	 * @param checkMode trueの場合、既に送信フラグがtrueの場合はエラーが返却されます.
	 */
	public void startSend(boolean checkMode) {
		if(checkMode) {
			if(sendFlag.setToGetBefore(true)) {
				throw new HttpException("The send process has already been called.");
			}
		} else {
			sendFlag.set(true);
		}
	}

	/**
	 * 送信エラーで送信キャンセルされた場合.
	 */
	public void cancelSend() {
		sendFlag.set(false);
	}

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
		cacheMode = !HttpServerConstants.isNoCacheMode();
		gzipMode = HttpServerConstants.isGzipMode();
		crossDomain = HttpServerConstants.isCrossDomainMode();
	}

	/**
	 * レスポンス情報をリセット.
	 */
	@Override
	public void reset() {
		header = null;
		state = HttpStatus.OK;
		message = null;
		contentType = null;
		charset = HttpConstants.getCharset();
		cacheMode = !HttpServerConstants.isNoCacheMode();
		gzipMode = HttpServerConstants.isGzipMode();
		crossDomain = HttpServerConstants.isCrossDomainMode();
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
	 * Httpステータスを取得.
	 * @return int Httpステータスが返却されます.
	 */
	@Override
	public int getStatusNo() {
		if(state == null) {
			return 200;
		}
		return state.getState();
	}

	/**
	 * Httpステータスのメッセージを取得.
	 * @return String Httpステータスメッセージが返却されます.
	 */
	@Override
	public String getMessage() {
		if(message != null && !message.isEmpty()) {
			return message;
		} else if(state != null) {
			return state.getMessage();
		}
		return "UNKNOWN";
	}

	/**
	 * コンテンツタイプが設定されているかチェック.
	 * @return boolean trueの場合コンテンツタイプは設定されています.
	 */
	@Override
	public boolean isContentType() {
		if(contentType == null || contentType.isEmpty()) {
			return false;
		}
		return true;
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
	 * gzip圧縮モードを取得.
	 * @return boolean trueの場合Requestで許可されている場合はGZIP圧縮して返却します.
	 */
	@Override
	public boolean isGzip() {
		return gzipMode;
	}

	/**
	 * クロスドメインを許可するか取得.
	 * @return boolean trueの場合クロスドメインを許可します.
	 */
	@Override
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
	@Override
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
	@Override
	public T setCacheMode(boolean mode) {
		this.cacheMode = !mode;
		return (T)this;
	}

	/**
	 * gzip圧縮モードをセット.
	 * @param mode trueの場合Requestで許可されている場合はGZIP圧縮して返却します.
	 * @return T レスポンスオブジェクトが返却されます.
	 */
	@Override
	public T setGzip(boolean mode) {
		this.gzipMode = mode;
		return (T)this;
	}

	/**
	 * クロスドメインを許可するかセット.
	 * @param mode trueの場合クロスドメインを許可します.
	 * @return T レスポンスオブジェクトが返却されます.
	 */
	@Override
	public T setCrossDomain(boolean mode) {
		this.crossDomain = mode;
		return (T)this;
	}

	/**
	 * HttpRequestを取得.
	 * @return Request HttpRequestが返却されます.
	 */
	@Override
	public Request getRequest() {
		return element.getRequest();
	}

	/**
	 * レスポンスオブジェクトを設定します.
	 * @return Response<?> HttpResponseが返却されます.
	 */
	@Override
	public Response<?> getResponse() {
		return this;
	}

	/**
	 * HttpElementを取得.
	 * @return HttpElement HttpElement を取得します.
	 */
	public HttpElement getElement() {
		return element;
	}

	/**
	 * MimeTypesを取得.
	 * @return MimeTypes MimeTypesが返却されます.
	 */
	public MimeTypes getMimeTypes() {
		return mimeTypes;
	}

	/**
	 * 送信処理が行われているかチェック.
	 * @return boolean trueの場合送信されています.
	 */
	@Override
	public boolean isSend() {
		return sendFlag.get();
	}

	/**
	 * 送信処理系メソッド[send(..)]が実行可能かチェック.
	 * @return boolean trueの場合、送信処理系メソッド[send(..)]の実行は可能です.
	 */
	@Override
	public boolean isCallSendMethod() {
		return true;
	}

	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	public abstract ComponentType getComponentType();

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
