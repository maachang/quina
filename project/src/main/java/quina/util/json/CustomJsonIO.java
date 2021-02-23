package quina.util.json;

/**
 * JSONのI/Oに対するカスタム変換処理を定義します.
 */
public interface CustomJsonIO {

	/**
	 * null情報をJSON文字列変換.
	 * @return JSON文字列が返却されます.
	 */
	default String nullToString() {
		return "null";
	}

	/**
	 * boolean型をJSON文字列変換.
	 * @param b boolean型を設定します.
	 * @return JSON文字列が返却されます.
	 */
	default String booleanToString(Boolean b) {
		return b.toString();
	}

	/**
	 * 数値型をJSON文字列変換.
	 * @param n 数値オブジェクトを設定します.
	 * @return JSON文字列が返却されます.
	 */
	default String numberToString(Number n) {
		return n.toString();
	}

	/**
	 * charをJSON文字変換.
	 * @param c charを設定します.
	 * @return JSON文字列が返却されます.
	 */
	default String charToString(Character c) {
		return new StringBuilder("\"")
			.append(c).append("\"").toString();
	}

	/**
	 * バイナリ情報をJSON文字変換.
	 * @param bin バイナリ情報を設定します.
	 * @return JSON文字列が返却されます.
	 */
	default String binaryToString(byte[] bin) {
		// Base64変換.
		return "\"" + java.util.Base64.getEncoder().encodeToString(bin) + "\"";
	}

	/**
	 * char配列をJSON文字変換.
	 * @param c char配列を設定します.
	 * @return JSON文字列が返却されます.
	 */
	default String charArrayToString(char[] c) {
		final int len = c.length;
		final char[] ret = new char[len + 2];
		ret[0] = ret[len - 1] = '\"';
		System.arraycopy(c, 0, ret, 1, len);
		return new String(ret);
	}

	/**
	 * DateオブジェクトをJSON文字変換.
	 * @param date Dateオブジェクトを設定します.
	 * @return JSON文字列が返却されます.
	 */
	default String dateToString(java.util.Date date) {
		return "\"" + Json.dateToString(date) + "\"";
	}

	/**
	 * 文字列をJSON文字変換.
	 * @param s 文字列を設定します.
	 * @return JSON文字列が返却されます.
	 */
	default String stringToString(String s) {
		return "\"" + s + "\"";
	}

	/**
	 * JSON文字列からNULLオブジェクトに変換.
	 * @return Object 変換されたオブジェクトが返却されます.
	 */
	default Object jsonToNull() {
		return null;
	}

	/**
	 * JSON文字列からBooleanオブジェクトに変換.
	 * @param v JSON文字列を設定します.
	 * @return Object 変換されたオブジェクトが返却されます.
	 */
	default Object jsonToBoolean(String v) {
		return Json.treeEq("true", v);
	}

	/**
	 * JSON文字列からNumberオブジェクトに変換.
	 * @param v JSON文字列を設定します.
	 * @return Object 変換されたオブジェクトが返却されます.
	 */
	default Object jsonToNumber(String v) {
		if (v.indexOf(".") != -1) {
			return Double.parseDouble(v);
		}
		return Long.parseLong(v);
	}

	/**
	 * JSON文字列からDateオブジェクトに変換.
	 * @param v JSON文字列を設定します.
	 * @return Object 変換されたオブジェクトが返却されます.
	 */
	default Object jsonToDate(String v) {
		return Json.stringToDate(v);
	}

	/**
	 * JSON文字列からStringオブジェクトに変換.
	 * @param v JSON文字列を設定します.
	 * @return Object 変換されたオブジェクトが返却されます.
	 */
	default Object jsonToString(String v) {
		return v;
	}

	/**
	 * 日付オブジェクトとしてJSON文字列が変換可能かチェック.
	 * @param v JSON文字列を設定します.
	 * @return boolean trueの場合、日付オブジェクトとして変換可能です.
	 */
	default boolean isDate(String v) {
		return Json.isDate(v);
	}
}
