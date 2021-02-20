package quina.http;

import java.util.Arrays;

import quina.util.BinaryUtil;
import quina.util.collection.ObjectList;
import quina.util.collection.TypesKeyValue;

/**
 * バイナリベースのHTTPヘッダをインデックス管理します.
 *
 * GET / HTTP/1.1¥r¥n
 * ------ ここから ------
 * Accept: image/gif, image/jpeg¥r¥n
 * Accept-Language: ja¥r¥n
 * Accept-Encoding: gzip, deflate¥r¥n
 * User-Agent: Mozilla/4.0 (Compatible; MSIE 6.0; Windows NT 5.1;)¥r¥n
 * Host: www.xxx.zzz¥r¥n
 * Connection: Keep-Alive¥r¥n
 * ....¥r¥n
 * ------ ここまで ------
 * ¥r¥n
 *
 * ここではサーバ側のRequestヘッダを示してますが、クライアント受けの
 * Responseヘッダも同様な仕様なので、同じ受付でのHTTPヘッダと言う意味で
 * 扱います。
 *
 * このような管理をされているHTTPヘッダに対するKeyのインデックスを
 * 定義します.
 */
public class HttpIndexHeaders implements TypesKeyValue<String, String> {

	// Httpヘッダー要素管理.
	private static final class Element implements Comparable<Element> {
		private byte[] headers;
		private int startKPos;
		private int endKPos;
		private int startVPos;
		private int endVPos;

		/**
		 * コンストラクタ.
		 * @param k キー情報を設定します.
		 * @param sv value開始位置を設定します.
		 * @param ev value終了位置を設定します.
		 */
		public Element(final byte[] hs, final int sk,
			final int ek, final int sv, final int ev) {
			headers = hs;
			startKPos = sk;
			endKPos = ek;
			startVPos = sv;
			endVPos = ev;
		}

		/**
		 * 比較.
		 * @param 比較先を設定します.
		 * @return int 比較結果が返却されます.
		 *             [マイナス]の場合は比較元が大きいです.
		 *             [プラス]の場合は比較先が大きいです.
		 *             [同じ]場合は、内容が一致しています.
		 */
		@Override
		public int compareTo(final Element o) {
			return BinaryUtil.comparableEng(
				headers, startKPos, endKPos,
				o.headers, o.startKPos, o.endKPos);
		}

		/**
		 * キー情報を文字列で取得.
		 * @return String キー情報が文字列で返却されます.
		 */
		public String getKey() {
			final int len = endKPos - startKPos;
			return BinaryUtil.toAscii(headers, startKPos, len);
		}

		/**
		 * バリュー情報を文字列で取得.
		 * @return byte[] バリュー情報が文字列で返却されます.
		 */
		public String getValue(String charset) {
			charset = (charset == null || charset.length() == 0) ?
				HttpConstants.getCharset() : charset;
			final int len = endVPos - startVPos;
			try {
				return new String(headers, startVPos, len, charset);
			} catch(Exception e) {
				throw new HttpException(e);
			}
		}

		/**
		 * キー開始ポジションを取得.
		 * @return
		 */
		public int getStartKPos() {
			return startKPos;
		}

		/**
 		 * キー終了ポジションを取得.
		 * @return
		 */
		public int getEndKPos() {
			return endKPos;
		}
	}

	/** Httpヘッダー要素管理. **/
	private Element[] list;

	/** Httpヘッダの塊. **/
	private byte[] headers;

	// ヘッダKeyと要素の区切り.
	private static final byte SEPARATOR_KEY_VALUE = (byte)':';

	// ヘッダバイナリを解析して要素群を生成.
	private static final Element[] analysisHeader(final byte[] headerBin) {
		byte b;
		long pos;
		int i, p, s, e;
		final int len = headerBin.length - 1;
		byte[] line = HttpConstants.END_LINE;
		Element em = null;
		final ObjectList<Element> list = new ObjectList<Element>();
		for(i = 0, p = 0, s = -1, e = -1; i < len; i ++) {
			b = headerBin[i];
			// ヘッダ要素の区切り情報がある場合.
			if(b == line[0]) {
				// ¥r¥nの区切り情報の場合.
				if(headerBin[i+1] == line[1]) {
					// ヘッダキーを取得.
					if(s != -1) {
						pos = BinaryUtil.trimPos(headerBin, p, i);
						em = new Element(headerBin, s, e,
							BinaryUtil.getLow(pos), BinaryUtil.getHigh(pos));
						list.add(em);
					}
					i ++;
					// 次のヘッダ要素開始条件をセット.
					p = i + 1;
					s = e = -1;
					continue;
				}
			}
			// キー情報のチェック.
			if(s == -1) {
				// keyとvalueの区切りの場合.
				if(b == SEPARATOR_KEY_VALUE) {
					// キー情報を抽出.
					pos = BinaryUtil.trimPos(headerBin, p, i);
					s = BinaryUtil.getLow(pos);
					e = BinaryUtil.getHigh(pos);
					p = i + 1;
				}
			}
		}
		// 生成されたキー要素リストをソート.
		final Element[] ret = list.toArray(Element.class);
		Arrays.sort(ret);
		return ret;
	}

	/**
	 * コンストラクタ.
	 * @param headerBin ヘッダ情報のバイナリを設定します.
	 */
	public HttpIndexHeaders(final byte[] headerBin) {
		Element[] emList = analysisHeader(headerBin);
		this.headers = headerBin;
		this.list = emList;
	}

	/**
	 * ヘッダ数を取得.
	 * @return int ヘッダ数が返却されます.
	 */
	public int size() {
		return list.length;
	}

	/**
	 * ヘッダキー名を取得.
	 * @param no 取得項番を設定します.
	 * @return String ヘッダキー名が返却されます.
	 */
	public String getKey(final int no) {
		if(no < 0 || no >= list.length) {
			throw new IndexOutOfBoundsException();
		}
		return list[no].getKey();
	}

	/**
	 * ヘッダ要素名を取得.
	 * @param no 取得項番を設定します.
	 * @return String ヘッダ要素が返却されます.
	 */
	public String getValue(final int no) {
		return getValue(no, null);
	}

	/**
	 * ヘッダ要素名を取得.
	 * @param no 取得項番を設定します.
	 * @param charset 文字コードを設定します.
	 * @return String ヘッダ要素が返却されます.
	 */
	public String getValue(final int no, String charset) {
		if(no < 0 || no >= list.length) {
			throw new IndexOutOfBoundsException();
		}
		return list[no].getValue(charset);
	}

	// キー検索.
	private static final int searchKey(
		final byte[] headers, final Element[] emList, final String key) {
		final byte[] bk = BinaryUtil.asciiToBinary(key);
		int low = 0;
		int high = emList.length - 1;
		int mid, cmp;
		while (low <= high) {
			mid = (low + high) >>> 1;
			if((cmp = BinaryUtil.comparableEng(
				headers, emList[mid].getStartKPos(), emList[mid].getEndKPos(),
				bk, 0, bk.length)) < 0) {
				low = mid + 1;
			} else if (cmp > 0) {
				high = mid - 1;
			} else {
				return mid; // key found
			}
		}
		return -1;
	}

	/**
	 * 指定ヘッダキーが存在するかチェック.
	 * @param key 対象のヘッダキーを設定します.
	 * @return boolean [true]が返却されます.
	 */
	public boolean contains(final String key) {
		return searchKey(headers, list, key) != -1;
	}

	/**
	 * 要素を取得.
	 * @param key 対象キーを設定します.
	 * @return String 要素情報が返却されます.
	 */
	public String get(final Object key) {
		return get((String)key, null);
	}

	/**
	 * 要素を取得.
	 * @param key 対象キーを設定します.
	 * @param charset 変換文字コードを設定します.
	 * @return String 要素情報が返却されます.
	 */
	public String get(final String key, final String charset) {
		final int p = searchKey(headers, list, key);
		if(p == -1) {
			return null;
		}
		return list[p].getValue(charset);
	}

	/*
	public static final void main(String[] args) throws Exception {
		byte[] headers = (
			"Accept: image/gif, image/jpeg\r\n" +
			"accept-Language: ja\r\n" +
			"Accept-Encoding: gzip, deflate\r\n" +
			"user-Agent: Mozilla/4.0 (Compatible; MSIE 6.0; Windows NT 5.1;)\r\n" +
			"Host: www.xxx.zzz\r\n" +
			"connection: Keep-Alive\r\n"
			).getBytes("UTF8");

		HttpIndexHeaders ih = new HttpIndexHeaders(headers);

		System.out.println("size:" + ih.size());
		int len = ih.size();
		for(int i = 0; i < len; i ++) {
			System.out.println("key(" + i + "): [" + ih.getKey(i) + "]");
			System.out.println(" key: " + ih.getKey(i) + " contains: " + ih.contains(ih.getKey(i)));
			System.out.println(" key: " + ih.getKey(i) + " value: [" + ih.get(ih.getKey(i)) + "]");
		}

		System.out.println("key: accept: " + ih.get("accept"));
		System.out.println("key: hoge: " + ih.get("hoge"));
	}
	*/
}
