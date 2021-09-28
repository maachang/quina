package quina.component;

import quina.http.HttpException;
import quina.http.Method;
import quina.http.Request;

/**
 * コンポーネント定義.
 */
public class ComponentConstants {
	private ComponentConstants() {}

	/**
	 * コンポーネント属性: 同期タイプ.
	 */
	public static final int ATTRIBUTE_SYNC = 0x0100;

	/**
	 * コンポーネント属性: RESTfulタイプ.
	 */
	public static final int ATTRIBUTE_RESTFUL = 0x0200;

	/**
	 * コンポーネント属性: ノーマルコンポーネント.
	 */
	public static final int ATTRIBUTE_NORMAL = 0x0000;

	/**
	 * コンポーネント属性: ファイルコンポーネント.
	 */
	public static final int ATTRIBUTE_FILE = 0x00ff;

	/**
	 * コンポーネント属性: エラーコンポーネントタイプ
	 */
	public static final int ATTRIBUTE_ERROR = 0x8000;

	/**
	 * HTTPメソッド: 全属性.
	 */
	public static final int HTTP_METHOD_ALL =
		  Method.GET.getType()
		| Method.POST.getType()
		| Method.DELETE.getType()
		| Method.PUT.getType()
		| Method.PATCH.getType()
		;

	/**
	 * HTTPメソッド: GET.
	 */
	public static final int HTTP_METHOD_GET = Method.GET.getType();

	/**
	 * HTTPメソッド: POST.
	 */
	public static final int HTTP_METHOD_POST = Method.POST.getType();

	/**
	 * HTTPメソッド: DELETE.
	 */
	public static final int HTTP_METHOD_DELETE = Method.DELETE.getType();

	/**
	 * HTTPメソッド: PUT.
	 */
	public static final int HTTP_METHOD_PUT = Method.PUT.getType();

	/**
	 * HTTPメソッド: PATCH.
	 */
	public static final int HTTP_METHOD_PATCH = Method.PATCH.getType();
	
	/**
	 * Httpエラー405を返却.
	 * @param req
	 */
	public static final void httpError405(Request req) {
		throw new HttpException(405,
			"The specified method: " +
			req.getMethod() +
			" cannot be used for this URL: " +
			req.getUrl());
	}
}
