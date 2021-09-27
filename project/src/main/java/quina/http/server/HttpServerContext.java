package quina.http.server;

import quina.exception.QuinaException;
import quina.http.HttpContext;
import quina.http.HttpElement;
import quina.http.Request;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.NormalResponse;
import quina.http.server.response.NormalResponseImpl;
import quina.http.server.response.RESTfulResponse;
import quina.http.server.response.RESTfulResponseImpl;
import quina.http.server.response.SyncResponse;
import quina.http.server.response.SyncResponseImpl;
import quina.worker.QuinaContext;

/**
 * HttpServerContext.
 */
public class HttpServerContext implements HttpContext {
	// スレッド毎でコンテキストを管理するスレッドローカル.
	private static final ThreadLocal<HttpServerContext> threadLocal =
		new ThreadLocal<HttpServerContext>();
	
	/**
	 * 現在のスレッドに新しいコンテキストを生成.
	 * @param context 対象のQuinaContextを設定します.
	 * @return HttpContext HttpContextが返却されます.
	 */
	public static final HttpContext set(
		QuinaContext context) {
		if(context == null) {
			throw new QuinaException("The argument is null.");
		}
		if(!(context instanceof HttpServerContext)) {
			throw new QuinaException(
				"The specified Context is not an HttpServerContext.");
		}
		HttpServerContext ctx = (HttpServerContext)context;
		threadLocal.set(ctx);
		return ctx;
	}
	
	/**
	 * 現在のスレッドに新しいコンテキストを生成.
	 * @param em 対象のHttpElementを設定します.
	 * @return HttpContext HttpContextが返却されます.
	 */
	public static final HttpContext create(
		HttpElement em) {
		if(em == null ||
			em.getRequest() == null ||
			em.getResponse() == null) {
			throw new QuinaException("The argument is null.");
		} else if(!em.isConnection()) {
			throw new QuinaException(
				"HttpElement has already been destroyed.");
		}
		final HttpServerContext ctx = new HttpServerContext(em);
		threadLocal.set(ctx);
		return ctx;
	}
	
	/**
	 * コンテキストが作成可能かチェック.
	 * @param em 対象のHttpElementを設定します.
	 * @return boolean trueの場合作成可能です.
	 */
	public static final boolean isCreate(HttpElement em) {
		if(em == null || !em.isConnection() ||
			em.getRequest() == null ||
			em.getResponse() == null) {
			return false;
		}
		return true;
	}
	
	/**
	 * HttpServerContextを取得.
	 * @return HttpServerContext HttpServerContextが返却されます.
	 */
	public static final HttpServerContext get() {
		// 現在のスレッドのコンテキストを取得.
		return threadLocal.get();
	}
	
	/**
	 * 現在のスレッドのコンテキストをクリア.
	 */
	public static final void clear() {
		threadLocal.set(null);
	}
	
	// HttpElement.
	private HttpElement element;
	
	// threadScope.
	private int threadScope;
	
	// コンストラクタ.
	private HttpServerContext() {}
	
	// コンストラクタ.
	private HttpServerContext(HttpElement em) {
		this.element = em;
		// 生成時のスレッドスコープ値は１.
		this.threadScope = 1;
	}
	
	/**
	 * スレッドスコープ値を設定.
	 * @param threadScope 対象のスレッドスコープ値を
	 *                    設定します.
	 */
	public void setThreadScope(int threadScope) {
		this.threadScope = threadScope;
	}
	
	/**
	 * 新しいスレッドスコープを実行.
	 */
	public void startThreadScope() {
		threadScope ++;
	}
	
	/**
	 * 現在のスレッドスコープを終了.
	 */
	public void exitThreadScope() {
		threadScope --;
	}
	
	/**
	 * スレッドスコープ値を取得.
	 * @return int スレッドスコープ値が返却されます.
	 */
	@Override
	public int getThreadScope() {
		return threadScope;
	}
	
	/**
	 * HttpElementを取得.
	 * @return HttpElement HttpElementが返却されます.
	 */
	public HttpElement getHttpElement() {
		return element;
	}
	
	@Override
	public Request getRequest() {
		return element.isConnection() ?
			element.getRequest() : null;
	}
	
	// 対象のレスポンスを取得.
	private AbstractResponse<?> _getResponse() {
		return element.isConnection() ?
			(AbstractResponse<?>)element.getResponse() :
			null;
	}
	
	/**
	 * ノーマルレスポンスを取得.
	 * @return NormalResponse ノーマルレスポンスが返却されます.
	 */
	@Override
	public NormalResponse getNormalResponse() {
		AbstractResponse<?> res = _getResponse();
		if(res == null) {
			return null;
		} else if(res instanceof NormalResponse) {
			return (NormalResponse)res;
		}
		return new NormalResponseImpl(res);
	}

	/**
	 * RESTfulレスポンスを取得.
	 * @return RESTfulResponse RESTfulレスポンスが返却されます.
	 */
	@Override
	public RESTfulResponse getRESTfulResponse() {
		AbstractResponse<?> res = _getResponse();
		if(res == null) {
			return null;
		} else if(res instanceof RESTfulResponse) {
			return (RESTfulResponse)res;
		}
		return new RESTfulResponseImpl(res);
	}

	/**
	 * 同期レスポンスを取得.
	 * @return SyncResponse 同期レスポンスが返却されます.
	 */
	@Override
	public SyncResponse getSyncResponse() {
		AbstractResponse<?> res = _getResponse();
		if(res == null) {
			return null;
		} else if(res instanceof SyncResponse) {
			return (SyncResponse)res;
		}
		return new SyncResponseImpl(res);
	}
	
	@Override
	public String toString() {
		return "quina.worker.QuinaContext";
	}
}
