package quina.util.collection.js;

import java.util.List;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;

import quina.exception.QuinaException;

/**
 * [graalvm専用].
 * JavaのListをJavascriptで利用するラッパー.
 * またjavascript側で呼び出される getメソッド
 * では、取得結果がListやMapや配列やDate系の場合は
 * それらはラッパーオブジェクトに自動変換されます.
 */
public class JsWrapperList implements ProxyArray {
	private List<Object> value;
	
	/**
	 * コンストラクタ.
	 * @param list Listオブジェクトを設定します.
	 */
	public JsWrapperList(List<Object> list) {
		if(list == null) {
			throw new QuinaException("Argument is null.");
		}
		this.value = list;
	}
	
	/**
	 * JsProxyListを取得.
	 * @param list listを設定します.
	 * @return JsProxyList オブジェクトが返却されます.
	 */
	public static final JsWrapperList from(List<Object> list) {
		return new JsWrapperList(list);
	}

	@Override
	public Object get(long index) {
		return JsWrapperUtil.convertJsProxy(
			value.get((int)index));
	}

	@Override
	public void set(long index, Value value) {
		this.value.set((int)index, value.isHostObject() ?
			value.asHostObject() : value);
	}

	@Override
	public long getSize() {
		return (long)value.size();
	}
	
	@Override
	public boolean remove(long index) {
		value.remove((int)index);
		return true;
	}
}
