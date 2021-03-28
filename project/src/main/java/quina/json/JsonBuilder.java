package quina.json;

/**
 * Json用のBuilder.
 */
public interface JsonBuilder {

	/**
	 * 情報を追加します.
	 * @param s 文字列を設定します.
	 * @return JsonBuilder JsonBuilderが返却されます.
	 */
	public JsonBuilder append(String s);

	/**
	 * 文字列変換します.
	 * @return String 文字列が返却されます.
	 */
	public String toString();
}
