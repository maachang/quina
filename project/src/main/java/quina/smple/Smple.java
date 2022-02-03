package quina.smple;

/**
 * Simple Templateオブジェクト.
 */
public interface Smple {
	
	/**
	 * スクリプト生成.
	 * @param values スクリプトに注入する要素群を設定します.
	 *               [0]キー名, [1]要素... と言う形で設定します.
	 * @return String SimpleTemplateの生成結果が返却されます.
	 */
	public String compile(Object... values);

}
