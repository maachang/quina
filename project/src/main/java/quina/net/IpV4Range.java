package quina.net;

import java.net.InetAddress;
import java.util.regex.Pattern;

import quina.exception.QuinaException;
import quina.util.NumberUtil;

/**
 * IP(V4)アドレス+マスク値での、
 * 指定IPアドレスの範囲チェック.
 * 
 * 192.168.1.0/24 や
 * 172.16.0.0/16 や
 * 10.0.0.0/8 など
 * 
 * またはドメイン名の一致チェック.
 * 
 * その他　192.168.0.100 - 192.168.0.200 など.
 */
public class IpV4Range {
	// マスクリスト.
	// MASK_LIST[mask - 8]で処理が必要.
	private static final long[] MASK_LIST = new long[] {
		0x00000000ff000000L, // 8
		0x00000000ff800000L, // 9
		0x00000000ffc00000L, // 10
		0x00000000ffe00000L, // 11
		0x00000000fff00000L, // 12
		0x00000000fff80000L, // 13
		0x00000000fffc0000L, // 14
		0x00000000fffe0000L, // 15
		0x00000000ffff0000L, // 16
		0x00000000ffff8000L, // 17
		0x00000000ffffc000L, // 18
		0x00000000ffffe000L, // 19
		0x00000000fffff000L, // 20
		0x00000000fffff800L, // 21
		0x00000000fffffc00L, // 22
		0x00000000fffffe00L, // 23
		0x00000000ffffff00L, // 24
		0x00000000ffffff80L, // 25
		0x00000000ffffffc0L, // 26
		0x00000000ffffffe0L, // 27
		0x00000000fffffff0L, // 28
		0x00000000fffffff8L, // 29
		0x00000000fffffffcL, // 30
		0x00000000fffffffeL, // 31
		0x00000000ffffffffL  // 32
	};
	
	// 最小評価IPアドレス.
	private long min = 0;
	
	// 最大評価IPアドレス.
	private long max = 0;
	
	// IPマスク値.
	private int mask = 0;
	
	// ドメイン名.
	private String domain = null;
	
	/**
	 * コンストラクタ.
	 * @param start 開始アドレスを設定します.
	 * @param end 終了アドレスを設定します.
	 */
	public IpV4Range(Object start, Object end) {
		create(start, end);
	}
	
	/**
	 * コンストラクタ.
	 * @param addr [IPアドレス/マスク値]を文字列で指定します。
	 *             または[ドメイン名]を設定します.
	 *             または[開始IPアドレス-終了IPアドレス]を設定します.
	 */
	public IpV4Range(String addr) {
		int p = addr.indexOf("-");
		if(p != -1) {
			create(addr.substring(0, p).trim(),
				addr.substring(p + 1).trim());
			return;
		}
		long[] a = analysisIpMask(addr);
		if(a != null) {
			this.mask = (int)a[1];
			this.min = toStartMaskLongIp(a[0], this.mask);
			this.max = toEndMaskLongIp(a[0], this.mask);
		} else {
			this.domain = addr;
		}
	}
	
	/**
	 * コンストラクタ.
	 * @param addr InetAddressオブジェクトを設定します.
	 * @param mask マスク値を設定します.
	 */
	public IpV4Range(InetAddress addr, int mask) {
		this(addr.getAddress(), mask);
	}
	
	/**
	 * コンストラクタ.
	 * @param addr IPアドレスを設定します.
	 * @param mask マスク値を設定します.
	 */
	public IpV4Range(String addr, int mask) {
		checkMask(mask);
		Long a = convertLongIp(addr);
		if(a == null) {
			throw new QuinaException(
				"Not an IPV4 address.");
		}
		this.mask = mask;
		this.min = toStartMaskLongIp(a, this.mask);
		this.max = toEndMaskLongIp(a, this.mask);
	}
	
	/**
	 * コンストラクタ.
	 * @param ip IPアドレスの４バイト整数を設定します.
	 * @param mask マスク値を設定します.
	 */
	public IpV4Range(int ip, int mask) {
		checkMask(mask);
		this.mask = mask;
		this.min = toStartMaskLongIp((long)ip, this.mask);
		this.max = toEndMaskLongIp((long)ip, this.mask);
	}
	
	/**
	 * コンストラクタ.
	 * @param ip IPアドレスの４バイト整数を設定します.
	 * @param mask マスク値を設定します.
	 */
	public IpV4Range(long ip, int mask) {
		checkMask(mask);
		this.mask = mask;
		this.min = toStartMaskLongIp(ip, this.mask);
		this.max = toEndMaskLongIp(ip, this.mask);
	}
	
	/**
	 * コンストラクタ.
	 * @param ip IPアドレスのbyte[4]を設定します.
	 * @param mask マスク値を設定します.
	 */
	public IpV4Range(byte[] ip, int mask) {
		checkMask(mask);
		long a = convertLongIp(ip);
		this.mask = mask;
		this.min = toStartMaskLongIp(a, this.mask);
		this.max = toEndMaskLongIp(a, this.mask);
	}
	
	// IP範囲設定条件で生成.
	private final void create(Object start, Object end) {
		if(start == null || end == null) {
			throw new QuinaException(
				"Start and end addresses are not set.");
		}
		try {
			Long m, x;
			if(start instanceof InetAddress) {
				m = convertLongIp(((InetAddress)start)
					.getAddress());
			} else if(start instanceof Number) {
				m = ((Number)start).longValue();
			} else {
				m = convertLongIp("" + start);
				if(m == null) {
					m = convertLongIp(
						InetAddress.getByName("" + start)
							.getAddress());
				}
			}
			if(end instanceof InetAddress) {
				x = convertLongIp(((InetAddress)end)
					.getAddress());
			} else if(end instanceof Number) {
				x = ((Number)end).longValue();
			} else {
				x = convertLongIp("" + end);
				if(x == null) {
					x = convertLongIp(
						InetAddress.getByName("" + end)
							.getAddress());
				}
			}
			if(m > x) {
				long d = m;
				m = x;
				x = d;
			}
			this.min = m;
			this.max = x;
			this.mask = -1;
			this.domain = null;
		} catch(QuinaException re) {
			throw re;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
	
	// nnn.nnn.nnn.nnn/nn の文字列からIPアドレスとマスク値を取得.
	private static final long[] analysisIpMask(String addr) {
		if(addr == null || addr.length() == 0) {
			throw new QuinaException(
				"The IPV4 and mask strings are not set.");
		}
		// nnn.nnn.nnn.nnn/nn であるかチェック.
		int p = addr.indexOf("/");
		if(p == -1) {
			Long a = convertLongIp(addr);
			if(a == null) {
				// domainの可能性.
				return null;
			}
			// 同一IPアドレスのみ.
			return new long[] {
				a, 32
			};
		}
		String mask = addr.substring(p + 1);
		if(!NumberUtil.isNumeric(mask)) {
			// エラー.
			throw new QuinaException(
				"Mask value is not numeric.");
		}
		int maskNumber = NumberUtil.parseInt(mask);
		checkMask(maskNumber);
		return new long[] {
			convertLongIp(addr.substring(0, p)),
			(long)maskNumber
		};
	}
	
	// マスクの範囲チェック.
	private static final void checkMask(int mask) {
		if(mask < 8 || mask > 32) {
			throw new QuinaException(
				"Mask value is out of range.");
		}
	}
	
	
	// 開始アドレスを取得.
	private static final long toStartMaskLongIp(long addr, int mask) {
		return (long)(addr & (long)MASK_LIST[mask - 8]);
	}
	
	// 終了アドレスを取得.
	private static final long toEndMaskLongIp(long addr, int mask) {
		final long maskCode = MASK_LIST[mask - 8];
		return ((addr & maskCode) | ~maskCode)
				& 0x00000000ffffffffL;
	}
	
	/**
	 * 文字列からLongIP変換.
	 * @param addr IPV4のアドレス(xxx.xxx.xxx.xxx)か
	 *             ドメイン名を設定します.
	 * @return Long LongIpが返却されます.
	 */
	protected static final Long convertLongIp(String addr) {
		if(addr == null || addr.length() == 0) {
			return null;
		}
		final String[] ip = addr.split(Pattern.quote("."));
		if(ip.length != 4) {
			// ドメインの可能性.
			try {
				// InetAddressで取得.
				return convertLongIp(InetAddress.getByName(addr));
			} catch(Exception e) {
				// エラーの場合はnull返却.
				return null;
			}
		}
		try {
			int a = NumberUtil.parseInt(ip[0]);
			int b = NumberUtil.parseInt(ip[1]);
			int c = NumberUtil.parseInt(ip[2]);
			int d = NumberUtil.parseInt(ip[3]);
			if((a & 0xffffff00) != 0 ||
				(b & 0xffffff00) != 0 ||
				(c & 0xffffff00) != 0 ||
				(d & 0xffffff00) != 0) {
				// 255の範囲内で無い数字の場合はnull返却.
				return null;
			}
			// IPアドレス情報をLongで返却.
			return (long)((long)a << 24L) |
				((long)b << 16L) |
				((long)c << 8L) |
				((long)d << 0L);
		} catch(Exception e) {
			// ドメインの可能性.
			try {
				// InetAddressで取得.
				return convertLongIp(InetAddress.getByName(addr));
			} catch(Exception ee) {
				// エラーの場合はnull返却.
				return null;
			}
		}
	}
	
	/**
	 * 文字列からLongIP変換.
	 * @param addr InetAddressを設定します.
	 * @return Long LongIpが返却されます.
	 */
	protected static final Long convertLongIp(InetAddress addr) {
		if(addr == null) {
			return null;
		}
		// HostAddressでLongIp変換.
		return convertLongIp(addr.getHostAddress());
	}
	
	/**
	 * binaryからLongIp変換
	 * @param addr byte[4]からlongのIP変換.
	 *             192.168.0.1 の場合
	 *             [0] = 192, [1] = 168, [2] = 0, [3] = 1
	 *             となります.
	 * @return Long LongIpが返却されます.
	 */
	protected static final Long convertLongIp(byte[] addr) {
		if(addr == null || addr.length != 4) {
			return null;
		}
		return (((long)addr[0] & 0x0ffL) << 24L)|
			(((long)addr[1] & 0x0ffL) << 16L) |
			(((long)addr[2] & 0x0ffL) << 8L) |
			(((long)addr[3] & 0x0ffL) << 0L);
	}
	
	/**
	 * LongIpを文字列変換.
	 * @param addr LongIpを設定します.
	 * @return String IPV4形式の文字列が返却されます.
	 */
	protected static final String longIpToString(long addr) {
		return new StringBuilder()
			.append((addr & 0x0ff000000L) >> 24L)
			.append(".")
			.append((addr & 0x000ff0000L) >> 16L)
			.append(".")
			.append((addr & 0x00000ff00L) >> 8L)
			.append(".")
			.append((addr & 0x0000000ffL) >> 0L)
			.toString();
	}
	
	/**
	 * ドメインチェック.
	 * @param addr IPV4のアドレス(xxx.xxx.xxx.xxx)か
	 *             ドメイン名を設定します.
	 * @return Boolean ドメインチェックが行われた場合は
	 *                 true か false が返却されます.
	 *                 それ以外は null が返却されます.
	 */
	protected Boolean isEqDomain(String addr) {
		if(domain != null) {
			try {
				return InetAddress.getByName(domain)
					.equals(InetAddress.getByName(addr));
			} catch(Exception e) {
				return false;
			}
		}
		return null;
	}
	
	/**
	 * ドメインチェック.
	 * @param addr InetAddressを設定します.
	 * @return Boolean ドメインチェックが行われた場合は
	 *                 true か false が返却されます.
	 *                 それ以外は null が返却されます.
	 */
	protected Boolean isEqDomain(InetAddress addr) {
		if(domain != null) {
			try {
				return InetAddress.getByName(domain)
					.equals(addr);
			} catch(Exception e) {
				return false;
			}
		}
		return null;
	}
	
	/**
	 * IPアドレスの範囲内かチェック.
	 * @param addr LongIpを設定します.
	 * @return trueの場合範囲内です.
	 */
	protected boolean isRange(Long addr) {
		return addr != null &&
			addr >= min && addr <= max;
	}
	
	/**
	 * IPアドレスをチェック.
	 * @param addr IPアドレス及びドメイン名を設定します.
	 * @return boolean [true]の場合、このIPアドレスの範囲内です.
	 */
	public boolean isRange(String addr) {
		if(addr == null || addr.isEmpty()) {
			return false;
		}
		// ドメインで判別.
		final Boolean retDmn = isEqDomain(addr);
		if(retDmn != null) {
			// 判別出来た場合.
			return retDmn;
		}
		// LongIpで処理.
		final Long a = convertLongIp(addr);
		// LongIp変換出来ない場合.
		if(a == null) {
			// InetAddressで再度チェック.
			try {
				return isRange(InetAddress.getByName(addr));
			} catch(Exception e) {
				return false;
			}
		}
		// IP範囲で定義されている場合.
		return isRange(a);
	}
	
	/**
	 * IPアドレスをチェック.
	 * @param addr InetAddress を設定します.
	 * @return boolean [true]の場合、このIPアドレスの範囲内です.
	 */
	public boolean isRange(InetAddress addr) {
		// 指定アドレスがnullの場合.
		if(addr == null) {
			return false;
		}
		// ドメインで判別.
		Boolean retDmn = isEqDomain(addr);
		if(retDmn != null) {
			// 判別出来た場合.
			return retDmn;
		}
		// IPアドレスの範囲内かチェック.
		return isRange(convertLongIp(addr.getAddress()));
	}
	
	/**
	 * ドメインでのチェックか.
	 * @return boolean [true]の場合、ドメイン指定です.
	 */
	public boolean isDomain() {
		return domain != null;
	}
	
	/**
	 * ipRangeでのMask付きチェックか.
	 * @return boolean [true]の場合
	 *                 nnn.nnn.nnn.nnn/nnn の設定です.
	 */
	public boolean isIpMask() {
		return domain == null && mask != -1;
	}
	
	/**
	 * ipRangeでのヘッダ番号を取得.
	 * @return Integer たとえば 10.0.0.1 の場合 10 が
	 *                 返却されます.
	 */
	protected Integer getHead() {
		if(!isIpMask()) {
			return null;
		}
		return getHead(min);
	}
	
	/**
	 * LongIpヘッダ番号を取得.
	 * @param longIp LongIpを設定します.
	 * @return Integer たとえば LongIpの元のIPV4アドレスが10.0.0.1
	 *                 の場合 10 が返却されます.
	 */
	protected static Integer getHead(long longIp) {
		return (int)(((longIp & 0x00ff000000L) >> 24L) &
			0x0ffL);
	}
	
	
	@Override
	public boolean equals(Object n) {
		if(n == null || !(n instanceof IpV4Range)) {
			return false;
		} else if(n == this) {
			return true;
		}
		IpV4Range r = (IpV4Range)n;
		if(domain == null) {
			return min == r.min &&
					max == r.max &&
					mask == r.mask;
		} else if(r.domain != null) {
			try {
				return InetAddress
					.getByName(domain)
					.equals(InetAddress
						.getByName(r.domain));
			} catch(Exception e) {}
		}
		return false;
	}
	
	@Override
	public String toString() {
		if(domain != null) {
			return domain;
		} else if(mask == -1) {
			return new StringBuilder()
				.append(longIpToString(min))
				.append(" - ")
				.append(longIpToString(max))
				.toString();
		}
		return new StringBuilder()
			.append(longIpToString(min))
			.append("/").append(mask)
			.toString();
	}
}
