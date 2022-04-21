package quina.smple;

import quina.util.BooleanUtil;
import quina.util.DateUtil;
import quina.util.NumberUtil;
import quina.util.StringUtil;

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
	
	/**
	 * SmpleのArgsを変換.
	 * @param key 定義名を設定します.
	 * @param args 変換オブジェクトを設定します.
	 * @return Object 変換結果が返却されます.
	 */
	default Object convertArgs(String key, Object args) {
		if(args == null) {
			return null;
		} else if("boolean".equals(key) || "Boolean".equals(key)) {
			return BooleanUtil.parseBoolean(args);
		} else if("byte".equals(key) || "Byte".equals(key)) {
			return NumberUtil.parseByte(args);
		} else if("char".equals(key) || "Character".equals(key)) {
			return (char)(NumberUtil.parseInt(args) & 0x0000ffff);
		} else if("short".equals(key) || "Short".equals(key)) {
			return NumberUtil.parseShort(args);
		} else if("int".equals(key) || "Integer".equals(key)) {
			return NumberUtil.parseInt(args);
		} else if("long".equals(key) || "Long".equals(key)) {
			return NumberUtil.parseLong(args);
		} else if("float".equals(key) || "Float".equals(key)) {
			return NumberUtil.parseFloat(args);
		} else if("double".equals(key) || "Double".equals(key)) {
			return NumberUtil.parseDouble(args);
		} else if("String".equals(key) || "java.lang.String".equals(key)) {
			return StringUtil.parseString(args);
		} else if("Data".equals(key) || "java.util.Date".equals(key)) {
			return DateUtil.parseDate(args);
		}
		return args;
	}
}
