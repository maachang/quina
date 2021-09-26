package quina.http;

import quina.http.server.response.NormalResponse;
import quina.http.server.response.RESTfulResponse;
import quina.http.server.response.SyncResponse;

/**
 * HttpContext.
 */
public interface HttpContext {
	/**
	 * 対象のコンテキストをコピー.
	 * @return HttpContext コピーされたContextが返却されます.
	 */
	public HttpContext copy();
	
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
