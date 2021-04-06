package quina.http.client.sync;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import quina.http.Header;
import quina.http.client.HttpClientException;
import quina.http.client.sync.HttpClient.NoULCode;
import quina.json.Json;
/**
 * HttpClientレスポンス結果.
 */
public class HttpResult{
	protected Header header = null;

	protected byte[] body = null;
	protected int status = -1;
	protected String message = null;
	protected String url = null;
	protected String contentType = null;
	protected HttpBodyFile bodyFile = null;
	protected Object json = null;

	/**
	 * コンストラクタ.
	 *
	 * @param url
	 * @param status
	 * @param header
	 */
	protected HttpResult(String url, int status, String message, Header header) {
		this.url = url;
		this.status = status;
		this.message = message;
		this.header = header;
	}

	/**
	 * クリア.
	 */
	public void clear() {
		HttpBodyFile bf = bodyFile;
		bodyFile = null;
		if (bodyFile != null) {
			bf.close();
			bf = null;
		}
		url = null;
		headers = null;
		headersString = null;
		body = null;
		status = -1;
		message = null;
	}

	/**
	 * urlを取得.
	 *
	 * @return String urlが返却されます.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * HTTPステータス取得.
	 *
	 * @return int HTTPステータスが返却されます.
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * メッセージを取得.
	 * @return String HTTPメッセージが返却サれます.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * HTTPヘッダを取得.
	 *
	 * @param key
	 *            キー名を設定します.
	 * @return String 要素が返却されます.
	 */
	protected String getHeader(String key) {
		try {
			convertString();
		} catch (Exception e) {
			throw new HttpClientException(500, e);
		}
		if (headersString == null) {
			return null;
		}

		final int p = NoULCode.indexOf(headersString, key + ":", 0);
		if (p == -1) {
			return null;
		}
		int end = headersString.indexOf("\r\n", p);
		if (end == -1) {
			return null;
		}
		return headersString.substring(p + key.length() + 1, end).trim();
	}

	/**
	 * HTTPヘッダのキー名一覧を取得.
	 *
	 * @return List<String> HTTPヘッダのキー名一覧が返却されます.
	 */
	public List<String> getHeaders() {
		try {
			convertString();
		} catch (Exception e) {
			throw new HttpClientException(500, e);
		}
		if (headersString == null) {
			return null;
		}
		int p;
		int b = 0;
		final List<String> ret = new ArrayList<String>();
		while ((p = NoULCode.indexOf(headersString, ":", b)) != -1) {
			ret.add(headersString.substring(b, p));
			b = p + 1;
			p = headersString.indexOf("\r\n", b);
			if (p == -1) {
				break;
			}
			b = p + 2;
		}
		return ret;
	}

	private final void convertString() throws IOException {
		if (headers != null) {
			headersString = new String(headers, "UTF8");
			headers = null;
		}
	}

	private static final String charset(String contentType) {
		int p = NoULCode.indexOf(contentType, ";charset=", 0);
		if (p == -1) {
			p = NoULCode.indexOf(contentType, " charset=", 0);
			if(p == -1) {
				return "UTF8";
			}
		}
		int b = p + 9;
		p = contentType.indexOf(";", b);
		if (p == -1) {
			p = contentType.length();
		}
		return contentType.substring(b, p).trim();
	}

	private final String getContentType() {
		if (contentType == null) {
			try {
				contentType = getHeader("content-type");
			} catch (Exception e) {
				contentType = null;
			}
		}
		return contentType;
	}

	/**
	 * jsonボディーをセット.
	 *
	 * @param json
	 */
	protected void setResponseBodyJson(Object json) {
		this.json = json;
	}

	/**
	 * binaryレスポンスボディをセット.
	 *
	 * @param body
	 */
	protected void setResponseBody(byte[] body) {
		this.body = body;
	}

	/**
	 * ファイルレスポンスボディをセット.
	 *
	 * @param body
	 */
	protected void setReponseBodyFile(HttpBodyFile body) {
		this.bodyFile = body;
	}

	/**
	 * 受信条件を取得.
	 *
	 * @return String 受信条件が返却されます. "binary" の場合は byte[] で受信されています. "inputStream"
	 *         の場合は InputStream で受信されています. "" の場合は、受信Bodyは存在しません.
	 */
	public String responseType() {
		if (body != null) {
			return "binary";
		} else if (bodyFile != null) {
			return "inputStream";
		}
		return "";
	}

	/**
	 * 受信条件がGZIPか取得.
	 *
	 * @return boolean [true]の場合GZIP圧縮されています.
	 */
	public boolean isGzip() {
		if (bodyFile != null && isResponseGzip()) {
			return true;
		}
		return false;
	}

	/**
	 * レスポンスボディサイズを取得.
	 *
	 * @return long レスポンスボディサイズが返却されます.
	 */
	public long responseBodySize() {
		if (body != null) {
			return (long) body.length;
		} else if (bodyFile != null) {
			return bodyFile.getFileLength();
		}
		return -1L;
	}

	/**
	 * レスポンスボディバイナリを取得.
	 *
	 * @return byte[] レスポンスボディバイナリが返却されます.
	 */
	public byte[] responseBody() {
		if (body != null) {
			// バイナリで受信している場合は、そのまま返却.
			return body;
		}
		// inputStreamで受信している場合は、バイナリに展開.
		InputStream in = null;
		try {
			in = responseInputStream();
			if (in != null) {
				int len;
				byte[] buf = new byte[1024];
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				while ((len = in.read(buf)) != -1) {
					out.write(buf, 0, len);
				}
				out.flush();
				in.close();
				in = null;
				return out.toByteArray();
			}
		} catch (Exception e) {
			throw new HttpClientException(500, e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	/**
	 * レスポンスボディInputStreamを取得.
	 *
	 * @return InputStream レスポンスボディInputStreamが返却されます.
	 */
	public InputStream responseInputStream() {
		if (bodyFile != null) {
			// gzip圧縮されている場合.
			if (isResponseGzip()) {
				try {
					return new GZIPInputStream(bodyFile.getInputStream());
				} catch (Exception e) {
					throw new HttpClientException(500, e);
				}
			}
			return bodyFile.getInputStream();
		} else if (body != null) {
			return new ByteArrayInputStream(body);
		}
		return null;
	}

	// 受信データがGZIP圧縮されているかチェック.
	protected final boolean isResponseGzip() {
		final String value = getHeader("content-encoding");
		if (NoULCode.eqs(value, "gzip") != -1) {
			return true;
		}
		return false;
	}

	/**
	 * レスポンスボディを取得.
	 *
	 * @return String レスポンスボディが返却されます.
	 */
	public String responseText() {
		final byte[] b = responseBody();
		if (b != null) {
			try {
				String charset = charset(getContentType());
				return new String(b, charset);
			} catch (Exception e) {
				throw new HttpClientException(500, e);
			}
		}
		return null;
	}

	/**
	 * JSONオブジェクトを取得.
	 * @return
	 */
	public Object responseJson() {
		if(json != null) {
			return json;
		}
		json = Json.decode(responseText());
		return json;
	}

	/**
	 * 文字列返却.
	 *
	 * @return String 文字列が返却されます.
	 */
	public String toString() {
		return Json.encode(this);
	}
}
