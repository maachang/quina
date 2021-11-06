package quina.util.collection.mc;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import quina.util.collection.ObjectList;

/**
 * 複数条件抽出用配列.
 * 
 * @param <T> 配列の要素.
 */
@SuppressWarnings("unchecked")
public class McArray<T> extends AbstractList<T>
	implements McCollection<McList<T>, T> {
	// 配列
	private Object[] array;
	// Iterator.
	private Iterator<T> itr = null;
	
	/**
	 * 新しい空のオブジェクトを生成します.
	 * @return McCollection<T, V> 新しいオブジェクトが
	 *                            返却されます.
	 */
	@Override
	public McList<T> newInstance() {
		return new McList<T>();
	}
	
	/**
	 * Next取得条件をクリア.
	 */
	@Override
	public void clearNext() {
		itr = null;
	}
	
	/**
	 * 次の情報が読み込み可能かチェック.
	 * @return boolean trueの場合、読み込み可能です.
	 */
	@Override
	public boolean hasNext() {
		if(itr == null) {
			itr = this.iterator();
		}
		return itr.hasNext();
	}
	
	/**
	 * 次の情報を取得.
	 * @return T 次の情報が返却されます.
	 * @throws NoSuchElementException 次の情報が存在しない場合発生します.
	 */
	@Override
	public T next() throws NoSuchElementException {
		if(itr == null) {
			itr = this.iterator();
		}
		return itr.next();
	}
	
	/**
	 * 指定サイズの情報までをListとして取得.
	 * @param out 出力先のListオブジェクトを設定します.
	 * @param limit 取得サイズを設定します.
	 *            -1を設定した場合、全体を取得します.
	 * @return List Listオブジェクトが返却されます.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List getList(List out, int limit) {
		Iterator itr = this.iterator();
		limit = limit <= -1 ? -1 : limit;
		while(itr.hasNext()) {
			if(limit != -1 && out.size() >= limit) {
				break;
			}
			out.add(itr.next());
		}
		return out;
	}
	
	/**
	 * コンストラクタ.
	 */
	protected McArray() {
	}
	
	/**
	 * コンストラクタ.
	 * @param array Object配列を設定します.
	 */
	public McArray(List<T> list) {
		final int len = list == null ? 0 : list.size();
		this.array = new Object[len];
		for(int i = 0; i < len; i ++) {
			this.array[i] = list.get(i);
		}
	}
	
	/**
	 * コンストラクタ.
	 * @param list List情報を設定します.
	 */
	public McArray(ObjectList<T> list) {
		final int len = list == null ? 0 : list.size();
		this.array = new Object[len];
		for(int i = 0; i < len; i ++) {
			this.array[i] = list.get(i);
		}
	}
	
	/**
	 * コンストラクタ.
	 * @param array リスト情報を設定します.
	 */
	public McArray(Object... array) {
		final int len = array == null ? 0 : array.length;
		this.array = new Object[len];
		for(int i = 0; i < len; i ++) {
			this.array = array;
		}
	}
	
	@Override
	public void clear() {
		array = null;
	}

	@Override
	public T get(int index) {
		return (T)array[index];
	}

	@Override
	public int size() {
		return array.length;
	}
	
	@Override
	public boolean add(T value) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void add(int index, T value) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public T set(int index, T value) {
		Object ret = array[index];
		array[index] = value;
		return (T)ret;
	}
	
	@Override
	public T remove(int index) {
		return null;
	}
}
