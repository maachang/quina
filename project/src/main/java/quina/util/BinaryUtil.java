package quina.util;

/**
 * バイナリ関連処理.
 */
public class BinaryUtil {
	private BinaryUtil() {
	}

	/** アルファベットの半角全角変換値. **/
	protected static final byte[] CHECK_ALPHABET = new byte[255];
	static {
		final int len = CHECK_ALPHABET.length;
		for (int i = 0; i < len; i++) {
			CHECK_ALPHABET[i] = (byte) i;
		}
		final int alpha = (int) ('z' - 'a') + 1;
		final int code = (int) 'a';
		for (int i = 0; i < alpha; i++) {
			CHECK_ALPHABET[i + code] = (byte) (code + i);
		}
		final int target = (int) 'A';
		for (int i = 0; i < alpha; i++) {
			CHECK_ALPHABET[i + target] = (byte) (code + i);
		}
	}

	/**
	 * アルファベットの半角全角変換値を取得します.
	 * @return
	 */
	public static final byte[] getCheckAlphabet() {
		return CHECK_ALPHABET;
	}

	/**
	 * 英字の大文字小文字を区別せずにチェック.
	 *
	 * @param src  比較元文字を設定します.
	 * @param dest 比較先文字を設定します.
	 * @return boolean [true]の場合、一致します.
	 */
	public static final boolean eqEng(byte[] src, byte[] dest) {
		if (src == null || dest == null) {
			return false;
		}
		int len = src.length;
		if (len != dest.length) {
			return false;
		}
		for (int i = 0; i < len; i++) {
			if(CHECK_ALPHABET[src[i] & 0x00ff] != CHECK_ALPHABET[dest[i] & 0xff]) {
				return false;
			}
		}
		return true;
	}


	/**
	 * 英字の大文字小文字を区別しない、バイトチェック.
	 *
	 * @param s 比較の文字を設定します.
	 * @param d 比較の文字を設定します.
	 * @return boolean [true]の場合、一致します.
	 */
	public static final boolean oneEng(byte s, byte d) {
		return CHECK_ALPHABET[s & 0x00ff] == CHECK_ALPHABET[d & 0x00ff];
	}

	/**
	 * 大なり小なりの判別を行います.
	 * @param s 比較元のバイナリを設定します.
	 * @param d 比較先のバイナリを設定します.
	 * @return int 戻り値が返却されます.
	 *             [マイナス」の場合は比較先[d]が大きいです.
	 *             [プラス」の場合は比較元[s]が大きいです.
	 *             [イコール]の場合は同じです.
	 */
	public static final int comparableEng(byte[] s, byte[] d) {
		int a, b;
		int len = s.length;
		len = (len > d.length) ? d.length : len;
		for(int i = 0; i < len; i ++) {
			a = CHECK_ALPHABET[s[i] & 0x00ff] & 0x00ff;
			b = CHECK_ALPHABET[d[i] & 0x00ff] & 0x00ff;
			if(a < b) {
				return -1;
			} else if(a > b) {
				return 1;
			}
		}
		return s.length - d.length;
	}

	/**
	 * 大なり小なりの判別を行います.
	 * @param s 比較元のバイナリを設定します.
	 * @param ss 比較元のバイナリ開始ポジションを設定します.
	 * @param se 比較元のバイナリ終了ポジションを設定します.
	 * @param d 比較先のバイナリを設定します.
	 * @param ds 比較先のバイナリ開始ポジションを設定します.
	 * @param de 比較先のバイナリ終了ポジションを設定します.
	 * @return int 戻り値が返却されます.
	 *             [マイナス」の場合は比較先[d]が大きいです.
	 *             [プラス」の場合は比較元[s]が大きいです.
	 *             [イコール]の場合は同じです.
	 */
	public static final int comparableEng(byte[] s, int ss, int se,
		byte[] d, int ds, int de) {
		int a, b, len;
		final int slen = se - ss;
		final int dlen = de - ds;
		len = (slen > dlen) ? dlen : slen;
		for(int i = 0; i < len; i ++) {
			a = CHECK_ALPHABET[s[ss + i] & 0x00ff] & 0x00ff;
			b = CHECK_ALPHABET[d[ds + i] & 0x00ff] & 0x00ff;
			if(a < b) {
				return -1;
			} else if(a > b) {
				return 1;
			}
		}
		return slen - dlen;
	}


	/**
	 * 大なり小なりの判別を行います.
	 * @param s 比較元のbyteを設定します.
	 * @param d 比較先のbyteを設定します.
	 * @return int 戻り値が返却されます.
	 *             [マイナス」の場合は比較先[d]が大きいです.
	 *             [プラス」の場合は比較元[s]が大きいです.
	 *             [イコール]の場合は同じです.
	 */
	public static final int comparableEng(byte s, byte d) {
		return (CHECK_ALPHABET[d & 0x00ff] & 0x00ff)
			- (CHECK_ALPHABET[s & 0x00ff] & 0x00ff);
	}

	/**
	 * バイナリindexOf.
	 *
	 * @param buf  設定対象のバイナリ情報を設定します.
	 * @param chk  チェック対象のバイナリ情報を設定します.
	 * @param off  設定対象のオフセット値を設定します.
	 * @param vLen 対象のバイナリ長を設定します.
	 * @return int マッチする位置が返却されます. [-1]の場合は情報は存在しません.
	 */
	public static final int indexOf(final byte[] buf, byte chk, final int off, int vLen) {
		for (int i = off; i < vLen; i++) {
			if (chk != buf[i]) {
				while (++i < vLen && chk != buf[i])
					;
				if (vLen != i) {
					return i;
				}
			} else {
				return i;
			}
		}
		return -1;
	}

	/**
	 * バイナリindexOf.
	 *
	 * @param buf  設定対象のバイナリ情報を設定します.
	 * @param chk  チェック対象のバイナリ情報を設定します.
	 * @param off  設定対象のオフセット値を設定します.
	 * @param vLen 対象のバイナリ長を設定します.
	 * @return int マッチする位置が返却されます. [-1]の場合は情報は存在しません.
	 */
	public static final int indexOf(final byte[] buf, final byte[] chk, final int off, int vLen) {
		final int len = chk.length;
		final byte first = chk[0];
		// 単数バイナリ検索.
		if (len == 1) {
			for (int i = off; i < vLen; i++) {
				if (first != buf[i]) {
					while (++i < vLen && first != buf[i])
						;
					if (vLen != i) {
						return i;
					}
				} else {
					return i;
				}
			}
		}
		// 複数バイナリ検索.
		else {
			vLen = vLen - (len - 1);
			int j, k, next;
			for (int i = off; i < vLen; i++) {
				if (first != buf[i]) {
					while (++i < vLen && buf[i] != first)
						;
				}
				if (i < vLen) {
					for (next = i + len, j = i + 1, k = 1;
						j < next && buf[j] == chk[k]; j++, k++)
						;
					if (j == next) {
						return i;
					}
				}
			}
		}
		return -1;
	}

	/**
	 * バイナリlastIndexOf.
	 *
	 * @param buf 設定対象のバイナリ情報を設定します.
	 * @param chk チェック対象のバイナリ情報を設定します.
	 * @param off 設定対象のオフセット値を設定します.
	 * @return int マッチする位置が返却されます. [-1]の場合は情報は存在しません.
	 */
	public static final int lastIndexOf(final byte[] buf, final byte[] chk, int off) {
		final int len = chk.length - 1;
		// 単数バイナリ検索.
		if (len == 0) {
			final byte last = chk[0];
			for (int i = off; i >= 0; i--) {
				if (last != buf[i]) {
					while (--i >= 0 && buf[i] != last)
						;
					if (off != i) {
						return i;
					}
				} else {
					return i;
				}
			}
		}
		// 複数バイナリ検索.
		else {
			int j, k, next;
			final byte last = chk[len];
			for (int i = off; i >= 0; i--) {
				if (last != buf[i]) {
					while (--i >= 0 && buf[i] != last)
						;
				}
				if (i >= len) {
					for (next = i - (len + 1), j = i - 1, k = len - 1;
						j > next && buf[j] == chk[k]; j--, k--)
						;
					if (j == next) {
						return j + 1;
					}
				}
			}
		}
		return -1;
	}

	/**
	 * 英字の大文字小文字を区別しない、バイナリindexOf.
	 *
	 * @param buf  設定対象のバイナリ情報を設定します.
	 * @param chk  チェック対象のバイナリ情報を設定します.
	 * @param off  設定対象のオフセット値を設定します.
	 * @param vLen 対象のバイナリ長を設定します.
	 * @return int マッチする位置が返却されます. [-1]の場合は情報は存在しません.
	 */
	public static final int indexOfEng(final byte[] buf, final byte[] chk, final int off, int vLen) {
		final int len = chk.length;
		// 単数バイナリ検索.
		if (len == 1) {
			final byte first = chk[0];
			for (int i = off; i < vLen; i++) {
				if (!oneEng(first, buf[i])) {
					while (++i < vLen && !oneEng(first, buf[i]))
						;
					if (vLen != i) {
						return i;
					}
				} else {
					return i;
				}
			}
		}
		// 複数バイナリ検索.
		else {
			final byte first = chk[0];
			vLen = vLen - (len - 1);
			int j, k, next;
			for (int i = off; i < vLen; i++) {
				if (!oneEng(first, buf[i])) {
					while (++i < vLen && !oneEng(first, buf[i]))
						;
				}
				if (i < vLen) {
					for (next = i + len, j = i + 1, k = 1;
						j < next && oneEng(buf[j], chk[k]); j++, k++)
						;
					if (j == next) {
						return i;
					}
				}
			}
		}
		return -1;
	}

	/**
	 * 英字の大文字小文字を区別しない、バイナリlastIndexOf.
	 *
	 * @param buf 設定対象のバイナリ情報を設定します.
	 * @param chk チェック対象のバイナリ情報を設定します.
	 * @param off 設定対象のオフセット値を設定します.
	 * @return int マッチする位置が返却されます. [-1]の場合は情報は存在しません.
	 */
	public static final int lastIndexOfEng(final byte[] buf, final byte[] chk, int off) {
		final int len = chk.length - 1;
		// 単数バイナリ検索.
		if (len == 0) {
			final byte last = chk[0];
			for (int i = off; i >= 0; i--) {
				if (!oneEng(last, buf[i])) {
					while (--i >= 0 && !oneEng(last, buf[i]))
						;
					if (off != i) {
						return i;
					}
				} else {
					return i;
				}
			}
		}
		// 複数バイナリ検索.
		else {
			int j, k, next;
			final byte last = chk[len];
			for (int i = off; i >= 0; i--) {
				if (!oneEng(last, buf[i])) {
					while (--i >= 0 && !oneEng(last, buf[i]))
						;
				}
				if (i >= len) {
					for (next = i - (len + 1), j = i - 1, k = len - 1;
						j > next && oneEng(buf[j], chk[k]); j--, k--)
						;
					if (j == next) {
						return j + 1;
					}
				}
			}
		}
		return -1;
	}

	/**
	 * バイナリに対して対象ポジションを開始位置として合う条件を検索.
	 * @param body
	 * @param positions
	 * @param target
	 * @return
	 */
	public static final int search(byte[] body, int[] positions, byte[] target) {
		int p, i, j;
		final int bodyLen = body.length;
		final int targetLen = target.length;
		final int len = positions.length;
		for(i = 0; i < len; i ++) {
			if((p = positions[i]) + targetLen < bodyLen) {
				for(j = 0; j < targetLen; j ++) {
					if(body[p + j] != target[j]) {
						break;
					}
				}
				return p;
			}
		}
		return -1;
	}

	/**
	 * バイナリに対して対象ポジションを開始位置として合う条件を検索.
	 * @param body
	 * @param positions
	 * @param target
	 * @return
	 */
	public static final int searchEng(byte[] body, int[] positions, byte[] target) {
		int p, i, j;
		final int bodyLen = body.length;
		final int targetLen = target.length;
		final int len = positions.length;
		for(i = 0; i < len; i ++) {
			if((p = positions[i]) + targetLen < bodyLen) {
				for(j = 0; j < targetLen; j ++) {
					if(!oneEng(body[p + j], target[j])) {
						break;
					}
				}
				return p;
			}
		}
		return -1;
	}

	// trim除外文字列.
	private static final byte[] TRIM_TARGETS = new byte[] {
		(byte)' ', (byte)'\t', (byte)'\r', (byte)'\n'
	};

	/**
	 * バイナリを文字列として前後のスペース等の内容を削除します.
	 * @param bin バイナリを設定します.
	 * @return byte[] トリムされた情報が返却されます.
	 */
	public static final byte[] trim(byte[] bin) {
		return trim(bin, 0, bin.length);
	}

	/**
	 * バイナリを文字列として前後のスペース等の内容を削除します.
	 * @param bin バイナリを設定します.
	 * @param start trim開始位置を設定します.
	 * @param end trim終了位置を設定します.
	 * @return byte[] トリムされた情報が返却されます.
	 */
	public static final byte[] trim(byte[] bin, int start, int end) {
		final long pos = trimPos(bin, start, end);
		start = getLow(pos);
		end = getHigh(pos);
		if(start == 0 && end == bin.length) {
			return bin;
		}
		final int len = end - start;
		final byte[] ret = new byte[len];
		System.arraycopy(bin, start, ret, 0, len);
		return ret;
	}

	/**
	 * バイナリを文字列として前後のスペース等の内容を削除された位置が返却されます.
	 * @param bin バイナリを設定します.
	 * @param start trim開始位置を設定します.
	 * @param end trim終了位置を設定します.
	 * @return long 下位３２ビットがtrim開始位置, 上位３２ビットがtrim終了位置が返却されます.
	 *   内容をバラす場合は、以下のように行います.
	 *     long pos = trimPos(bin, start, end);
	 *     start = BinaryUtil.getLow(pos);
	 *     end = BinaryUtil.getHigh(pos);
	 */
	public static final long trimPos(byte[] bin, int start, int end) {
		byte b;
		int i, j, s, e, o, p;
		final byte[] tb = TRIM_TARGETS;
		final int trimBinLen = tb.length;
		for(i = start, p = start; i < end; i ++) {
			b = bin[i];
			o = -1;
			for(j = 0; j < trimBinLen; j ++) {
				if(tb[j] == b) {
					o = i;
					p = o + 1;
					break;
				}
			}
			if(o == -1) {
				break;
			}
		}
		s = p;
		for(i = end - 1, p = end; i >= start; i --) {
			b = bin[i];
			o = -1;
			for(j = 0; j < trimBinLen; j ++) {
				if(tb[j] == b) {
					o = i;
					p = o;
					break;
				}
			}
			if(o == -1) {
				break;
			}
		}
		e = p;
		return (long)(((e & 0x00000000ffffffffL) << 32L)
			| (s & 0x00000000ffffffffL));
	}

	/**
	 * 64bit整数から、下位３２ビット数値を取得.
	 * @param n
	 * @return
	 */
	public static final int getLow(long n) {
		return (int)(n & 0x00000000ffffffffL);
	}

	/**
	 * 64bit整数から、上位３２ビット数値を取得.
	 * @param n
	 * @return
	 */
	public static final int getHigh(long n) {
		return (int)((n & 0xffffffff00000000L) >> 32L);
	}

	/**
	 * 小文字変換.
	 * @param v 対象のバイナリを設定します.
	 * @return byte[] 変換された結果が返却されます.
	 */
	public static final byte[] toLowerCase(byte[] v) {
		final int len = v.length;
		for(int i = 0; i < len; i ++) {
			v[i] = CHECK_ALPHABET[v[i] & 0x00ff];
		}
		return v;
	}

	/**
	 * バイナリ情報の内容を出力.
	 *
	 * @param src 対象のバイナリを設定します.
	 * @return String 文字列が返却されます.
	 */
	public static final String toBinaryString(byte[] src) {
		return toBinaryString(src, 0, src.length);
	}

	/**
	 * バイナリ情報の内容を出力.
	 *
	 * @param src 対象のバイナリを設定します.
	 * @param off 対象のオフセットを設定します.
	 * @param len 対象の長さを設定します.
	 * @return String 文字列が返却されます.
	 */
	public static final String toBinaryString(byte[] src, int off, int len) {
		String n;
		StringBuilder buf = new StringBuilder();
		buf.append("\n +0 +1 +2 +3 +4 +5 +6 +7    +8 +9 +A +B +C +D +E +F ");
		for (int i = 0; i < len; i++) {
			if ((i & 0x0f) == 0) {
				buf.append("\n ");
			} else if ((i & 0x0f) == 8) {
				buf.append("   ");
			} else {
				buf.append(" ");
			}
			n = Integer.toHexString(src[off + i] & 0x000000ff);
			buf.append("00".substring(n.length())).append(n);
		}
		buf.append("\n");
		return buf.toString();
	}

	/**
	 * バイナリをアスキー変換.
	 * @param b バイナリを設定します.
	 * @return 文字列が返却されます.
	 */
	public static final String toAscii(byte[] b) {
		return toAscii(b, 0, b.length);
	}

	/**
	 * バイナリをアスキー変換.
	 * @param b バイナリを設定します.
	 * @param off オフセット値を設定します.
	 * @param len 長さを設定します.
	 * @return 文字列が返却されます.
	 */
	public static final String toAscii(byte[] b, int off, int len) {
		char[] n = new char[len];
		for(int i = 0; i < len; i ++) {
			n[i] = (char)(b[off+i] & 0x00ff);
		}
		return new String(n);
	}

	/**
	 * アスキーからバイナリ変換.
	 * @param s アスキー文字列を設定します.
	 * @return バイナリが返却されます.
	 */
	public static final byte[] asciiToBinary(String s) {
		return asciiToBinary(s, 0, s.length());
	}

	/**
	 * アスキーからバイナリ変換.
	 * @param s アスキー文字列を設定します.
	 * @param off オフセット値を設定します.
	 * @param len 長さを設定します.
	 * @return バイナリが返却されます.
	 */
	public static final byte[] asciiToBinary(String s, int off, int len) {
		byte[] ret = new byte[len];
		for(int i = 0; i < len; i ++) {
			ret[i] = (byte)(s.charAt(i) & 0x00ff);
		}
		return ret;
	}

	/**
	 * 有効最大ビット長を取得.
	 *
	 * @param x 対象の数値を設定します.
	 * @return int 左ゼロビット数が返却されます.
	 */
	public static final int nlzs(int x) {
		if (x == 0) {
			return 0;
		}
		x |= (x >> 1);
		x |= (x >> 2);
		x |= (x >> 4);
		x |= (x >> 8);
		x |= (x >> 16);
		x = (x & 0x55555555) + (x >> 1 & 0x55555555);
		x = (x & 0x33333333) + (x >> 2 & 0x33333333);
		x = (x & 0x0f0f0f0f) + (x >> 4 & 0x0f0f0f0f);
		x = (x & 0x00ff00ff) + (x >> 8 & 0x00ff00ff);
		return (x & 0x0000ffff) + (x >> 16 & 0x0000ffff);
	}

	/**
	 * 有効最大ビット長を取得.
	 *
	 * @param x 対象の数値を設定します.
	 * @return int 左ゼロビット数が返却されます.
	 */
	public static final int nlzs(long x) {
		int xx = (int) ((x & 0xffffffff00000000L) >> 32L);
		if (nlzs(xx) == 0) {
			return nlzs((int) (x & 0x00000000ffffffff));
		}
		return nlzs(xx) + 32;
	}

	/**
	 * ビットサイズを取得.
	 *
	 * @param x 対象の数値を設定します.
	 * @return int ビット数が返却されます.
	 */
	public static final int bitMask(int x) {
		if (x <= 0) {
			return 0;
		}
		x |= (x >> 1);
		x |= (x >> 2);
		x |= (x >> 4);
		x |= (x >> 8);
		x |= (x >> 16);
		x = (x & 0x55555555) + (x >> 1 & 0x55555555);
		x = (x & 0x33333333) + (x >> 2 & 0x33333333);
		x = (x & 0x0f0f0f0f) + (x >> 4 & 0x0f0f0f0f);
		x = (x & 0x00ff00ff) + (x >> 8 & 0x00ff00ff);
		x = (x & 0x0000ffff) + (x >> 16 & 0x0000ffff);
		return 1 << (((x & 0x0000ffff) + (x >> 16 & 0x0000ffff)) - 1);
	}

	/**
	 * 有効最大バイト長を取得.
	 *
	 * @param x      対象の数値を設定します.
	 * @param addBit 追加ビット長を設定します.
	 * @return int 有効最大バイト長が返却されます.
	 */
	public static final int getMaxByte(int x) {
		return getMaxByte(x, 0);
	}

	/**
	 * 有効最大バイト長を取得.
	 *
	 * @param x      対象の数値を設定します.
	 * @param addBit 追加ビット長を設定します.
	 * @return int 有効最大バイト長が返却されます.
	 */
	public static final int getMaxByte(int x, int addBit) {
		x = nlzs(x) + addBit;
		return (x >> 3) + ((x & 1) | ((x >> 1) & 1) | ((x >> 2) & 1));
	}

	/**
	 * 有効最大バイト長を取得.
	 *
	 * @param x 対象の数値を設定します.
	 * @return int 有効最大バイト長が返却されます.
	 */
	public static final int getMaxByte(long x) {
		return getMaxByte(x, 0);
	}

	/**
	 * 有効最大バイト長を取得.
	 *
	 * @param xx     対象の数値を設定します.
	 * @param addBit 追加ビット長を設定します.
	 * @return int 有効最大バイト長が返却されます.
	 */
	public static final int getMaxByte(long xx, int addBit) {
		int x = nlzs(xx) + addBit;
		return (x >> 3) + ((x & 1) | ((x >> 1) & 1) | ((x >> 2) & 1));
	}

}
