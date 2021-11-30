package quina.http.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import quina.exception.QuinaException;
import quina.util.BooleanUtil;
import quina.util.DateUtil;
import quina.util.NumberUtil;
import quina.util.StringUtil;
import quina.util.collection.IndexKeyValueList;
import quina.util.collection.TypesClass;

/**
 * セッションストレージ.
 */
public class SessionStorage {
	
	// 前回アクセスした時間.
	private long accessTime = -1L;
	
	// ストレージ.
	private IndexKeyValueList<String, Object> storage =
		new IndexKeyValueList<String, Object>();
	
	/**
	 * コンストラクタ.
	 */
	public SessionStorage() {
		accessTime = System.currentTimeMillis();
	}
	
	/**
	 * クリアー.
	 */
	public void clear() {
		storage.clear();
	}
	
	// 引数チェック.
	private static final void checkArgs(String key, Object value) {
		if(key == null || value == null || (key = key.trim()).isEmpty()) {
			throw new QuinaException(
				"The specified argument is null.");
		}
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 */
	public void set(String key, Boolean value) {
		checkArgs(key, value);
		storage.put(key, value);
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 */
	public void set(String key, Byte value) {
		checkArgs(key, value);
		storage.put(key, value);
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 */
	public void set(String key, Short value) {
		checkArgs(key, value);
		storage.put(key, value);
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 */
	public void set(String key, Integer value) {
		checkArgs(key, value);
		storage.put(key, value);
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 */
	public void set(String key, Long value) {
		checkArgs(key, value);
		storage.put(key, value);
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 */
	public void set(String key, Float value) {
		checkArgs(key, value);
		storage.put(key, value);
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 */
	public void set(String key, Double value) {
		checkArgs(key, value);
		storage.put(key, value);
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 */
	public void set(String key, String value) {
		checkArgs(key, value);
		storage.put(key, value);
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 */
	public void set(String key, Date value) {
		checkArgs(key, value);
		storage.put(key, value);
	}
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Boolean 要素が返却されます.
	 */
	public Boolean getBoolean(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return BooleanUtil.parseBoolean(storage.get(key));
	}
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Byte 要素が返却されます.
	 */
	public Byte getByte(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return NumberUtil.parseByte(storage.get(key));
	}
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Short 要素が返却されます.
	 */
	public Short getShort(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return NumberUtil.parseShort(storage.get(key));
	}
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Integer 要素が返却されます.
	 */
	public Integer getInteger(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return NumberUtil.parseInt(storage.get(key));
	}
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Long 要素が返却されます.
	 */
	public Long getLong(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return NumberUtil.parseLong(storage.get(key));
	}
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Float 要素が返却されます.
	 */
	public Float getFloat(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return NumberUtil.parseFloat(storage.get(key));
	}
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Double 要素が返却されます.
	 */
	public Double getDouble(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return NumberUtil.parseDouble(storage.get(key));
	}

	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return String 要素が返却されます.
	 */
	public String getString(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return StringUtil.parseString(storage.get(key));
	}
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Date 要素が返却されます.
	 */
	public Date getDate(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return DateUtil.parseDate(storage.get(key));
	}
	
	/**
	 * 要素のタイプを取得.
	 * @param key キー名を設定します.
	 * @return TypesClass 要素のタイプが返却されます.
	 */
	public TypesClass getType(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return getTypesClass(storage.get(key));
	}
	
	/**
	 * 要素存在確認.
	 * @param key キー名を設定します.
	 * @return boolean trueの場合、存在します.
	 */
	public boolean contains(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return false;
		}
		return storage.containsKey(key);
	}
	
	/**
	 * 指定要素を削除.
	 * @param key キー名を設定します.
	 */
	public void remove(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return;
		}
		storage.remove(key);
	}
	
	/**
	 * 要素数を取得.
	 * @return
	 */
	public int size() {
		return storage.size();
	}
	
	/**
	 * アクセス時間を取得.
	 * @return long アクセス時間が返却されます.
	 */
	protected long getAccessTime() {
		return accessTime;
	}
	
	/**
	 * アクセス時間を更新.
	 */
	protected void updateAccessTime() {
		accessTime = System.currentTimeMillis();
	}
	
	// 指定オブジェクトのタイプクラスを取得.
	protected TypesClass getTypesClass(Object o) {
		if(o == null) {
			return null;
		} else if(o instanceof Boolean) {
			return TypesClass.Boolean;
		} else if(o instanceof Byte) {
			return TypesClass.Byte;
		} else if(o instanceof Short) {
			return TypesClass.Short;
		} else if(o instanceof Integer) {
			return TypesClass.Integer;
		} else if(o instanceof Long) {
			return TypesClass.Long;
		} else if(o instanceof Float) {
			return TypesClass.Float;
		} else if(o instanceof Double) {
			return TypesClass.Double;
		} else if(o instanceof String) {
			return TypesClass.String;
		} else if(o instanceof Date) {
			return TypesClass.Date;
		}
		return TypesClass.String;
	}
	
	/**
	 * セッションの読み込み.
	 * @param in 読み込み元のInputStreamを設定します.
	 * @throws IOException I/O例外.
	 */
	protected void load(InputStream in)
		throws IOException {
		
	}
	
	/**
	 * セッションの保存.
	 * @param out 保存先のOutputStreamを設定します.
	 * @throws IOException I/O例外.
	 */
	protected void save(OutputStream out)
		throws IOException {
		
	}
}
