package quina.http;

/**
 * Postパラメータをカスタマイズで解析するオブジェクト.
 */
public interface HttpCustomPostParams {

	/**
	 * POSTパラメータを解析.
	 * @param req 対象のRequestが設定されます.
	 * @return Object 解析されたオブジェクトが返却されます.
	 *                Params形式やMap形式での返却でない場合は
	 *                {value: postParams(req)} のMapで格納
	 *                されます.
	 */
	public Object postParams(Request req);

}
