package quina.util;

import java.math.BigDecimal;

/**
 * 数値ユーティリティ.
 */
public class NumberUtil {
	private NumberUtil() {}

	private static final byte[] CHECK_CHAR = Alphabet.CHECK_CHAR;

	/**
	 * 文字列内容が数値かチェック.
	 *
	 * @param num 対象のオブジェクトを設定します.
	 * @return boolean [true]の場合、文字列内は数値が格納されています.
	 */
	public static final boolean isNumeric(Object num) {
		if (num == null) {
			return false;
		} else if (num instanceof Number) {
			return true;
		} else if (!(num instanceof String)) {
			num = num.toString();
		}
		char c;
		String s = (String) num;
		if(s.length() > 2 && s.charAt(0) == '0' && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
			// 16進数文字列の場合.
			try {
				Long.parseLong(s.substring(2), 16);
			} catch(Exception e) {
				return false;
			}
			return true;
		}
		int i, start, end, flg, dot;
		start = flg = 0;
		dot = -1;
		end = s.length() - 1;

		for (i = start; i <= end; i++) {
			c = s.charAt(i);
			if (flg == 0 && CHECK_CHAR[c] != 1) {
				if (c == '-') {
					start = i + 1;
				} else {
					start = i;
				}
				flg = 1;
			} else if (flg == 1 && CHECK_CHAR[c] != 0) {
				if (c == '.') {
					if (dot != -1) {
						return false;
					}
					dot = i;
				} else {
					end = i - 1;
					break;
				}
			}
		}
		if (flg == 0) {
			return false;
		}
		if (start <= end) {
			for (i = start; i <= end; i++) {
				if (!((c = s.charAt(i)) == '.' || (c >= '0' && c <= '9'))) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * 対象文字列内容が小数点かチェック.
	 *
	 * @param n 対象のオブジェクトを設定します.
	 * @return boolean [true]の場合は、数値内容です.
	 */
	public static final boolean isFloat(Object n) {
		if (NumberUtil.isNumeric(n)) {
			String s;
			if (n instanceof Float || n instanceof Double || n instanceof BigDecimal) {
				return true;
			} else if (n instanceof String) {
				s = (String)n;
			} else {
				s = n.toString();
			}
			if(s.length() > 2 && s.charAt(0) == '0' && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
				return false;
			}
			return s.indexOf(".") != -1;
		}
		return false;
	}

	/**
	 * Byte変換.
	 *
	 * @param n 変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Byte parseByte(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Number) {
			return ((Number) o).byteValue();
		} else if (o instanceof String) {
			return (byte)NumberUtil.parseInt((String) o);
		} else if (o instanceof Boolean) {
			return (byte)(((Boolean) o).booleanValue() ? 1 : 0);
		}
		throw new NumberException("Byte conversion failed: " + o);
	}

	/**
	 * Short変換.
	 *
	 * @param n 変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Short parseShort(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Number) {
			return ((Number) o).shortValue();
		} else if (o instanceof String) {
			return (short)NumberUtil.parseInt((String) o);
		} else if (o instanceof Boolean) {
			return (short)(((Boolean) o).booleanValue() ? 1 : 0);
		}
		throw new NumberException("Short conversion failed: " + o);
	}

	/**
	 * Integer変換.
	 *
	 * @param n 変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Integer parseInt(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Integer) {
			return (Integer) o;
		} else if (o instanceof Number) {
			return ((Number) o).intValue();
		} else if (o instanceof String) {
			return NumberUtil.parseInt((String) o);
		} else if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue() ? 1 : 0;
		}
		throw new NumberException("Int conversion failed: " + o);
	}

	/**
	 * Long変換.
	 *
	 * @param n 変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Long parseLong(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Long) {
			return (Long) o;
		} else if (o instanceof Number) {
			return ((Number) o).longValue();
		} else if (o instanceof String) {
			return NumberUtil.parseLong((String) o);
		} else if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue() ? 1L : 0L;
		}
		throw new NumberException("Long conversion failed: " + o);
	}

	/**
	 * Float変換.
	 *
	 * @param n 変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Float parseFloat(final Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Float) {
			return (Float) o;
		} else if (o instanceof Number) {
			return ((Number) o).floatValue();
		} else if (o instanceof String && isNumeric(o)) {
			return NumberUtil.parseFloat((String) o);
		} else if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue() ? 1F : 0F;
		}
		throw new NumberException("Float conversion failed: " + o);
	}

	/**
	 * Double変換.
	 *
	 * @param n 変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Double parseDouble(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Double) {
			return (Double) o;
		} else if (o instanceof Number) {
			return ((Number) o).doubleValue();
		} else if (o instanceof String) {
			return NumberUtil.parseDouble((String) o);
		} else if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue() ? 1D : 0D;
		}
		throw new NumberException("Double conversion failed: " + o);
	}

	/**
	 * 文字列から、int型数値に変換.
	 *
	 * @param num 対象の文字列を設定します.
	 * @return int int型で変換された数値が返されます.
	 */
	public static final int parseInt(final String num) {
		char c;
		boolean mFlg = false;
		int v, i, len, ret, end;

		ret = end = v = 0;
		len = num.length();

		for (i = end; i < len; i++) {
			c = num.charAt(i);
			if (v == 0 && CHECK_CHAR[c] != 1) {
				if (c == '-') {
					end = i + 1;
					mFlg = true;
				} else {
					end = i;
				}
				v = 1;
			} else if (v == 1 && CHECK_CHAR[c] != 0) {
				len = i;
				break;
			}
		}
		if (v == 0) {
			throw new NumberException("Int conversion failed: " + num);
		}

		v = 1;
		for (i = len - 1; i >= end; i--) {
			c = num.charAt(i);
			if (c >= '0' && c <= '9') {
				ret += (v * (c - '0'));
				v *= 10;
			} else {
				throw new NumberException("Int conversion failed: " + num);
			}
		}
		return mFlg ? ret * -1 : ret;
	}

	/**
	 * 文字列から、long型数値に変換.
	 *
	 * @param num 対象の文字列を設定します.
	 * @return long long型で変換された数値が返されます.
	 */
	public static final long parseLong(final String num) {
		char c;
		boolean mFlg = false;
		long ret = 0L;
		int len, end, i, flg;

		end = flg = 0;
		len = num.length();

		for (i = end; i < len; i++) {
			c = num.charAt(i);
			if (flg == 0 && CHECK_CHAR[c] != 1) {
				if (c == '-') {
					end = i + 1;
					mFlg = true;
				} else {
					end = i;
				}
				flg = 1;
			} else if (flg == 1 && CHECK_CHAR[c] != 0) {
				len = i;
				break;
			}
		}
		if (flg == 0) {
			throw new NumberException("Long conversion failed: " + num);
		}

		long v = 1L;
		for (i = len - 1; i >= end; i--) {
			c = num.charAt(i);
			if (c >= '0' && c <= '9') {
				ret += (v * (long) (c - '0'));
				v *= 10L;
			} else {
				throw new NumberException("Long conversion failed: " + num);
			}
		}
		return mFlg ? ret * -1L : ret;
	}

	/**
	 * 文字列から、float型数値に変換.
	 *
	 * @param num 対象の文字列を設定します.
	 * @return float float型で変換された数値が返されます.
	 */
	public static final float parseFloat(final String num) {
		char c;
		boolean mFlg = false;
		float ret = 0f;
		int end, len, flg, dot, i;

		end = flg = 0;
		dot = -1;
		len = num.length();

		for (i = end; i < len; i++) {
			c = num.charAt(i);
			if (flg == 0 && CHECK_CHAR[c] != 1) {
				if (c == '-') {
					end = i + 1;
					mFlg = true;
				} else {
					end = i;
				}
				flg = 1;
			} else if (flg == 1 && CHECK_CHAR[c] != 0) {
				if (c == '.') {
					if (dot != -1) {
						throw new NumberException("Float conversion failed: " + num);
					}
					dot = i;
				} else {
					len = i;
					break;
				}
			}
		}
		if (flg == 0) {
			throw new NumberException("Float conversion failed: " + num);
		}

		float v = 1f;
		if (dot == -1) {
			for (i = len - 1; i >= end; i--) {
				c = num.charAt(i);
				if (c >= '0' && c <= '9') {
					ret += (v * (float) (c - '0'));
					v *= 10f;
				} else {
					throw new NumberException("Float conversion failed: " + num);
				}
			}
			return mFlg ? ret * -1f : ret;
		} else {
			for (i = dot - 1; i >= end; i--) {
				c = num.charAt(i);
				if (c >= '0' && c <= '9') {
					ret += (v * (float) (c - '0'));
					v *= 10f;
				} else {
					throw new NumberException("Float conversion failed: " + num);
				}
			}
			float dret = 0f;
			v = 1f;
			for (i = len - 1; i > dot; i--) {
				c = num.charAt(i);
				if (c >= '0' && c <= '9') {
					dret += (v * (float) (c - '0'));
					v *= 10f;
				} else {
					throw new NumberException("Float conversion failed: " + num);
				}
			}
			return mFlg ? (ret + (dret / v)) * -1f : ret + (dret / v);
		}
	}

	/**
	 * 文字列から、double型数値に変換.
	 *
	 * @param num 対象の文字列を設定します.
	 * @return double double型で変換された数値が返されます.
	 */
	public static final double parseDouble(final String num) {
		char c;
		boolean mFlg = false;
		double ret = 0d;
		int end, len, flg, dot, i;

		end = flg = 0;
		dot = -1;
		len = num.length();

		for (i = end; i < len; i++) {
			c = num.charAt(i);
			if (flg == 0 && CHECK_CHAR[c] != 1) {
				if (c == '-') {
					end = i + 1;
					mFlg = true;
				} else {
					end = i;
				}
				flg = 1;
			} else if (flg == 1 && CHECK_CHAR[c] != 0) {
				if (c == '.') {
					if (dot != -1) {
						throw new NumberException("Double conversion failed: " + num);
					}
					dot = i;
				} else {
					len = i;
					break;
				}
			}
		}
		if (flg == 0) {
			throw new NumberException("Double conversion failed: " + num);
		}

		double v = 1d;
		if (dot == -1) {
			for (i = len - 1; i >= end; i--) {
				c = num.charAt(i);
				if (c >= '0' && c <= '9') {
					ret += (v * (double) (c - '0'));
					v *= 10d;
				} else {
					throw new NumberException("Double conversion failed: " + num);
				}
			}
			return mFlg ? ret * -1d : ret;
		} else {
			for (i = dot - 1; i >= end; i--) {
				c = num.charAt(i);
				if (c >= '0' && c <= '9') {
					ret += (v * (double) (c - '0'));
					v *= 10d;
				} else {
					throw new NumberException("Double conversion failed: " + num);
				}
			}
			double dret = 0d;
			v = 1d;
			for (i = len - 1; i > dot; i--) {
				c = num.charAt(i);
				if (c >= '0' && c <= '9') {
					dret += (v * (double) (c - '0'));
					v *= 10d;
				} else {
					throw new NumberException("Double conversion failed: " + num);
				}
			}
			return mFlg ? (ret + (dret / v)) * -1d : ret + (dret / v);
		}
	}
	
	/**
	 * 容量を指定する文字列であるかチェック.
	 * @param num 対象のサイズの文字列を設定します.
	 * @return boolean parseCapacityByLong で変換が可能です.
	 */
	public static final boolean isCapacityString(String num) {
		// 最後の情報が数字の単位のアルファベットの場合.
		boolean dotFlag = false;
		int lastPos = num.length() - 1;
		char c = num.charAt(lastPos);
		if(Alphabet.oneEq(c, 'k') || Alphabet.oneEq(c, 'm') ||
			Alphabet.oneEq(c, 'g') || Alphabet.oneEq(c, 't') ||
			Alphabet.oneEq(c, 'p')) {
			// 最後の情報以外が数字の場合.
			final int len = num.length() - 1;
			for(int i = num.charAt(0) == '-' ? 1 : 0;
				i < len; i ++) {
				c = num.charAt(i);
				if(c == '.') {
					if(dotFlag || i == 0 || i + 1 >= len) {
						return false;
					}
					dotFlag = true;
				} else if(!(c >= '0' && c <= '9')) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	// 小数点表記の場合は小数点で計算して返却.
	private static final long convertLongOrDouble(
		String num, long value, int len) {
		char c;
		boolean dotFlag = false;
		boolean minusFlag = num.charAt(0) == '-';
		for(int i = minusFlag ? 1 : 0; i < len; i ++) {
			c = num.charAt(i);
			if(c == '.') {
				if(dotFlag || i == 0 || i + 1 >= len) {
					throw new NumberException("Long conversion failed: " + num);
				}
				dotFlag = true;
			} else if(!(c >= '0' && c <= '9')) {
				throw new NumberException("Long conversion failed: " + num);
			}
		}
		if(dotFlag) {
			if(minusFlag) {
				return ((long)(parseDouble(
					num.substring(1, len)) * (double)value)) * -1L;
			}
			return (long)(parseDouble(
				num.substring(0, len)) * (double)value);
		} else if(minusFlag) {
			return (parseLong(num.substring(1, len)) * value) * -1L;
		}
		return parseLong(num.substring(0, len)) * value;
	}
	
	/**
	 * 容量を指定する文字列からlong値に変換.
	 * 以下のように キロ,メガ,ギガ,テラ のような単位を
	 * long値に変換します.
	 * 
	 * "1024" = 1,024.
	 * "1k" = 1,024.
	 * "1m" = 1,048,576.
	 * "1g" = 1,073,741,824.
	 * "1t" = 1,099,511,627,776.
	 * "1p" = 1,125,899,906,842,624.
	 * 
	 * @param num 対象のサイズの文字列を設定します.
	 * @return long 変換されたLong値が返却されます.
	 */
	public static final long parseCapacityByLong(String num) {
		int lastPos = num.length() - 1;
		char c = num.charAt(lastPos);
		if(Alphabet.oneEq(c, 'k')) {
			return convertLongOrDouble(num, 1024L, lastPos);
		} else if(Alphabet.oneEq(c, 'm')) {
			return convertLongOrDouble(num, 1048576L, lastPos);
		} else if(Alphabet.oneEq(c, 'g')) {
			return convertLongOrDouble(num, 1073741824L, lastPos);
		} else if(Alphabet.oneEq(c, 't')) {
			return convertLongOrDouble(num, 1099511627776L, lastPos);
		} else if(Alphabet.oneEq(c, 'p')) {
			return convertLongOrDouble(num, 1125899906842624L, lastPos);
		}
		return convertLongOrDouble(num, 1L, lastPos + 1);
	}
}
