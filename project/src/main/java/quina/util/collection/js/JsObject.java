package quina.util.collection.js;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;

import quina.util.collection.IndexKeyValueList;

/**
 * graalvmのjavascript用オブジェクト.
 * 
 * コメントにある[java]とある部分がJavaプログラム
 * から利用を想定したものです.
 * 
 * 逆に@Overrideの部分は、Javascriptが利用します.
 */
public class JsObject implements ProxyObject {
	private IndexKeyValueList<String, Object> value =
		new IndexKeyValueList<String, Object>();
	
	/**
	 * パラメータを設定してJsObjectを生成.
	 * @param args key, value の順で設定します.
	 * @return JsObject 生成されたJsObjectが返却されます.
	 */
	public static final JsObject of(Object... args) {
		return new JsObject(args);
	}
	
	/**
	 * コンストラクタ.
	 * @param args key, value の順で設定します.
	 */
	public JsObject(Object... args) {
		value.putAll(args);
	}
	
	/**
	 * [java]登録されてるオブジェクトをクリア.
	 * @return JsObject オブジェクトが返却されます.
	 */
	public JsObject clearObject() {
		this.value.clear();
		return this;
	}
	
	/**
	 * [java]オブジェクトをPut.
	 * @param key 対象のキーを設定します.
	 * @param value 対象の要素を設定します.
	 * @return JsObject オブジェクトが返却されます.
	 */
	public JsObject putObject(String key, Object value) {
		this.value.put(key, value);
		return this;
	}
	
	/**
	 * [java]登録されたオブジェクトを削除.
	 * @param key 対象のキーを設定します.
	 * @return JsObject オブジェクトが返却されます.
	 */
	public JsObject removeObject(String key) {
		this.value.remove(key);
		return this;
	}
	
	/**
	 * [java]登録されたオブジェクトが存在するかチェック.
	 * @param key 対象のキーを設定します.
	 * @return boolean trueの場合、存在します.
	 */
	public boolean containsObject(String key) {
		return this.value.containsKey(key);
	}
	
	/**
	 * [java]登録されたオブジェクトを取得.
	 * @param key 対象のキーを設定します.
	 * @return Object オブジェクトが返却されます.
	 */
	public Object getObject(String key) {
		return this.value.get(key);
	}
	
	/**
	 * [java]登録されたオブジェクト数を取得.
	 * @return int 登録されたオブジェクト数が返却されます.
	 */
	public int objectSize() {
		return this.value.size();
	}
	
	/**
	 * [java]登録されたオブジェクトの指定項番のキー名を取得.
	 * @param index 項番を設定します.
	 * @return String キー名が返却されます.
	 */
	public String getObjectKey(int index) {
		return this.value.keyAt(index);
	}

	@Override
	public Object getMember(String key) {
		return value.get(key);
	}

	@Override
	public Object getMemberKeys() {
		return new JsKeyArray(value);
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
	private static final class JsKeyArray implements ProxyArray {
		private IndexKeyValueList<String, Object> value =
			new IndexKeyValueList<String, Object>();
		protected JsKeyArray(IndexKeyValueList<String, Object> value) {
			this.value = value;
		}
		@Override
		public Object get(long index) {
			return value.keyAt((int)index);
		}
		@Override
		public void set(long index, Value value) {
			throw new UnsupportedOperationException();
		}
		@Override
		public long getSize() {
			return value.size();
		}
	}
}
