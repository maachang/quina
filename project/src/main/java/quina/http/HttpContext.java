package quina.http;

import quina.http.server.response.NormalResponse;
import quina.http.server.response.RESTfulResponse;
import quina.http.server.response.SyncResponse;
import quina.worker.QuinaContext;

/**
 * HttpContext.
 */
public interface HttpContext extends QuinaContext {
	/**
	 * 対象のRequestを取得.
	 * @return Request Requestが返却されます.
	 */
	public Request getRequest();
	
	/**
	 * ノーマルレスポンスを取得.
	 * @return NormalResponse ノーマルレスポンスが返却されます.
	 */
	public NormalResponse getNormalResponse();

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
