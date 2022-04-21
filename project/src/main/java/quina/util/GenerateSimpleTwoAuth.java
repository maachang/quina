package quina.util;

import quina.exception.QuinaException;

/**
 * 単純二段階認証用コードの発行.
 */
public final class GenerateSimpleTwoAuth {
	private GenerateSimpleTwoAuth() {}
	
	/**
	 * 二段階認証用のコード作成.
	 * @param len コード数(数字桁数)を設定します.
	 * @param updateTime 生成更新されるタイミングを設定します.
	 *                   設定値は秒で設定します.
	 * @param user 生成識別となるユーザー名を設定します.
	 * @param domain 生成識別となるドメイン名を設定します.
	 * @return String[] 有効な二段階認証コードが返却されます.
	 *                  [0]は、生成更新タイミングより１つ前のコードです.
	 *                  [1]は、生成更新タイミングのコードです.
	 *                  [2]は、生成更新タイミングより１つ後のコードです.
	 */
	public static final String[] create(
		int len, int updateTime, String user, String domain) {
		if(len <= 0) {
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
		long now = now(updateTime);
		long userDomainCode = userDomainByLong(user, domain);
		return new String[] {
			createTowAuthCode(len, now - updateTime, userDomainCode)
			,createTowAuthCode(len, now, userDomainCode)
			,createTowAuthCode(len, now + updateTime, userDomainCode)
		};
	}
	
	// 現在時間を取得.
	private static final long now(int updateTime) {
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
		int len, long time, long code) {
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
		final StringBuilder ret = new StringBuilder(len);
		for(int i = 0; i < len; i ++) {
			ret.append((r.next() & 0x7fffffff) % 10);
		}
		return ret.toString();
	}
	
	// 専用２段階認証発生処理.
	private static final class TwoAuthEngine {
		private int a = 362436069;
		private int b = 521288629;
		private int c = 987654321;
		private int d = 88675123;
		
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
	
	public static final void main(String[] args) {
		String[] code1 = create(
			6, 30, "masahito.suzuki@supership.jp", "supership.jp");
		String[] code2 = create(
			6, 30, "maachang@gmail.com", "maachang.com");
		
		System.out.println("now: " + now(30));
		
		System.out.println("code1[0]: " + code1[0]);
		System.out.println("code1[1]: " + code1[1]);
		System.out.println("code1[2]: " + code1[2]);
		
		System.out.println();
		
		System.out.println("code2[0]: " + code2[0]);
		System.out.println("code2[1]: " + code2[1]);
		System.out.println("code2[2]: " + code2[2]);
	}
}
