package quina.util;

import java.util.Map;
import java.util.WeakHashMap;

import quina.util.collection.ObjectList;

/**
 * 環境変数キャッシュ.
 */
public class EnvCache {
	private static final ThreadLocal<Map<String, String>> cache = new ThreadLocal<Map<String, String>>();

	private static final Map<String, String> getCache() {
		Map<String, String> ret = cache.get();
		if (ret == null) {
			ret = new WeakHashMap<String, String>();
			cache.set(ret);
		}
		return ret;
	}

	/**
	 * 指定名の環境変数内容を取得.
	 *
	 * @param name
	 *            取得対象の環境変数名を設定します.
	 * @return String
	 */
	public static final String get(String name) {
		final Map<String, String> c = getCache();
		String ret = c.get(name);
		if (ret == null) {
			ret = System.getenv(name);
			c.put(name, ret);
		}
		return ret;
	}

	/**
	 * 環境変数のパスを含んだ条件を取得.
	 * 環境変数は２つの条件で設定が出来ます.
	 * /xxx/${HOME}/yyy/zzz.txt
	 * /xxx/%HOME%/yyy/zzz.txt
	 *
	 * @param path 対象の環境変数定義を含んだパスを設定します.
	 * @return String 定義された環境変数が適用されたパスが返却されます.
	 */
	public static final String path(String path) {
		final ObjectList<int[]> posList = new ObjectList<int[]>();
		char c;
		int len = path.length();
		int start = 0;
		int type = -1;
		for(int i = 0; i < len; i ++) {
			c = path.charAt(i);
			if(type != -1) {
				// $.../ or $...[END]
				if(type == 0 && (c == '/' || i + 1 == len)) {
					// $.../
					if(c == '/') {
						posList.add(new int[] {type, start, i});
					// $...[END]
					} else {
						posList.add(new int[] {type, start, i + 1});
					}
					type = -1;
				// ${...}
				} else if(type == 1 && c == '}') {
					posList.add(new int[] {type, start, i});
					type = -1;
				// %...%
				} else if(type == 2 && c == '%') {
					posList.add(new int[] {type, start, i});
					type = -1;
				}
			} else if(c == '$') {
				if(i + 1 < len && path.charAt(i + 1) == '{') {
					type = 1; // ${...}
					start = i;
					i ++;
				} else {
					type = 0; // $...
					start = i;
				}
			} else if(c == '%') {
				type = 2; // %...%
				start = i;
			}
		}
		if(posList.size() == 0) {
			return path;
		}
		int[] plst;
		String envSrc, envDest;
		int first = 0;
		int s, e;
		len = posList.size();
		final StringBuilder buf = new StringBuilder();
		for(int i = 0; i < len; i ++) {
			plst = posList.get(i);
			type = plst[0]; // type.
			s = plst[1]; // 開始位置.
			e = plst[2]; // 終了位置.
			plst = null;
			// $...
			if(type == 0) {
				start = s;
			// ${...}
			} else if(type == 1) {
				start = s + 1;
			// %...%
			} else if(type == 2) {
				start = s;
			}
			// 環境変数名.
			envSrc = path.substring(start + 1, e);
			// 環境変数名を変換.
			envDest = get(envSrc);
			buf.append(path.substring(first, s));
			if(envDest != null) {
				// 取得した環境変数をセット.
				buf.append(envDest);
			} else {
				// 取得できない場合はエラー出力.
				throw new StringException("Information for environment variable \"" +
					envSrc + "\" does not exist.");
			}
			envSrc = null; envDest = null;
			// $... の場合は${...} や %...% と違い終端が無いことを示す定義.
			first = (type == 0) ? e : e + 1;
		}
		buf.append(path.substring(first));
		return buf.toString();
	}
}
