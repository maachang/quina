package quina.util;

/**
 * CRC64.
 */
public class CRC64 {
	private static final int LOOKUPTABLE_SIZE = 256;
	private static final long POLY64REV = 0xC96C5795D7870F42L;
	private static final long LOOKUPTABLE[] = new long[LOOKUPTABLE_SIZE];
	static {
		int i, b;
		long r;
		final long poly64rev = POLY64REV;
		final long[] tbl = LOOKUPTABLE;
		final int bLen = LOOKUPTABLE_SIZE;
		final int iLen = 8;
		for (b = 0; b < bLen; ++b) {
			r = (long)b;
			for (i = 0; i < iLen; ++i) {
				r = ((r & 1L) == 1L) ? (r >>> 1L) ^ poly64rev : r >>> 1L;
			}
			tbl[b] = r;
		}
	}

	/**
	 * CRC値.
	 */
	private long crc = -1L;

	/**
	 * CRC更新.
	 * @param b １つのデータを設定します.
	 */
	public void update(final int b) {
		crc = LOOKUPTABLE[((b & 0x0FF) ^ (int) crc) & 0x0FF] ^ (crc >>> 8L);
	}

	/**
	 * CRC更新.
	 * @param buf バイナリを設定します.
	 */
	public void update(final byte[] buf) {
		update(buf, 0, buf.length);
	}

	/**
	 * CRC更新.
	 * @param buf バイナリを設定します.
	 * @param off オフセット値を設定します.
	 * @param len 長さを設定します.
	 */
	public void update(final byte[] buf, final int off, final int len) {
		final long[] tbl = LOOKUPTABLE;
		final int end = off + len;
		long c = crc;
		int o = off;
		while (o < end) {
			c = tbl[(buf[o++] ^ (int)c) & 0xFF] ^ (c >>> 8L);
		}
		crc = c;
	}

	/**
	 * CRC64を取得.
	 * @return long CRC64が返却されます.
	 */
	public long getValue() {
		return ~crc;
	}

	/**
	 * データリセット.
	 * @return long 前回のCRC64が返却されます.
	 */
	public long reset() {
		long ret = ~crc;
		crc = -1L;
		return ret;
	}
}
