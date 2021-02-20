package quina.net.nio.tcp;

/**
 * IPV4関連ユーティリティ.
 */
public class IpV4Util {
	protected IpV4Util() {}

	/**
	 * 文字のIPアドレスをint変換.
	 * @param addr
	 * @return
	 */
	public static final int ipToInt(String addr) {
		String[] list = addr.split("\\.");
		int ret = 0;
		ret |= (Integer.parseInt(list[3]) & 0x000000ff);
		ret |= (Integer.parseInt(list[2]) & 0x000000ff) << 8;
		ret |= (Integer.parseInt(list[1]) & 0x000000ff) << 16;
		return ret | (Integer.parseInt(list[0]) & 0x000000ff) << 24;
	}

	/**
	 * 文字のIPアドレスをバイナリ変換.
	 * @param out
	 * @param addr
	 */
	public static final void ipToBin(byte[] out, String addr) {
		String[] list = addr.split("\\.");
		out[0] = (byte)(Integer.parseInt(list[0]) & 0x000000ff);
		out[1] = (byte)(Integer.parseInt(list[1]) & 0x000000ff);
		out[2] = (byte)(Integer.parseInt(list[2]) & 0x000000ff);
		out[3] = (byte)(Integer.parseInt(list[3]) & 0x000000ff);
	}

	/**
	 * intのIPアドレスをバイナリ変換.
	 * @param out
	 * @param addr
	 */
	public static final void ipToBin(byte[] out, int addr) {
		out[0] = (byte)((addr & 0xff000000) >> 24);
		out[1] = (byte)((addr & 0x00ff0000) >> 16);
		out[2] = (byte)((addr & 0x0000ff00) >> 8);
		out[3] = (byte)(addr & 0x000000ff);
	}

	/**
	 * byte[]のipアドレスを文字変換.
	 * @param addr
	 * @param off
	 * @return
	 */
	public static final String ipToString(byte[] addr, int off) {
		return new StringBuilder()
			.append(addr[off] & 0x000000ff).append(".")
			.append(addr[off + 1] & 0x000000ff).append(".")
			.append(addr[off + 2] & 0x000000ff).append(".")
			.append(addr[off + 3] & 0x000000ff)
			.toString();
	}

	/**
	 * intのipアドレスを文字変換.
	 * @param addr
	 * @return
	 */
	public static final String ipToString(int addr) {
		long n = addr & 0x00000000ffffffffL;
		return new StringBuilder()
			.append((n & 0x0ff000000L) >> 24L).append(".")
			.append((n & 0x00ff0000L) >> 16L).append(".")
			.append((n & 0x0000ff00L) >> 8L).append(".")
			.append(n & 0x000000ffL)
			.toString();
	}

	/**
	 * longのipアドレスを文字変換.
	 * @param addr
	 * @return
	 */
	public static final String ipToString(long addr) {
		return ipToString((int)(addr & 0xffffffff));
	}

	/**
	 * 指定文字列がipアドレスかチェック.
	 * @param n
	 * @return
	 */
	public static final boolean isIp(String n) {
		char c;
		int len = n.length();
		int cnt = 0;
		for(int i = 0; i < len; i ++) {
			if((c = n.charAt(i)) == '.') {
				cnt ++;
			} else if(!(c >= '0' && c <= '9')) {
				return false;
			}
		}
		return cnt == 3;
	}

	/**
	 * ローカルIPかチェック.
	 * @param n
	 * @return
	 */
	public static final boolean isLocalIp(String n) {
		return n.startsWith("10.") ||
				n.startsWith("172.16.") ||
				n.startsWith("192.168.");
	}
}
