package quina;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import quina.json.Json;
import quina.util.collection.IndexMap;
import quina.util.collection.TreeKey;
import quina.util.collection.TypesClass;
import quina.util.collection.TypesElement;

/**
 * QuinaConfig.
 *
 * QuinaInfo内で管理するコンフィグ情報です.
 */
public class QuinaConfig {
	/**
	 * QuinaConfig要素.
	 */
	public static final class QuinaConfigElement implements TypesElement<Object> {
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
		protected QuinaConfigElement set(Object val, TypesClass clz, Object def) {
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


	// コンフィグパラメータ.
	private final Map<Object, QuinaConfigElement> config =
		new ConcurrentHashMap<Object, QuinaConfigElement>();

	// 予約キー情報.
	private final IndexMap<Object, QuinaConfigElement> reservationKeys =
		new IndexMap<Object, QuinaConfigElement>();

	/**
	 * コンストラクタ.
	 */
	protected QuinaConfig() {}

	/**
	 * 予約キー定義.
	 * @param defines [キー名, 型, デフォルト値, キー名, 型, デフォルト値, ...]
	 *                のように連続して定義します.
	 *                キー名は文字列で設定します.
	 *                型は TypesClass で定義します.
	 *                デフォルト値が存在しない場合はnullを設定します.
	 */
	public QuinaConfig(Object... defines) {
		TreeKey key;
		TypesClass clazz;
		Object k, v;
		int len = defines.length;
		for(int i = 0; i < len; i += 3) {
			if((k = defines[i]) == null || (v = defines[i+1]) == null) {
				continue;
			}
			key = new TreeKey("" + k);
			if(v instanceof TypesClass) {
				clazz = (TypesClass)v;
			} else if(v instanceof String) {
				clazz = TypesClass.get((String)v);
			} else if(v instanceof Number) {
				clazz = TypesClass.get(((Number)v).intValue());
			} else {
				clazz = TypesClass.get(v.toString());
			}
			if(clazz != null) {
				// 型とデフォルト値をセット.
				reservationKeys.put(key, new QuinaConfigElement().
					set(null, clazz, defines[i + 2]));
			}
			key = null;
			clazz = null;
		}
	}

	/**
	 * データをクリア.
	 */
	public void clear() {
		config.clear();
	}

	/**
	 * コンフィグデータをセット.
	 * @param json データをセットします.
	 */
	public void setConfig(Map<String, Object> json) {
		Entry<String, Object> e;
		Iterator<Entry<String, Object>> it = json.entrySet().iterator();
		while(it.hasNext()) {
			try {
				e = it.next();
				set(e.getKey(), e.getValue());
			} catch(Exception ex) {}
		}
	}

	/**
	 * コンフィグデータをセット.
	 * @param key 対象のキー名を設定します.
	 * @param value 対象の要素を設定します.
	 * @retrun boolean 設定に成功した場合trueが返却されます.
	 */
	public boolean set(String key, Object value) {
		final QuinaConfigElement c = reservationKeys.get(key);
		if(c == null) {
			return false;
		}
		QuinaConfigElement em = config.get(key);
		if(em == null) {
			em = new QuinaConfigElement();
			config.put(new TreeKey(key), em);
		}
		em.set(value, c.getTypesClass(), c.getDefaultValue());
		return true;
	}

	/**
	 * 指定キーに対する定義された型を取得.
	 * @param key 対象のキー名を設定します.
	 * @return TypesClass 定義されている型が返却されます.
	 */
	public TypesClass getTypesClass(String key) {
		final QuinaConfigElement ret = reservationKeys.get(key);
		if(ret == null) {
			return null;
		}
		return ret.getTypesClass();
	}

	/**
	 * 指定キーに対する定義されたデフォルト値を取得.
	 * @param key 対象のキー名を設定します.
	 * @return Object 定義されているデフォルト値が返却されます.
	 */
	public Object getDefaultValue(String key) {
		final QuinaConfigElement ret = reservationKeys.get(key);
		if(ret == null) {
			return null;
		}
		return ret.getDefaultValue();
	}

	/**
	 * 対象の要素を取得.
	 * @param key 対象のキー名を設定します.
	 * @return QuinaConfigElement コンフィグ要素が返却されます.
	 */
	public QuinaConfigElement get(String key) {
		QuinaConfigElement ret = config.get(key);
		if(ret == null) {
			return reservationKeys.get(key);
		}
		return ret;
	}

	/**
	 * 定義されているコンフィグ要素の数を取得.
	 * @return int 定義されているコンフィグ要素の数が返却されます.
	 */
	public int size() {
		return reservationKeys.size();
	}

	/**
	 * 定義されているコンフィグ要素のキー名を取得します.
	 * @param no 指定項番を設定します.
	 * @return String キー名が返却されます.
	 */
	public String keyAt(int no) {
		Object ret = reservationKeys.keyAt(no);
		if(ret == null) {
			return null;
		}
		return ((TreeKey)ret).getKey();
	}

	/**
	 * コンフィグの内容を文字出力.
	 * @param out 出力先のStringBuilderオブジェクトを設定します.
	 * @param space 行出力時のインデント値を設定します.
	 */
	public void toString(StringBuilder out, int space) {
		TreeKey key;
		final int len = reservationKeys.size();
		boolean f = false;
		for(int i = 0; i < len; i ++) {
			key = (TreeKey)reservationKeys.keyAt(i);
			try {
				if(f) {
					out.append("\n");
				} else {
					f = true;
				}
				// スペースセット.
				for(int k = 0; k < space; k ++) {
					out.append(" ");
				}
				out.append(key).append(": ");
				out.append(config.get(key));
			} catch(Exception e) {}
		}
	}
}
