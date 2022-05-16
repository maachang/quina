package quina.util;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
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
	 * String.trim() と違い、指定ポジションに対するtrimを行います.
	 * これによって、別途String.substring(...).trim()のような処理では
	 * なく、指定position(start, end)に対するtrimを行います.
	 * これによって、メモリ効率の良いtrimが実施出来ます.
	 * @param startEnd 開始位置、終了位置を設定します.
	 *                 int[0]: 開始位置, int[1]: 終了位置.
	 * @param string 対象の文字列を設定します.
	 * @return boolean trueの場合、前後のスペース等が取り除かれた場合
	 *                 その場合はstartEndパラメータが取り除かれた値と
	 *                 して、セットされます.
	 */
	public static final boolean trim(int[] startEnd, String string) {
		char c;
		int i, sPos, ePos, srPos, erPos;
		sPos = srPos = startEnd[0];
		ePos = erPos = startEnd[1];
		// 開始位置から、終了位置までチェック.
		for(i = sPos; i < ePos; i ++) {
			c = string.charAt(i);
			// trimしない文字列が見つかった場合.
			if (c != ' ' && c != '\r' && c != '\n' && c != '\t') {
				break;
			}
			srPos = i;
		}
		// trimしない文字列が存在しない場合.
		if(srPos + 1 == ePos) {
			startEnd[0] = sPos;
			startEnd[1] = sPos;
			return true;
		}
		// 終了位置から開始位置までチェック.
		for(i = ePos; i >= sPos; i --) {
			c = string.charAt(i);
			// trimしない文字列が見つかった場合.
			if (c != ' ' && c != '\r' && c != '\n' && c != '\t') {
				break;
			}
			erPos = i;
		}
		// trimの変化が無い場合.
		if(sPos == srPos && ePos == erPos) {
			return false;
		}
		// trimの変化がある場合.
		startEnd[0] = srPos;
		startEnd[1] = erPos;
		return true;
	}
	
	/**
	 * 指定位置の文字列が、クォーテーションが文字表現ものかチェック.
	 * 
	 * たとえば
	 * "a\"bcd\\\"ef\\\"ghi\"jkl"
	 * のように、文字列を表現する場合において￥マークを
	 * 設定すると、対象クォーテーションを文字列内で表現できます.
	 * だけど単純に ￥が前回文字列にあり、対象のクォーテーションで
	 * ある場合にそれが文字列だとした場合、以下の条件で正しく検出
	 * 出来ない問題があります.
	 * 
	 * "\\"
	 * 
	 * なので「前の条件が￥か」を調べるだけでなく、その￥が連続する
	 * 場合において「奇数」かをチェックするようにします.
	 * 
	 * @param src 対象の文字列を設定します.
	 * @param pos 対象の文字ポジションを設定します.
	 * @param srcQuotation チェックするクォーテーション文字列を設定
	 * @return true の場合 クォーテーションが文字列で定義されています.
	 */
	public static final boolean isStringQuotation(
		String src, int pos, char srcQuotation) {
		if(src.charAt(pos) != srcQuotation) {
			return true;
		}
		int yenCount = 0;
		for(int i = pos - 1; i >= 0; i --) {
			if(src.charAt(i) == '\\') {
				yenCount ++;
				continue;
			}
			break;
		}
		return (yenCount & 1) == 1;
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
			if ((c >= 'a' && c <= 'z') ||
					(c >= 'A' && c <= 'Z') ||
					(c >= '0' && c <= '9') ||
					(c == '.') || (c == '-') ||
					(c == '_')) {
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
	 * @param mode  区切られた時の文字列が無い場合に、無視するかチェックします.
	 *              [true]の場合は、無視しません.
	 *              [false]の場合は、無視します.
	 * @param str   区切り対象の情報を設置します.
	 * @param check 区切り対象の文字情報をセットします.
	 *              区切り対象文字を複数設定する事により、それらに対応した区切りとなります.
	 */
	public static final void cutString(
		List<String> out, boolean mode, String str, String check) {
		int i, j;
		int len;
		int lenJ;
		int s = -1;
		char strCode;
		char[] checkCode = null;
		String tmp = null;
		if (out == null || str == null || (len = str.length()) <= 0
			|| check == null || check.length() <= 0) {
			throw new IllegalArgumentException();
		}
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
	 * @param out      区切られた情報が格納されます.
	 * @param quote    コーテーション対応であるか設定します.
	 *                 [true]を設定した場合、各コーテーション ( ",' ) で囲った情報内は
	 *                 区切り文字と判別しません. [false]を設定した場合、コーテーション対応を行いません.
	 * @param quoteFlg コーテーションが入っている場合に、コーテーションを範囲に含むか否かを 設定します.
	 *                 [true]を設定した場合、コーテーション情報も範囲に含みます.
	 *                 [false]を設定した場合、コーテーション情報を範囲としません.
	 * @param str      区切り対象の情報を設置します.
	 * @param check    区切り対象の文字情報をセットします.
	 *                 区切り対象文字を複数設定する事により、それらに対応した区切りとなります.
	 */
	public static final void cutString(
		List<String> out, boolean quote, boolean quoteFlg,
		String str, String check) {
		int i, j;
		int len;
		int lenJ;
		int s = -1;
		char quoteChr;
		char nowChr;
		char strCode;
		char[] checkCode = null;
		String tmp = null;
		if (!quote) {
			cutString(out, false, str, check);
		} else {
			if (out == null || str == null || (len = str.length()) <= 0
				|| check == null || check.length() <= 0) {
				throw new IllegalArgumentException();
			}
			lenJ = check.length();
			checkCode = new char[lenJ];
			check.getChars(0, lenJ, checkCode, 0);
			if (lenJ == 1) {
				int befCode = -1;
				boolean yenFlag = false;
				for (i = 0, s = -1, quoteChr = 0; i < len; i++) {
					strCode = str.charAt(i);
					nowChr = strCode;
					s = (s == -1) ? i : s;
					if (quoteChr == 0) {
						if (nowChr == '\'' || nowChr == '\"') {
							quoteChr = nowChr;
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
						if (befCode != '\\' && quoteChr == nowChr) {
							yenFlag = false;
							quoteChr = 0;
							if (s == i && quoteFlg == true) {
								out.add(new StringBuilder()
									.append(strCode).append(strCode).toString());
								s = -1;
							} else if (s < i) {
								if (quoteFlg == true) {
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
				for (i = 0, s = -1, quoteChr = 0; i < len; i++) {
					strCode = str.charAt(i);
					nowChr = strCode;
					s = (s == -1) ? i : s;
					if (quoteChr == 0) {
						if (nowChr == '\'' || nowChr == '\"') {
							quoteChr = nowChr;
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
						if (befCode != '\\' && quoteChr == nowChr) {
							quoteChr = 0;
							yenFlag = false;
							if (s == i && quoteFlg == true) {
								out.add(new StringBuilder().append(strCode)
									.append(strCode).toString());
								s = -1;
							} else if (s < i) {
								if (quoteFlg == true) {
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
				if (quoteChr != 0 && quoteFlg == true) {
					tmp = str.substring(s - 1, len) + (char) quoteChr;
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
	 * クォーテーション内を検知しないIndexOf.
	 *
	 * @param base
	 *            検索元の情報を設定します.
	 * @param cc
	 *            チェック対象の内容を設定します.
	 * @return int 検索結果の内容が返されます.
	 */
	public static final int indexOfNoQuate(
		final String base, final String cc) {
		return indexOfNoQuate(base, cc, 0);
	}

	/**
	 * クォーテーション内を検知しないIndexOf.
	 *
	 * @param base
	 *            検索元の情報を設定します.
	 * @param cc
	 *            チェック対象の内容を設定します.
	 * @param off
	 *            対象のオフセット値を設定します.
	 * @return int 検索結果の内容が返されます.
	 */
	public static final int indexOfNoQuate(
		final String base, final String cc, final int off) {
		final int len = base.length();
		final char[] ck = cc.toCharArray();
		final int cLen = ck.length;
		int j;
		char c = 0, bef = 0;
		int quote = -1;
		for (int i = off; i < len; i++) {
			if (quote != -1) {
				if(!isStringQuotation(base, len, (char)quote)) {
					quote = -1;
					bef = base.charAt(i);
				}
			} else {
				c = base.charAt(i);
				if (bef != '\\' && (c == '\'' || c == '\"')) {
					quote = c;
				} else if (c == ck[0]) {
					boolean res = true;
					for (j = 1; j < cLen; j++) {
						if (i + j >= len || ck[j] != base.charAt(i + j)) {
							res = false;
							break;
						}
					}
					if (res == true) {
						return i;
					}
				}
				bef = c;
			}
		}
		return -1;
	}

	/**
	 * コメント除去.
	 * @param str コメント除去対象の文字列を設定します.
	 * @return String コメントが除去された情報が返却されます.
	 */
	public static final String cutComment(String str) {
		return cutComment(true, str);
	}

	/**
	 * コメント除去.
	 * @param comment2 trueの場合、／＊ ... ＊／ に対応します.
	 * @param str コメント除去対象の文字列を設定します.
	 * @return String コメントが除去された情報が返却されます.
	 */
	public static final String cutComment(boolean comment2, String str) {
		if (str == null || str.length() <= 0) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		int len = str.length();
		int cote = -1;
		int commentType = -1;
		int bef = -1;
		char c, c2;
		for (int i = 0; i < len; i++) {
			if (i != 0) {
				bef = str.charAt(i - 1);
			}
			c = str.charAt(i);
			// コメント内の処理.
			if (commentType != -1) {
				switch (commentType) {
				case 1: // １行コメント.
					if (c == '\n') {
						buf.append(c);
						commentType = -1;
					}
					break;
				case 2: // 複数行コメント.
					if (c == '\n') {
						buf.append(c);
					} else if (len > i + 1 && c == '*' && str.charAt(i + 1) == '/') {
						i++;
						commentType = -1;
					}
					break;
				}
				continue;
			}
			// シングル／ダブルコーテーション内の処理.
			if (cote != -1) {
				if (c == cote && (char) bef != '\\') {
					cote = -1;
				}
				buf.append(c);
				continue;
			}
			// コメント(// or /* ... */).
			if (c == '/') {
				if (len <= i + 1) {
					buf.append(c);
					continue;
				}
				c2 = str.charAt(i + 1);
				if (comment2 && c2 == '*') {
					commentType = 2;
					continue;
				} else if (c2 == '/') {
					commentType = 1;
					continue;
				}
			}
			// コメント(--)
			else if (c == '-') {
				if (len <= i + 1) {
					buf.append(c);
					continue;
				}
				c2 = str.charAt(i + 1);
				if (c2 == '-') {
					commentType = 1;
					continue;
				}
			}
			// コメント(#)
			else if (c == '#') {
				if (len <= i + 1) {
					buf.append(c);
					continue;
				}
				commentType = 1;
				continue;
			}
			// コーテーション開始.
			else if ((c == '\'' || c == '\"') && (char) bef != '\\') {
				cote = (int) (c & 0x0000ffff);
			}
			buf.append(c);
		}
		return buf.toString();
	}
	
	/**
	 * 出力処理.
	 * @param buf 書き込み先のStringBuilderを設定します.
	 * @param tab インデントのタブ数を設定します.
	 * @param args 出力文字群を設定します.
	 * @return StringBuilder StringBuilderが返却されます.
	 */
	public static final StringBuilder print(
		StringBuilder buf, int tab, String... args) {
		final int len = args == null ? 0 : args.length;
		if(len == 0) {
			for(int i = 0; i < tab; i ++) {
				buf.append("\t");
			}
			return buf;
		}
		int i, j;
		for(i = 0; i < len; i ++) {
			if(i != 0) {
				buf.append("\n");
			}
			for(j = 0; j < tab; j ++) {
				buf.append("\t");
			}
			buf.append(args[i]);
		}
		return buf;
	}
	
	/**
	 * 出力処理.
	 * @param w Writerオブジェクトを設定します.
	 * @param tab インデントのタブ数を設定します.
	 * @param args 出力文字群を設定します.
	 * @throws IOException I/O例外.
	 */
	public static final void print(
		Writer w, int tab, String... args)
		throws IOException {
		final StringBuilder buf = new StringBuilder();
		print(buf, tab, args);
		w.append(buf.toString());
	}
	
	/**
	 * 改行して出力処理.
	 * @param buf 書き込み先のStringBuilderを設定します.
	 * @param tab インデントのタブ数を設定します.
	 * @param args 出力文字群を設定します.
	 * @return StringBuilder StringBuilderが返却されます.
	 */
	public static final StringBuilder println(
		StringBuilder buf, int tab, String... args) {
		print(buf, tab, args);
		buf.append("\n");
		return buf;
	}
	
	/**
	 * 改行して出力処理.
	 * @param w Writerオブジェクトを設定します.
	 * @param tab インデントのタブ数を設定します.
	 * @param args 出力文字群を設定します.
	 * @throws IOException I/O例外.
	 */
	public static final void println(
		Writer w, int tab, String... args)
		throws IOException {
		final StringBuilder buf = new StringBuilder();
		println(buf, tab, args);
		w.append(buf.toString());
	}
	
	/**
	 * クラス名を取得.
	 * 通常の Class.getName() では配列などのクラス名が
	 * 正しく取得出来ません.
	 * このメソッドでは、配列クラス名を取得します.
	 * @param clazz 対象のクラスを設定します.
	 * @return String クラス名が返却されます.
	 */
	public static final String getClassName(
		Class<?> clazz) {
		return getClassName(clazz, null);
	}
	
	/**
	 * クラス名を取得.
	 * 通常の Class.getName() では配列などのクラス名が
	 * 正しく取得出来ません.
	 * このメソッドでは、配列クラス名を取得します.
	 * @param clazz 対象のクラスを設定します.
	 * @param genericType 対象のGenericTypeを設定します.
	 * @return String クラス名が返却されます.
	 */
	public static final String getClassName(
		Class<?> clazz, Type genericType) {
		if(genericType != null) {
			return genericType.getTypeName();
		}
		int count = 0;
		while(true) {
			if(clazz.isArray()) {
				clazz = clazz.getComponentType();
				count ++;
				continue;
			}
			StringBuilder buf = null;
			buf = new StringBuilder(clazz.getName());
			for(int i = 0; i < count; i ++) {
				buf.append("[]");
			}
			return buf.toString();
		}
	}
}
