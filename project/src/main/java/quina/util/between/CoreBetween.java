package quina.util.between;

/**
 * betweenコア処理.
 */
final class CoreBetween<F extends Comparable<F>, V> {
	/**
	 * between開始条件.
	 */
	protected F start;
	
	/**
	 * between終了条件.
	 */
	protected F end;
	
	/**
	 * value.
	 */
	protected V value;
	
	/**
	 * 初期設定.
	 * @param start between開始条件を設定します.
	 * @param end between終了条件を設定します.
	 * @param value valueを設定します.
	 */
	protected void init(F start, F end, V value) {
		if(start != null && end != null) {
			if(start.compareTo(end) > 0 ) {
				F n = end;
				end = start;
				start = n;
			}
		}
		this.start = start;
		this.end = end;
		this.value = value;
	}
	
	/**
	 * 範囲内かチェック.
	 * @param n チェック対象の値を設定します.
	 * @return boolean trueの場合、範囲内です.
	 */
	protected boolean between(F n) {
		if(start != null) {
			if(end != null) {
				return (start.compareTo(n) <= 0 &&
					end.compareTo(n) >= 0);
			}
			return (start.compareTo(n) <= 0);
		} else if(end != null) {
			return (end.compareTo(n) >= 0);
		}
		return true;
	}
	
	/**
	 * 設定されたValueを取得します.
	 * @return V 対象のValueが返却.
	 */
	protected final V getValue() {
		return value;
	}
}
