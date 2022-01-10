package quina.net;

import java.net.InetAddress;
import java.util.List;

import quina.exception.QuinaException;
import quina.util.collection.IndexKeyValueList;
import quina.util.collection.ObjectList;

/**
 * 
 */
public class IpPermission {
	
	// IpV4に対するMask範囲チェック用.
	// keyは先頭のIPアドレス番号(192.168.0.1なら 192)がセットされる.
	private IndexKeyValueList<Integer, ObjectList<IpV4Range>> rangeIpList;
	
	// Mask範囲チェック以外の内容.
	private ObjectList<IpV4Range> otherIpList;
	
	// 登録リスト.
	private ObjectList<IpV4Range> allList;
	
	/**
	 * コンストラクタ.
	 */
	public IpPermission() {
	}
	
	/**
	 * コンストラクタ.
	 * @param args 追加条件のIpV4Range群を設定します.
	 */
	public IpPermission(IpV4Range... args) {
		final int len = args == null ? 0 : args.length;
		for(int i = 0; i < len; i ++) {
			add(args[i]);
		}
	}
	
	/**
	 * コンストラクタ.
	 * @param args 追加条件のIpV4Range群を設定します.
	 */
	public IpPermission(ObjectList<IpV4Range> args) {
		final int len = args == null ? 0 : args.size();
		for(int i = 0; i < len; i ++) {
			add(args.get(i));
		}
	}
	
	/**
	 * コンストラクタ.
	 * @param args 追加条件のIpV4Range群を設定します.
	 */
	public IpPermission(List<IpV4Range> args) {
		final int len = args == null ? 0 : args.size();
		for(int i = 0; i < len; i ++) {
			add(args.get(i));
		}
	}
	
	/**
	 * 新しいIpV4Rangeを追加.
	 * @param ipV4Range 追加対象のIpV4Rangeを設定します.
	 * @return IpPermission オブジェクトが返却されます.
	 */
	public IpPermission add(IpV4Range ipV4Range) {
		// 追加対象がnullの場合.
		if(ipV4Range == null) {
			throw new QuinaException("The specified argument is null.");
		}
		// Ipマスク定義の場合.
		if(ipV4Range.isIpMask()) {
			// 範囲検索で処理.
			if(rangeIpList == null) {
				rangeIpList = new IndexKeyValueList<
					Integer, ObjectList<IpV4Range>>();
			}
			// 一番先頭のIpV4アドレスを取得.
			int key = ipV4Range.getHead();
			ObjectList<IpV4Range> list = rangeIpList.get(key);
			if(list == null) {
				list = new ObjectList<IpV4Range>();
				rangeIpList.put(key, list);
			}
			list.add(ipV4Range);
		// Ipマスク定義以外の場合.
		} else {
			if(otherIpList == null) {
				otherIpList = new ObjectList<IpV4Range>();
			}
			// 個別一致で処理.
			otherIpList.add(ipV4Range);
		}
		// 登録リストにセット.
		if(allList != null) {
			allList = new ObjectList<IpV4Range>();
		}
		allList.add(ipV4Range);
		return this;
	}
	
	/**
	 * 対象のアドレスが範囲内かチェック.
	 * @param addr アドレスを設定します.
	 * @return boolean trueの場合、範囲内です.
	 */
	public boolean isRange(String addr) {
		// null か 空文字の場合.
		if(addr == null || (addr = addr.trim()).isEmpty()) {
			return false;
		}
		// Ipマスク範囲検索の定義が存在する場合.
		Long longIp = null;
		if(rangeIpList != null) {
			// IpV4で定義されている場合.
			longIp = IpV4Range.convertLongIp(addr);
			if(longIp != null) {
				// 範囲検索を行う.
				Integer key = IpV4Range.getHead(longIp);
				ObjectList<IpV4Range> list = rangeIpList.get(key);
				if(list != null) {
					final int len = list.size();
					for(int i = 0; i < len; i ++) {
						if(list.get(i).isRange(longIp)) {
							return true;
						}
					}
				}
			}
		}
		// Ipマスク範囲検索以外の定義が存在する場合.
		if(otherIpList != null) {
			IpV4Range range;
			InetAddress iaddr = null;
			final int len = otherIpList.size();
			for(int i = 0; i < len; i ++) {
				range = otherIpList.get(i);
				// この定義がドメイン名で定義されている場合.
				if(range.isDomain()) {
					// InetAddress変換.
					if(iaddr == null) {
						try {
							iaddr = InetAddress.getByName(addr);
						} catch(Exception e) {
							return false;
						}
					}
					if(range.isEqDomain(iaddr)) {
						return true;
					}
				// この定義がIpマスク検索以外の範囲検索の場合.
				} else {
					if(longIp == null) {
						longIp = IpV4Range.convertLongIp(addr);
					}
					if(range.isRange(longIp)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * 対象のアドレスが範囲内かチェック.
	 * @param addr アドレスを設定します.
	 * @return boolean trueの場合、範囲内です.
	 */
	public boolean isRange(InetAddress addr) {
		// nullの場合.
		if(addr == null) {
			return false;
		}
		// Ipマスク範囲検索の定義が存在する場合.
		Long longIp = null;
		if(rangeIpList != null) {
			// IpV4で定義されている場合.
			longIp = IpV4Range.convertLongIp(addr);
			if(longIp != null) {
				// 範囲検索を行う.
				Integer key = IpV4Range.getHead(longIp);
				ObjectList<IpV4Range> list = rangeIpList.get(key);
				if(list != null) {
					final int len = list.size();
					for(int i = 0; i < len; i ++) {
						if(list.get(i).isRange(longIp)) {
							return true;
						}
					}
				}
			}
		}
		// Ipマスク範囲検索以外の定義が存在する場合.
		if(otherIpList != null) {
			IpV4Range range;
			final int len = otherIpList.size();
			for(int i = 0; i < len; i ++) {
				range = otherIpList.get(i);
				// この定義がドメイン名で定義されている場合.
				if(range.isDomain()) {
					if(range.isEqDomain(addr)) {
						return true;
					}
				// この定義がIpマスク検索以外の範囲検索の場合.
				} else {
					if(longIp == null) {
						longIp = IpV4Range.convertLongIp(addr);
					}
					if(range.isRange(longIp)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * 登録内容を取得.
	 * @param no 指定項番を設定します.
	 * @return IpV4Range 指定項番の登録内容が返却されます.
	 */
	public IpV4Range get(int no) {
		if(allList == null) {
			return null;
		}
		try {
			return allList.get(no);
		} catch(Exception e) {
			return null;
		}
	}
	
	/**
	 * 登録数を取得.
	 * @return int 登録数が返却されます.
	 */
	public int size() {
		return allList == null ? 0 : allList.size();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o ==null || !(o instanceof IpPermission)) {
			return false;
		} else if(o == this) {
			return true;
		}
		final IpPermission pm = (IpPermission)o;
		if(size() != pm.size()) {
			return false;
		}
		int i, j;
		boolean eq;
		final int len = size();
		for(i = 0; i < len; i ++) {
			eq = false;
			for(j = 0; j < len; j ++) {
				if(allList.get(i).equals(pm.allList.get(j))) {
					eq = true;
					break;
				}
			}
			if(!eq) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		final int len = size();
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				buf.append("\n");
			}
			buf.append("[")
				.append(i + 1)
				.append("] ")
				.append(allList.get(i));
		}
		return buf.toString();
	}
}
