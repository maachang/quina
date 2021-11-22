package quina.http.server.response;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.Quina;
import quina.QuinaConfig;
import quina.component.ComponentType;
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
	protected boolean corsMode = HttpServerConstants.isCrossDomainMode();
	/** 送信済みフラグ. **/
	protected Bool sendFlag = new Bool(false);
	/** SendData処理フラグ. **/
	protected Bool execSendDataFlag = new Bool(false);
	/** 元のコンポーネントタイプ **/
	protected ComponentType srcComponentType = null;
	/** Read-Writeロックオブジェクト. **/
	protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * レスポンスの内容をセット.
	 * @param res セット元のResponseを設定します.
	 */
	protected void setting(AbstractResponse<?> res) {
		lock.writeLock().lock();
		try {
			this.element = res.element;
			this.header = res.header;
			this.state = res.state;
			this.message = res.message;
			this.contentType = res.contentType;
			this.charset = res.charset;
			this.cacheMode = res.cacheMode;
			this.gzipMode = res.gzipMode;
			this.corsMode = res.corsMode;
			this.sendFlag.set(res.sendFlag.get());
			this.execSendDataFlag.set(res.execSendDataFlag.get());
			this.srcComponentType = res.srcComponentType;
		} finally {
			lock.writeLock().unlock();
		}
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
		lock.writeLock().lock();
		try {
			element = null;
			header = null;
			state = null;
			message = null;
			contentType = null;
			charset = null;
			cacheMode = !HttpServerConstants.isNoCacheMode();
			gzipMode = HttpServerConstants.isGzipMode();
			corsMode = HttpServerConstants.isCrossDomainMode();
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * レスポンス情報をリセット.
	 */
	@Override
	public void reset() {
		lock.writeLock().lock();
		try {
			header = null;
			state = HttpStatus.OK;
			message = null;
			contentType = null;
			charset = HttpConstants.getCharset();
			cacheMode = !HttpServerConstants.isNoCacheMode();
			gzipMode = HttpServerConstants.isGzipMode();
			corsMode = HttpServerConstants.isCrossDomainMode();
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Httpステータスを取得.
	 * @return HttpStatus Httpステータスが返却されます.
	 */
	@Override
	public HttpStatus getStatus() {
		lock.readLock().lock();
		try {
			if(state == null) {
				return HttpStatus.OK;
			}
			return state;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Httpステータスを取得.
	 * @return int Httpステータスが返却されます.
	 */
	@Override
	public int getStatusNo() {
		lock.readLock().lock();
		try {
			if(state == null) {
				return 200;
			}
			return state.getState();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Httpステータスのメッセージを取得.
	 * @return String Httpステータスメッセージが返却されます.
	 */
	@Override
	public String getMessage() {
		lock.readLock().lock();
		try {
			if(message != null && !message.isEmpty()) {
				return message;
			} else if(state != null) {
				return state.getMessage();
			}
			return "UNKNOWN";
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * コンテンツタイプが設定されているかチェック.
	 * @return boolean trueの場合コンテンツタイプは設定されています.
	 */
	@Override
	public boolean isContentType() {
		lock.readLock().lock();
		try {
			if(contentType == null || contentType.isEmpty()) {
				return false;
			}
			return true;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * コンテンツタイプを取得.
	 * @return String コンテンツタイプが返却されます.
	 */
	@Override
	public String getContentType() {
		lock.readLock().lock();
		try {
			if(contentType == null || contentType.isEmpty()) {
				return MimeTypes.UNKNONW_MIME_TYPE;
			}
			return contentType;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * コンテンツの文字コードを取得.
	 * @return Strig 文字コードが返却されます.
	 */
	@Override
	public String getCharset() {
		lock.readLock().lock();
		try {
			if(charset == null || charset.isEmpty()) {
				return HttpConstants.getCharset();
			}
			return charset;
		} finally {
			lock.readLock().unlock();
		}
	}


	/**
	 * キャッシュモードを取得.
	 * @return boolean trueの場合はキャッシュモードはONです.
	 */
	public boolean isCacheMode() {
		lock.readLock().lock();
		try {
			return cacheMode;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * gzip圧縮モードを取得.
	 * @return boolean trueの場合Requestで許可されている場合はGZIP圧縮して返却します.
	 */
	@Override
	public boolean isGzip() {
		lock.readLock().lock();
		try {
			return gzipMode;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * クロスドメインを許可するか取得.
	 * @return boolean trueの場合クロスドメインを許可します.
	 */
	@Override
	public boolean isCors() {
		lock.readLock().lock();
		try {
			return corsMode;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Httpヘッダ情報を取得.
	 * @return Header Httpヘッダが返却されます.
	 */
	@Override
	public Header getHeader() {
		HttpSendHeader ret = null;
		lock.readLock().lock();
		try {
			ret = header;
		} finally {
			lock.readLock().unlock();
		}
		if(ret == null) {
			ret = new HttpSendHeader();
			lock.writeLock().lock();
			try {
				header = ret;
			} finally {
				lock.writeLock().unlock();
			}
		}
		return ret;
	}

	/**
	 * Httpステータスをセット.
	 * @param state 対象のHttpステータスを設定します.
	 * @return T レスポンスオブジェクトが返却されます.
	 */
	@Override
	public T setStatus(HttpStatus state) {
		lock.writeLock().lock();
		try {
			this.state = state;
		} finally {
			lock.writeLock().unlock();
		}
		return (T)this;
	}

	/**
	 * Httpステータスのメッセージをセット.
	 * @param msg 対象のHttpステータスメッセージを設定します.
	 * @return T レスポンスオブジェクトが返却されます.
	 */
	@Override
	public T setMessage(String msg) {
		lock.writeLock().lock();
		try {
			this.message = msg;
		} finally {
			lock.writeLock().unlock();
		}
		return (T)this;
	}

	/**
	 * コンテンツタイプを設定.
	 * @param contentType コンテンツタイプを設定します.
	 * @return T レスポンスオブジェクトが返却されます.
	 */
	@Override
	public T setContentType(String contentType) {
		lock.writeLock().lock();
		try {
			this.contentType = contentType;
		} finally {
			lock.writeLock().unlock();
		}
		return (T)this;
	}

	/**
	 * 文字コードを設定.
	 * @param charset 文字コードを設定します.
	 * @return T レスポンスオブジェクトが返却されます.
	 */
	@Override
	public T setCharset(String charset) {
		lock.writeLock().lock();
		try {
			this.charset = charset;
		} finally {
			lock.writeLock().unlock();
		}
		return (T)this;
	}

	/**
	 * キャッシュモードをセット.
	 * @param mode tureの場合キャッシュモードがONになります.
	 * @return T レスポンスオブジェクトが返却されます.
	 */
	@Override
	public T setCacheMode(boolean mode) {
		lock.writeLock().lock();
		try {
			this.cacheMode = mode;
		} finally {
			lock.writeLock().unlock();
		}
		return (T)this;
	}

	/**
	 * gzip圧縮モードをセット.
	 * @param mode trueの場合Requestで許可されている場合はGZIP圧縮して返却します.
	 * @return T レスポンスオブジェクトが返却されます.
	 */
	@Override
	public T setGzip(boolean mode) {
		// gzipモードをONに設定.
		if(mode) {
			// requestでgzip対応が可能か取得.
			final boolean reqGzip = ResponseUtil
				.isRequestByGzip(getRequest());
			// requestでgzipが不可の場合.
			if(!reqGzip) {
				// gzipレスポンスは行わない.
				mode = false;
			}
		}
		lock.writeLock().lock();
		try {
			this.gzipMode = mode;
		} finally {
			lock.writeLock().unlock();
		}
		return (T)this;
	}

	/**
	 * クロスドメインを許可するかセット.
	 * @param mode trueの場合クロスドメインを許可します.
	 * @return T レスポンスオブジェクトが返却されます.
	 */
	@Override
	public T setCors(boolean mode) {
		lock.writeLock().lock();
		try {
			this.corsMode = mode;
		} finally {
			lock.writeLock().unlock();
		}
		return (T)this;
	}

	/**
	 * HttpRequestを取得.
	 * @return Request HttpRequestが返却されます.
	 */
	@Override
	public Request getRequest() {
		lock.readLock().lock();
		try {
			return element.getRequest();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * HttpElementを取得.
	 * @return HttpElement HttpElement を取得します.
	 */
	public HttpElement getElement() {
		lock.readLock().lock();
		try {
			return element;
		} finally {
			lock.readLock().unlock();
		}
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
	 * @return boolean trueの場合、送信処理系メソッド
	 *                 [send(..)]の実行は可能です.
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
	 * 元のコンポーネントタイプをセット.
	 * @param componentType 対象のコンポーネントタイプを設定します.
	 */
	protected void setSrcComponentType(ComponentType componentType) {
		if(componentType == null) {
			// コンポーネントタイプが設定されてない場合.
			// エラー４０４などのエラー返却に対してRESTfulで
			// 返却するか取得.
			QuinaConfig conf = Quina.get().getHttpServerConfig();
			if(conf.getBoolean("error404RESTful")) {
				// RESTful返却の場合.
				componentType = ComponentType.RESTful;
			} else {
				// 通常返却の場合.
				componentType = ComponentType.Any;
			}
		}
		this.srcComponentType = componentType;

	}
	
	/**
	 * 元のコンポーネントタイプを取得.
	 * @return ComponentType 元のコンポーネントタイプが返却されます.
	 */
	public ComponentType getSrcComponentType() {
		return srcComponentType;
	}

	/**
	 * 対象のHTTPヘッダを生成します.
	 * @param bodyLength
	 * @param charset
	 * @return
	 */
	protected final NioSendData createHeader(long bodyLength, String charset) {
		lock.readLock().lock();
		try {
			return CreateResponseHeader.createHeader(
				state.getState(), getMessage(), header,
				getContentType(), ResponseUtil.lastCharset(charset),
				!cacheMode, corsMode, bodyLength);
		} finally {
			lock.readLock().unlock();
		}
	}
}
