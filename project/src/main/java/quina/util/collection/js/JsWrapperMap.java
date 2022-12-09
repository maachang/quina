package quina.util.collection.js;

import java.util.Map;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;

import quina.exception.QuinaException;

/**
 * [graalvm専用].
 * JavaのMapをJavascriptで利用するラッパー.
 * またjavascript側で呼び出される getMemberメソッド
 * では、取得結果がListやMapや配列やDate系の場合は
 * それらはラッパーオブジェクトに自動変換されます.
 */
public class JsWrapperMap implements ProxyObject {
	private Map<String, Object> value;
	
	/**
	 * コンストラクタ.
	 * @param map Mapオブジェクトを設定します.
	 */
	public JsWrapperMap(Map<String, Object> map) {
		if(map == null) {
			throw new QuinaException("Argument is null.");
		}
		value = map;
	}
	
	/**
	 * JsProxyMapを取得.
	 * @param map mapを設定します.
	 * @return JsProxyMap オブジェクトが返却されます.
	 */
	public static final JsWrapperMap from(Map<String, Object> map) {
		return new JsWrapperMap(map);
	}
	
	@Override
	public Object getMember(String key) {
		return JsWrapperUtil.convertJsProxy(
			value.get(key));
	}

	@Override
	public Object getMemberKeys() {
		return new JsWrapperKeyArray(
			value.keySet().toArray());
	}

	@Override
	public boolean hasMember(String key) {
		return value.containsKey(key);
	}

	@Override
	public void putMember(String key, Value value) {
		this.value.put(key, value.isHostObject() ?
			value.asHostObject() : value);
	}
	
	@Override
	public boolean removeMember(String key) {
		if (value.containsKey(key)) {
			value.remove(key);
			return true;
		} else {
			return false;
		}
	}
	
	// key一覧.
	private static final class JsWrapperKeyArray
		implements ProxyArray {
		private Object[] list;
		protected JsWrapperKeyArray(Object[] list) {
			this.list = list;
		}
		@Override
		public Object get(long index) {
			return list[(int)index];
		}
		@Override
		public void set(long index, Value value) {
			throw new UnsupportedOperationException();
		}
		@Override
		public long getSize() {
			return list.length;
		}
	}
}
