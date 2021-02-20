package quina.util;

import java.io.IOException;

/**
 * Fnv-Hash.
 */
public class FnvHash {

	/**
	 * fnv16コード.
	 */
	private static final long FNV_16_CODE = 0x000000000000C34FL;

	/**
	 * fnv32コード.
	 */
	private static final long FNV_32_CODE = 0x00000000811C9DC5L;

	/**
	 * fnv64コード.
	 */
	private static final long FNV_64_CODE = 0xCBF29CE484222325L;

	/**
	 * fnv16-Hash変換.
	 *
	 * @param key
	 *            対象のKeyを設定します.
	 * @param charset
	 *            変換文字列が設定されます.
	 * @return int 変換されたHashが返されます.
	 * @exception IOException IO例外.
	 */
	public static int fnv16(String key, String charset)
		throws IOException {
		byte[] b = key.getBytes("UTF8");
		final int len = b.length;
		long ret = FNV_16_CODE;
		for (int i = 0; i < len; i++) {
			ret += (ret << 1L) + (ret << 4L) + (ret << 7L) + (ret << 8L);
			ret ^= b[i];
		}
		return (int) (ret & 0x000000000000ffffL);
	}

	/**
	 * fnv16-Hash変換.
	 *
	 * @param key
	 *            対象のKeyを設定します.
	 * @param charset
	 *            変換文字列が設定されます.
	 * @return int 変換されたHashが返されます.
	 * @exception IOException IO例外.
	 */
	public static int fnv16a(String key, String charset)
		throws IOException {
		byte[] b = key.getBytes("UTF8");
		final int len = b.length;
		long ret = FNV_16_CODE;
		for (int i = 0; i < len; i++) {
			ret ^= b[i];
			ret += (ret << 1L) + (ret << 4L) + (ret << 7L) + (ret << 8L);
		}
		return (int) (ret & 0x000000000000ffffL);
	}

	/**
	 * fnv32-Hash変換.
	 *
	 * @param key
	 *            対象のKeyを設定します.
	 * @param charset
	 *            変換文字列が設定されます.
	 * @return int 変換されたHashが返されます.
	 * @exception IOException IO例外.
	 */
	public static int fnv32(String key, String charset)
		throws IOException {
		byte[] b = key.getBytes("UTF8");
		final int len = b.length;
		long ret = FNV_32_CODE;
		for (int i = 0; i < len; i++) {
			ret += (ret << 1L) + (ret << 4L) + (ret << 7L) + (ret << 8L)
					+ (ret << 24L);
			ret ^= b[i];
		}
		return (int) (ret & 0x00000000ffffffffL);
	}

	/**
	 * fnv32a-Hash変換.
	 *
	 * @param key
	 *            対象のKeyを設定します.
	 * @param charset
	 *            変換文字列が設定されます.
	 * @return int 変換されたHashが返されます.
	 * @exception IOException IO例外.
	 */
	public static int fnv32a(String key, String charset)
		throws IOException {
		byte[] b = key.getBytes("UTF8");
		final int len = b.length;
		long ret = FNV_32_CODE;
		for (int i = 0; i < len; i++) {
			ret ^= b[i];
			ret += (ret << 1L) + (ret << 4L) + (ret << 7L) + (ret << 8L)
					+ (ret << 24L);
		}
		return (int) (ret & 0x00000000ffffffffL);
	}

	/**
	 * fnv64-Hash変換.
	 *
	 * @param key
	 *            対象のKeyを設定します.
	 * @param charset
	 *            変換文字列が設定されます.
	 * @return long 変換されたHashが返されます.
	 * @exception IOException IO例外.
	 */
	public static long fnv64(String key, String charset)
		throws IOException {
		byte[] b = key.getBytes("UTF8");
		final int len = b.length;
		long ret = FNV_64_CODE;
		for (int i = 0; i < len; i++) {
			ret += (ret << 1L) + (ret << 4L) + (ret << 5L) + (ret << 7L)
					+ (ret << 8L) + (ret << 40L);
			ret ^= b[i];
		}
		return ret;
	}

	/**
	 * fnv64a-Hash変換.
	 *
	 * @param key
	 *            対象のKeyを設定します.
	 * @param charset
	 *            変換文字列が設定されます.
	 * @return long 変換されたHashが返されます.
	 * @exception IOException IO例外.
	 */
	public static long fnv64a(String key, String charset)
		throws IOException {
		byte[] b = key.getBytes(charset);
		final int len = b.length;
		long ret = FNV_64_CODE;
		for (int i = 0; i < len; i++) {
			ret ^= b[i];
			ret += (ret << 1L) + (ret << 4L) + (ret << 5L) + (ret << 7L)
					+ (ret << 8L) + (ret << 40L);
		}
		return ret;
	}

	/**
	 * fnv16-Hash変換.
	 *
	 * @param b
	 *            対象のKeyを設定します.
	 * @return int 変換されたHashが返されます.
	 */
	public static int fnv16(char[] b) {
		final int len = b.length;
		long ret = FNV_16_CODE;
		for (int i = 0; i < len; i++) {
			ret += (ret << 1L) + (ret << 4L) + (ret << 7L) + (ret << 8L);
			ret ^= b[i];
		}
		return (int) (ret & 0x000000000000ffffL);
	}

	/**
	 * fnv16-Hash変換.
	 *
	 * @param b
	 *            対象のKeyを設定します.
	 * @return int 変換されたHashが返されます.
	 */
	public static int fnv16a(char[] b) {
		final int len = b.length;
		long ret = FNV_16_CODE;
		for (int i = 0; i < len; i++) {
			ret ^= b[i];
			ret += (ret << 1L) + (ret << 4L) + (ret << 7L) + (ret << 8L);
		}
		return (int) (ret & 0x000000000000ffffL);
	}

	/**
	 * fnv32-Hash変換.
	 *
	 * @param b
	 *            対象のKeyを設定します.
	 * @return int 変換されたHashが返されます.
	 */
	public static int fnv32(char[] b) {
		final int len = b.length;
		long ret = FNV_32_CODE;
		for (int i = 0; i < len; i++) {
			ret += (ret << 1L) + (ret << 4L) + (ret << 7L) + (ret << 8L)
					+ (ret << 24L);
			ret ^= b[i];
		}
		return (int) (ret & 0x00000000ffffffffL);
	}

	/**
	 * fnv32a-Hash変換.
	 *
	 * @param b
	 *            対象のKeyを設定します.
	 * @return int 変換されたHashが返されます.
	 */
	public static int fnv32a(char[] b) {
		final int len = b.length;
		long ret = FNV_32_CODE;
		for (int i = 0; i < len; i++) {
			ret ^= b[i];
			ret += (ret << 1L) + (ret << 4L) + (ret << 7L) + (ret << 8L)
					+ (ret << 24L);
		}
		return (int) (ret & 0x00000000ffffffffL);
	}

	/**
	 * fnv64-Hash変換.
	 *
	 * @param b
	 *            対象のKeyを設定します.
	 * @return long 変換されたHashが返されます.
	 */
	public static long fnv64(char[] b) {
		final int len = b.length;
		long ret = FNV_64_CODE;
		for (int i = 0; i < len; i++) {
			ret += (ret << 1L) + (ret << 4L) + (ret << 5L) + (ret << 7L)
					+ (ret << 8L) + (ret << 40L);
			ret ^= b[i];
		}
		return ret;
	}

	/**
	 * fnv64a-Hash変換.
	 *
	 * @param b
	 *            対象のKeyを設定します.
	 * @return long 変換されたHashが返されます.
	 */
	public static long fnv64a(char[] b) {
		final int len = b.length;
		long ret = FNV_64_CODE;
		for (int i = 0; i < len; i++) {
			ret ^= b[i];
			ret += (ret << 1L) + (ret << 4L) + (ret << 5L) + (ret << 7L)
					+ (ret << 8L) + (ret << 40L);
		}
		return ret;
	}

	/**
	 * fnv16-Hash変換.
	 *
	 * @param b
	 *            対象のKeyを設定します.
	 * @return int 変換されたHashが返されます.
	 */
	public static int fnv16(byte[] b) {
		final int len = b.length;
		long ret = FNV_16_CODE;
		for (int i = 0; i < len; i++) {
			ret += (ret << 1L) + (ret << 4L) + (ret << 7L) + (ret << 8L);
			ret ^= b[i];
		}
		return (int) (ret & 0x000000000000ffffL);
	}

	/**
	 * fnv16-Hash変換.
	 *
	 * @param b
	 *            対象のKeyを設定します.
	 * @return int 変換されたHashが返されます.
	 */
	public static int fnv16a(byte[] b) {
		final int len = b.length;
		long ret = FNV_16_CODE;
		for (int i = 0; i < len; i++) {
			ret ^= b[i];
			ret += (ret << 1L) + (ret << 4L) + (ret << 7L) + (ret << 8L);
		}
		return (int) (ret & 0x000000000000ffffL);
	}

	/**
	 * fnv32-Hash変換.
	 *
	 * @param b
	 *            対象のKeyを設定します.
	 * @return int 変換されたHashが返されます.
	 */
	public static int fnv32(byte[] b) {
		final int len = b.length;
		long ret = FNV_32_CODE;
		for (int i = 0; i < len; i++) {
			ret += (ret << 1L) + (ret << 4L) + (ret << 7L) + (ret << 8L)
					+ (ret << 24L);
			ret ^= b[i];
		}
		return (int) (ret & 0x00000000ffffffffL);
	}

	/**
	 * fnv32a-Hash変換.
	 *
	 * @param b
	 *            対象のKeyを設定します.
	 * @return int 変換されたHashが返されます.
	 */
	public static int fnv32a(byte[] b) {
		final int len = b.length;
		long ret = FNV_32_CODE;
		for (int i = 0; i < len; i++) {
			ret ^= b[i];
			ret += (ret << 1L) + (ret << 4L) + (ret << 7L) + (ret << 8L)
					+ (ret << 24L);
		}
		return (int) (ret & 0x00000000ffffffffL);
	}

	/**
	 * fnv64-Hash変換.
	 *
	 * @param b
	 *            対象のKeyを設定します.
	 * @return long 変換されたHashが返されます.
	 */
	public static long fnv64(byte[] b) {
		final int len = b.length;
		long ret = FNV_64_CODE;
		for (int i = 0; i < len; i++) {
			ret += (ret << 1L) + (ret << 4L) + (ret << 5L) + (ret << 7L)
					+ (ret << 8L) + (ret << 40L);
			ret ^= b[i];
		}
		return ret;
	}

	/**
	 * fnv64a-Hash変換.
	 *
	 * @param b
	 *            対象のKeyを設定します.
	 * @return long 変換されたHashが返されます.
	 */
	public static long fnv64a(byte[] b) {
		final int len = b.length;
		long ret = FNV_64_CODE;
		for (int i = 0; i < len; i++) {
			ret ^= b[i];
			ret += (ret << 1L) + (ret << 4L) + (ret << 5L) + (ret << 7L)
					+ (ret << 8L) + (ret << 40L);
		}
		return ret;
	}

}
