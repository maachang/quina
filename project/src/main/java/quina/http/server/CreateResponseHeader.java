package quina.http.server;

import java.util.Iterator;
import java.util.Map.Entry;

import quina.QuinaConstants;
import quina.http.Header;
import quina.http.HttpConstants;
import quina.http.HttpException;
import quina.http.HttpStatus;
import quina.http.MimeTypes;
import quina.net.nio.tcp.NioSendBinaryListData;
import quina.net.nio.tcp.NioSendData;
import quina.util.collection.IndexKeyValueList;
import quina.util.collection.TreeKey;

/**
 * HttpServerResponseヘッダ作成処理.
 */
public class CreateResponseHeader {
	private CreateResponseHeader() {}

	/**
	 * 予約ヘッダ情報.
	 * value = Object[2] = {mode, valueInfo}.
	 * mode = 0 : 標準.
	 * mode = 1 : キャッシュなし.
	 * mode = 2 : ブラウザ用のクロスドメイン対応ヘッダ.
	 *
	 * valueInfo = 実際のHttpHeaderの要素.
	 */
	private static final IndexKeyValueList<Object, Object[]> RESERVATION_HEADERS;

	/** Optionsレスポンス[基本]. **/
	private static final byte[] RESPONSE_OPSIONS_RESPONSE;

	/** ステータス指定レスポンス200ステータスデフォルト[開始情報]. **/
	private static final byte[] RESPONSE_STATE_RESPONSE_DEFAULT_FIRST;
	/** ステータス指定レスポンス[基本]. **/
	private static final byte[] RESPONSE_STATE_RESPONSE_RESERVATION;

	/** レスポンス[キャッシュなし対応]. **/
	private static final byte[] RESPONSE_NO_CACHE_RESPONSE;
	/** レスポンス[クロスドメイン対応]. **/
	private static final byte[] RESPONSE_CROSS_DOMAIN_RESPONSE;

	// static - init.
	static {
		byte[] optionsHeader;
		byte[] responseHeaderDefaultFirst;
		byte[] responseHeaderReservation;
		byte[] noCacheHeader;
		byte[] crossDomainHeader;
		final String charset = HttpConstants.getCharset();
		// サーバー名.
		final String serverName = QuinaConstants.SERVER_NAME +
			"(" + QuinaConstants.SERVER_VERSION + ")";
		// 予約ヘッダを定義.
		IndexKeyValueList<Object, Object[]> reservationHeaders = new IndexKeyValueList<Object, Object[]>();
		reservationHeaders.put(new TreeKey("X-Accel-Buffering"),
				new Object[] {0, "no"});
		reservationHeaders.put(new TreeKey("Server"),
			new Object[] {0, serverName});
		reservationHeaders.put(new TreeKey("Connection"),
			new Object[] {0, "close"});
		reservationHeaders.put(new TreeKey("Cache-Control"),
				new Object[] {1, "no-cache"});
		reservationHeaders.put(new TreeKey("Pragma"),
			new Object[] {1, "no-cache"});
		reservationHeaders.put(new TreeKey("Expire"),
			new Object[] {1, "-1"});
		reservationHeaders.put(new TreeKey("Access-Control-Allow-Origin"),
			new Object[] {2, "*"});
		reservationHeaders.put(new TreeKey("Access-Control-Allow-Headers"),
			new Object[] {2, "content-type,x-accel-buffering,*"});
		reservationHeaders.put(new TreeKey("Access-Control-Allow-Methods"),
			new Object[] {2, "GET,POST,DELETE,PUSH,PATCH"});
		String optionsFirst = "HTTP/1.1 200 OK\r\n" +
			"Allow:GET,POST,DELETE,PUSH,PATCH,OPTIONS\r\n";
		// 固定ヘッダ情報を生成.
		try {
			int i, len;
			Object[] v;

			// OptionHeaderを生成.
			StringBuilder buf = new StringBuilder();
			StringBuilder noCacheBuf = new StringBuilder();
			StringBuilder crossDomainBuf = new StringBuilder();
			len = reservationHeaders.size();
			for(i = 0; i < len; i ++) {
				v = reservationHeaders.valueAt(i);
				switch((Integer)v[0]) {
				case 0: // 標準.
					buf.append(reservationHeaders.keyAt(i)).append(":")
						.append(v[1]).append("\r\n");
					break;
				case 1: // ノーキャッシュ用.
					noCacheBuf.append(reservationHeaders.keyAt(i)).append(":")
						.append(v[1]).append("\r\n");
					break;
				case 2: // クロスドメイン用.
					crossDomainBuf.append(reservationHeaders.keyAt(i)).append(":")
					.append(v[1]).append("\r\n");
					break;
				}
			}
			// レスポンス標準ヘッダを出力.
			responseHeaderReservation = buf.toString().getBytes(charset);
			// オプション用ヘッダを生成.
			buf.append("Content-Length:0\r\n\r\n");
			optionsHeader = (optionsFirst + buf.toString()).getBytes(charset);
			buf = null;

			// ノーキャッシュとクロスドメインは共通.
			noCacheHeader = noCacheBuf.toString().getBytes(charset);
			noCacheBuf = null;
			crossDomainHeader = crossDomainBuf.toString().getBytes(charset);
			crossDomainBuf = null;

			// 通常200ステータスヘッダ開始条件.
			responseHeaderDefaultFirst = ("HTTP/1.1 200 OK\r\n").getBytes(charset);

			// 最後にContent-LengthとContent-Typeを予約条件としてセット.
			reservationHeaders.put(new TreeKey("Content-Length"),
				new Object[] {0, "0"});
			reservationHeaders.put(new TreeKey("Content-Type"),
				new Object[] {0, "X"});
		} catch (Exception e) {
			reservationHeaders = null;
			crossDomainHeader = null;
			responseHeaderDefaultFirst = null;
			responseHeaderReservation = null;
			optionsHeader = null;
			noCacheHeader = null;
		}
		// 情報をセット.
		RESERVATION_HEADERS = reservationHeaders;
		RESPONSE_OPSIONS_RESPONSE = optionsHeader;
		RESPONSE_STATE_RESPONSE_DEFAULT_FIRST = responseHeaderDefaultFirst;
		RESPONSE_STATE_RESPONSE_RESERVATION = responseHeaderReservation;
		RESPONSE_NO_CACHE_RESPONSE = noCacheHeader;
		RESPONSE_CROSS_DOMAIN_RESPONSE = crossDomainHeader;
	}

	/**
	 * 送信可能なOptionヘッダを生成.
	 * @param noCache trueの場合はnocahcモードでヘッダを付与します.
	 * @param crossDomain trueの場合はcrossDomain対応のヘッダを付与します.
	 * @return NioSendData NioSendDataが返却されます.
	 */
	public static final NioSendData createOptionsHeader(boolean noCache, boolean crossDomain) {
		// 開始ヘッダをセット.
		NioSendBinaryListData ret = new NioSendBinaryListData(RESPONSE_OPSIONS_RESPONSE);
		// noCacheヘッダが有効な場合.
		if(noCache) {
			ret.offer(RESPONSE_NO_CACHE_RESPONSE);
		}
		// crossDomainヘッダが有効な場合.
		if(crossDomain) {
			ret.offer(RESPONSE_CROSS_DOMAIN_RESPONSE);
		}
		return ret;
	}

	/**
	 * 送信可能なレスポンス用Httpヘッダを生成.
	 * @param state Httpステータスを設定します.
	 * @param msg Httpステータスメッセージを設定します.
	 * @param mimeTypes mimeTypesを設定します.
	 * @param header Httpヘッダを設定します.
	 * @param mime 設定されたMimeTypeを設定します.
	 * @param charset 文字コードを設定します.
	 * @param noCache trueの場合はnocahcモードでヘッダを付与します.
	 * @param crossDomain trueの場合はcrossDomain対応のヘッダを付与します.
	 * @param bodyLength コンテンツ長を設定します.
	 *                   -1を設定した場合はContent-Lengthは追加しません.
	 * @return NioSendData NioSendDataが返却されます.
	 */
	public static final NioSendData createHeader(
		int state, String msg, MimeTypes mimeTypes, Header header, String mime,
		String charset, boolean noCache, boolean crossDomain, long bodyLength) {
		// 文字コードはデフォルトの内容を取得.
		charset = (charset == null || charset.isEmpty()) ? HttpConstants.getCharset() : charset;
		try {
			// 開始ヘッダをセット.
			NioSendBinaryListData ret = new NioSendBinaryListData();
			if(msg == null && state == 200) {
				// ステータス200 OK 返却の場合.
				ret.offer(RESPONSE_STATE_RESPONSE_DEFAULT_FIRST);
			} else {
				// それ以外のステータスの場合.
				if(msg == null) {
					// ステータスのデフォルトのメッセージを設定します.
					msg = HttpStatus.getHttpStatus(state).getMessage();
				}
				ret.offer(new StringBuilder("HTTP/1.1 ")
					.append(state).append(" ").append(msg).append("\r\n")
					.toString().getBytes(charset));
			}
			int flagMode = 0;
			// 予約ヘッダをセット.
			ret.offer(RESPONSE_STATE_RESPONSE_RESERVATION);
			// noCacheヘッダが有効な場合.
			if(noCache) {
				ret.offer(RESPONSE_NO_CACHE_RESPONSE);
			} else {
				// noCacheヘッダが無効な場合.
				flagMode |= 1;
			}
			// crossDomainヘッダが有効な場合.
			if(crossDomain) {
				ret.offer(RESPONSE_CROSS_DOMAIN_RESPONSE);
			} else {
				// crossDomainHeaderが無効な場合.
				flagMode |= 2;
			}
			// オリジナルヘッダをセット.
			StringBuilder buf = new StringBuilder();
			if(bodyLength >= 0) {
				// コンテンツ長が設定されている場合はコンテンツ長をセット.
				buf.append("Content-Length:").append(bodyLength).append("\r\n");
			}
			// mimeTypeが存在する場合.
			if(mime != null) {
				// 対象のMimeTypeに対して、文字コードが必要かチェック.
				if(mimeTypes.isAppendCharset(mime)) {
					// 文字コードが設定されてない場合のみ設定.
					if(mime.indexOf("charset") == -1) {
						mime = mime + "; charset=" + charset;
					}
				}
				buf.append("Content-Type:").append(mime).append("\r\n");
			// MimeTypeが存在しない場合.
			} else {
				buf.append("Content-Type:application/octet-stream\r\n");
			}
			// オリジナルヘッダが存在する場合.
			if(header != null && header.size() > 0) {
				Object[] v;
				Entry<String,String> e;
				Iterator<Entry<String, String>> it = header.entrySet().iterator();
				while(it.hasNext()) {
					e = it.next();
					v = RESERVATION_HEADERS.get(e.getKey());
					// オリジナルヘッダ名が予約ヘッダ名とかぶる場合はセット出来ない.
					// [RESERVATION_HEADERS]に存在しない場合.
					// 存在した場合の区分とflgModeをand計算した時０以外が返却された場合.
					// これらの場合は、オリジナルヘッダとして利用できる.
					if(v == null || ((Integer)v[0] & flagMode) != 0) {
						// １つのオリジナルヘッダ要素をセット.
						buf.append(e.getKey()).append(":").append(e.getValue()).append("\r\n");
					}
				}
			}
			// ヘッダ終端をセット.
			buf.append("\r\n");
			// 中間ヘッダをバイナリ変換.
			ret.offer(buf.toString().getBytes(charset));
			buf = null;
			return ret;
		} catch(Exception e) {
			throw new HttpException(e);
		}
	}
}
