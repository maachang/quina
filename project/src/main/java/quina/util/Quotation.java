package quina.util;

/**
 * クォーテーション処理.
 */
public class Quotation {
	private Quotation() {}
	
	/**
	 * 指定文字内のクォーテーションインデントを1つ上げる.
	 * 
	 * @param string
	 *            対象の文字列を設定します.
	 * @param indent
	 *            対象のインデント値を設定します. 0を設定した場合は１つインデントを増やします。
	 *            -1を設定した場合は１つインデントを減らします。
	 * @param dc
	 *            [true]の場合、ダブルクォーテーションで処理します.
	 * @return String 変換された文字列が返されます.
	 */
	public static final String convertQuotation(
		String string, int indent, boolean dc) {
		if (string == null || string.length() <= 0) {
			return string;
		}
		char quotation = (dc) ? '\"' : '\'';
		int len = string.length();
		char c;
		int j;
		int yenLen = 0;
		StringBuilder buf = new StringBuilder((int) (len * 1.25d));
		for (int i = 0; i < len; i++) {
			if ((c = string.charAt(i)) == quotation) {
				if (yenLen > 0) {
					if (indent == -1) {
						yenLen >>= 1;
					} else {
						yenLen <<= 1;
					}
					for (j = 0; j < yenLen; j++) {
						buf.append("\\");
					}
					yenLen = 0;
				}
				if (indent == -1) {
					buf.append(quotation);
				} else {
					buf.append("\\").append(quotation);
				}
			} else if ('\\' == c) {
				yenLen++;
			} else {
				if (yenLen != 0) {
					for (j = 0; j < yenLen; j++) {
						buf.append("\\");
					}
					yenLen = 0;
				}
				buf.append(c);
			}
		}
		if (yenLen != 0) {
			for (j = 0; j < yenLen; j++) {
				buf.append("\\");
			}
		}
		return buf.toString();
	}

	/**
	 * 指定文字内のダブルクォーテーションインデントを1つ上げる.
	 * 
	 * @param string
	 *            対象の文字列を設定します.
	 * @return String 変換された文字列が返されます.
	 */
	public static final String upDoubleQuotation(
		String string) {
		return convertQuotation(string, 0, true);
	}

	/**
	 * 指定文字内のシングルクォーテーションインデントを1つ上げる.
	 * 
	 * @param string
	 *            対象の文字列を設定します.
	 * @return String 変換された文字列が返されます.
	 */
	public static final String upSingleQuotation(
		String string) {
		return convertQuotation(string, 0, false);
	}

	/**
	 * 指定文字内のダブルクォーテーションインデントを1つ下げる.
	 * 
	 * @param string
	 *            対象の文字列を設定します.
	 * @return String 変換された文字列が返されます.
	 */
	public static final String downDoubleQuotation(
		String string) {
		// 文字列で検出されるダブルクォーテーションが
		// ￥始まりの場合は、処理する.
		boolean exec = false;
		int len = string.length();
		char c, b;
		b = 0;
		for (int i = 0; i < len; i++) {
			c = string.charAt(i);
			if (c == '\"') {
				if (b == '\\') {
					exec = true;
				}
				break;
			}
			b = c;
		}
		if (exec) {
			return convertQuotation(string, -1, true);
		}
		return string;
	}

	/**
	 * 指定文字内のシングルクォーテーションインデントを1つ下げる.
	 * 
	 * @param string
	 *            対象の文字列を設定します.
	 * @return String 変換された文字列が返されます.
	 */
	public static final String downSingleQuotation(
		String string) {
		// 文字列で検出されるシングルクォーテーションが
		// ￥始まりの場合は、処理する.
		boolean exec = false;
		int len = string.length();
		char c, b;
		b = 0;
		for (int i = 0; i < len; i++) {
			c = string.charAt(i);
			if (c == '\'') {
				if (b == '\\') {
					exec = true;
				}
				break;
			}
			b = c;
		}
		if (exec) {
			return convertQuotation(string, -1, false);
		}
		return string;
	}
}
