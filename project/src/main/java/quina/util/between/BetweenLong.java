package quina.util.between;

import quina.exception.QuinaException;
import quina.util.NumberUtil;

/**
 * 数字の範囲チェック.
 */
public class BetweenLong<V> implements Between<V> {
	// coreBetween.
	private final BetweenCore<Long, V> core =
		new BetweenCore<Long, V>();
	
	/**
	 * コンストラクタ.
	 * @param value 対象の要素を設定します.
	 */
	public BetweenLong(V value) {
		this(null, null, value);
	}
	
	/**
	 * コンストラクタ.
	 * @param start 開始値を設定します.
	 * @param end 終了値を設定します.
	 * @param value 対象の要素を設定します.
	 */
	public BetweenLong(Object start, Object end, V value) {
		if(start != null && !NumberUtil.isNumeric(start)) {
			throw new QuinaException("The starting value is not a number.");
		} else if(end != null && !NumberUtil.isNumeric(end)) {
			throw new QuinaException("The end value is not a number.");
		}
		Long a = start == null ? null : NumberUtil.parseLong(start);
		Long b = end == null ? null : NumberUtil.parseLong(end);
		core.init(a, b, value);
	}
	
	/**
	 * 指定した値が範囲内かチェック.
	 * @param o 範囲内か調べるDateを設定します.
	 * @return boolean trueの場合、範囲内です.
	 */
	@Override
	public final boolean between(Object o) {
		if(!NumberUtil.isNumeric(o)) {
			return false;
		}
		return core.between(NumberUtil.parseLong(o));
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
