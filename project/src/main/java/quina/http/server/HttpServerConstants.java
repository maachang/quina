package quina.http.server;

import quina.net.nio.tcp.NioAtomicValues.Bool;
import quina.net.nio.tcp.NioAtomicValues.Number32;

/**
 * HttpServer用定義.
 */
public class HttpServerConstants {
	private HttpServerConstants() {}

	// HttpResponseのNoCacheのデフォルトモード.
	private static final boolean DEF_NO_CACHE_MODE = true;

	// [Browser]でのCrossDomain対応のデフォルト値.
	private static final boolean DEF_CROSS_DOMAIN_MODE = true;

	// チャング送信での１つの塊のバッファサイズデフォルト値.
	private static final int DEF_SEND_CHUNKED_BUFFER_LENGTH = 4096;

	// HttpResponseのNoCacheのモード.
	private static final Bool noCacheMode = new Bool(DEF_NO_CACHE_MODE);

	// [Browser]でのCrossDomain対応のモード.
	private static final Bool crossDomainMode = new Bool(DEF_CROSS_DOMAIN_MODE);

	// チャング送信での１つの塊のバッファサイズ.
	private static final Number32 sendChunkedBufferLength =
		new Number32(DEF_SEND_CHUNKED_BUFFER_LENGTH);

	/**
	 * デフォルトの条件でHttpレスポンスでキャッシュなしの定義をするか取得.
	 * @return [true]の場合はキャッシュなしの定義となります.
	 */
	public static final boolean isNoCacheMode() {
		return noCacheMode.get();
	}

	/**
	 * デフォルトの条件でHttpレスポンスでキャッシュなしの定義をするか設定.
	 * @param mode [true]の場合はキャッシュなしの定義となります.
	 */
	public static final void setNoCacheMode(boolean mode) {
		noCacheMode.set(mode);
	}

	/**
	 * デフォルトの条件でこの通信でブラウザの場合のドメイン超えを行えるか取得します.
	 * @return boolean [true]の場合はドメイン超えのレスポンス定義を行います.
	 */
	public static final boolean isCrossDomainMode() {
		return crossDomainMode.get();
	}

	/**
	 * デフォルトの条件でこの通信でブラウザの場合のドメイン超えを行えるか設定します.
	 * @param mode [true]の場合はドメイン超えのレスポンス定義を行います.
	 */
	public static final void setCrossDomainMode(boolean mode) {
		crossDomainMode.set(mode);
	}

	/**
	 * デフォルトの条件でチャング通信の送信塊のサイズを取得.
	 * @return int チャング通信の送信塊のサイズを取得します.
	 */
	public static final int getSendChunkedBufferLength() {
		return sendChunkedBufferLength.get();
	}

	/**
	 * デフォルトの条件でチャング通信の送信塊のサイズを設定.
	 * @return long チャング通信の送信塊のサイズを設定します.
	 */
	public static final void setSendChunkedBufferLength(int len) {
		sendChunkedBufferLength.set(len);
	}
}
