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
 * Httpサーバ用Httpリクエスト.
 */
public class HttpServerRequest implements Request {
	/** Http Method. **/
	private Method method;

	/** コンポーネントURL. **/
	private String componentUrl;

	/** 元のUrl. **/
	private String baseUrl;

	/** urlだけを抽出した情報. **/
	private String url;

	/** Http Version. **/
	private String version;

	/** Bodyサイズ. **/
	private long contentLength;

	/** Http Header Body. **/
	private HttpReceiveHeader header;

	/** HTTP要素. **/
	private HttpElement element;

	/** Httpパラメータ. **/
	private Params params;

	/** HttpBody取得フラグ. **/
	private boolean readHttpBodyFlag = false;

	/**
	 * コンストラクタ.
	 * @param element NioElementを設定します.
	 * @param method HttpMethodを設定します.
	 * @param url URLを設定します.
	 * @param version HTTPバージョンを設定します.
	 * @param contentLength コンテンツ長を設定します.
	 * @param header Httpヘッダ情報を設定します.
	 */
	protected HttpServerRequest(HttpElement element, Method method, String url,
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
	 */
	public void setComponentUrl(String componentUrl) {
		this.componentUrl = componentUrl;
	}

	// Httpパラメータをセット.
	protected void setParams(Params params) {
		this.params = params;
	}

	// Http要素を取得.
	protected HttpElement getElement() {
		return element;
	}
}
