package quina.util;

import quina.exception.QuinaException;

/**
 * 単純二段階認証用コードの発行.
 */
public final class SimpleTwoAuth {
	private SimpleTwoAuth() {}
	
	/**
	 * 二段階認証用のコード作成.
	 * @param outNextChangeMsec 次の更新時間(ミリ秒)が返却されます.
	 *                          [0] 次の更新時間.
	 *                          [1] 最大更新時間.
	 * @param user 生成識別となるユーザー名を設定します.
	 * @param domain 生成識別となるドメイン名を設定します.
	 * @param codeLen コード数(数字桁数)を設定します.
	 * @param updateTime 生成更新されるタイミングを設定します.
	 *                   設定値は秒で設定します.
	 * @return String[] 有効な二段階認証コードが返却されます.
	 *                  [0]は、生成更新タイミングより１つ前のコードです.
	 *                  [1]は、生成更新タイミングのコードです.
	 *                  [2]は、生成更新タイミングより１つ後のコードです.
	 *                  入力結果をチェックする場合は[0]か[1]で判別すれば
	 *                  良いです.
	 */
	public static final String[] create(
		int[] outNextChangeMsec,
		String user, String domain,
		int codeLen, int updateTime) {
		return create(
			outNextChangeMsec, user, domain,
			codeLen, updateTime, null);
	}
	/**
	 * 二段階認証用のコード作成.
	 * @param outNextChangeMsec 次の更新時間(ミリ秒)が返却されます.
	 *                          [0] 次の更新時間.
	 *                          [1] 最大更新時間.
	 * @param user 生成識別となるユーザー名を設定します.
	 * @param domain 生成識別となるドメイン名を設定します.
	 * @param codeLen コード数(数字桁数)を設定します.
	 * @param updateTime 生成更新されるタイミングを設定します.
	 *                   設定値は秒で設定します.
	 * @param cu カスタムでUserとDomainをLong変換する処理を設定します.
	 * @return String[] 有効な二段階認証コードが返却されます.
	 *                  [0]は、生成更新タイミングより１つ前のコードです.
	 *                  [1]は、生成更新タイミングのコードです.
	 *                  [2]は、生成更新タイミングより１つ後のコードです.
	 *                  入力結果をチェックする場合は[0]か[1]で判別すれば
	 *                  良いです.
	 */
	public static final String[] create(
		int[] outNextChangeMsec,
		String user, String domain,
		int codeLen, int updateTime,
		CustomUserDomain cu) {
		// 引数チェック.
		if(codeLen <= 0) {
			throw new QuinaException(
				"The number of number frames is 0 or less.");
		} else if(updateTime <= 0) {
			throw new QuinaException(
				"The generation update timing second is 0 or less.");
		} else if(user == null || user.isEmpty()) {
			throw new QuinaException("The user name has not been set.");
		} else if(domain == null || domain.isEmpty()) {
			throw new QuinaException(
				"The target domain name has not been set.");
		}
		// updateTimeに対する現在時間の値を取得.
		long now = nowTiming(updateTime);
		// 更新残り時間を取得.
		if(outNextChangeMsec != null &&
			outNextChangeMsec.length >= 1) {
			// 残り時間をセット.
			outNextChangeMsec[0] = (int)(
				(updateTime * 1000L) -
				(
					(System.currentTimeMillis()) -
					(now * 1000L)
				)
			);
			// 最大時間をセット.
			if(outNextChangeMsec.length >= 2) {
				outNextChangeMsec[1] = updateTime * 1000;
			}
		}
		// ユーザーとドメインのlong化.
		long userDomainCode;
		if(cu != null) {
			/// カスタム変換.
			userDomainCode = cu.convert(user, domain);
		} else {
			// 既存変換
			userDomainCode = userDomainByLong(user, domain);
		}
		// ２段階認証コードを取得.
		return new String[] {
			createTowAuthCode(codeLen, now - updateTime, userDomainCode)
			,createTowAuthCode(codeLen, now, userDomainCode)
			,createTowAuthCode(codeLen, now + updateTime, userDomainCode)
		};
	}
	
	// updateTimeに対する現在時間の値を取得.
	private static final long nowTiming(int updateTime) {
		long utime = (long)updateTime;
		return (long)((double)(System.currentTimeMillis() / 1000L) /
			(double)utime) * utime;
	}
	
	// user + domain をlong変換.
	private static final long userDomainByLong(
		String user, String domain) {
		// user名を元にdomain名の文字コードを
		// xorで処理する(userCodeに格納).
		final int userLen = user.length();
		final int[] userCode = new int[userLen];
		for(int i = 0; i < userLen; i++) {
			userCode[i] += user.charAt(i) & 0x0000ffff;
		}
		// userCodeにxorで処理するDomain名を実行.
		int cnt = 0;
		final int domainLen = domain.length();
		for(int i = 0; i < domainLen; i++) {
			userCode[cnt] += (int)(
				(userCode[cnt] ^ domain.charAt(i)) &
				0x0000ffff);
			cnt ++;
			if(cnt >= userLen) {
				cnt = 0;
			}
		}
		// userCodeを合算する.
		// その場のループ数のプラスする形で.
		long ret = 0L;
		long cntKK = 1L;
		for(int i = 0; i < userLen; i++) {
			ret += (userCode[i] & 0x0000ffffL) +
				(long)(cntKK * 3.5d);
			cntKK ++;
			if(cntKK >= 32L) {
				cntKK = 1L;
			}
		}
		return ret;
	}
	
	// １つの承認コードを生成.
	private static final String createTowAuthCode(
		int codeLen, long time, long code) {
		// 奇数の場合は反転.
		if((time & 0x01) == 1) {
			time = (~time) & 0x7fffffffffffffffL;
		}
		// 奇数の場合は反転.
		if((code & 0x01) == 1) {
			code = (~code) & 0x7fffffffffffffffL;
		}
		// 独自乱数発生装置を利用.
		TwoAuthEngine r = new TwoAuthEngine(time);
		// 最大16回ループ
		int loop = (int)((code - time) & 0x0f);
		for(int i = 0; i < loop; i ++) {
			r.next();
		}
		// 指定数の数字文字列を生成.
		int n, len;
		final StringBuilder ret = new StringBuilder(codeLen);
		if((codeLen & 0x01) != 0) {
			len = codeLen - 1;
		} else {
			len = codeLen;
		}
		// 認証コードを生成(2文字)
		for(int i = 0; i < len; i +=2) {
			ret.append((n = r.next() & 0x7fffffff) % 10);
			ret.append((n / 100) % 10);
		}
		// 残りの認証コード生成が必要な場合.
		if((codeLen & 0x01) != 0) {
			// 認証コードを生成(1文字)
			ret.append((r.next() & 0x7fffffff) % 10);
		}
		return ret.toString();
	}
	
	// 専用２段階認証発生処理.
	private static final class TwoAuthEngine {
		private int a = 362436069;
		private int b = 521288629;
		private int c = 987654321;
		private int d = 886751230;
		
		// コンストラクタ.
		public TwoAuthEngine(final long ss) {
			int s = (int)(ss & 0x00000000ffffffffL);
			a = s = 1812433253 * (s ^ (s >> 30)) + 1;
			b = s = 1812433253 * (s ^ (s >> 30)) + 2;
			s = ((int)(ss & 0xffffffff00000000L >> 32L) ^ s);
			c = s = 1812433253 * (s ^ (s >> 30)) + 3;
			d = s = 1812433253 * (s ^ (s >> 30)) + 4;
		}
		
		// コード生成.
		public final int next() {
			int t, r;
			t = a;
			r = t;
			t <<= 11;
			t ^= r;
			r = t;
			r >>= 8;
			t ^= r;
			r = b;
			a = r;
			r = c;
			b = r;
			r = d;
			c = r;
			t ^= r;
			r >>= 19;
			r ^= t;
			d = r;
			return r;
		}
	}
	
	/**
	 * ユーザー・ドメインをlong変換.
	 */
	public static interface CustomUserDomain {
		/**
		 * 変換処理.
		 * @param user 生成識別となるユーザー名を設定します.
		 * @param domain 生成識別となるドメイン名を設定します.
		 * @return long 値が返却されます.
		 */
		public long convert(String user, String domain);
	}
	
	// test.
	/**
	public static final void main(String[] args) {
		int codeLen = 6;
		int time = 30;
		int[] out = new int[2];
		String[] code1 = create(out,
			"maachang@hogehoge.jp", "hogehoge.jp", codeLen, time);
		String[] code2 = create(out,
			"maachang@mogemoge.com", "mogemoge.com", codeLen, time);
		
		System.out.println("now:     " + (nowTiming(30) * 1000L));
		System.out.println("current: " + System.currentTimeMillis());
		System.out.println("out[0]: " + out[0]);
		System.out.println("out[1]: " + out[1]);
		
		System.out.println("code1[0]: " + code1[0]);
		System.out.println("code1[1]: " + code1[1]);
		System.out.println("code1[2]: " + code1[2]);
		
		System.out.println();
		
		System.out.println("code2[0]: " + code2[0]);
		System.out.println("code2[1]: " + code2[1]);
		System.out.println("code2[2]: " + code2[2]);
	}
	**/
}
