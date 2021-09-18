package quina;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.exception.QuinaException;
import quina.json.Json;
import quina.util.collection.IndexKeyValueList;
import quina.util.collection.IndexMap;
import quina.util.collection.TreeKey;
import quina.util.collection.TypesClass;
import quina.util.collection.TypesElement;
import quina.util.collection.TypesKeyValue;

/**
 * QuinaConfig.
 *
 * QuinaInfo内で管理するコンフィグ情報です.
 */
public class QuinaConfig implements
	TypesKeyValue<String, Object> {
	/**
	 * QuinaConfig要素.
	 */
	public static final class QuinaConfigElement
		implements TypesElement<Object> {
		// 管理データ.
		private Object value;
		// 変換型.
		private TypesClass clazz;
		// デフォルト値.
		private Object defaultValue;

		/**
		 * コンストラクタ.
		 */
		protected QuinaConfigElement() {
		}

		/**
		 * オブジェクトを取得.
		 * @return Object オブジェクトが返却されます.
		 */
		@Override
		public Object get() {
			// null情報の場合はデフォルト値を返却.
			if(value == null) {
				return defaultValue;
			}
			return value;
		}

		/**
		 * 定義されてる型を取得します.
		 * @return TypesClass 定義されている型が返却されます.
		 */
		public TypesClass getTypesClass() {
			return clazz;
		}

		/**
		 * 定義されているデフォルト返却情報を取得します.
		 * @return Object デフォルト返却情報が返されます.
		 */
		public Object getDefaultValue() {
			return defaultValue;
		}

		/**
		 * データセット.
		 * @param val オブジェクトを設定します.
		 * @param clz 定義された型を設定します.
		 * @param def デフォルト値を設定します.
		 */
		protected QuinaConfigElement set(
			Object val, TypesClass clz, Object def) {
			this.value = val;
			this.clazz = clz;
			this.defaultValue = def;
			if(value != null) {
				try {
					switch(clz) {
					case Boolean: this.value = this.getBoolean(); break;
					case Byte: this.value = this.getByte(); break;
					case Short: this.value = this.getShort(); break;
					case Integer: this.value = this.getInteger(); break;
					case Long: this.value = this.getLong(); break;
					case Float: this.value = this.getFloat(); break;
					case Double: this.value = this.getDouble(); break;
					case Date: this.value = this.getDate(); break;
					case String: this.value = this.getString(); break;
					}
				} catch(Exception e) {
					this.value = null;
				}
			}
			return this;
		}

		@Override
		public String toString() {
			// 最初はvalueを取得.
			Object o = value;
			if(value == null) {
				// valueがnullの場合はデフォルトを取得.
				o = defaultValue;
				if(o == null) {
					// デフォルトもnullの場合.
					return "null";
				}
			}
			switch(clazz) {
			case Boolean:
			case Byte:
			case Short:
			case Integer:
			case Long:
			case Float:
			case Double:
				return o.toString();
			case String:
				return "\"" + o + "\"";
			case Date:
				return Json.dateToString((java.util.Date)o);
			}
			return o.toString();
		}
	}

	// コンフィグ名.
	private String name;

	// コンフィグパラメータ.
	private final IndexKeyValueList<Object, QuinaConfigElement> config =
		new IndexKeyValueList<Object, QuinaConfigElement>();

	// 予約キー情報.
	private final IndexKeyValueList<Object, QuinaConfigElement> reservationKeys =
		new IndexKeyValueList<Object, QuinaConfigElement>();
	
	// Read-Writeロックオブジェクト.
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * コンストラクタ.
	 */
	protected QuinaConfig() {}

	/**
	 * 予約キー定義.<br>
	 * @param name 対象のコンフィグ名を設定します.
	 * @param defines [キー名, 型, デフォルト値, キー名, 型, デフォルト値, ...]<br>
	 *                のように連続して定義します.<br>
	 *                キー名は文字列で設定します.<br>
	 *                型は TypesClass で定義します.<br>
	 *                デフォルト値が存在しない場合はnullを設定します.
	 */
	public QuinaConfig(String name, Object... defines) {
		Object k, t;
		if(name == null || name.isEmpty()) {
			throw new QuinaException("The config name has not been set.");
		}
		this.name = name;
		int len = defines.length;
		for(int i = 0; i < len; i += 3) {
			if((k = defines[i]) == null ||
				(t = defines[i + 1]) == null) {
				continue;
			}
			putReservation(k, t, defines[i + 2]);
		}
	}
	
	/**
	 * １つの予約キー情報を追加.
	 * @param key Key名を設定します.
	 * @param type Type情報を設定します.
	 * @param defVal デフォルト値を設定します.
	 * @return QuinaConfig オブジェクトが返却されます.
	 */
	public QuinaConfig putReservation(Object key, Object type, Object defVal) {
		TypesClass clazz = null;
		if(!(key instanceof TreeKey)) {
			key = new TreeKey("" + key);
		}
		if(type instanceof TypesClass) {
			clazz = (TypesClass)type;
		} else if(type instanceof String) {
			clazz = TypesClass.get((String)type);
		} else if(type instanceof Number) {
			clazz = TypesClass.get(((Number)type).intValue());
		} else {
			clazz = TypesClass.get(type.toString());
		}
		if(clazz != null) {
			lock.writeLock().lock();
			try {
				reservationKeys.put(key, new QuinaConfigElement().
					set(null, clazz, defVal));
			} finally {
				lock.writeLock().unlock();
			}
		}
		return this;
	}

	/**
	 * データをクリア.
	 */
	public void clear() {
		lock.writeLock().lock();
		try {
			config.clear();
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * コンフィグ情報を読み込む.
	 * @param configDir コンフィグファイル読み込み先のディレクトリを設定します.
	 */
	public void readConfig(String configDir) {
		final IndexMap<String, Object> json = QuinaUtil.loadJson(
			configDir, name);
		if(json != null) {
			setConfig(json);
		}
	}

	/**
	 * コンフィグデータをセット.
	 * @param json データをセットします.
	 */
	public void setConfig(Map<String, Object> json) {
		Entry<String, Object> e;
		Iterator<Entry<String, Object>> it = json
			.entrySet().iterator();
		while(it.hasNext()) {
			try {
				e = it.next();
				set(e.getKey(), e.getValue());
			} catch(Exception ex) {}
		}
	}
	
	/**
	 * コンフィグ名を取得.
	 * @return String コンフィグ名が返却されます.
	 */
	public String getName() {
		return name;
	}

	/**
	 * コンフィグデータをセット.
	 * @param key 対象のキー名を設定します.
	 * @param value 対象の要素を設定します.
	 * @return boolean 設定に成功した場合trueが返却されます.
	 */
	public boolean set(String key, Object value) {
		lock.writeLock().lock();
		try {
			QuinaConfigElement rsv;
			rsv = reservationKeys.get(key);
			if(rsv == null) {
				return false;
			}
			QuinaConfigElement em = new QuinaConfigElement();
			config.put(new TreeKey(key), em);
			em.set(value, rsv.getTypesClass(), rsv.getDefaultValue());
		} finally {
			lock.writeLock().unlock();
		}
		return true;
	}
	
	@Override
	public Object get(Object key) {
		lock.readLock().lock();
		try {
			QuinaConfigElement em =
				getElement(key.toString());
			if(em == null) {
				return null;
			}
			return em.get();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * 対象の要素を取得.
	 * @param key 対象のキー名を設定します.
	 * @return QuinaConfigElement コンフィグ要素が返却されます.
	 */
	public QuinaConfigElement getElement(Object key) {
		lock.readLock().lock();
		try {
			// 設定値を取得.
			QuinaConfigElement em = config.get(key);
			if(em == null) {
				// 存在しない場合はデフォルト値を取得.
				em = reservationKeys.get(key);
			}
			return em;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * 定義されているコンフィグ要素の数を取得.
	 * @return int 定義されているコンフィグ要素の数が返却されます.
	 */
	public int size() {
		lock.readLock().lock();
		try {
			return reservationKeys.size();
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * コンフィグの内容を文字出力.
	 * @param out 出力先のStringBuilderオブジェクトを設定します.
	 * @param space 行出力時のインデント値を設定します.
	 */
	public void toString(StringBuilder out, int space) {
		int k;
		// スペースセット.
		for(k = 0; k < space; k ++) {
			out.append(" ");
		}
		// コンフィグ名を出力.
		out.append("configName: ").append(name).append("\n");
		space += 2;
		TreeKey key;
		final int len = reservationKeys.size();
		boolean f = false;
		lock.readLock().lock();
		try {
			for(int i = 0; i < len; i ++) {
				key = (TreeKey)reservationKeys.keyAt(i);
				try {
					if(f) {
						out.append("\n");
					} else {
						f = true;
					}
					// スペースセット.
					for(k = 0; k < space; k ++) {
						out.append(" ");
					}
					out.append(key).append(": ");
					out.append(config.get(key));
				} catch(Exception e) {}
			}
		} finally {
			lock.readLock().unlock();
		}
	}
}
