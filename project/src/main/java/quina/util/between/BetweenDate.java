package quina.util.between;

import quina.util.DateUtil;

/**
 * 日付の範囲チェック.
 */
public class BetweenDate<V> implements Between<V> {
	// coreBetween.
	private final CoreBetween<Long, V> core =
		new CoreBetween<Long, V>();
	
	/**
	 * コンストラクタ.
	 * @param value 対象の要素を設定します.
	 */
	public BetweenDate(V value) {
		this((java.util.Date)null, (java.util.Date)null, value);
	}
	
	/**
	 * コンストラクタ.
	 * @param start 開始日付を設定します.
	 * @param end 終了日付を設定します.
	 * @param value 対象の要素を設定します.
	 */
	public BetweenDate(java.util.Date start, java.util.Date end, V value) {
		Long a = start == null ? null : start.getTime();
		Long b = end == null ? null : end.getTime();
		core.init(a, b, value);
	}
	
	/**
	 * コンストラクタ.
	 * @param start 開始日付を設定します.
	 * @param end 終了日付を設定します.
	 * @param value 対象の要素を設定します.
	 */
	public BetweenDate(String start, String end, V value) {
		this(start == null ? null : DateUtil.parseDate(start),
			end == null ? null : DateUtil.parseDate(end),
			value);
	}
	
	/**
	 * 指定したDateが範囲内かチェック.
	 * @param o 範囲内か調べるDateを設定します.
	 * @return boolean trueの場合、範囲内です.
	 */
	@Override
	public final boolean between(Object o) {
		if(o instanceof String) {
			o = DateUtil.parseDate((String)o);
		} else if(!(o instanceof java.util.Date)) {
			return false;
		}
		return core.between(((java.util.Date)o).getTime());
	}
	
	/**
	 * 設定されたValueを取得します.
	 * @return V 対象のValueが返却.
	 */
	@Override
	public final V getValue() {
		return core.getValue();
	}
}
