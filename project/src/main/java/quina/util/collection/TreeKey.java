package quina.util.collection;

import quina.util.Alphabet;

/**
 * 大文字・小文字を区別しないキー情報.
 */
public class TreeKey implements Comparable<Object> {
	private final char[] key;
	private final int hashCode;

	/**
	 * コンストラクタ.
	 * @param key 対象のキーを設定します.
	 */
	public TreeKey(String key) {
		if(key == null) {
			key = "null";
		}
		// 文字列のキーはchar配列に変換.
		final int len = key.length();
		final char[] k = new char[len];
		for(int i = 0; i < len; i ++) {
			k[i] = key.charAt(i);

		}
		this.key = k;
		this.hashCode = k.hashCode();
	}

	/**
	 * キー内容を取得.
	 * @return String 対象のキー情報が返却されます.
	 */
	public String getKey() {
		return new String(key);
	}

	@Override
	public int compareTo(Object o) {
		int a, b, destLen;
		int len = key.length;
		final char[] checkAlphabet = Alphabet.getCheckAlphabet();
		// キー条件がPutDeleteKeyの場合.
		if(o instanceof TreeKey) {
			final char[] destKey = ((TreeKey)o).key;
			destLen = destKey.length;
			len = (len > destLen) ? destLen : len;
			for(int i = 0; i < len; i ++) {
				a = checkAlphabet[key[i] & 0x00ff] & 0x00ff;
				b = checkAlphabet[destKey[i] & 0x00ff] & 0x00ff;
				if(a < b) {
					return -1;
				} else if(a > b) {
					return 1;
				}
			}
		// キー条件が文字列の場合.
		} else {
			final String destKey = (String)o;
			destLen = destKey.length();
			len = (len > destLen) ? destLen : len;
			for(int i = 0; i < len; i ++) {
				a = checkAlphabet[key[i] & 0x00ff] & 0x00ff;
				b = checkAlphabet[destKey.charAt(i) & 0x00ff] & 0x00ff;
				if(a < b) {
					return -1;
				} else if(a > b) {
					return 1;
				}
			}
		}
		return key.length - destLen;
	}

	@Override
	public boolean equals(Object o) {
		return compareTo(o) == 0;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		return new String(key);
	}
}
