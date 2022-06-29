package quina.util.between;

import quina.util.DateUtil;

/**
 * 日付の範囲かをチェック.
 */
@SuppressWarnings("deprecation")
public class BetweenDate<V> implements Between<V> {
	// start未定義の条件.
	private static final long NOSET_START_DATE = new
		java.util.Date("1970/01/01").getTime();
	
	// end未指定の条件.
	private static final long NOSET_END_DATE = new
		java.util.Date("2099/12/31 23:59:59").getTime();
	// 開始日付.
	private final long start;
	// 終了日付.
	private final long end;
	
	// 紐づくValue.
	private V value;
	
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
	 * @param value 対象の要素を設定します.
	 */
	public BetweenDate(java.util.Date start, V value) {
		this(start, null, value);
	}
	
	/**
	 * コンストラクタ.
	 * @param start 開始日付を設定します.
	 * @param end 終了日付を設定します.
	 * @param value 対象の要素を設定します.
	 */
	public BetweenDate(java.util.Date start, java.util.Date end, V value) {
		long a = start == null ? NOSET_START_DATE : start.getTime();
		long b = end == null ? NOSET_END_DATE : end.getTime();
		if(a > b) {
			final long n = b;
			b = a;
			a = n;
		}
		this.start = a;
		this.end = b;
		this.value = value;
	}
	
	/**
	 * コンストラクタ.
	 * @param start 開始日付を設定します.
	 * @param value 対象の要素を設定します.
	 */
	public BetweenDate(String start, V value) {
		this(start, null, value);
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
		final long n = ((java.util.Date)o).getTime();
		return (start <= n && end >= n);
	}
	
	/**
	 * 設定されたValueを取得します.
	 * @return V 対象のValueが返却.
	 */
	@Override
	public final V getValue() {
		return value;
	}
}
