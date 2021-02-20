package quina.util;

/**
 * BooleanUtil.
 */
public class BooleanUtil {
	private BooleanUtil() {}

	private static final byte[] CHECK_CHAR = Alphabet.CHECK_CHAR;

	/**
	 * 対象のオブジェクトがBooleanとして解釈できるかチェック.
	 *
	 * @param n 変換対象の条件を設定します.
	 * @return boolean [true]の場合、Booleanで解釈が可能です.
	 */
	public static final Boolean isBool(Object o) {
		if (o == null) {
			return false;
		} else if (o instanceof Boolean) {
			return true;
		} else if (o instanceof Number) {
			return true;
		} else if (o instanceof String) {
			String s = (String)o;
			if (NumberUtil.isNumeric(s) || Alphabet.eq(s, "true") || Alphabet.eq(s, "t")
				|| Alphabet.eq(s, "on") || Alphabet.eq(s, "false") || Alphabet.eq(s, "f")
				|| Alphabet.eq(s, "off")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Boolean変換.
	 *
	 * @param n 変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Boolean parseBoolean(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Boolean) {
			return (Boolean) o;
		} else if (o instanceof Number) {
			return (((Number) o).intValue() == 0) ? false : true;
		} else if (o instanceof String) {
			return parseBoolean((String) o);
		}
		throw new BooleanException("BOOL conversion failed: " + o);
	}

	/**
	 * 文字列から、Boolean型に変換.
	 *
	 * @param s 対象の文字列を設定します.
	 * @return boolean Boolean型が返されます.
	 */
	public static final boolean parseBoolean(String s) {
		char c;
		int i, start, flg, len;

		start = flg = 0;
		len = s.length();

		for (i = start; i < len; i++) {
			c = s.charAt(i);
			if (flg == 0 && CHECK_CHAR[c] != 1) {
				start = i;
				flg = 1;
			} else if (flg == 1 && CHECK_CHAR[c] == 1) {
				len = i;
				break;
			}
		}
		if (flg == 0) {
			throw new BooleanException("Boolean conversion failed: " + s);
		}

		if (NumberUtil.isNumeric(s)) {
			return "0".equals(s) ? false : true;
		} else if (Alphabet.eq(s, start, (len -= start), "true")
			|| Alphabet.eq(s, start, len, "t") || Alphabet.eq(s, start, len, "on")) {
			return true;
		} else if (Alphabet.eq(s, start, len, "false")
			|| Alphabet.eq(s, start, len, "f") || Alphabet.eq(s, start, len, "off")) {
			return false;
		}
		throw new BooleanException("Boolean conversion failed: " + s);
	}
}
