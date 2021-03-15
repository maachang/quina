package quina.http.server;

import java.io.IOException;
import java.io.InputStream;

import quina.http.Header;
import quina.http.HttpAnalysis;
import quina.http.HttpElement;
import quina.http.HttpException;
import quina.http.HttpIndexHeaders;
import quina.http.HttpReceiveHeader;
import quina.http.Method;
import quina.http.Params;
import quina.http.Request;

/**
 * Httpリクエスト.
 */
public class HttpServerRequest implements Request {
	/** Http Method. **/
	protected Method method;

	/** コンポーネントURL. **/
	protected String componentUrl;

	/** コンポーネントスラッシュカウント. **/
	protected int componentSlashCount;

	/** 元のUrl. **/
	protected String baseUrl;

	/** urlだけを抽出した情報. **/
	protected String url;

	/** Http Version. **/
	protected String version;

	/** Bodyサイズ. **/
	protected long contentLength;

	/** Http Header Body. **/
	protected HttpReceiveHeader header;

	/** HTTP要素. **/
	protected HttpElement element;

	/** Httpパラメータ. **/
	protected Params params;

	/** HttpBody取得フラグ. **/
	protected boolean readHttpBodyFlag = false;

	/**
	 * コンストラクタ.
	 * @param element NioElementを設定します.
	 * @param method HttpMethodを設定します.
	 * @param url URLを設定します.
	 * @param version HTTPバージョンを設定します.
	 * @param contentLength コンテンツ長を設定します.
	 * @param header Httpヘッダ情報を設定します.
	 */
	public HttpServerRequest(HttpElement element, Method method, String url,
		String version, long contentLength, HttpIndexHeaders header) {
		this.method = method;
		this.baseUrl = url;
		this.url = HttpAnalysis.getUrl(url);
		this.version = version;
		this.contentLength = contentLength;
		this.header = new HttpReceiveHeader(header);
		this.element = element;
		this.params = null;
	}

	/**
	 * コンストラクタ.
	 *
	 * HttpServerRequestにURLを設定した新しいRequestを設定します.
	 * @param src コピーするRequestを設定します.
	 * @param url 変更するURLを設定します.
	 */
	public HttpServerRequest(HttpServerRequest src, String url) {
		this.method = src.method;
		this.componentUrl = src.componentUrl;
		this.componentSlashCount = src.componentSlashCount;
		this.baseUrl = url;
		this.url = HttpAnalysis.getUrl(url);
		this.version = src.version;
		this.contentLength = src.contentLength;
		this.header = src.header;
		this.element = src.element;
		this.params = src.params;
		this.readHttpBodyFlag = src.readHttpBodyFlag;
	}

	@Override
	public void close() throws IOException {
		this.method = null;
		this.componentUrl = null;
		this.baseUrl = null;
		this.url = null;
		this.version = null;
		this.contentLength = 0L;
		this.header = null;
		this.element = null;
		this.params = null;
	}

	@Override
	public Method getMethod() {
		return method;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public String getBaseUrl() {
		return baseUrl;
	}

	@Override
	public String getComponentUrl() {
		return componentUrl;
	}

	@Override
	public int getComponentUrlSlashCount() {
		return componentSlashCount;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public long getContentLength() {
		if(element.getReceiveBody() != null) {
			return element.getReceiveBody().getLength();
		}
		return contentLength;
	}

	@Override
	public Header getHeader() {
		return header;
	}

	@Override
	public InputStream getInputStream() {
		if(!readHttpBodyFlag) {
			try {
				InputStream in = element.getReceiveBody().getInputStream();
				readHttpBodyFlag = true;
				return in;
			} catch(Exception e) {
				throw new HttpException(e);
			}
		}
		return null;
	}

	@Override
	public Params getParams() {
		return params;
	}

	/**
	 * コンポーネントURLを設定.
	 * @param componentUrl コンポーネントURLを設定します.
	 * @param componentSlashCount コンポーネントURLのスラッシュの数を設定します
	 */
	public void setComponentUrl(String componentUrl, int componentSlashCount) {
		this.componentUrl = componentUrl;
		this.componentSlashCount = componentSlashCount;
	}

	/**
	 * Httpパラメータをセット.
	 * @param params
	 */
	public void setParams(Params params) {
		this.params = params;
	}

	/**
	 * Http要素を取得.
	 * @return
	 */
	public HttpElement getElement() {
		return element;
	}
}