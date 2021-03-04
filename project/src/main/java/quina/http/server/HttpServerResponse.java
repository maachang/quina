package quina.http.server;

import java.io.IOException;
import java.io.InputStream;

import quina.http.EditMimeTypes;
import quina.http.Header;
import quina.http.HttpConstants;
import quina.http.HttpElement;
import quina.http.HttpException;
import quina.http.HttpSendChunked;
import quina.http.HttpSendChunkedInputStreamData;
import quina.http.HttpSendHeader;
import quina.http.HttpStatus;
import quina.http.MimeTypes;
import quina.http.Response;
import quina.json.Json;
import quina.net.nio.tcp.NioAtomicValues.Bool;
import quina.net.nio.tcp.NioSendBinaryListData;
import quina.net.nio.tcp.NioSendData;
import quina.net.nio.tcp.NioSendFileData;
import quina.net.nio.tcp.NioSendInputStreamData;
import quina.util.FileUtil;

/**
 * Httpサーバ用Httpレスポンス.
 */
public class HttpServerResponse implements Response {
	/** HTTP要素. **/
	private HttpElement element = null;
	/** MimeTypes. **/
	private MimeTypes mimeTypes = null;
	/** 送信ヘッダ. **/
	private HttpSendHeader header = null;
	/** ステータス. **/
	private HttpStatus state = HttpStatus.OK;
	/** ステータスメッセージ. **/
	private String message = null;
	/** コンテンツタイプ. **/
	private String contentType = null;
	/** 文字コード. **/
	private String charset = HttpConstants.getCharset();
	/** キャッシュモード. **/
	private boolean cacheMode = !HttpServerConstants.isNoCacheMode();
	/** クロスドメインモード. **/
	private boolean crossDomain = HttpServerConstants.isCrossDomainMode();
	/** 送信済みフラグ. **/
	private Bool sendFlag = new Bool(false);

	/**
	 * コンストラクタ.
	 * @param element Http要素を設定します.
	 * @param mimeTypes MimeType群を設定します.
	 */
	protected HttpServerResponse(HttpElement element, MimeTypes mimeTypes) {
		this.element = element;
		this.mimeTypes = mimeTypes;
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
	 * @return HttpServerResponse レスポンスオブジェクトが返却されます.
	 */
	@Override
	public Response setStatus(HttpStatus state) {
		this.state = state;
		return this;
	}

	/**
	 * Httpステータスのメッセージをセット.
	 * @param msg 対象のHttpステータスメッセージを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public Response setMessage(String msg) {
		this.message = msg;
		return this;
	}

	/**
	 * コンテンツタイプを設定.
	 * @param contentType コンテンツタイプを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	@Override
	public Response setContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	/**
	 * 文字コードを設定.
	 * @param charset 文字コードを設定します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	@Override
	public Response setCharset(String charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * キャッシュモードをセット.
	 * @param mode tureの場合キャッシュモードがONになります.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public Response setCacheMode(boolean mode) {
		this.cacheMode = mode;
		return this;
	}

	/**
	 * クロスドメインを許可するかセット.
	 * @param mode trueの場合クロスドメインを許可します.
	 * @return Response レスポンスオブジェクトが返却されます.
	 */
	public Response setCrossDomain(boolean mode) {
		this.crossDomain = mode;
		return this;
	}

	// 最終決定の文字コードを取得.
	private final String lastCharset(String charset) {
		// 設定した文字コードが無効な場合はデフォルト的の文字コードを取得.
		if(charset == null || charset.isEmpty()) {
			return HttpConstants.getCharset();
		}
		// 設定した文字コードを返却.
		return charset;
	}

	// 対象のHTTPヘッダを生成します.
	private final NioSendData createHeader(long bodyLength, String charset) {
		return CreateResponseHeader.createHeader(
			state.getState(), getMessage(), mimeTypes, header,
			getContentType(), lastCharset(charset),
			!cacheMode, crossDomain, bodyLength);
	}

	// 送信データをセットして、送信処理を完了.
	private final void sendData(NioSendData... datas) {
		// 既に送信済みの場合はエラー.
		if(sendFlag.setToGetBefore(true)) {
			throw new HttpException("It has already been sent.");
		}
		try {
			// 送信データをセット.
			element.setSendData(datas);
			// 送信開始.
			element.startWrite();
		} catch(Exception e) {
			// 例外の場合は要素をクローズして終了.
			try {
				element.close();
			} catch(Exception ee) {}
			try {
				this.close();
			} catch(Exception ee) {}
			throw new HttpException(e);
		}
	}

	// 拡張子を取得.
	private static final String getExtension(String path) {
		// "aaa/bbb.ccc" の場合は "ccc"の位置を取得.
		int p = path.lastIndexOf(".");
		if(p == -1) {
			return null;
		}
		final String ret = path.substring(p + 1);
		// "aaa.bb/ccc" のような状況の場合.
		if(ret.indexOf("/") != -1) {
			return null;
		}
		return ret;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	@Override
	public Response send() {
		// Bodyなしのヘッダのみを送信.
		sendData(createHeader(0L, null));
		return this;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	@Override
	public Response send(byte[] value, String charset) {
		// ヘッダデータを作成.
		final NioSendBinaryListData data = (NioSendBinaryListData)createHeader(
			value.length, charset);
		// BinaryBodyデータをデータ送信.
		sendData(data.offer(value));
		return this;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	@Override
	public Response send(InputStream value, long length, String charset) {
		NioSendData sendBody = null;
		// データ長が不明な場合.
		if(length < 0L) {
			// チャング送信.
			length = -1L;
			value = new HttpSendChunked(
				HttpServerConstants.getSendChunkedBufferLength(), value);
			header.put("Transfer-Encoding", "chunked");
			sendBody = new HttpSendChunkedInputStreamData(value);
		} else {
			sendBody = new NioSendInputStreamData(value, length);
		}
		// データ送信.
		sendData(createHeader(length, charset), sendBody);
		return this;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	@Override
	public Response send(String value, String charset) {
		try {
			charset = lastCharset(charset);
			return send(value.getBytes(charset), charset);
		} catch(Exception e) {
			throw new HttpException(e);
		}
	}

	/**
	 * 送信処理.
	 * @param name 送信するファイル名を設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	@Override
	public Response sendFile(String name, String charset) {
		try {
			// ファイル長を取得.
			final long len = FileUtil.getFileLength(name);
			if(len == -1L) {
				throw new HttpException(HttpStatus.NotFound);
			// Content-Typeがヘッダに設定されてない場合.
			} else if(getContentType() == null) {
				// 拡張子からMimeTypeを取得してセット.
				String extension = getExtension(name);
				if(extension != null) {
					extension = extension.trim();
					// 拡張子からmimeTypeを取得してセット.
					String mimeType = mimeTypes.getMimeType(extension);
					if(mimeType != null) {
						setContentType(mimeType);
					}
				}
			}
			// データ送信.
			sendData(createHeader(len, charset), new NioSendFileData(name, len));
		} catch(HttpException he) {
			throw he;
		} catch(Exception e) {
			throw new HttpException(e);
		}
		return this;
	}

	/**
	 * 送信処理.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	@Override
	public Response sendJSON(Object value, String charset) {
		final String json = Json.encode(value);
		if(contentType == null) {
			setContentType("application/json");
		}
		return send(json, charset);
	}
}