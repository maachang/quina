package quina.http.response;

import java.io.InputStream;

import quina.http.HttpConstants;
import quina.http.HttpException;
import quina.http.HttpSendChunked;
import quina.http.HttpSendChunkedInputStreamData;
import quina.http.HttpStatus;
import quina.http.server.HttpServerConstants;
import quina.json.Json;
import quina.net.nio.tcp.NioSendBinaryListData;
import quina.net.nio.tcp.NioSendData;
import quina.net.nio.tcp.NioSendFileData;
import quina.net.nio.tcp.NioSendInputStreamData;
import quina.util.FileUtil;

/**
 * Http送信処理.
 */
public final class ResponseUtil {
	private ResponseUtil() {}

	/**
	 * 最終決定の文字コードを取得.
	 * @param charset
	 * @return
	 */
	public static final String lastCharset(String charset) {
		// 設定した文字コードが無効な場合はデフォルト的の文字コードを取得.
		if(charset == null || charset.isEmpty()) {
			return HttpConstants.getCharset();
		}
		// 設定した文字コードを返却.
		return charset;
	}

	/**
	 * 拡張子を取得.
	 * @param path
	 * @return
	 */
	public static final String getExtension(String path) {
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
	 * 送信データをセットして、送信処理を完了.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param datas 送信データを設定します
	 */
	public static final void sendData(AbstractResponse<?> res, NioSendData... datas) {
		// 既に送信済みの場合はエラー.
		if(res.sendFlag.setToGetBefore(true)) {
			throw new HttpException("It has already been sent.");
		}
		try {
			// 送信データをセット.
			res.element.setSendData(datas);
			// 送信開始.
			res.element.startWrite();
		} catch(Exception e) {
			// 例外の場合は要素をクローズして終了.
			try {
				res.element.close();
			} catch(Exception ee) {}
			try {
				res.close();
			} catch(Exception ee) {}
			throw new HttpException(e);
		}
	}

	// レスポンスの文字コードを取得.
	private static final String getCharset(AbstractResponse<?> res, String charset) {
		if(charset == null) {
			String ret = res.charset;
			if(ret == null || ret.isEmpty()) {
				return HttpConstants.getCharset();
			}
			return ret;
		}
		return charset;
	}

	/**
	 * 送信処理.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 */
	public static final void send(AbstractResponse<?> res) {
		// Bodyなしのヘッダのみを送信.
		sendData(res, res.createHeader(0L, null));
	}

	/**
	 * 送信処理.
	 * @param sendFlag 送信フラグを設定します.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param element NioElementを設定します.
	 * @param value 送信データを設定します.
	 */
	public static final void send(AbstractResponse<?> res, byte[] value) {
		send(res, value, null);
	}

	/**
	 * 送信処理.
	 * @param sendFlag 送信フラグを設定します.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param element NioElementを設定します.
	 * @param value 送信データを設定します.
	 * @param charset 文字コードを設定します.
	 */
	public static final void send(AbstractResponse<?> res, byte[] value, String charset) {
		charset = getCharset(res, charset);
		// ヘッダデータを作成.
		final NioSendBinaryListData data = (NioSendBinaryListData)res.createHeader(
			value.length, charset);
		// BinaryBodyデータをデータ送信.
		sendData(res, data.offer(value));
	}

	/**
	 * 送信処理.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 */
	public static final void send(AbstractResponse<?> res, InputStream value, long length) {
		send(res, value, length, null);
	}

	/**
	 * 送信処理.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param value 送信データを設定します.
	 * @param length 対象の送信データ長を設定します.
	 * @param charset 文字コードを設定します.
	 */
	public static final void send(AbstractResponse<?> res, InputStream value, long length,
		String charset) {
		charset = getCharset(res, charset);
		NioSendData sendBody = null;
		// データ長が不明な場合.
		if(length < 0L) {
			// チャング送信.
			length = -1L;
			value = new HttpSendChunked(
				HttpServerConstants.getSendChunkedBufferLength(), value);
			res.getHeader().put("Transfer-Encoding", "chunked");
			sendBody = new HttpSendChunkedInputStreamData(value);
		} else {
			sendBody = new NioSendInputStreamData(value, length);
		}
		// データ送信.
		sendData(res, res.createHeader(length, charset), sendBody);
	}

	/**
	 * 送信処理.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param value 送信データを設定します.
	 */
	public static final void send(AbstractResponse<?> res, String value) {
		send(res, value, null);
	}

	/**
	 * 送信処理.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param value 送信データを設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 */
	public static final void send(AbstractResponse<?> res, String value, String charset) {
		charset = getCharset(res, charset);
		try {
			charset = lastCharset(charset);
			send(res, value.getBytes(charset), charset);
		} catch(Exception e) {
			throw new HttpException(e);
		}
	}

	/**
	 * 送信処理.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param name 送信するファイル名を設定します.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	public static final void sendFile(AbstractResponse<?> res, String name) {
		sendFile(res, name, null);
	}

	/**
	 * 送信処理.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param name 送信するファイル名を設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 * @return Response Responseオブジェクトが返却されます.
	 */
	public static final void sendFile(AbstractResponse<?> res, String name, String charset) {
		charset = getCharset(res, charset);
		try {
			// ファイル長を取得.
			final long len = FileUtil.getFileLength(name);
			if(len == -1L) {
				throw new HttpException(HttpStatus.NotFound);
			// Content-Typeがヘッダに設定されてない場合.
			} else if(res.getContentType() == null) {
				// 拡張子からMimeTypeを取得してセット.
				String extension = getExtension(name);
				if(extension != null) {
					extension = extension.trim();
					// 拡張子からmimeTypeを取得してセット.
					String mimeType = res.mimeTypes.getMimeType(extension);
					if(mimeType != null) {
						res.setContentType(mimeType);
					}
				}
			}
			// データ送信.
			sendData(res, res.createHeader(len, charset), new NioSendFileData(name, len));
		} catch(HttpException he) {
			throw he;
		} catch(Exception e) {
			throw new HttpException(e);
		}
	}

	/**
	 * 送信処理.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param json 送信するJSONオブジェクトを設定します.
	 */
	public static final void sendJSON(AbstractResponse<?> res, Object value) {
		sendJSON(res, value, null);
	}

	/**
	 * 送信処理.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param json 送信するJSONオブジェクトを設定します.
	 */
	public static final void sendJSON(AbstractResponse<?> res, Object value, String charset) {
		charset = getCharset(res, charset);
		final String json = Json.encode(value);
		if(res.contentType == null) {
			res.setContentType("application/json");
		}
		send(res, json, charset);
	}
}
