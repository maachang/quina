package quina.util.collection.js;

import java.lang.reflect.Array;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;

import quina.exception.QuinaException;

/**
 * Javaの配列をJavascriptで利用するラッパー.
 * またjavascript側で呼び出される getメソッド
 * では、取得結果がListやMapや配列やDate系の場合は
 * それらはラッパーオブジェクトに自動変換されます.
 */
public class JsWrapperArray implements ProxyArray {
	private Object value;
	
	/**
	 * コンストラクタ.
	 * @param list Listオブジェクトを設定します.
	 */
	public JsWrapperArray(Object array) {
		if(array == null) {
			throw new QuinaException("Argument is null.");
		} else if(!array.getClass().isArray()) {
			throw new QuinaException("Not an array.");
		}
		this.value = array;
	}
	
	/**
	 * JsProxyArrayを取得.
	 * @param array arrayを設定します.
	 * @return JsProxyArray オブジェクトが返却されます.
	 */
	public static final JsWrapperArray from(Object array) {
		return new JsWrapperArray(array);
	}
	
	@Override
	public Object get(long index) {
		return JsWrapperUtil.convertJsProxy(
			Array.get(value, (int)index));
	}

	@Override
	public void set(long index, Value value) {
		Array.set(this.value, (int)index,
			value.isHostObject() ? value.asHostObject() : value);
	}

	@Override
	public long getSize() {
		return (long)Array.getLength(value);
	}
}
