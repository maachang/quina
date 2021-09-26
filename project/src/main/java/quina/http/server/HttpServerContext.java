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

/**
 * HttpServerContext.
 */
public class HttpServerContext implements HttpContext {
	// スレッド毎でコンテキストを管理するスレッドローカル.
	private static final ThreadLocal<HttpServerContext> threadLocal =
		new ThreadLocal<HttpServerContext>();
	
	/**
	 * 現在のスレッドに新しいコンテキストを生成.
	 * @param em 対象のHttpElementを設定します.
	 * @return HttpContext HttpContextが返却されます.
	 */
	public static final HttpContext create(
		HttpElement em) {
		HttpServerContext ctx = threadLocal.get();
		if(em == null || !em.isConnection() ||
			em.getRequest() == null ||
			em.getResponse() == null) {
			throw new QuinaException("The argument is null.");
		}
		if(ctx != null) {
			return ctx._create(em);
		}
		ctx = new HttpServerContext(em);
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
	 * 現在のスレッドのコンテキストをクリア.
	 */
	public static final void clear() {
		HttpServerContext ctx = threadLocal.get();
		if(ctx != null) {
			ctx._clear();
		}
	}
	
	/**
	 * HttpServerContextを取得.
	 * @return HttpServerContext HttpServerContextが返却されます.
	 */
	public static final HttpServerContext get() {
		// 現在のスレッドのコンテキストを取得.
		return threadLocal.get();
	}
	
	// HttpElement.
	private HttpElement element;
	
	// コンストラクタ.
	private HttpServerContext() {}
	
	// コンストラクタ.
	private HttpServerContext(HttpElement em) {
		this.element = em;
	}
	
	// 新しく設定.
	private final HttpServerContext _create(
		HttpElement em) {
		// 既に登録されるものと同じ場合は登録しない.
		if(element != em) {
			element = em;
		}
		return this;
	}
	
	// 内容をクリア.
	private final void _clear() {
		element = null;
	}
	
	/**
	 * クリアーされてる状態の場合.
	 * @return boolean true の場合、クリアされています.
	 */
	public boolean isClear() {
		return element == null;
	}
	
	/**
	 * 対象のコンテキストをコピー.
	 * @return HttpContext コピーされたContextが返却されます.
	 */
	@Override
	public HttpContext copy() {
		return new HttpServerContext(element);
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
}
