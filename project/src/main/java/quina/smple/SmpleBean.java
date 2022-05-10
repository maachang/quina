package quina.smple;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import quina.exception.QuinaException;
import quina.json.Json;
import quina.util.BooleanUtil;
import quina.util.DateUtil;
import quina.util.NumberUtil;
import quina.util.StringUtil;
import quina.util.collection.IndexMap;
import quina.util.collection.TypesKeyValue;

/**
 * Smple用Bean.
 */
public abstract class SmpleBean
	implements TypesKeyValue<String, Object> {
	// Bean要素.
	protected final IndexMap<String, Object> values =
		new IndexMap<String, Object>();
	
	/**
	 * 情報をクリア.
	 */
	public void clear() {
		values.clear();
	}
	
	/**
	 * 複数の定義を一挙にPutします.
	 * @param args 複数の定義を一挙に行います.
	 *             [0] key, [1] value, [2] key, [3] value ....
	 *             のように定義します.
	 * @return SmpleBean このオブジェクトが返却されます.
	 */
	public SmpleBean putArgs(Object... args) {
		int len = args.length;
		for(int i = 0; i < len; i += 2) {
			if(args[i] == null) {
				throw new QuinaException(
					"key [" + i + "] is defined as null. ");
			}
			put(args[i].toString(), args[i + 1]);
		}
		return this;
	}
	
	/**
	 * Map定義を一挙にPutします.
	 * @param map java.util.Mapオブジェクトを設定します.
	 * @return SmpleBean このオブジェクトが返却されます.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SmpleBean putMap(Map map) {
		Entry e;
		Iterator<Entry> itr = map.entrySet().iterator();
		while(itr.hasNext()) {
			e = itr.next();
			if(e.getKey() == null) {
				throw new QuinaException(
					"key is defined as null. ");
			}
			put(e.getKey().toString(), e.getValue());
		}
		return this;
	}
	
	
	/**
	 * 要素をセット.
	 * @param name 要素名を設定します.
	 * @param value 要素を設定します.
	 * @return SmpleBean このオブジェクトが返却されます.
	 */
	public SmpleBean put(String name, Object value) {
		if(name == null || name.isEmpty()) {
			return this;
		}
		values.put(name, value);
		return this;
	}
	
	/**
	 * 要素を削除.
	 * @param name 要素名を設定します.
	 * @return SmpleBean このオブジェクトが返却されます.
	 */
	public SmpleBean remove(String name) {
		if(name == null || name.isEmpty()) {
			return this;
		}
		values.remove(name);
		return this;
	}
	
	/**
	 * 要素を取得.
	 * @param name 要素名を設定します.
	 * @return Object 要素が返却されます.
	 */
	public Object get(Object name) {
		if(name == null || !(name instanceof String) ||
			((String)name).isEmpty()) {
			return null;
		}
		return values.get((String)name);
	}
	
	/**
	 * 要素が存在するか取得.
	 * @param name 要素名を設定します.
	 * @return boolean trueの場合、存在します.
	 */
	public boolean contains(String name) {
		if(name == null || name.isEmpty()) {
			return false;
		}
		return values.containsKey(name);
	}
	
	/**
	 * 設定データ数を取得.
	 * @return int データ数が返却されます.
	 */
	public int size() {
		return values.size();
	}
	
	/**
	 * 指定項番の要素名を取得.
	 * @param no 指定項番を設定します.
	 * @return String 要素名が返却されます.
	 */
	public String keyAt(int no) {
		return values.keyAt(no);
	}
	
	/**
	 * 指定項番の要素を取得.
	 * @param no 指定項番を設定します.
	 * @return Object 要素が返却されます.
	 */
	public Object valueAt(int no) {
		return values.valueAt(no);
	}
	
	
	@Override
	public String toString() {
		return Json.encode(values);
	}
	
	/**
	 * SmpleのArgsを変換.
	 * @param type 定義タイプを設定します.
	 * @param args 変換オブジェクトを設定します.
	 * @return Object 変換結果が返却されます.
	 */
	public static final Object convertArgs(
		String type, Object args) {
		if(args == null) {
			return null;
		} else if("boolean".equals(type) || "Boolean".equals(type)) {
			return BooleanUtil.parseBoolean(args);
		} else if("byte".equals(type) || "Byte".equals(type)) {
			return NumberUtil.parseByte(args);
		} else if("char".equals(type) || "Character".equals(type)) {
			return (char)(NumberUtil.parseInt(args) & 0x0000ffff);
		} else if("short".equals(type) || "Short".equals(type)) {
			return NumberUtil.parseShort(args);
		} else if("int".equals(type) || "Integer".equals(type)) {
			return NumberUtil.parseInt(args);
		} else if("long".equals(type) || "Long".equals(type)) {
			return NumberUtil.parseLong(args);
		} else if("float".equals(type) || "Float".equals(type)) {
			return NumberUtil.parseFloat(args);
		} else if("double".equals(type) || "Double".equals(type)) {
			return NumberUtil.parseDouble(args);
		} else if("String".equals(type) || "java.lang.String".equals(type)) {
			return StringUtil.parseString(args);
		} else if("Data".equals(type) || "java.util.Date".equals(type)) {
			return DateUtil.parseDate(args);
		}
		return args;
	}
	
}
