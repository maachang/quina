package quina.http.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
	private Method method;

	/** コンポーネントURL. **/
	private String componentUrl;

	/** コンポーネントスラッシュカウント. **/
	private int componentSlashCount;

	/** 元のUrl. **/
	private String srcUrl;

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
	
	// Read-Writeロックオブジェクト.
	private final ReentrantReadWriteLock lock =
		new ReentrantReadWriteLock();

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
		this.srcUrl = url;
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
		this.srcUrl = url;
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
		lock.writeLock().lock();
		try {
			this.method = null;
			this.componentUrl = null;
			this.srcUrl = null;
			this.url = null;
			this.version = null;
			this.contentLength = 0L;
			this.header = null;
			this.element = null;
			this.params = null;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	@Override
	public boolean isConnection() {
		lock.readLock().lock();
		try {
			return element != null &&
				element.isConnection();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Method getMethod() {
		lock.readLock().lock();
		try {
			return method;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String getUrl() {
		lock.readLock().lock();
		try {
			return url;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String getSrcUrl() {
		lock.readLock().lock();
		try {
			return srcUrl;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String getComponentUrl() {
		lock.readLock().lock();
		try {
			return componentUrl;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int getComponentUrlSlashCount() {
		lock.readLock().lock();
		try {
			return componentSlashCount;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String getVersion() {
		lock.readLock().lock();
		try {
			return version;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public long getContentLength() {
		lock.readLock().lock();
		try {
			if(element.getReceiveBody() != null) {
				return element.getReceiveBody().getLength();
			}
			return contentLength;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Header getHeader() {
		lock.readLock().lock();
		try {
			return header;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public InputStream getInputStream() {
		lock.writeLock().lock();
		try {
			if(!readHttpBodyFlag) {
				try {
					InputStream in = element.getReceiveBody()
						.getInputStream();
					readHttpBodyFlag = true;
					return in;
				} catch(Exception e) {
					throw new HttpException(e);
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
		return null;
	}

	@Override
	public Params getParams() {
		lock.readLock().lock();
		try {
			return params;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * コンポーネントURLを設定.
	 * @param componentUrl コンポーネントURLを設定します.
	 * @param componentSlashCount コンポーネントURLのスラッシュの数を設定します
	 */
	public void setComponentUrl(String componentUrl, int componentSlashCount) {
		lock.writeLock().lock();
		try {
			this.componentUrl = componentUrl;
			this.componentSlashCount = componentSlashCount;
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Httpパラメータをセット.
	 * @param params
	 */
	public void setParams(Params params) {
		lock.writeLock().lock();
		try {
			this.params = params;
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Http要素を取得.
	 * @return
	 */
	public HttpElement getElement() {
		lock.readLock().lock();
		try {
			return element;
		} finally {
			lock.readLock().unlock();
		}
	}
}
