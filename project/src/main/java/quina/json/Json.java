package quina.json;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import quina.util.Alphabet;
import quina.util.DateUtil;
import quina.util.NumberUtil;
import quina.util.collection.IndexMap;

/**
 * Json変換処理.
 */
@SuppressWarnings("rawtypes")
public final class Json {
	// Arrayタイプ.
	private static final int TYPE_ARRAY = 0;

	// Mapタイプ.
	private static final int TYPE_MAP = 1;

	// 基本変換クラス.
	private static final class BaseJsonIO implements CustomJsonIO {
		// ノーマル実装で処理を行う.
	}

	// 変換クラス.
	private static final AtomicReference<CustomJsonIO> convertJsonIO =
		new AtomicReference<CustomJsonIO>(new BaseJsonIO());

	/** コンストラクタ. **/
	protected Json() {
	}

	/**
	 * カスタムJSON変換クラスを取得.
	 * @return CustomJsonIO 現在のカスタムJSON変換オブジェクトが返却されます.
	 */
	public static final CustomJsonIO getCustomJsonIO() {
		return convertJsonIO.get();
	}

	/**
	 * カスタムJSON変換クラスを登録.
	 * @param custom 置き換えるカスタムJSON変換オブジェクトを設定します.
 	 * @return CustomJsonIO 前回登録されてたカスタムJSON変換オブジェクトが返却されます.
	 */
	public static final CustomJsonIO setCustomJsonIO(final CustomJsonIO custom) {
		if(custom == null) {
			return null;
		}
		CustomJsonIO old = convertJsonIO.get();
		while(!convertJsonIO.compareAndSet(old, custom)) {
			old = convertJsonIO.get();
		}
		return old;
	}

	// StringBuilderを内包するJsonBuilder.
	private static final class StringJsonBuilder implements JsonBuilder {
		private StringBuilder buf = new StringBuilder();
		@Override
		public JsonBuilder append(String s) {
			buf.append(s);
			return this;
		}
		@Override
		public String toString() {
			String ret = buf.toString();
			buf = null;
			return ret;
		}
	}

	/**
	 * JSON変換.
	 *
	 * @param target
	 *            対象のターゲットオブジェクトを設定します.
	 * @return String 変換されたJSON情報が返されます.
	 */
	public static final String encode(final Object target) {
		return encode(new StringJsonBuilder(), target);
	}

	/**
	 * JSON変換.
	 *
	 * @param buf JsonBuilderを設定します.
	 * @param target
	 *            対象のターゲットオブジェクトを設定します.
	 * @return String 変換されたJSON情報が返されます.
	 */
	public static final String encode(JsonBuilder buf, final Object target) {
		_encode(buf, target, target);
		return buf.toString();
	}

	/**
	 * JSON形式から、オブジェクト変換.
	 * この呼び出しでは、コメントは除去しません.
	 *
	 * @param json
	 *            対象のJSON情報を設定します.
	 * @return Object 変換されたJSON情報が返されます.
	 */
	public static final Object decode(String json) {
		return decode(false, json);
	}

	/**
	 * JSON形式から、オブジェクト変換.
	 *
	 * @param cutComment
	 *            コメントを削除する場合はtrue.
	 * @param json
	 *            対象のJSON情報を設定します.
	 * @return Object 変換されたJSON情報が返されます.
	 */
	public static final Object decode(boolean cutComment, String json) {
		if (json == null) {
			return null;
		}
		// コメント削除条件が設定されている場合.
		if(cutComment) {
			// コメントを削除する.
			json = cutComment(json);
			if(json == null || json.isEmpty()) {
				return null;
			}
		}
		List<Object> list;
		final int[] n = new int[1];
		while (true) {
			// token解析が必要な場合.
			if (json.startsWith("[") || json.startsWith("{")) {
				// JSON形式をToken化.
				list = analysisJsonToken(json);
				// Token解析処理.
				if ("[".equals(list.get(0))) {
					// List解析.
					return createJsonInfo(n, list, TYPE_ARRAY, 0, list.size());
				} else {
					// Map解析.
					return createJsonInfo(n, list, TYPE_MAP, 0, list.size());
				}
			} else if (json.startsWith("(") && json.endsWith(")")) {
				json = json.substring(1, json.length() - 1).trim();
				continue;
			}
			break;
		}
		return decJsonValue(n, 0, json);
	}

	/** [encodeJSON]jsonコンバート. **/
	private static final void _encode(
		final JsonBuilder buf, final Object base, final Object target) {
		final CustomJsonIO conv = convertJsonIO.get();
		if (target == null) {
			buf.append(conv.nullToString());
		} else if (target instanceof Map) {
			encodeJsonMap(buf, base, (Map) target);
		} else if (target instanceof List) {
			encodeJsonList(buf, base, (List) target);
		} else if (target instanceof Number) {
			buf.append(conv.numberToString((Number)target));
		} else if (target instanceof String) {
			buf.append(conv.stringToString((String)target));
		} else if (target instanceof java.util.Date) {
			buf.append(conv.dateToString((java.util.Date)target));
		} else if (target instanceof Boolean) {
			buf.append(conv.booleanToString((Boolean)target));
		} else if (target instanceof Character) {
			buf.append(conv.charToString((Character)target));
		} else if (target instanceof byte[]) {
			buf.append(conv.binaryToString((byte[])target));
		} else if (target instanceof char[]) {
			buf.append(conv.charArrayToString((char[])target));
		} else if (target.getClass().isArray()) {
			if (Array.getLength(target) == 0) {
				buf.append("[]");
			} else {
				encodeJsonArray(buf, base, target);
			}
		} else {
			buf.append(conv.stringToString(target.toString()));
		}
	}

	/** [encodeJSON]jsonMapコンバート. **/
	private static final void encodeJsonMap(
		final JsonBuilder buf, final Object base, final Map map) {
		String key;
		Object value;
		boolean flg = false;
		final Map mp = (Map) map;
		final Iterator it = mp.keySet().iterator();
		buf.append("{");
		while (it.hasNext()) {
			key = (String) it.next();
			if ((value = mp.get(key)) == base) {
				continue;
			} else if (flg) {
				buf.append(",");
			} else {
				flg = true;
			}
			buf.append("\"").append(key).append("\":");
			_encode(buf, base, value);
		}
		buf.append("}");
	}

	/** [encodeJSON]jsonListコンバート. **/
	private static final void encodeJsonList(
		final JsonBuilder buf, final Object base, final List list) {
		Object value;
		boolean flg = false;
		final List lst = (List) list;
		buf.append("[");
		int len = lst.size();
		for (int i = 0; i < len; i++) {
			if ((value = lst.get(i)) == base) {
				continue;
			} else if (flg) {
				buf.append(",");
			} else {
				flg = true;
			}
			_encode(buf, base, value);
		}
		buf.append("]");
	}

	/** [encodeJSON]json配列コンバート. **/
	private static final void encodeJsonArray(
		final JsonBuilder buf, final Object base, final Object list) {
		Object value;
		boolean flg = false;
		final int len = Array.getLength(list);
		buf.append("[");
		for (int i = 0; i < len; i++) {
			if ((value = Array.get(list, i)) == base) {
				continue;
			} else if (flg) {
				buf.append(",");
			} else {
				flg = true;
			}
			_encode(buf, base, value);
		}
		buf.append("]");
	}

	/** [decodeJSON]１つの要素を変換. **/
	private static final Object decJsonValue(final int[] n, final int no, String json) {
		int len;
		if ((len = json.length()) <= 0) {
			return json;
		}
		// JSON変換I/Oを取得.
		final CustomJsonIO conv = convertJsonIO.get();
		// 文字列コーテーション区切り.
		if ((json.startsWith("\"") && json.endsWith("\""))
				|| (json.startsWith("\'") && json.endsWith("\'"))) {
			json = json.substring(1, len - 1);
			// Date変換対象かチェック.
			if (conv.isDate(json)) {
				return conv.jsonToDate(json);
			}
			return conv.jsonToString(json);
		}
		// NULL文字.
		else if (treeEq("null", json)) {
			return conv.jsonToNull();
		}
		// BOOLEAN.
		else if (treeEq("true", json) || treeEq("false", json)) {
			return conv.jsonToBoolean(json);
		}
		// 数値.
		else if (isNumeric(json)) {
			return conv.jsonToNumber(json);
		}
		// その他.
		throw new JsonException("Failed to parse JSON(" + json + "):No:" + no);
	}

	/** JSON_Token_解析処理 **/
	private static final List<Object> analysisJsonToken(final String json) {
		int s = -1;
		char c;
		int cote = -1;
		int bef = -1;
		int len = json.length();
		List<Object> ret = new ArrayList<Object>();
		// Token解析.
		for (int i = 0; i < len; i++) {
			c = json.charAt(i);
			// コーテーション内.
			if (cote != -1) {
				// コーテーションの終端.
				if (bef != '\\' && cote == c) {
					ret.add(json.substring(s - 1, i + 1));
					cote = -1;
					s = i + 1;
				}
			}
			// コーテーション開始.
			else if (bef != '\\' && (c == '\'' || c == '\"')) {
				cote = c;
				if (s != -1 && s != i && bef != ' ' && bef != '　'
						&& bef != '\t' && bef != '\n' && bef != '\r') {
					ret.add(json.substring(s, i + 1));
				}
				s = i + 1;
				bef = -1;
			}
			// ワード区切り.
			else if (c == '[' || c == ']' || c == '{' || c == '}' || c == '('
					|| c == ')' || c == ':' || c == ';' || c == ','
					|| (c == '.' && (bef < '0' || bef > '9'))) {
				if (s != -1 && s != i && bef != ' ' && bef != '　'
						&& bef != '\t' && bef != '\n' && bef != '\r') {
					ret.add(json.substring(s, i));
				}
				ret.add(new String(new char[] { c }));
				s = i + 1;
			}
			// 連続空間区切り.
			else if (c == ' ' || c == '　' || c == '\t' || c == '\n'
					|| c == '\r') {
				if (s != -1 && s != i && bef != ' ' && bef != '　'
						&& bef != '\t' && bef != '\n' && bef != '\r') {
					ret.add(json.substring(s, i));
				}
				s = -1;
			}
			// その他文字列.
			else if (s == -1) {
				s = i;
			}
			bef = c;
		}
		return ret;
	}

	/** Json-Token解析. **/
	private static final Object createJsonInfo(
		final int[] n, final List<Object> token, final int type, final int no,
		final int len) {
		String value;
		StringBuilder before = null;
		final CustomJsonIO conv = convertJsonIO.get();
		// List.
		if (type == TYPE_ARRAY) {
			final List<Object> ret = new ArrayList<Object>();
			int flg = 0;
			for (int i = no + 1; i < len; i++) {
				value = (String) token.get(i);
				if (",".equals(value) || "]".equals(value)) {
					if ("]".equals(value)) {
						if (flg == 1 && before != null) {
							ret.add(decJsonValue(n, i, before.toString()));
						}
						n[0] = i;
						return ret;
					} else if (flg == 1) {
						if (before == null) {
							ret.add(conv.jsonToNull());
						} else {
							ret.add(decJsonValue(n, i, before.toString()));
						}
					}
					before = null;
					flg = 0;
				} else if ("[".equals(value)) {
					ret.add(createJsonInfo(n, token, 0, i, len));
					i = n[0];
					before = null;
					flg = 0;
				} else if ("{".equals(value)) {
					ret.add(createJsonInfo(n, token, 1, i, len));
					i = n[0];
					before = null;
					flg = 0;
				} else {
					if (before == null) {
						before = new StringBuilder();
						before.append(value);
					} else {
						before.append(" ").append(value);
					}
					flg = 1;
				}
			}
			n[0] = len - 1;
			return ret;
		}
		// map.
		else if (type == TYPE_MAP) {
			String key = null;
			final Map<String, Object> ret = new IndexMap<String, Object>();
			for (int i = no + 1; i < len; i++) {
				value = (String) token.get(i);
				if (":".equals(value)) {
					if (key == null) {
						throw new JsonException("Map format is invalid(No:" + i + ")");
					}
				} else if (",".equals(value) || "}".equals(value)) {
					if ("}".equals(value)) {
						if (key != null) {
							if (before == null) {
								ret.put(key, null);
							} else {
								ret.put(key, decJsonValue(n, i, before.toString()));
							}
						}
						n[0] = i;
						return ret;
					} else {
						if (key == null) {
							if (before == null) {
								continue;
							}
							throw new JsonException("Map format is invalid(No:" + i + ")");
						} else if (before == null) {
							ret.put(key, null);
						} else {
							ret.put(key, decJsonValue(n, i, before.toString()));
						}
						before = null;
						key = null;
					}
				} else if ("[".equals(value)) {
					if (key == null) {
						throw new JsonException("Map format is invalid(No:" + i + ")");
					}
					ret.put(key, createJsonInfo(n, token, 0, i, len));
					i = n[0];
					key = null;
					before = null;
				} else if ("{".equals(value)) {
					if (key == null) {
						throw new JsonException("Map format is invalid(No:" + i + ")");
					}
					ret.put(key, createJsonInfo(n, token, 1, i, len));
					i = n[0];
					key = null;
					before = null;
				} else if (key == null) {
					key = value;
					if ((key.startsWith("'") && key.endsWith("'"))
							|| (key.startsWith("\"") && key.endsWith("\""))) {
						key = key.substring(1, key.length() - 1).trim();
					}
				} else if (before == null) {
					before = new StringBuilder();
					before.append(value);
				} else {
					before.append(" ").append(value);
				}
			}
			n[0] = len - 1;
			return ret;
		}
		// その他.
		throw new JsonException("Failed to parse JSON");
	}

	/**
	 * 日付情報チェック.
	 * @param o
	 * @return
	 */
	public static final boolean isNumeric(final String o) {
		return NumberUtil.isNumeric(o);
	}

	/**
	 * 大文字・小文字区別なしの文字判別.
	 * @param s
	 * @param d
	 * @return
	 */
	public static final boolean treeEq(final String s, final String d) {
		return Alphabet.eq(s, d);
	}

	/**
	 * 日付変換対象の文字列かチェック.
	 * @param s
	 * @return
	 */
	public static final boolean isDate(final String s) {
		return DateUtil.isISO8601(s);
	}

	/**
	 * 日付を文字変換.
	 * @param d
	 * @return
	 */
	public static final String dateToString(final java.util.Date d) {
		return DateUtil.toISO8601(d);
	}

	/**
	 * 文字を日付変換.
	 * @param s
	 * @return
	 */
	public static final Date stringToDate(final String s) {
		return DateUtil.toISO8601(s);
	}

	/**
	 * コメント除去.
	 *
	 * @param str コメント除去を行う文字列を設定します.
	 * @return String 除外された文字列が返却されます.
	 */
	public static final String cutComment(String str) {
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
				if (c2 == '*') {
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
}
