package quina.http.server;

import java.io.IOException;
import java.io.InputStream;

import quina.http.Header;
import quina.http.HttpConstants;
import quina.http.HttpElement;
import quina.http.HttpSendHeader;
import quina.http.HttpStatus;
import quina.http.Response;

/**
 * Httpサーバ用Httpレスポンス.
 */
public class HttpServerResponse implements Response {
	/** HTTP要素. **/
	private HttpElement element = null;
	/** 送信ヘッダ. **/
	private HttpSendHeader header = null;
	/** ステータス. **/
	private HttpStatus state = HttpStatus.OK;
	/** コンテンツタイプ. **/
	private String contentType;
	/** 文字コード. **/
	private String charset = HttpConstants.getCharset();

	/**
	 * コンストラクタ.
	 * @param element Http要素を設定します.
	 */
	protected HttpServerResponse(HttpElement element) {
		this.element = element;
	}

	/**
	 * クローズ処理.
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException {
		element = null;
		header = null;
		state = null;
		contentType = null;
		charset = null;
	}

	/**
	 * Httpステータスを取得.
	 * @return HttpStatus Httpステータスが返却されます.
	 */
	@Override
	public HttpStatus getStatus() {
		return state;
	}

	/**
	 * コンテンツタイプを取得.
	 * @return String コンテンツタイプが返却されます.
	 */
	@Override
	public String getContentType() {
		return contentType;
	}

	/**
	 * コンテンツの文字コードを取得.
	 * @return Strig 文字コードが返却されます.
	 */
	@Override
	public String getCharset() {
		return charset;
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
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	@Override
	public Response send(byte[] value) {
		return this;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	@Override
	public Response send(InputStream value) {
		return this;
	}

	/**
	 * 送信処理.
	 * @param value 送信データを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	@Override
	public Response send(String value) {
		return this;
	}

	/**
	 * 送信処理.
	 * @param name 送信するファイル名を設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	@Override
	public Response sendFile(String name) {
		return this;
	}

	/**
	 * 送信処理.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	@Override
	public Response sendJSON(Object value) {
		return this;
	}
}
