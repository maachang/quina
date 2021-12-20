package quina.http.server;

import quina.util.AtomicNumber;
import quina.util.Flag;

/**
 * HttpServer用定義.
 */
public class HttpServerConstants {
	private HttpServerConstants() {}
	
	// HttpResponseのNoCacheのデフォルトモード.
	private static final boolean DEF_NO_CACHE_MODE = true;

	// デフォルトのgzip圧縮モード.
	private static final boolean DEF_GZIP_MODE = false;

	// [Browser]でのCrossDomain対応のデフォルト値.
	private static final boolean DEF_CROSS_DOMAIN_MODE = true;

	// サーバーソケットのバインドポート番号.
	private static final int DEF_BIND_SERVER_SOCKET_PORT = 3333;
	
	// デフォルトではエラー４０４でのRESTful返却モード.
	private static final boolean DEF_ERROR404_RESTFUL = true;

	// HttpResponseのNoCacheのモード.
	private static final Flag noCacheMode = new Flag(DEF_NO_CACHE_MODE);

	// GZIP圧縮モード.
	private static final Flag gzipMode = new Flag(DEF_GZIP_MODE);

	// [Browser]でのCrossDomain対応のモード.
	private static final Flag crossDomainMode = new Flag(DEF_CROSS_DOMAIN_MODE);
	
	// error404でのRESTful返却のモード.
	private static final Flag error404RESTful = new Flag(DEF_ERROR404_RESTFUL);

	// サーバーソケットのバインドポート番号.
	private static final AtomicNumber bindServerSocketPort =
		new AtomicNumber(DEF_BIND_SERVER_SOCKET_PORT);

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
	 * デフォルトのレスポンスGZIP圧縮モードを取得.
	 * @return [true]の場合Requestが許可する場合、GZIP圧縮します.
	 */
	public static final boolean isGzipMode() {
		return gzipMode.get();
	}

	/**
	 * デフォルトのレスポンスGZIP圧縮モードをするか設定.
	 * @param mode [true]の場合Requestが許可する場合、GZIP圧縮定義となります.
	 */
	public static final void setGzipMode(boolean mode) {
		gzipMode.set(mode);
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
	 * デフォルトのサーバソケットバインドポートを取得.
	 * @return int サーバソケットバインドポートを取得します.
	 */
	public static final int getBindServerSocketPort() {
		return bindServerSocketPort.get();
	}

	/**
	 * デフォルトのサーバソケットバインドポートを設定.
	 * @param port サーバソケットバインドポートを設定します.
	 */
	public static final void setBindServerSocketPort(int port) {
		bindServerSocketPort.set(port);
	}
	
	/**
	 * エラー４０４での返却にRESTfulで返却するかを取得.
	 * @return boolean [true]の場合RESTfulで返却します.
	 */
	public static final boolean isError404RESTful() {
		return error404RESTful.get();
	}

	/**
	 * エラー４０４での返却にRESTfulで返却するかを設定.
	 * @param mode [true]の場合RESTfulで返却します.
	 */
	public static final void setError404RESTful(boolean mode) {
		error404RESTful.set(mode);
	}

}
