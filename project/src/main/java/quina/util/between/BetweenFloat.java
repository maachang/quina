package quina.util.between;

import quina.exception.QuinaException;
import quina.util.NumberUtil;

/**
 * 浮動小数点の範囲チェック.
 */
public class BetweenFloat<V> implements Between<V> {
	// coreBetween.
	private final CoreBetween<Float, V> core =
		new CoreBetween<Float, V>();
	
	/**
	 * コンストラクタ.
	 * @param value 対象の要素を設定します.
	 */
	public BetweenFloat(V value) {
		this(null, null, value);
	}
	
	/**
	 * コンストラクタ.
	 * @param start 開始値を設定します.
	 * @param end 終了値を設定します.
	 * @param value 対象の要素を設定します.
	 */
	public BetweenFloat(Object start, Object end, V value) {
		if(start != null && !NumberUtil.isNumeric(start)) {
			throw new QuinaException("The starting value is not a number.");
		} else if(end != null && !NumberUtil.isNumeric(end)) {
			throw new QuinaException("The end value is not a number.");
		}
		Float a = start == null ? null : NumberUtil.parseFloat(start);
		Float b = end == null ? null : NumberUtil.parseFloat(end);
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
		return core.between(NumberUtil.parseFloat(o));
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