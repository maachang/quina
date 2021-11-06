package quina.util.collection.mc;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import quina.util.collection.ObjectList;

/**
 * 複数条件抽出用List.
 * 
 * @param <T> 配列の要素.
 */
public class McList<T> extends AbstractList<T>
	implements McCollection<McList<T>, T> {
	// リスト情報.
	private final ObjectList<T> list;
	// Iterator.
	private Iterator<T> itr = null;
	
	/**
	 * 新しい空のオブジェクトを生成します.
	 * @return McCollection<T, V> 新しいオブジェクトが返却されます.
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
	protected McList() {
		list = new ObjectList<T>();
	}
	
	/**
	 * コンストラクタ.
	 * @param array Object配列を設定します.
	 */
	public McList(List<T> list) {
		final int len = list == null ? 0 : list.size();
		this.list = new ObjectList<T>(len);
		for(int i = 0; i < len; i ++) {
			this.list.add(list.get(i));
		}
	}
	
	/**
	 * コンストラクタ.
	 * @param list List情報を設定します.
	 */
	public McList(ObjectList<T> list) {
		final int len = list == null ? 0 : list.size();
		this.list = new ObjectList<T>(len);
		for(int i = 0; i < len; i ++) {
			this.list.add(list.get(i));
		}
	}
	
	/**
	 * コンストラクタ.
	 * @param array リスト情報を設定します.
	 */
	@SuppressWarnings("unchecked")
	public McList(Object... array) {
		final int len = array == null ? 0 : array.length;
		this.list = new ObjectList<T>(len);
		for(int i = 0; i < len; i ++) {
			this.list.add((T)array[i]);
		}
	}
	
	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public T get(int index) {
		return list.get(index);
	}

	@Override
	public int size() {
		return list.size();
	}
	
	@Override
	public boolean add(T value) {
		list.add(value);
		return true;
	}
	
	@Override
	public void add(int index, T value) {
		list.add(index, value);
	}
	
	@Override
	public T set(int index, T value) {
		return list.set(index, value);
	}
	
	@Override
	public T remove(int index) {
		return list.remove(index);
	}
}
