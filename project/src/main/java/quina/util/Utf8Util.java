package quina.util;

/**
 * UTF8ユーティリティ.
 */
public class Utf8Util {
	
	/**
	 * UTF8文字列(char[])のバイナリ変換長を取得.
	 * 
	 * @param value 対象の文字列(char[])を設定します.
	 * @param off 文字列(char[])のオフセット値を設定します.
	 * @param len 文字列(char[])の長さを設定します.
	 * @return int バイナリ変換される文字列の長さを設定します.
	 */
	public static final int utf8Length(final char[] value, final int off,
		final int len) {
		if(value == null || value.length == 0) {
			return 0;
		}
		int c;
		int ret = 0;
		for (int i = 0; i < len; i++) {
			c = (int) value[off + i];
			// サロゲートペア処理.
			if (c >= 0xd800 && c <= 0xdbff) {
				c = 0x10000 + (((c - 0xd800) << 10) |
					((int) value[off + i + 1] - 0xdc00));
				i ++;
			}
			if ((c & 0xffffff80) == 0) {
				ret += 1;
			} else if (c < 0x800) {
				ret += 2;
			} else if (c < 0x10000) {
				ret += 3;
			} else {
				ret += 4;
			}
		}
		return ret;
	}
	
	/**
	 * UTF8文字列(char[])のバイナリ変換長を取得.
	 * 
	 * @param value 対象の文字列(char[])を設定します.
	 * @param off 文字列(char[])のオフセット値を設定します.
	 * @param len 文字列(char[])の長さを設定します.
	 * @return int バイナリ変換される文字列の長さを設定します.
	 */
	public static final int utf8Length(final String value, final int off,
		final int len) {
		if(value == null || value.length() == 0) {
			return 0;
		}
		int c;
		int ret = 0;
		for (int i = 0; i < len; i++) {
			c = (int) value.charAt(off + i);
			// サロゲートペア処理.
			if (c >= 0xd800 && c <= 0xdbff) {
				c = 0x10000 + (((c - 0xd800) << 10) |
					((int) value.charAt(off + i + 1) - 0xdc00));
				i ++;
			}
			if ((c & 0xffffff80) == 0) {
				ret += 1;
			} else if (c < 0x800) {
				ret += 2;
			} else if (c < 0x10000) {
				ret += 3;
			} else {
				ret += 4;
			}
		}
		return ret;
	}

	/**
	 * UTF8文字列(char[])をバイナリ変換.
	 * @param out 受け取るバイナリ情報を設定します.
	 *            この値は len の４倍の長さを設定します.
	 * @param oOff 受け取るバイナリのオフセット値を設定します.
	 * @param value 対象の文字列(char[])を設定します.
	 * @param off 文字列(char[])のオフセット値を設定します.
	 * @param len 文字列(char[])の長さを設定します.
	 * @return int 変換したバイナリ長が返却されます.
	 */
	public static final int toBinary(final byte[] out, final int oOff,
		final char[] value, final int off, final int len) {
		if (value == null || len == 0) {
			return 0;
		}
		int c;
		int o = oOff;
		int ret = 0;
		try {
			for (int i = 0; i < len; i++) {
				c = (int) value[off + i];
	
				// サロゲートペア処理.
				if (c >= 0xd800 && c <= 0xdbff) {
					c = 0x10000 + (((c - 0xd800) << 10) |
						((int) value[off + i + 1] - 0xdc00));
					i++;
				}
	
				if ((c & 0xffffff80) == 0) {
					out[o ++] = (byte)c;
				} else if (c < 0x800) {
					out[o ++] = (byte) ((c >> 6) | 0xc0);
					out[o ++] = (byte) ((c & 0x3f) | 0x80);
				} else if (c < 0x10000) {
					out[o ++] = (byte) ((c >> 12) | 0xe0);
					out[o ++] = (byte) (((c >> 6) & 0x3f) | 0x80);
					out[o ++] = (byte) ((c & 0x3f) | 0x80);
				} else {
					out[o ++] = (byte) ((c >> 18) | 0xf0);
					out[o ++] = (byte) (((c >> 12) & 0x3f) | 0x80);
					out[o ++] = (byte) (((c >> 6) & 0x3f) | 0x80);
					out[o ++] = (byte) ((c & 0x3f) | 0x80);
				}
			}
			ret = o - oOff;
		} catch(Exception e) {
		}
		return ret;
	}
	
	/**
	 * UTF8文字列(char[])をバイナリ変換.
	 * @param value 対象の文字列(char[])を設定します.
	 * @param off 文字列(char[])のオフセット値を設定します.
	 * @param len 文字列(char[])の長さを設定します.
	 * @return byte[] 変換されたバイナリが返却されます.
	 */
	public static final byte[] toBinary(final char[] value,
		final int off, final int len) {
		if (value == null || len == 0) {
			return value == null ? null : new byte[0];
		}
		byte[] o = new byte[len << 2];
		int oLen = toBinary(o, 0, value, off, len);
		if(oLen == 0) {
			return new byte[0];
		}
		byte[] ret = new byte[oLen];
		System.arraycopy(o, 0, ret, 0, oLen);
		return ret;
	}
	
	/**
	 * UTF8文字列をバイナリ変換.
	 * @param out 受け取るバイナリ情報を設定します.
	 *            この値は len の４倍の長さを設定します.
	 * @param oOff 受け取るバイナリのオフセット値を設定します.
	 * @param value 対象の文字列を設定します.
	 * @param off 文字列のオフセット値を設定します.
	 * @param len 文字列の長さを設定します.
	 * @return int 変換したバイナリ長が返却されます.
	 */
	public static final int toBinary(final byte[] out, final int oOff,
		final String value, final int off, final int len) {
		if (value == null || len == 0) {
			return 0;
		}
		int c;
		int o = oOff;
		int ret = 0;
		try {
			for (int i = 0; i < len; i++) {
				c = (int) value.charAt(off + i);
	
				// サロゲートペア処理.
				if (c >= 0xd800 && c <= 0xdbff) {
					c = 0x10000 + (((c - 0xd800) << 10) |
						((int) value.charAt(off + i + 1) - 0xdc00));
					i++;
				}
	
				if ((c & 0xffffff80) == 0) {
					out[o ++] = (byte)c;
				} else if (c < 0x800) {
					out[o ++] = (byte) ((c >> 6) | 0xc0);
					out[o ++] = (byte) ((c & 0x3f) | 0x80);
				} else if (c < 0x10000) {
					out[o ++] = (byte) ((c >> 12) | 0xe0);
					out[o ++] = (byte) (((c >> 6) & 0x3f) | 0x80);
					out[o ++] = (byte) ((c & 0x3f) | 0x80);
				} else {
					out[o ++] = (byte) ((c >> 18) | 0xf0);
					out[o ++] = (byte) (((c >> 12) & 0x3f) | 0x80);
					out[o ++] = (byte) (((c >> 6) & 0x3f) | 0x80);
					out[o ++] = (byte) ((c & 0x3f) | 0x80);
				}
			}
			ret = o - oOff;
		} catch(Exception e) {
		}
		return ret;
	}
	
	/**
	 * UTF8文字列をバイナリ変換.
	 * @param value 対象の文字列を設定します.
	 * @param off 文字列のオフセット値を設定します.
	 * @param len 文字列の長さを設定します.
	 * @return byte[] 変換されたバイナリが返却されます.
	 */
	public static final byte[] toBinary(final String value,
		final int off, final int len) {
		if (value == null || len == 0) {
			return value == null ? null : new byte[0];
		}
		byte[] o = new byte[len << 2];
		int oLen = toBinary(o, 0, value, off, len);
		if(oLen == 0) {
			return new byte[0];
		}
		byte[] ret = new byte[oLen];
		System.arraycopy(o, 0, ret, 0, oLen);
		return ret;
	}
	
	/**
	 * バイナリをUTF8文字列(char[])に変換.
	 * 
	 * @param out 変換先の文字列(char[])を設定します.
	 *            この値はlenの半分の長さと同じ長さを設定します.
	 * @param oOffset 変換先の文字列(char[])のオフセット値を設定します.
	 * @param value 対象のバイナリを設定します.
	 * @param offset オフセット値を設定します.
	 * @param length バイナリの長さを設定します.
	 * @return int 変換された文字列(char[])の長さが返却されます.
	 */
	public static final int toCharArray(final char[] out, final int oOffset,
		final byte[] value, final int offset, final int length) {
		if (length == 0) {
			return 0;
		}
		int c, n;
		int o = oOffset;
		int off = offset;
		int ret = 0;
		try {
			for (int i = 0; i < length; i++) {
				if (((c = (int) (value[off] & 0x000000ff)) & 0x80) == 0) {
					n = (int) (c & 255);
					off += 1;
				} else if ((c >> 5) == 0x06) {
					n = (int) (((c & 0x1f) << 6) | (value[off + 1] & 0x3f));
					off += 2;
					i += 1;
				} else if ((c >> 4) == 0x0e) {
					n = (int) (((c & 0x0f) << 12)
						| (((value[off + 1]) & 0x3f) << 6) | ((value[off + 2]) & 0x3f));
					off += 3;
					i += 2;
				} else {
					n = (int) (((c & 0x07) << 18)
						| (((value[off + 1]) & 0x3f) << 12)
						| (((value[off + 2]) & 0x3f) << 6) | (value[off + 3]) & 0x3f);
					off += 4;
					i += 3;
				}

				// サロゲートペア.
				if ((n & 0xffff0000) != 0) {
					n -= 0x10000;
					out[o ++] = (char) (0xd800 | (n >> 10));
					out[o ++] = (char) (0xdc00 | (n & 0x3ff));
				// サロゲートペア以外.
				} else {
					out[o ++] = (char) n;
				}
			}
			ret = o - oOffset;
		} catch(Exception e) {
		}
		return ret;
	}
	
	/**
	 * バイナリをUTF8文字列(char[])に変換.
	 * 
	 * @param value 対象のバイナリを設定します.
	 * @param offset オフセット値を設定します.
	 * @param length バイナリの長さを設定します.
	 * @return char[] 文字列(char[])が返却されます.
	 */
	public static final char[] toCharArray(final byte[] value, final int offset, final int length) {
		if (value == null || length == 0) {
			return value == null ? null : new char[0];
		}
		char[] o = new char[length >> 1];
		int oLen = toCharArray(o, 0, value, offset, length);
		if(oLen == 0) {
			return null;
		}
		char[] ret = new char[oLen];
		System.arraycopy(o, 0, ret, 0, oLen);
		return ret;
	}
	
	/**
	 * バイナリをUTF8文字列に変換.
	 * 
	 * @param out 変換先の文字列(char[])を設定します.
	 *            この値はlenの半分の長さと同じ長さを設定します.
	 * @param oOffset 変換先の文字列(char[])のオフセット値を設定します.
	 * @param value 対象のバイナリを設定します.
	 * @param offset オフセット値を設定します.
	 * @param length バイナリの長さを設定します.
	 * @return int 変換された文字列(char[])の長さが返却されます.
	 */
	public static final String toString(final byte[] value, final int offset, final int length) {
		if (value == null || length == 0) {
			return value == null ? null : "";
		}
		char[] o = new char[length >> 1];
		int oLen = toCharArray(o, 0, value, offset, length);
		if(oLen == 0) {
			return "";
		}
		return new String(o, 0, oLen);
	}
}
