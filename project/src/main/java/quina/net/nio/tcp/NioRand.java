package quina.net.nio.tcp;

/**
 * Nio専用ランダム発生処理.
 * 
 * 中身は xor128 です.
 */
public final class NioRand {
	private int a = 123456789;
	private int b = 362436069;
	private int c = 521288629;
	private int d = 88675123;
	
	/** このシステムでの汎用ランダムオブジェクト. **/
	private static final ThreadLocal<NioRand> XOR128 = new ThreadLocal<NioRand>();
	
	/**
	 * このシステムでの汎用ランダムオブジェクトを取得.
	 * @return
	 */
	public static final NioRand get() {
		NioRand ret = XOR128.get();
		if(ret == null) {
			ret = new NioRand(System.nanoTime());
			XOR128.set(ret);
		}
		return ret;
	}

	/**
	 * コンストラクタ.
	 */
	public NioRand() {
		this.setSeet(System.currentTimeMillis());
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param s
	 *            乱数初期係数を設定します.
	 */
	public NioRand(int s) {
		this.setSeet(s);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param s
	 *            乱数初期係数を設定します.
	 */
	public NioRand(long s) {
		this.setSeet(s);
	}

	/**
	 * ランダム係数を設定.
	 * 
	 * @param s
	 *            ランダム係数を設定します.
	 */
	public final void setSeet(int s) {
		setSeet((long) s);
	}

	/**
	 * ランダム係数を設定.
	 * 
	 * @param ss
	 *            ランダム係数を設定します.
	 */
	public final void setSeet(long ss) {
		int s = (int) (ss & 0x00000000ffffffffL);
		a = s = 1812433253 * (s ^ (s >> 30)) + 1;
		b = s = 1812433253 * (s ^ (s >> 30)) + 2;
		c = s = 1812433253 * (s ^ (s >> 30)) + 3;
		d = s = 1812433253 * (s ^ (s >> 30)) + 4;
	}

	/**
	 * 32ビット乱数を取得.
	 * 
	 * @return int 32ビット乱数が返されます.
	 */
	public final int nextInt() {
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
	
	/**
	 * バイナリに乱数を設定.
	 * @param out
	 */
	public void nextBytes(byte[] out) {
		int i, n, p, len, len4, lenEtc;
		p = 0;
		len = out.length;
		len4 = len >> 2;
		lenEtc = len - (len << 2);
		for(i = 0; i < len4; i ++) {
			n = nextInt();
			out[p ++] = (byte)(n & 0x000000ff);
			out[p ++] = (byte)((n & 0x0000ff00) >> 8);
			out[p ++] = (byte)((n & 0x00ff0000) >> 16);
			out[p ++] = (byte)((n & 0xff000000) >> 24);
		}
		for(i = 0; i < lenEtc; i ++) {
			out[p ++] = (byte)nextInt();
		}
	}
}
