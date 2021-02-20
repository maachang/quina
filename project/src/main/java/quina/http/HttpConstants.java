package quina.http;

import quina.util.AtomicNumber64;
import quina.util.AtomicObject;
import quina.util.Flag;

/**
 * HttpConstants.
 */
public class HttpConstants {
	/** Httpの１つの終端を示す記号. **/
	public static final byte[] END_LINE = "\r\n".getBytes();

	/** Httpの１つの終端を示す記号の長さ. **/
	public static final int END_LINE_LENGTH = END_LINE.length;

	/** JSONのMimeType. **/
	public static final String MIME_TYPE_JSON = "application/json";

	// ヘッダ終端.
	public static final byte[] END_HEADER = "\r\n\r\n".getBytes();
	public static final int END_HEADER_LENGTH = END_HEADER.length;

	// デフォルトの文字コード.
	private static final String DEF_CHARSET = "UTF8";

	// 文字コード.
	private static final AtomicObject<String> charset =
		new AtomicObject<String>(DEF_CHARSET);

	// デフォルトのメモリに受信可能なデータ長.
	private static final long DEF_MAX_RECV_MEMORY_BODY_LENGTH = 0x00100000L;

	// メモリに受信可能なデータ長.
	private static final AtomicNumber64 maxRecvMemoryBodyLength =
		new AtomicNumber64(DEF_MAX_RECV_MEMORY_BODY_LENGTH);

	// HttpRequestのBodyに対して、TransferEncodingのchunkedを受け付けるかの定義.
	private static final boolean DEF_REQUEST_BODY_CHUNKED = false;

	// HttpRequestのBodyに対して、TransferEncodingのchunkedを受け付けるか.
	private static final Flag requestBodyChunked = new Flag(DEF_REQUEST_BODY_CHUNKED);

	/**
	 * 文字コードを設定.
	 * @param charset
	 */
	public static final void setCharset(String cset) {
		if(cset == null || cset.isEmpty()) {
			cset = "UTF8";
		}
		charset.set(cset);
	}

	/**
	 * 文字コードを取得.
	 * @return
	 */
	public static final String getCharset() {
		return charset.get();
	}

	/**
	 * メモリに受信可能なデータサイズの既定値を設定.
	 * @param max
	 */
	public static final void setMaxRecvMemoryBodyLength(long max) {
		maxRecvMemoryBodyLength.set(max);
	}

	/**
	 * メモリに受信可能なデータサイズの規定値を取得.
	 * @return
	 */
	public static final long getMaxRecvMemoryBodyLength() {
		return maxRecvMemoryBodyLength.get();
	}

	/**
	 * HttpRequestのBodyに対して、TransferEncodingのchunkedを受け付けるか設定.
	 * @param f
	 */
	public static final void setRequestBodyChunked(boolean f) {
		requestBodyChunked.set(f);
	}

	/**
	 * HttpRequestのBodyに対して、TransferEncodingのchunkedを受け付けるか判別.
	 * @return
	 */
	public static final boolean isRequestBodyChunked() {
		return requestBodyChunked.get();
	}
}
