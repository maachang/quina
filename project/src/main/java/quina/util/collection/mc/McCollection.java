package quina.util.collection.mc;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * 複数条件抽出用Collection.
 * 
 * @param <T> 継承先のオブジェクト.
 * @param <V> 配列の要素.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public interface McCollection<T, V> {
	/**
	 * 新しいオブジェクトを生成します.
	 * @return McCollection<T, V> 新しいオブジェクトが返却されます.
	 */
	public McCollection<T, V> newInstance();
	
	/**
	 * Next取得条件をクリア.
	 */
	default void clearNext() {
	}
	
	/**
	 * 次の情報が読み込み可能かチェック.
	 * @return boolean trueの場合、読み込み可能です.
	 */
	public boolean hasNext();
	
	/**
	 * 次の情報を取得.
	 * @return V 次の情報が返却されます.
	 * @throws NoSuchElementException 次の情報が存在しない場合発生します.
	 */
	public V next() throws NoSuchElementException;
	
	/**
	 * 全情報をListとして取得.
	 * @return McList<Map<String, Object>> 全情報が返却されます.
	 */
	default McList<Map<String, Object>> getList() {
		return getList(-1);
	}
	
	/**
	 * 指定サイズの情報までをListとして取得.
	 * @param max 取得サイズを設定します.
	 *            -1を設定した場合、全体を取得します.
	 * @return McList<Map<String, Object>> 取得情報が返却されます.
	 */
	default McList<Map<String, Object>> getList(int limit) {
		final McList<Map<String, Object>> ret =
			new McList<Map<String, Object>>();
		getList(ret, limit);
		return ret;
	}
	
	/**
	 * 指定サイズの情報までをListとして取得.
	 * @param out 出力先のListオブジェクトを設定します.
	 * @return List Listオブジェクトが返却されます.
	 */
	default List getList(List out) {
		getList(out, -1);
		return out;
	}
	
	/**
	 * 指定サイズの情報までをListとして取得.
	 * @param out 出力先のListオブジェクトを設定します.
	 * @param limit 取得サイズを設定します.
	 *            -1を設定した場合、全体を取得します.
	 * @return List Listオブジェクトが返却されます.
	 */
	public List getList(List out, int limit);
	
	/**
	 * 情報の追加.
	 * @param value 追加対象の条件を設定します.
	 * @return boolean trueの場合、追加されました.
	 */
	default boolean add(V value) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 与えられた関数によって実装されたテストに合格した
	 * すべての配列からなる新しい配列を生成します.
	 * @param func テストに合格した場合 true が返却する
	 *             functionを設定します.
	 * @return T 新しいオブジェクトが返却されます.
	 */
	default T filter(BooleanFunction<V> func) {
		V value;
		McCollection<T, V> ret = newInstance();
		clearNext();
		while(hasNext()) {
			if(func.call(value = next())) {
				ret.add(value);
			}
		}
		return (T)ret;
	}
	
	/**
	 * 提供されたテスト関数を満たす配列内の
	 * 最初の要素の値を返します.
	 * @param func テストに合格した場合 true が返却する
	 *             functionを設定します.
	 * @return V 最初の要素が返却されます.
	 *           null の場合満たす要素は存在しません.
	 */
	default V find(BooleanFunction<V> func) {
		V value;
		clearNext();
		while(hasNext()) {
			if(func.call(value = next())) {
				return value;
			}
		}
		return null;
	}
	
	/**
	 * 配列内の指定されたテスト関数を満たす最初の要素の
	 * 位置を返します.
	 * @param func テストに合格した場合 true が返却する
	 *             functionを設定します.
	 * @return int 最初の要素位置が返却されます.
	 *             -1 の場合満たす要素は存在しません.
	 */
	default int findIndex(BooleanFunction<V> func) {
		int count = 0;
		clearNext();
		while(hasNext()) {
			if(func.call(next())) {
				return count;
			}
			count ++;
		}
		return -1;
	}
	
	/**
	 * 与えられた関数を、配列の各要素に対して一度ずつ実行します.
	 * @param func 配列の各要素を処理するfunctionを設定します.
	 */
	default void foreach(VoidFunction<V> func) {
		clearNext();
		while(hasNext()) {
			func.call(next());
		}
	}
	
	/**
	 * 特定の要素が配列に含まれているかどうかを
	 * true または false で返します。
	 * @param value 特定の要素を設定します.
	 * @return boolean true の場合含まれています.
	 */
	default boolean includes(V value) {
		V e;
		clearNext();
		while(hasNext()) {
			e = next();
			if(value == e ||
				value != null && value.equals(e)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 与えられた関数を配列のすべての要素に対して呼び出し、
	 * その結果からなる新しい配列を生成します
	 * @param func 新しい要素を作成するfunctionを設定します.
	 * @return T 新しいオブジェクトが返却されます.
	 */
	default T map(ValueFunction<V> func) {
		McCollection<T, V> ret = newInstance();
		clearNext();
		while(hasNext()) {
			ret.add(func.call(next()));
		}
		return (T)ret;
	}
	
	/**
	 * 配列のそれぞれの要素に対してユーザーが提供した「縮小」
	 * コールバック関数を呼び出します。
	 * その際、直前の要素における計算結果の返値を渡します。
	 * 配列のすべての要素に対して縮小関数を実行した結果が単一の
	 * 値が最終結果になります。
	 * @param func 計算用functionを設定します.
	 * @param initValue 初期値を設定します.
	 * @return Object 計算結果が返却されます.
	 */
	default Object reduce(CalcFunction<V> func, Object initValue) {
		Object ret = initValue;
		clearNext();
		while(hasNext()) {
			ret = func.call(ret, next());
		}
		return ret;
	}
	
	/**
	 * 配列の少なくとも一つの要素が、指定された関数で実装されたテストに
	 * 合格するかどうかをテストします.
	 * @param func テストに合格した場合 true が返却する
	 *             functionを設定します.
	 * @return boolean 一つでも合格した場合は true が返却されます.
	 */
	default boolean some(BooleanFunction<V> func) {
		clearNext();
		while(hasNext()) {
			if(func.call(next())) {
				return true;
			}
		}
		return false;
	}
}
