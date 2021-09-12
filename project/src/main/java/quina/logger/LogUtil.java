package quina.logger;

import java.util.Date;

/**
 * ログユーティリティ.
 */
class LogUtil {
	private LogUtil() {}
	
	// 数値変換可能かチェック.
	protected static final boolean isNumeric(Object o) {
		if(o instanceof Number) {
			return true;
		}
		return isNumeric("" + o);
	}

	/**
	 * 指定文字列が数値かチェック.
	 * @param s 文字列を設定します.
	 * @return trueの場合、文字列です.
	 */
	protected static final boolean isNumeric(String s) {
		if(s == null || s.isEmpty()) {
			return false;
		}
		char c;
		int off = 0;
		int dot = 0;
		final int len = s.length();
		if(s.charAt(0) == '-') {
			off = 1;
		}
		for (int i = off; i < len; i++) {
			if (!((c = s.charAt(i)) == '.' || (c >= '0' && c <= '9'))) {
				return false;
			} else if(c == '.') {
				dot ++;
				if(dot > 1) {
					return false;
				}
			}
		}
		return true;
	}

	// int 変換.
	protected static final int convertInt(Object o) {
		if(o instanceof Number) {
			return ((Number)o).intValue();
		}
		return Integer.parseInt(""+o);
	}

	// long 変換.
	protected static final long convertLong(Object o) {
		if(o instanceof Number) {
			return ((Number)o).longValue();
		}
		return Long.parseLong(""+o);
	}

	// 日付情報を取得.
	@SuppressWarnings("deprecation")
	protected static final String dateString(Date d) {
		String n;
		return new StringBuilder().append((d.getYear() + 1900)).append("-")
				.append("00".substring((n = "" + (d.getMonth() + 1)).length())).append(n).append("-")
				.append("00".substring((n = "" + d.getDate()).length())).append(n).toString();
	}
}
