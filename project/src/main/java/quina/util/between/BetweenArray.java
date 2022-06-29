package quina.util.between;

import java.util.ArrayList;
import java.util.List;

/**
 * Between群.
 */
@SuppressWarnings("unchecked")
public class BetweenArray<V> {
	// between群管理.
	private final List<Between<V>> list =
		new ArrayList<Between<V>>();
	
	/**
	 * コンストラクタ.
	 * @param args 設定するBetween群を設定します.
	 */
	public BetweenArray(Between<V>... args) {
		push(args);
	}
	
	/**
	 * Between条件を追加.
	 * @param args 設定するBetween群を設定します.
	 * @return BetweenArray<V> このオブジェクトが返却されます.
	 */
	public BetweenArray<V> push(Between<V>... args) {
		if(args == null || args.length <= 0) {
			return this;
		}
		final int len = args.length;
		for(int i = 0; i < len; i ++) {
			list.add(args[0]);
		}
		return this;
	}
	
	/**
	 * 対象範囲内に当てはまるValueを取得.
	 * @param o 範囲内なのか条件を設定します.
	 * @return V 範囲内の要素が返却されます.
	 */
	public V rangeValue(Object o) {
		Between<V> bt;
		final int len = list.size();
		for(int i = 0; i < len; i ++) {
			if((bt = list.get(i)).between(o)) {
				return bt.getValue();
			}
		}
		return null;
	}
}
