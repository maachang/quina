package quina.http.server;

import quina.http.HttpConstants;
import quina.http.HttpElement;
import quina.http.HttpElementState;
import quina.http.HttpException;
import quina.http.HttpIndexHeaders;
import quina.http.HttpStatus;
import quina.http.Method;
import quina.net.nio.tcp.NioBuffer;
import quina.util.Alphabet;
import quina.util.StringUtil;

/**
 * Httpサーバ用の解析処理.
 */
public class HttpServerAnalysis {
	/**
	 * HttpRequest情報を取得.
	 * @param element NioElementを設定します.
	 * @param recvBin 受信されたバイナリ情報を設定します.
	 * @return boolean trueの場合、リクエストの作成が完了しました.
	 */
	public static final boolean getRequest(
		HttpElement element, byte[] recvBin) {
		return getRequest(element, null, recvBin);
	}

	/**
	 * HttpRequest情報を取得.
	 * @param element NioElementを設定します.
	 * @param charset 変換文字コードを設定します.
	 * @param recvBin 受信されたバイナリ情報を設定します.
	 * @return boolean trueの場合、リクエストの作成が完了しました.
	 */
	public static final boolean getRequest(
		HttpElement element, String charset,
		byte[] recvBin) {
		// リクエストヘッダ作成中のステータスでない場合.
		//if(element.getState() !=
		//	HttpElementState.STATE_RECEIVING_HEADER) {
		//	return true;
		//}
		// バッファからHTTPヘッダ終端情報が存在するかチェック.
		final NioBuffer buffer = element.getBuffer();
		try {
			if(charset == null || charset.isEmpty()) {
				charset = HttpConstants.getCharset();
			}
		} catch(Exception e) {}
		buffer.write(recvBin);
		// Httpヘッダの終端を検索.
		final int endPoint = buffer.indexOf(HttpConstants.END_HEADER,
			element.getReceiveHeaderPosition());
		// 終端が見つからない場合.
		if(endPoint == -1) {
			// HTTPヘッダの終端が見つからない場合は、
			// 現在のバッファ長 - 終端の長さ分を引いた
			// ポジションを次の検索開始位置にセットする.
			element.setReceiveHeaderPosition(
				buffer.size() <= HttpConstants.END_HEADER_LENGTH ?
					endPoint : buffer.size() - HttpConstants.END_HEADER_LENGTH);
			return false;
		// 文字コードが指定されていない場合.
		} else if(charset == null || charset.isEmpty()) {
			charset = HttpConstants.getCharset();
		}
		// HTTPヘッダを解析.
		try {
			// method url version を取得.
			int firstPoint = buffer.indexOf(HttpConstants.END_LINE);
			if(firstPoint == -1) {
				throw new HttpException(
					"Received data is not an HTTP request");
			}
			byte[] b = new byte[firstPoint];
			buffer.read(b);
			buffer.skip(HttpConstants.END_HEADER_LENGTH);
			String v = new String(b, charset).trim();
			b = null;
			String[] list = v.split(" ");
			if (list.length != 3) {
				throw new HttpException(
					"Received data is not an HTTP request: \"" +
					v + "\"");
			}
			v = null;
			Long contentLength;
			Method method = Method.get(list[0].trim().toUpperCase());
			String url = getDecodeUrl(list[1].trim(), charset);
			String version = list[2].trim().toUpperCase();
			list = null;

			// headerを取得.
			//
			// HTTPヘッダの範囲は以下の範囲
			// GET / HTTP/1.1¥r¥n
			// ------ ここから ------
			// Accept: image/gif, image/jpeg, */*¥r¥n
			// Accept-Language: ja¥r¥n
			// Accept-Encoding: gzip, deflate¥r¥n
			// User-Agent: Mozilla/4.0 (Compatible; MSIE 6.0; Windows NT 5.1;)¥r¥n
			// Host: www.xxx.zzz¥r¥n
			// Connection: Keep-Alive¥r¥n
			// ....¥r¥n
			// ------ ここまで ------
			// ¥r¥n
			int len = (endPoint + HttpConstants.END_LINE_LENGTH)
				- (firstPoint + HttpConstants.END_LINE_LENGTH);
			b = new byte[len];
			buffer.read(b);

			// HTTPヘッダ情報を生成.
			HttpIndexHeaders indexHeader = new HttpIndexHeaders(b);
			b = null;

			// コンテンツ長を取得.
			contentLength = indexHeader.getLong("content-length");
			if(contentLength == null) {
				// nullの場合はコンテンツ長が設定されていない.
				contentLength = -1L;
			}

			// HttpBodyが存在するMethodの場合.
			if(method.isBody()) {
				// コンテンツ長が設定されていない.
				if(contentLength == -1L) {
					// コンテンツ長が存在しない場合.
					// chunked受信かチェック.
					final String transferEncoding = indexHeader.get(
						"transfer-encoding");
					// chunked受信ではない場合はHTTP400エラー返却.
					if(!Alphabet.eq(transferEncoding, "chunked")) {
						// リクエストの定義がおかしい.
						throw new HttpException(HttpStatus.Conflict);
					// chunked受信が許可されていない場合.
					} else if(!HttpConstants.isRequestBodyChunked()) {
						// ContentLengthが必須.
						throw new HttpException(HttpStatus.LengthRequired);
					}
				}
				// HttpHeader受信完了.
				element.setState(HttpElementState.STATE_END_RECV_HTTP_HEADER);
			// HttpBodyが存在しないMethodの場合.
			} else {
				if(contentLength > 0L) {
					// リクエストの定義がおかしい.
					throw new HttpException(HttpStatus.Conflict);
				}
				final String transferEncoding = indexHeader.get(
					"transfer-encoding");
				if(Alphabet.eq(transferEncoding, "chunked")) {
					// リクエストの定義がおかしい.
					throw new HttpException(HttpStatus.Conflict);
				}
				// コンテンツ長が無いので0でセット.
				contentLength = 0L;
				// 受信処理終了.
				element.setState(HttpElementState.STATE_END_RECV);
			}
			// HttpRequestを生成.
			HttpServerRequest r = new HttpServerRequest(
				element, method, url, version, contentLength,
				indexHeader);
			// Http要素にリクエストをセット.
			element.setRequest(r);
			element.resetReceiveHeaderPosition();
			return true;
		} catch(HttpException qe) {
			throw qe;
		} catch(Exception e) {
			throw new HttpException(e);
		}
	}
	
	// エンコードされてるURLを変換.
	private static final String getDecodeUrl(
		String url, String charset) {
		int p = url.indexOf("?");
		if(p == -1) {
			return StringUtil.urlDecode(url, charset);
		}
		return StringUtil.urlDecode(url.substring(0, p), charset) +
			url.substring(p);
	}
}
