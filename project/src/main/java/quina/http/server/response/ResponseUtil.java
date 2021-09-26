package quina.http.server.response;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import quina.component.ExecuteComponent;
import quina.exception.CoreException;
import quina.exception.QuinaException;
import quina.http.HttpConstants;
import quina.http.HttpElement;
import quina.http.HttpException;
import quina.http.HttpSendChunkedData;
import quina.http.HttpStatus;
import quina.json.Json;
import quina.json.JsonBuilder;
import quina.net.nio.tcp.NioAsyncBuffer;
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
	 * 送信データをセットして、送信処理を完了.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param datas 送信データを設定します
	 */
	public static final void sendData(AbstractResponse<?> res, NioSendData... datas) {
		if(res.execSendDataFlag.setToGetBefore(true)) {
			// １度SendData呼び出しの場合は処理しない.
			return;
		}
		try {
			// 送信データをセット.
			res.element.setSendData(datas);
			// 送信開始.
			res.element.startWrite();
		} catch(Exception e) {
			// 例外の場合は要素とResponseをクローズして終了.
			try {
				res.element.close();
			} catch(Exception ee) {}
			try {
				res.close();
			} catch(Exception ee) {}
			
			// Core例外の場合.
			if(e instanceof CoreException) {
				throw (CoreException)e;
			}
			// 通常例外の理由で送信処理登録に失敗した場合.
			// コネクションが切れた例外は無視する.
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

	/** GZIPが許可されているかチェック + レスポンスヘッダに付与. **/
	private static final boolean isGzip(AbstractResponse<?> res) {
		// リクエストヘッダにGZIPが許可されていない場合は処理しない.
		String n = res.getRequest().getHeader().get("accept-encoding");
		if (n == null || n.indexOf("gzip") == -1) {
			return false;
		}
		// レスポンスでGZIP圧縮が許可されている場合.
		if(res.isGzip()) {
			// レスポンスヘッダにGZIP圧縮の条件をセット.
			res.getHeader().put("Content-Encoding", "gzip");
			return true;
		}
		return false;
	}

	/** バイナリをGZIP圧縮. **/
	private static final byte[] pressGzip(byte[] body) {
		ByteArrayOutputStream bo = null;
		try {
			bo = new ByteArrayOutputStream();
			final GZIPOutputStream go = new GZIPOutputStream(bo);
			go.write(body);
			go.flush();
			go.finish();
			go.close();
			byte[] ret = bo.toByteArray();
			bo.close();
			bo = null;
			return ret;
		} catch(Exception e) {
			throw new HttpException(e);
		} finally {
			if(bo != null) {
				try {
					bo.close();
				} catch(Exception e) {}
			}
		}
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
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param value 送信データを設定します.
	 */
	public static final void send(AbstractResponse<?> res, byte[] value) {
		send(res, value, null);
	}

	/**
	 * 送信処理.
	 * @param sendFlag 送信フラグを設定します.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param value 送信データを設定します.
	 * @param charset 文字コードを設定します.
	 */
	public static final void send(AbstractResponse<?> res, byte[] value, String charset) {
		charset = getCharset(res, charset);
		// gzipが許可されている場合.
		if(isGzip(res)) {
			value = pressGzip(value);
		}
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
			res.getHeader().put("Transfer-Encoding", "chunked");
			sendBody = new HttpSendChunkedData(
				HttpConstants.getSendChunkedBufferLength(), value);
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
			} else if(!res.isContentType()) {
				// 拡張子からmimeTypeを取得してセット.
				String mimeType = res.mimeTypes.getMimeType(name);
				if(mimeType != null) {
					res.setContentType(mimeType);
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
	 * JSON用送信処理.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param json 送信するJSONオブジェクトを設定します.
	 */
	public static final void sendJSON(AbstractResponse<?> res, Object value) {
		sendJSON(res, value, null);
	}

	/**
	 * JSON用送信処理.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 */
	public static final void sendJSON(AbstractResponse<?> res, Object value, String charset) {
		charset = getCharset(res, charset);
		final String json = Json.encode(value);
		if(res.contentType == null) {
			res.setContentType("application/json");
		}
		send(res, json, charset);
	}

	/**
	 * OutputStreamを使った送信処理を実施します.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 * @return OutputStream OutputStreamが返却されます.
	 */
	public static final OutputStream sendOutInStream(AbstractResponse<?> res, String charset) {
		try {
			// 非同期バッファを生成.
			NioAsyncBuffer buf = new NioAsyncBuffer();
			// gzipが許可されてる場合、ヘッダにセット.
			boolean gzip = isGzip(res);
			// TransferEncodingをchunkedで、InputStream送信.
			send(res, buf.getInputStream(), -1, charset);
			// gzipが許可されている場合はOutputStreamはGZIP圧縮版で処理.
			if(gzip) {
				return new GZIPOutputStream(buf.getOutputStream());
			}
			// gzipが許可されていない場合は通常のOutputStreamで処理.
			return buf.getOutputStream();
		} catch(HttpException he) {
			throw he;
		} catch(Exception e) {
			throw new HttpException(e);
		}
	}

	// OutputStreamを内包するJsonBuilder.
	private static final class OutputStreamJsonBuilder implements JsonBuilder {
		private BufferedWriter buf = null;
		public OutputStreamJsonBuilder(OutputStream out, String charset) {
			try {
				buf = new BufferedWriter(new OutputStreamWriter(out, charset));
			} catch(Exception e) {
				throw new HttpException(e);
			}
		}
		@Override
		public JsonBuilder append(String s) {
			try {
				buf.write(s);
			} catch(Exception e) {
				throw new HttpException(e);
			}
			return this;
		}
		@Override
		public String toString() {
			if(buf != null) {
				try {
					buf.flush();
					buf.close();
				} catch(Exception e) {}
				buf = null;
			}
			// 文字列は空返却.
			return "";
		}
	}

	/**
	 * 大きめのJSON送信処理.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param json 送信するJSONオブジェクトを設定します.
	 */
	public static final void sendLargeJSON(AbstractResponse<?> res, Object value) {
		sendLargeJSON(res, value, null);
	}

	/**
	 * 大きめのJSON送信処理.
	 * @param res 対象のレスポンスオブジェクトを設定します.
	 * @param json 送信するJSONオブジェクトを設定します.
	 * @param charset 変換対象の文字コードが設定されます.
	 */
	public static final void sendLargeJSON(AbstractResponse<?> res, Object value, String charset) {
		charset = getCharset(res, charset);
		// コンテンツタイプが設定されていない場合.
		if(res.contentType == null) {
			// json設定.
			res.setContentType("application/json");
		}
		// sendOutInStreamでOutputStreamを取得して、都度JSON変換しながら返却処理.
		Json.encode(
			new OutputStreamJsonBuilder(
				sendOutInStream(res, charset), charset), value);
	}
	
	/**
	 * 同期での処理結果を返信.
	 * @param res レスポンスオブジェクトを設定します.
	 * @param value 同期処理で返却されたオブジェクトを設定します.
	 */
	public static final void sendSync(AbstractResponse<?> res, Object value) {
		// 送信なしを示す場合.
		if(SyncResponse.NOSEND == value) {
			return;
		// 返却内容が空の場合.
		} else if(value == null) {
			// 空の返却.
			ResponseUtil.send((AbstractResponse<?>)res);
		// 返却条件がバイナリの場合.
		} else if(value instanceof byte[]) {
			// バイナリ送信.
			ResponseUtil.send((AbstractResponse<?>)res, (byte[])value);
		// 返却条件が文字列の場合.
		} else if(value instanceof String) {
			// 文字列送信.
			ResponseUtil.send((AbstractResponse<?>)res, (String)value);
		// 返却条件がファイルオブジェクトの場合.
		} else if(value instanceof File) {
			// ファイル送信.
			String name;
			try {
				name = ((File)value).getCanonicalPath();
			} catch(Exception ex) {
				throw new QuinaException(ex);
			}
			ResponseUtil.sendFile((AbstractResponse<?>)res, name);
		// 返却条件が上記以外の場合.
		} else {
			// JSON返却.
			ResponseUtil.sendJSON((AbstractResponse<?>)res, value);
		}
	}

	/**
	 * 別のコンポーネントに対してフォワードします
	 * @param path フォワード先のコンポーネントパスを設定します.
	 */
	public static final void forward(HttpElement em, String path) {
		ExecuteComponent.getInstance().execute(path, em);
	}

	/**
	 * リダイレクト処理.
	 * @param url リダイレクト先のURLを設定します.
	 */
	public static final void redirect(AbstractResponse<?> res, String url) {
		redirect(res, HttpStatus.MovedPermanently, url);
	}

	/**
	 * リダイレクト処理.
	 * @param status Httpステータスを設定します.
	 * @param url リダイレクト先のURLを設定します.
	 */
	public static final void redirect(AbstractResponse<?> res, int status, String url) {
		redirect(res, HttpStatus.getHttpStatus(status), url);
	}

	/**
	 * リダイレクト処理.
	 * @param status Httpステータスを設定します.
	 * @param url リダイレクト先のURLを設定します.
	 */
	public static final void redirect(AbstractResponse<?> res, HttpStatus status, String url) {
		// HTTPステータスを設定.
		res.setStatus(status);
		// リダイレクト先を設定.
		res.getHeader().put("Location", url);
		// 0バイトデータを設定.
		ResponseUtil.send((AbstractResponse<?>)res);
	}
}
