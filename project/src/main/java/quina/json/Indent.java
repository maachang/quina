package quina.json;

/**
 * インデント処理.
 */
public class Indent {
	private Indent() {}
	
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
	public static final String indentQuote(String string, int indent, boolean dc) {
		if (string == null || string.length() <= 0) {
			return string;
		}
		char quote = (dc) ? '\"' : '\'';
		int len = string.length();
		char c;
		int j;
		int yenLen = 0;
		StringBuilder buf = new StringBuilder((int) (len * 1.25d));
		for (int i = 0; i < len; i++) {
			if ((c = string.charAt(i)) == quote) {
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
					buf.append(quote);
				} else {
					buf.append("\\").append(quote);
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
	public static final String upIndentDoubleQuote(String string) {
		return indentQuote(string, 0, true);
	}

	/**
	 * 指定文字内のシングルクォーテーションインデントを1つ上げる.
	 * 
	 * @param string
	 *            対象の文字列を設定します.
	 * @return String 変換された文字列が返されます.
	 */
	public static final String upIndentSingleQuote(String string) {
		return indentQuote(string, 0, false);
	}

	/**
	 * 指定文字内のダブルクォーテーションインデントを1つ下げる.
	 * 
	 * @param string
	 *            対象の文字列を設定します.
	 * @return String 変換された文字列が返されます.
	 */
	public static final String downIndentDoubleQuote(String string) {
		// 文字列で検出されるダブルクォーテーションが￥始まりの場合は、処理する.
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
			return indentQuote(string, -1, true);
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
	public static final String downIndentSingleQuote(String string) {
		// 文字列で検出されるシングルクォーテーションが￥始まりの場合は、処理する.
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
			return indentQuote(string, -1, false);
		}
		return string;
	}
}