package quina.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Collection;
import java.util.List;

/**
 * 文字列ユーティリティー.
 */
public class StringUtil {
	private StringUtil() {}

	private static final byte[] CHECK_CHAR = Alphabet.CHECK_CHAR;

	/**
	 * 文字列変換.
	 *
	 * @param n 変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final String parseString(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof String) {
			return (String) o;
		}
		return String.valueOf(o);
	}

	/**
	 * 文字情報の置き換え.
	 *
	 * @param src 置き換え元の文字列を設定します.
	 * @param s   置き換え文字条件を設定します.
	 * @param d   置き換え先の文字条件を設定します.
	 * @return String 文字列が返却されます.
	 */
	public static final String changeString(String src, String s, String d) {
		return changeString(src, 0, src.length(), s, d);
	}

	/**
	 * 文字情報の置き換え.
	 *
	 * @param src 置き換え元の文字列を設定します.
	 * @param off 置き換え元文字のオフセット値を設定します.
	 * @param len 置き換え元文字の長さを設定します.
	 * @param s   置き換え文字条件を設定します.
	 * @param d   置き換え先の文字条件を設定します.
	 * @return String 文字列が返却されます.
	 */
	public static final String changeString(String src, int off, int len, String s, String d) {
		int j, k;
		char t = s.charAt(0);
		int lenS = s.length();
		StringBuilder buf = new StringBuilder(len);
		for (int i = off; i < len; i++) {
			if (src.charAt(i) == t) {
				j = i;
				k = 0;
				while (++k < lenS && ++j < len && src.charAt(j) == s.charAt(k))
					;
				if (k >= lenS) {
					buf.append(d);
					i += (lenS - 1);
				} else {
					buf.append(t);
				}
			} else {
				buf.append(src.charAt(i));
			}
		}
		return buf.toString();
	}

	/**
	 * 対象文字列が存在するかチェック.
	 *
	 * @param v 対象の情報を設定します.
	 * @return boolean [true]の場合、文字列が存在します.
	 */
	@SuppressWarnings("rawtypes")
	public static final boolean useString(Object v) {
		if (v == null) {
			return false;
		}
		if (v instanceof CharSequence) {
			CharSequence cs = (CharSequence) v;
			if (cs.length() > 0) {
				int len = cs.length();
				for (int i = 0; i < len; i++) {
					if (CHECK_CHAR[cs.charAt(i)] == 1) {
						continue;
					}
					return true;
				}
			}
			return false;
		} else if (v instanceof Collection) {
			return !((Collection) v).isEmpty();
		}
		return true;
	}

	/**
	 * 前後のスペース等を取り除く.
	 *
	 * @param string 対象の文字列を設定します.
	 * @return String 文字列が返されます.
	 */
	public static final String trim(String string) {
		int s = -1;
		int e = -1;
		int len = string.length();
		boolean sFlg = false;
		for (int i = 0; i < len; i++) {
			char c = string.charAt(i);
			if (c != ' ' && c != '　' && c != '\r' && c != '\n' && c != '\t') {
				s = i;
				break;
			}
			sFlg = true;
		}
		if (sFlg && s == -1) {
			return "";
		}
		boolean eFlg = false;
		for (int i = len - 1; i >= 0; i--) {
			char c = string.charAt(i);
			if (c != ' ' && c != '　' && c != '\r' && c != '\n' && c != '\t') {
				e = i;
				break;
			}
			eFlg = true;
		}
		if (sFlg == true && eFlg == true) {
			return string.substring(s, e + 1);
		} else if (sFlg == true) {
			return string.substring(s);
		} else if (eFlg == true) {
			return string.substring(0, e + 1);
		}
		return string;
	}

	/**
	 * URLデコード.
	 *
	 * @param info    変換対象の条件を設定します.
	 * @param charset 対象のキャラクタセットを設定します.
	 * @return 変換された情報が返されます.
	 */
	public static final String urlDecode(String info, String charset) {
		int len;
		if (info == null || (len = info.length()) <= 0) {
			return "";
		}
		if (charset == null || charset.length() <= 0) {
			charset = "utf-8";
		}
		char c;
		byte[] bin = new byte[len];
		int j = 0;
		for (int i = 0; i < len; i++) {
			c = info.charAt(i);
			if (c == '%') {
				bin[j] = (byte) ((hexChar(info.charAt(i + 1)) << 4) | hexChar(info.charAt(i + 2)));
				i += 2;
				j++;
			} else if (c == '+') {
				bin[j] = (byte) ' ';
				j++;
			} else {
				bin[j] = (byte) c;
				j++;
			}
		}
		try {
			return new String(bin, 0, j, charset);
		} catch(Exception e) {
			throw new StringException(e);
		}
	}

	/**
	 * URLエンコード.
	 *
	 * @param info    変換対象の条件を設定します.
	 * @param charset 対象のキャラクタセットを設定します.
	 * @return 変換された情報が返されます.
	 */
	public static final String urlEncode(String info, String charset) {
		int len;
		if (info == null || (len = info.length()) <= 0) {
			return "";
		}
		if (charset == null || charset.length() <= 0) {
			charset = "utf-8";
		}
		CharsetEncoder cEnd = null;
		CharBuffer cBuf = null;
		ByteBuffer bBuf = null;
		StringBuilder buf = new StringBuilder(len << 1);
		int n, j;
		char c;
		for (int i = 0; i < len; i++) {
			c = info.charAt(i);
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '.') || (c == '-')
					|| (c == '_')) {
				buf.append(c);
			} else if (c == ' ') {
				// buf.append( "+" ) ;
				buf.append("%20");
			} else {
				if (cEnd == null) {
					cEnd = Charset.forName(charset).newEncoder();
					cBuf = CharBuffer.allocate(1);
					bBuf = ByteBuffer.allocate(4);
				} else {
					bBuf.clear();
					cBuf.clear();
				}
				cBuf.put(c);
				cBuf.flip();
				cEnd.encode(cBuf, bBuf, true);
				n = bBuf.position();
				for (j = 0; j < n; j++) {
					buf.append("%");
					toHex(buf, bBuf.get(j));
				}
			}
		}
		return buf.toString();
	}

	/** HexChar変換. **/
	private static final int hexChar(char c) {
		if (c >= '0' && c <= '9') {
			return ((int) (c - '0') & 0x0000000f);
		} else if (c >= 'A' && c <= 'F') {
			return ((int) (c - 'A') & 0x0000000f) + 10;
		} else if (c >= 'a' && c <= 'f') {
			return ((int) (c - 'a') & 0x0000000f) + 10;
		}
		throw new StringException("Not a hexadecimal value: " + c);
	}

	/** Hex文字列変換. **/
	private static final void toHex(StringBuilder buf, byte b) {
		switch ((b & 0x000000f0) >> 4) {
		case 0:
			buf.append("0");
			break;
		case 1:
			buf.append("1");
			break;
		case 2:
			buf.append("2");
			break;
		case 3:
			buf.append("3");
			break;
		case 4:
			buf.append("4");
			break;
		case 5:
			buf.append("5");
			break;
		case 6:
			buf.append("6");
			break;
		case 7:
			buf.append("7");
			break;
		case 8:
			buf.append("8");
			break;
		case 9:
			buf.append("9");
			break;
		case 10:
			buf.append("A");
			break;
		case 11:
			buf.append("B");
			break;
		case 12:
			buf.append("C");
			break;
		case 13:
			buf.append("D");
			break;
		case 14:
			buf.append("E");
			break;
		case 15:
			buf.append("F");
			break;
		}
		switch (b & 0x0000000f) {
		case 0:
			buf.append("0");
			break;
		case 1:
			buf.append("1");
			break;
		case 2:
			buf.append("2");
			break;
		case 3:
			buf.append("3");
			break;
		case 4:
			buf.append("4");
			break;
		case 5:
			buf.append("5");
			break;
		case 6:
			buf.append("6");
			break;
		case 7:
			buf.append("7");
			break;
		case 8:
			buf.append("8");
			break;
		case 9:
			buf.append("9");
			break;
		case 10:
			buf.append("A");
			break;
		case 11:
			buf.append("B");
			break;
		case 12:
			buf.append("C");
			break;
		case 13:
			buf.append("D");
			break;
		case 14:
			buf.append("E");
			break;
		case 15:
			buf.append("F");
			break;
		}
	}

	/**
	 * 文字列１６進数を数値変換.
	 *
	 * @param s 対象の文字列を設定します.
	 * @return int 変換された数値が返されます.
	 */
	public static final int parseHexInt(String s) {
		int len = s.length();
		int ret = 0;
		int n = 0;
		final char[] mM = Alphabet.CHECK_ALPHABET;
		for (int i = len - 1; i >= 0; i--) {
			switch (mM[s.charAt(i)]) {
			case '0':
				break;
			case '1':
				ret |= 1 << n;
				break;
			case '2':
				ret |= 2 << n;
				break;
			case '3':
				ret |= 3 << n;
				break;
			case '4':
				ret |= 4 << n;
				break;
			case '5':
				ret |= 5 << n;
				break;
			case '6':
				ret |= 6 << n;
				break;
			case '7':
				ret |= 7 << n;
				break;
			case '8':
				ret |= 8 << n;
				break;
			case '9':
				ret |= 9 << n;
				break;
			case 'a':
				ret |= 10 << n;
				break;
			case 'b':
				ret |= 11 << n;
				break;
			case 'c':
				ret |= 12 << n;
				break;
			case 'd':
				ret |= 13 << n;
				break;
			case 'e':
				ret |= 14 << n;
				break;
			case 'f':
				ret |= 15 << n;
				break;
			default:
				throw new StringException("Not a hexadecimal value: " + s);
			}
			n += 4;
		}
		return ret;
	}

	/**
	 * チェック情報単位で情報を区切ります.
	 *
	 * @param out   区切られた情報が格納されます.
	 * @param mode  区切られた時の文字列が無い場合に、無視するかチェックします. [true]の場合は、無視しません.
	 *              [false]の場合は、無視します.
	 * @param str   区切り対象の情報を設置します.
	 * @param check 区切り対象の文字情報をセットします. 区切り対象文字を複数設定する事により、それらに対応した区切りとなります.
	 */
	public static final void cutString(List<String> out, boolean mode, String str, String check) {
		int i, j;
		int len;
		int lenJ;
		int s = -1;
		char strCode;
		char[] checkCode = null;
		String tmp = null;
		if (out == null || str == null || (len = str.length()) <= 0 || check == null || check.length() <= 0) {
			throw new IllegalArgumentException();
		}
		out.clear();
		lenJ = check.length();
		checkCode = new char[lenJ];
		check.getChars(0, lenJ, checkCode, 0);
		if (lenJ == 1) {
			for (i = 0, s = -1; i < len; i++) {
				strCode = str.charAt(i);
				s = (s == -1) ? i : s;
				if (strCode == checkCode[0]) {
					if (s < i) {
						tmp = str.substring(s, i);
						out.add(tmp);
						tmp = null;
						s = -1;
					} else if (mode == true) {
						out.add("");
						s = -1;
					} else {
						s = -1;
					}
				}
			}
		} else {
			for (i = 0, s = -1; i < len; i++) {
				strCode = str.charAt(i);
				s = (s == -1) ? i : s;
				for (j = 0; j < lenJ; j++) {
					if (strCode == checkCode[j]) {
						if (s < i) {
							tmp = str.substring(s, i);
							out.add(tmp);
							tmp = null;
							s = -1;
						} else if (mode == true) {
							out.add("");
							s = -1;
						} else {
							s = -1;
						}
						break;
					}
				}
			}
		}
		if (s != -1) {
			tmp = str.substring(s, len);
			out.add(tmp);
			tmp = null;
		}
		checkCode = null;
		tmp = null;
	}

	/**
	 * チェック情報単位で情報を区切ります。
	 *
	 * @param out     区切られた情報が格納されます.
	 * @param cote    コーテーション対応であるか設定します. [true]を設定した場合、各コーテーション ( ",' ) で囲った情報内は
	 *                区切り文字と判別しません. [false]を設定した場合、コーテーション対応を行いません.
	 * @param coteFlg コーテーションが入っている場合に、コーテーションを範囲に含むか否かを 設定します.
	 *                [true]を設定した場合、コーテーション情報も範囲に含みます.
	 *                [false]を設定した場合、コーテーション情報を範囲としません.
	 * @param str     区切り対象の情報を設置します.
	 * @param check   区切り対象の文字情報をセットします. 区切り対象文字を複数設定する事により、それらに対応した区切りとなります.
	 */
	public static final void cutString(List<String> out, boolean cote, boolean coteFlg, String str, String check) {
		int i, j;
		int len;
		int lenJ;
		int s = -1;
		char coteChr;
		char nowChr;
		char strCode;
		char[] checkCode = null;
		String tmp = null;
		if (cote == false) {
			cutString(out, false, str, check);
		} else {
			if (out == null || str == null || (len = str.length()) <= 0 || check == null || check.length() <= 0) {
				throw new IllegalArgumentException();
			}
			out.clear();
			lenJ = check.length();
			checkCode = new char[lenJ];
			check.getChars(0, lenJ, checkCode, 0);
			if (lenJ == 1) {
				int befCode = -1;
				boolean yenFlag = false;
				for (i = 0, s = -1, coteChr = 0; i < len; i++) {
					strCode = str.charAt(i);
					nowChr = strCode;
					s = (s == -1) ? i : s;
					if (coteChr == 0) {
						if (nowChr == '\'' || nowChr == '\"') {
							coteChr = nowChr;
							if (s < i) {
								tmp = str.substring(s, i);
								out.add(tmp);
								tmp = null;
								s = -1;
							} else {
								s = -1;
							}
						} else if (strCode == checkCode[0]) {
							if (s < i) {
								tmp = str.substring(s, i);
								out.add(tmp);
								tmp = null;
								s = -1;
							} else {
								s = -1;
							}
						}
					} else {
						if (befCode != '\\' && coteChr == nowChr) {
							yenFlag = false;
							coteChr = 0;
							if (s == i && coteFlg == true) {
								out.add(new StringBuilder().append(strCode).append(strCode).toString());
								s = -1;
							} else if (s < i) {
								if (coteFlg == true) {
									tmp = str.substring(s - 1, i + 1);
								} else {
									tmp = str.substring(s, i);
								}
								out.add(tmp);
								tmp = null;
								s = -1;
							} else {
								s = -1;
							}
						} else if (strCode == '\\' && befCode == '\\') {
							yenFlag = true;
						} else {
							yenFlag = false;
						}
					}
					if (yenFlag) {
						yenFlag = false;
						befCode = -1;
					} else {
						befCode = strCode;
					}
				}
			} else {
				int befCode = -1;
				boolean yenFlag = false;
				for (i = 0, s = -1, coteChr = 0; i < len; i++) {
					strCode = str.charAt(i);
					nowChr = strCode;
					s = (s == -1) ? i : s;
					if (coteChr == 0) {
						if (nowChr == '\'' || nowChr == '\"') {
							coteChr = nowChr;
							if (s < i) {
								tmp = str.substring(s, i);
								out.add(tmp);
								tmp = null;
								s = -1;
							} else {
								s = -1;
							}
						} else {
							for (j = 0; j < lenJ; j++) {
								if (strCode == checkCode[j]) {
									if (s < i) {
										tmp = str.substring(s, i);
										out.add(tmp);
										tmp = null;
										s = -1;
									} else {
										s = -1;
									}
									break;
								}
							}
						}
					} else {
						if (befCode != '\\' && coteChr == nowChr) {
							coteChr = 0;
							yenFlag = false;
							if (s == i && coteFlg == true) {
								out.add(new StringBuilder().append(strCode).append(strCode).toString());
								s = -1;
							} else if (s < i) {
								if (coteFlg == true) {
									tmp = str.substring(s - 1, i + 1);
								} else {
									tmp = str.substring(s, i);
								}

								out.add(tmp);
								tmp = null;
								s = -1;
							} else {
								s = -1;
							}
						} else if (strCode == '\\' && befCode == '\\') {
							yenFlag = true;
						} else {
							yenFlag = false;
						}
					}
					if (yenFlag) {
						yenFlag = false;
						befCode = -1;
					} else {
						befCode = strCode;
					}
				}
			}
			if (s != -1) {
				if (coteChr != 0 && coteFlg == true) {
					tmp = str.substring(s - 1, len) + (char) coteChr;
				} else {
					tmp = str.substring(s, len);
				}
				out.add(tmp);
				tmp = null;
			}
			checkCode = null;
			tmp = null;
		}
	}

	/**
	 * コーテーション内を検知しないIndexOf.
	 *
	 * @param base
	 *            検索元の情報を設定します.
	 * @param cc
	 *            チェック対象の内容を設定します.
	 * @return int 検索結果の内容が返されます.
	 */
	public static final int indexOfNoCote(final String base, final String cc) {
		return indexOfNoCote(base, cc, 0);
	}

	/**
	 * コーテーション内を検知しないIndexOf.
	 *
	 * @param base
	 *            検索元の情報を設定します.
	 * @param cc
	 *            チェック対象の内容を設定します.
	 * @param off
	 *            対象のオフセット値を設定します.
	 * @return int 検索結果の内容が返されます.
	 */
	public static final int indexOfNoCote(final String base, final String cc, final int off) {
		final int len = base.length();
		final char[] ck = cc.toCharArray();
		final int cLen = ck.length;
		char bef = 0;
		int cote = -1;
		boolean yenFlag = false;
		for (int i = off; i < len; i++) {
			char c = base.charAt(i);
			if (cote != -1) {
				if (bef != '\\' && c == cote) {
					yenFlag = false;
					cote = -1;
				} else if (c == '\\' && bef == '\\') {
					yenFlag = true;
				} else {
					yenFlag = false;
				}
			} else if (bef != '\\' && (c == '\'' || c == '\"')) {
				cote = c;
			} else if (c == ck[0]) {
				boolean res = true;
				for (int j = 1; j < cLen; j++) {
					if (i + j >= len || ck[j] != base.charAt(i + j)) {
						res = false;
						break;
					}
				}
				if (res == true) {
					return i;
				}
			}
			if (yenFlag) {
				yenFlag = false;
				bef = 0;
			} else {
				bef = c;
			}
		}
		return -1;
	}
}
