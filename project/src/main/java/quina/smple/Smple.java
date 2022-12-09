package quina.smple;

/**
 * Simple Templateオブジェクト.
 * 仕様については https://github.com/maachang/quina/issues/1
 * で確認してください.
 */
public interface Smple {
	
	/**
	 * スクリプト生成.
	 * @param values スクリプトに注入する要素群を設定します.
	 *               この決定は、対象テンプレート内の
	 *               $smple.values or $smple.args で設定された
	 *               順番の内容となります.
	 * @return String SimpleTemplateの生成結果が返却されます.
	 */
	public String execute(Object... values);
	
	/**
	 * Smpleに定義されているSmpleBeanを生成.
	 * @param name 対象のBean名を設定します.
	 * @return SmpleBean nameが存在する場合は空のSmpleBeanが
	 *                   返却されます.
	 */
	public SmpleBean createBean(String name);
}
