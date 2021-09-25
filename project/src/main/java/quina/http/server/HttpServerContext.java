package quina.http.server;

import java.util.concurrent.locks.ReentrantReadWriteLock;

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
import quina.util.AtomicNumber;

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
	protected static final HttpContext create(
		HttpElement em) {
		HttpServerContext ctx = threadLocal.get();
		if(ctx != null) {
			return ctx._create(
				(HttpServerRequest)em.getRequest(),
				(AbstractResponse<?>)em.getResponse());
		}
		ctx = new HttpServerContext(
			(HttpServerRequest)em.getRequest(),
			(AbstractResponse<?>)em.getResponse());
		threadLocal.set(ctx);
		return ctx;
	}
	
	/**
	 * 現在のスレッドに新しいコンテキストを生成.
	 * @param req Requestを設定します.
	 * @param res Responseを設定します.
	 * @return HttpContext HttpContextが返却されます.
	 */
	protected static final HttpContext create(
		HttpServerRequest req, AbstractResponse<?> res) {
		HttpServerContext ctx = threadLocal.get();
		if(ctx != null) {
			return ctx._create(req, res);
		}
		ctx = new HttpServerContext(req, res);
		threadLocal.set(ctx);
		return ctx;
	}
	
	/**
	 * 現在のスレッドのコンテキストをクリア.
	 */
	protected static final void clear() {
		HttpServerContext ctx = threadLocal.get();
		if(ctx != null) {
			ctx._clear();
		}
	}
	
	/**
	 * HttpContextを取得.
	 * @return HttpContext HttpContextが返却されます.
	 */
	public static final HttpContext get() {
		// 現在のスレッドのコンテキストを取得.
		return threadLocal.get();
	}
	
	// スレッド呼び出しカウント.
	private final AtomicNumber scope = new AtomicNumber(0);
	
	// HttpRequest.
	private HttpServerRequest request;
	
	// HttpResponse.
	private AbstractResponse<?> response;
	
	// Read-Writeロックオブジェクト.
	private final ReentrantReadWriteLock lock =
		new ReentrantReadWriteLock();
	
	// コンストラクタ.
	private HttpServerContext() {}
	
	// コンストラクタ.
	private HttpServerContext(
		HttpServerRequest req, AbstractResponse<?> res) {
		_create(req, res);
	}
	
	// 新しく設定.
	private final HttpServerContext _create(
		HttpServerRequest req, AbstractResponse<?> res) {
		if(req == null || res == null) {
			throw new QuinaException("The argument is null.");
		}
		lock.writeLock().lock();
		try {
			request = req;
			response = res;
		} finally {
			lock.writeLock().unlock();
		}
		// 呼び出しスコープの追加.
		scope.inc();
		return this;
	}
	
	// 内容をクリア.
	private final void _clear() {
		lock.writeLock().lock();
		try {
			if(request != null) {
				request = null;
				response = null;
				// 呼び出しスコープの削除.
				scope.dec();
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * 対象のコンテキストをコピー.
	 * @return HttpContext コピーされたContextが返却されます.
	 */
	@Override
	public HttpContext copy() {
		return new HttpServerContext(request, response);
	}
	
	/**
	 * HttpElementを取得.
	 * @return HttpElement HttpElementが返却されます.
	 */
	public HttpElement getHttpElement() {
		lock.readLock().lock();
		try {
			if(request != null) {
				return request.getElement();
			}
		} finally {
			lock.readLock().unlock();
		}
		return null;
	}
	
	@Override
	public Request getRequest() {
		lock.readLock().lock();
		try {
			return request;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	// 対象のレスポンスを取得.
	private AbstractResponse<?> _getResponse() {
		lock.readLock().lock();
		try {
			return response;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	// レスポンス生成.
	private final AbstractResponse<?> _setResponse(
		AbstractResponse<?> res) {
		lock.writeLock().lock();
		try {
			if(response != null) {
				response = res;
				return res;
			} else {
				return null;
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * スレッド呼び出しのスレッドスコープが終端の場合.
	 * @return boolean true の場合、スレッドスコープの終端です.
	 */
	protected boolean isExitScoped() {
		return scope.get() <= 0;
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
		return (NormalResponse)_setResponse(
			(AbstractResponse<?>)new NormalResponseImpl(res));
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
		return (RESTfulResponse)_setResponse(
			(AbstractResponse<?>)new RESTfulResponseImpl(res));
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
		return (SyncResponse)_setResponse(
			(AbstractResponse<?>)new SyncResponseImpl(res));
	}
}
