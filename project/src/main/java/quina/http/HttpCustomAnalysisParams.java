package quina.http;

/**
 * HTTPパラメータ解析をカスタマイズ解析するオブジェクト.
 */
public interface HttpCustomAnalysisParams {

	/**
	 * GETパラメータを解析.
	 * @param param URLの？以降の内容を設定します.
	 * @return Object 変換結果が返却されます.
	 */
	public Object getParams(String param);

	/**
	 * POSTパラメータを解析.
	 * @param req 対象のRequestが設定されます.
	 * @param body 対象のBody条件が設定されます.
	 * @param contentType コンテンツタイプを設定します.
	 * @return Object 解析されたオブジェクトが返却されます.
	 *                Params形式やMap形式での返却でない場合は
	 *                {value: postParams(req)} のMapで格納
	 *                されます.
	 */
	public Object postParams(Request req, String body, String contentType);
}
