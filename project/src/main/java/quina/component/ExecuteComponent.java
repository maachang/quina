package quina.component;

import quina.http.HttpCustomAnalysisParams;
import quina.http.HttpElement;
import quina.http.HttpStatus;
import quina.http.MimeTypes;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.HttpServerUtil;
import quina.util.AtomicObject;

/**
 * コンポーネント実行用オブジェクト.
 */
public class ExecuteComponent {
	private ExecuteComponent() {}
	
	// シングルトン.
	private static final ExecuteComponent SNGL =
		new ExecuteComponent();
	
	/**
	 * オブジェクトを取得.
	 * @return ExecuteComponent オブジェクトが返却されます.
	 */
	public static final ExecuteComponent getInstance() {
		return SNGL;
	}
	
	// カスタムなPostBody解析.
	private AtomicObject<HttpCustomAnalysisParams> custom =
		new AtomicObject<HttpCustomAnalysisParams>();
	
	/**
	 * HTTPパラメータ解析をカスタマイズ解析するオブジェクトを設定.
	 * @return custom カスタムオブジェクトを設定します.
	 */
	public void setHttpCustomAnalysisParams(
		HttpCustomAnalysisParams custom) {
		this.custom.set(custom);
	}
	
	/**
	 * HTTPパラメータ解析をカスタマイズ解析するオブジェクトを取得.
	 * @return HttpCustomAnalysisParams カスタムオブジェクトが返却されます.
	 */
	public HttpCustomAnalysisParams getHttpCustomAnalysisParams() {
		return custom.get();
	}

	/**
	 * URLを指定してコンポーネントを実行.
	 * @param em 対象のHttp要素を設定します.
	 */
	public final void execute(HttpElement em) {
		execute(null, em);
	}
	
	/**
	 * URLを指定してコンポーネントを実行.
	 * @param url 対象のURLを設定します.
	 *            null の場合は em.getRequest().getSrcUrl() で
	 *            処理されます
	 * @param em 対象のHttp要素を設定します.
	 */
	public final void execute(String url, HttpElement em) {
		HttpServerUtil.execComponent(
			url, em, MimeTypes.getInstance(), custom.get());
	}
	
	/**
	 * HttpErrorを送信.
	 * @param em 対象のHTTP要素を設定します.
	 */
	public static final void sendError(HttpElement em) {
		HttpServerUtil.sendError(em);
	}
	
	/**
	 * HttpErrorを送信.
	 * @param status 対象のHTTPステータスを設定します.
	 * @param em 対象のHTTP要素を設定します.
	 */
	public static final void sendError(
		int status, HttpElement em) {
		HttpServerUtil.sendError(status, em);
	}
	
	/**
	 * HttpErrorを送信.
	 * @param status 対象のHTTPステータスを設定します.
	 * @param em 対象のHTTP要素を設定します.
	 */
	public static final void sendError(
		HttpStatus status, HttpElement em) {
		HttpServerUtil.sendError(status, em);
	}
	
	/**
	 * HttpErrorを送信.
	 * @param status 対象のHTTPステータスを設定します.
	 * @param message 対象のHTTPステータスメッセージを設定します.
	 * @param em 対象のHTTP要素を設定します.
	 */
	public static final void sendError(
		int status, String message, HttpElement em) {
		HttpServerUtil.sendError(status, message, em);
	}
	
	/**
	 * HttpErrorを送信.
	 * @param status 対象のHTTPステータスを設定します.
	 * @param message 対象のHTTPステータスメッセージを設定します.
	 * @param em 対象のHTTP要素を設定します.
	 */
	public static final void sendError(
		HttpStatus status, String message, HttpElement em) {
		HttpServerUtil.sendError(status, message, em);
	}

	/**
	 * HttpErrorを送信.
	 * @param em 対象のHTTP要素を設定します.
	 * @param e 例外を設定します.
	 */
	public static final void sendError(
		HttpElement em, Throwable e) {
		HttpServerUtil.sendError(em, e);
	}
	
	/**
	 * HttpErrorを送信.
	 * @param status 対象のHTTPステータスを設定します.
	 * @param em 対象のHTTP要素を設定します.
	 * @param e 例外を設定します.
	 */
	public static final void sendError(
		int status, HttpElement em, Throwable e) {
		HttpServerUtil.sendError(status, em, e);
	}
	
	/**
	 * HttpErrorを送信.
	 * @param status 対象のHTTPステータスを設定します.
	 * @param em 対象のHTTP要素を設定します.
	 * @param e 例外を設定します.
	 */
	public static final void sendError(
		HttpStatus status, HttpElement em, Throwable e) {
		HttpServerUtil.sendError(status, em, e);
	}
	
	/**
	 * HttpErrorを送信.
	 * @param status 対象のHTTPステータスを設定します.
	 * @param message 対象のHTTPステータスメッセージを設定します.
	 * @param em 対象のHTTP要素を設定します.
	 * @param e 例外を設定します.
	 */
	public static final void sendError(
		int status, String message, HttpElement em, Throwable e) {
		HttpServerUtil.sendError(status, message, em, e);
	}
	
	/**
	 * HttpErrorを送信.
	 * @param status 対象のHTTPステータスを設定します.
	 * @param message 対象のHTTPステータスメッセージを設定します.
	 * @param em 対象のHTTP要素を設定します.
	 * @param e 例外を設定します.
	 */
	public static final void sendError(
		HttpStatus status, String message, HttpElement em,
		Throwable e) {
		HttpServerUtil.sendError(status, message, em, e);
	}
	
	/**
	 * HttpErrorを送信.
	 * @param req HttpRequestを設定します.
	 * @param res httpResponseを設置します.
	 */
	public static final void sendError(
		Request req, Response<?> res) {
		HttpServerUtil.sendError(req, res, null);
	}
	
	/**
	 * HttpErrorを送信.
	 * @param req HttpRequestを設定します.
	 * @param res httpResponseを設置します.
	 * @param e 例外を設定します.
	 */
	public static final void sendError(
		Request req, Response<?> res, Throwable e) {
		HttpServerUtil.sendError(req, res, e);
	}
}
