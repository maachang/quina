package quina.http.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import quina.http.Header;
import quina.http.HttpAnalysis;
import quina.http.HttpStatus;
import quina.json.Json;
import quina.net.nio.tcp.NioRecvBody;
import quina.util.Alphabet;

/**
 * HttpClient処理結果実装.
 */
class HttpResultSync implements HttpResult {
	protected Header header = null;
	protected HttpStatus status = null;
	protected String message = null;
	protected String contentType = null;
	protected NioRecvBody recvBody = null;

	/**
	 * コンストラクタ.
	 * @param status
	 * @param message
	 * @param header
	 */
	protected HttpResultSync(int status, String message, Header header) {
		this.status = HttpStatus.getHttpStatus(status);
		this.message = message;
		this.header = header;
	}

	/**
	 * クローズ処理.
	 */
	@Override
	public void close() throws IOException {
		NioRecvBody bf = recvBody;
		recvBody = null;
		if (bf != null) {
			try {
				bf.close();
			} catch(Exception e) {}
			bf = null;
		}
		header = null;
		status = null;
		message = null;
	}

	/**
	 * HTTPステータス取得.
	 *
	 * @return HttpStatus HTTPステータスが返却されます.
	 */
	@Override
	public HttpStatus getStatus() {
		return status;
	}

	/**
	 * メッセージを取得.
	 * @return String HTTPメッセージが返却サれます.
	 */
	@Override
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
	@Override
	public Header getHeader() {
		return header;
	}

	/**
	 * NioRecvBodyを設定.
	 * @param body
	 */
	protected void setNioRecvBody(NioRecvBody body) {
		this.recvBody = body;
	}

	/**
	 * コンテンツタイプを取得.
	 *
	 * @return String コンテンツタイプが返却されます.
	 */
	@Override
	public String getContentType() {
		if (contentType == null) {
			try {
				contentType = header.get("content-type");
			} catch (Exception e) {
				contentType = null;
			}
		}
		return contentType;
	}

	/**
	 * 受信条件がGZIPか取得.
	 *
	 * @return boolean [true]の場合GZIP圧縮されています.
	 */
	@Override
	public boolean isGzip() {
		final String value = header.get("content-encoding");
		if (Alphabet.indexOf(value, "gzip") != -1) {
			return true;
		}
		return false;
	}

	/**
	 * コンテンツ長を取得.
	 *
	 * @return long コンテンツ長が返却されます.
	 */
	@Override
	public long getContentLength() {
		Long len = header.getLong("content-length");
		if(len == null) {
			len = recvBody.getLength();
		}
		return len;
	}

	/**
	 * レスポンスボディInputStreamを取得.
	 *
	 * @return InputStream レスポンスボディInputStreamが返却されます.
	 */
	@Override
	public InputStream getInputStream() {
		if (recvBody != null) {
			InputStream ret = null;
			try {
				// gzip圧縮されている場合.
				if (isGzip()) {
					ret = new GZIPInputStream(recvBody.getInputStream());
				} else {
					ret = recvBody.getInputStream();
				}
				recvBody = null;
				return ret;
			} catch (Exception e) {
				throw new HttpClientException(500, e);
			}
		}
		return null;
	}

	/**
	 * レスポンスボディバイナリを取得.
	 *
	 * @return byte[] レスポンスボディバイナリが返却されます.
	 */
	@Override
	public byte[] getBody() {
		// inputStreamで受信している場合は、バイナリに展開.
		InputStream in = null;
		try {
			in = getInputStream();
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
	 * レスポンスボディを取得.
	 *
	 * @return String レスポンスボディが返却されます.
	 */
	@Override
	public String getText() {
		final byte[] b = getBody();
		if (b != null) {
			try {
				String charset = HttpAnalysis.contentTypeToCharset(getContentType());
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
	@Override
	public Object getJson() {
		return Json.decode(getText());
	}
}
