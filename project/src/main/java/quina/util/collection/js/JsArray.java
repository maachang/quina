package quina.util.collection.js;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;

import quina.util.collection.ObjectList;

/**
 * [graalvm専用].
 * graalvmのjavascript用Arrayオブジェクト.
 * 
 * コメントにある[java]とある部分がJavaプログラム
 * から利用を想定したものです.
 * 
 * 逆に@Overrideの部分は、Javascriptが利用します.
 */
public class JsArray implements ProxyArray {
	private ObjectList<Object> value =
		new ObjectList<Object>();
	
	/**
	 * パラメータを設定してJsArrayを生成.
	 * @param args 追加するパラメータを設定します.
	 * @return JsArray 生成されたJsArrayが返却されます.
	 */
	public static final JsArray of(Object... args) {
		return new JsArray(args);
	}
	
	/**
	 * コンストラクタ.
	 * @param args 追加するパラメータを設定します.
	 */
	public JsArray(Object... args) {
		final int len = args.length;
		for(int i = 0; i < len; i ++) {
			value.add(args[i]);
		}
	}
	
	/**
	 * [java]登録オブジェクトをクリア.
	 * @return JsArray オブジェクトが返却されます.
	 */
	public JsArray clearObject() {
		this.value.clear();
		return this;
	}
	
	/**
	 *[java]情報追加.
	 * @param value
	 * @return JsArray オブジェクトが返却されます.
	 */
	public JsArray addObject(Object value) {
		this.value.add(value);
		return this;
	}
	
	/**
	 * [java]情報セット.
	 * @param index 対象の項番を設定します.
	 * @param value セットするオブジェクトを設定します.
	 * @return JsArray オブジェクトが返却されます.
	 */
	public JsArray setObject(int index, Object value) {
		this.value.set(index, value);
		return this;
	}
	
	/**
	 * [java]オブジェクトをRemove.
	 * @param key 対象のキーを設定します.
	 * @return JsArray オブジェクトが返却されます.
	 */
	public JsArray removeObject(int index) {
		value.remove(index);
		return this;
	}
	
	/**
	 * [java]オブジェクトをGet.
	 * @param key 対象のキーを設定します.
	 * @return Object オブジェクトが返却されます.
	 */
	public Object getObject(int index) {
		return value.get(index);
	}
	
	/**
	 * [java]オブジェクト登録数を取得.
	 * @return int オブジェクト登録数が返却されます.
	 */
	public int objectSize() {
		return value.size();
	}

	@Override
	public Object get(long index) {
		return value.get((int)index);
	}

	@Override
	public void set(long index, Value value) {
		setObject((int)index, value.isHostObject() ?
			value.asHostObject() : value);
	}

	@Override
	public boolean remove(long index) {
		value.remove((int)index);
		return true;
	}
	
	@Override
	public long getSize() {
		return (long)value.size();
	}
}
