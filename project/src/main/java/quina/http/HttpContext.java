package quina.http;

import quina.http.server.response.AnyResponse;
import quina.http.server.response.RESTfulResponse;
import quina.http.server.response.SyncResponse;
import quina.worker.QuinaContext;

/**
 * HttpContext.
 */
public interface HttpContext extends QuinaContext {
	
	/**
	 * HttpElementを取得.
	 * @return HttpElement HttpElementが返却されます.
	 */
	public HttpElement getHttpElement();
	
	/**
	 * 対象のRequestを取得.
	 * @return Request Requestが返却されます.
	 */
	public Request getRequest();
	
	/**
	 * Anyレスポンスを取得.
	 * @return AnyResponse Anyレスポンスが返却されます.
	 */
	public AnyResponse getAnyResponse();

	/**
	 * RESTfulレスポンスを取得.
	 * @return RESTfulResponse RESTfulレスポンスが返却されます.
	 */
	public RESTfulResponse getRESTfulResponse();

	/**
	 * 同期レスポンスを取得.
	 * @return SyncResponse 同期レスポンスが返却されます.
	 */
	public SyncResponse getSyncResponse();
}
