package quina.http.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import quina.exception.QuinaException;
import quina.util.BinaryIO;
import quina.util.BooleanUtil;
import quina.util.DateUtil;
import quina.util.NumberUtil;
import quina.util.StringUtil;
import quina.util.collection.IndexKeyValueLockList;
import quina.util.collection.TypesClass;
import quina.util.collection.TypesConstants;

/**
 * セッションストレージ情報.
 */
public class SessionStorage {
	
	// ストレージ管理.
	protected IndexKeyValueLockList
		<String, Object> storage;
	
	/**
	 * コンストラクタ.
	 */
	public SessionStorage() {}
	
	/**
	 * クリアー.
	 */
	public void clear() {
		storage = null;
	}
	
	// 引数チェック.
	private static final void checkArgs(String key, Object value) {
		if(key == null || value == null ||
			(key = key.trim()).isEmpty()) {
			throw new QuinaException(
				"The specified argument is null.");
		}
	}
	
	// storage情報を取得.
	protected IndexKeyValueLockList<String, Object> storage() {
		if(storage == null) {
			storage = new IndexKeyValueLockList
				<String, Object>();
		}
		return storage;
	}
	
	/**
	 * 指定キー名に対して新しいSessionStorageを生成します.
	 * @param key キー名を設定します.
	 * @return Storage 新しく生成されたStorageが返却されます.
	 */
	public SessionStorage makeStorage(String key) {
		SessionStorage value = new SessionStorage();
		checkArgs(key, value);
		storage().put(key, value);
		return value;
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	public SessionStorage set(String key, Boolean value) {
		checkArgs(key, value);
		storage().put(key, value);
		return this;
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	public SessionStorage set(String key, Byte value) {
		checkArgs(key, value);
		storage().put(key, value);
		return this;
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	public SessionStorage set(String key, Short value) {
		checkArgs(key, value);
		storage().put(key, value);
		return this;
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	public SessionStorage set(String key, Integer value) {
		checkArgs(key, value);
		storage().put(key, value);
		return this;
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	public SessionStorage set(String key, Long value) {
		checkArgs(key, value);
		storage().put(key, value);
		return this;
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	public SessionStorage set(String key, Float value) {
		checkArgs(key, value);
		storage().put(key, value);
		return this;
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	public SessionStorage set(String key, Double value) {
		checkArgs(key, value);
		storage().put(key, value);
		return this;
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	public SessionStorage set(String key, String value) {
		checkArgs(key, value);
		storage().put(key, value);
		return this;
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	public SessionStorage set(String key, Date value) {
		checkArgs(key, value);
		storage().put(key, value);
		return this;
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
		if(storage != null) {
			return BooleanUtil.parseBoolean(storage.get(key));
		}
		return null;
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
		if(storage != null) {
			return NumberUtil.parseByte(storage.get(key));
		}
		return null;
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
		if(storage != null) {
			return NumberUtil.parseShort(storage.get(key));
		}
		return null;
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
		if(storage != null) {
			return NumberUtil.parseInt(storage.get(key));
		}
		return null;
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
		if(storage != null) {
			return NumberUtil.parseLong(storage.get(key));
		}
		return null;
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
		if(storage != null) {
			return NumberUtil.parseFloat(storage.get(key));
		}
		return null;
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
		if(storage != null) {
			return NumberUtil.parseDouble(storage.get(key));
		}
		return null;
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
		if(storage != null) {
			return StringUtil.parseString(storage.get(key));
		}
		return null;
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
		if(storage != null) {
			return DateUtil.parseDate(storage.get(key));
		}
		return null;
	}
	
	/**
	 * SessionStorageオブジェクトを取得.
	 * @param key キー名を設定します.
	 * @return Storageオブジェクトが返却されます.
	 */
	public SessionStorage getStorage(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		if(storage != null) {
			Object ret = storage.get(key);
			if(ret != null && ret instanceof SessionStorage) {
				return (SessionStorage)ret;
			}
		}
		return null;
	}
	
	/**
	 * 要素のタイプを取得.
	 * @param key キー名を設定します.
	 * @return TypesClass 要素のタイプが返却されます.
	 *                    TypesClass.Objectの場合SessionStorageです.
	 */
	public TypesClass getType(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		if(storage != null) {
			return getTypesClass(storage.get(key));
		}
		return null;
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
		if(storage != null) {
			return storage.containsKey(key);
		}
		return false;
	}
	
	/**
	 * 指定要素を削除.
	 * @param key キー名を設定します.
	 */
	public void remove(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return;
		}
		if(storage != null) {
			storage.remove(key);
		}
	}
	
	/**
	 * 要素数を取得.
	 * @return
	 */
	public int size() {
		if(storage != null) {
			return storage.size();
		}
		return 0;
	}
	
	// 指定オブジェクトのタイプクラスを取得.
	protected static final TypesClass getTypesClass(
		Object o) {
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
		} else if(o instanceof SessionStorage) {
			return TypesClass.Object;
		}
		return TypesClass.String;
	}
	
	/**
	 * Storageの読み込み.
	 * @param in 読み込み元のInputStreamを設定します.
	 * @throws IOException I/O例外.
	 */
	protected void load(InputStream in)
		throws IOException {
		String key;
		Object value;
		TypesClass cls;
		SessionStorage st;
		final byte[] tmp = BinaryIO.createTmp();
		// storageLength.
		final int len = BinaryIO.readSavingInt(in, tmp);
		for(int i = 0; i < len; i ++) {
			key = BinaryIO.readString(in, tmp);
			cls = TypesClass.getByTypeNo(
				BinaryIO.readInt1(in, tmp));
			switch(cls) {
				case Null:
					value = null;
					break;
				case Boolean:
					value = BinaryIO.readInt1(in, tmp) == 1;
					break;
				case Byte:
					value = (byte)BinaryIO.readInt1(in, tmp);
					break;
				case Short:
					value = (short)BinaryIO.readInt2(in, tmp);
					break;
				case Integer:
					value = BinaryIO.readInt4(in, tmp);
					break;
				case Long:
					value = BinaryIO.readLong(in, tmp);
					break;
				case Float:
					value = Float.intBitsToFloat(
						BinaryIO.readInt4(in, tmp));
					break;
				case Double:
					value = Double.longBitsToDouble(
						BinaryIO.readLong(in, tmp));
					break;
				case Date:
					value = new Date(BinaryIO.readLong(in, tmp));
					break;
				case String:
					value = BinaryIO.readString(in, tmp);
					break;
				case Object:
					st = new SessionStorage();
					st.load(in);
					value = st;
					break;
				default :
					// string.
					value = BinaryIO.readString(in, tmp);
					break;
			}
			storage().put(key, value);
			key = null; value = null;
		}
	}
	
	/**
	 * Storageの保存.
	 * @param out 保存先のOutputStreamを設定します.
	 * @throws IOException I/O例外.
	 */
	protected void save(OutputStream out)
		throws IOException {
		String key;
		Object value;
		TypesClass cls;
		final byte[] tmp = BinaryIO.createTmp();
		final int len = size();
		// storageLength.
		BinaryIO.writeSavingBinary(out, tmp, len);
		for(int i = 0; i < len; i ++) {
			key = storage.keyAt(i);
			value = storage.valueAt(i);
			// key.
			BinaryIO.writeString(out, tmp, key);
			// valueがnull.
			if(value == null) {
				BinaryIO.writeInt1(out, tmp,
					TypesConstants.TYPENO_NULL);
				continue;
			}
			// タイプ毎に保存.
			cls = getTypesClass(value);
			switch(cls) {
			case Boolean:
				BinaryIO.writeInt1(out, tmp, cls.getTypeNo());
				BinaryIO.writeInt1(out, tmp,
					((Boolean)value) ? 1 : 0);
				break;
			case Byte:
				BinaryIO.writeInt1(out, tmp, cls.getTypeNo());
				BinaryIO.writeInt1(out, tmp, (Integer)value);
				break;
			case Short:
				BinaryIO.writeInt1(out, tmp, cls.getTypeNo());
				BinaryIO.writeInt2(out, tmp, (Integer)value);
				break;
			case Integer:
				BinaryIO.writeInt1(out, tmp, cls.getTypeNo());
				BinaryIO.writeInt4(out, tmp, (Integer)value);
				break;
			case Long:
				BinaryIO.writeInt1(out, tmp, cls.getTypeNo());
				BinaryIO.writeLong(out, tmp, (Long)value);
				break;
			case Float:
				BinaryIO.writeInt1(out, tmp, cls.getTypeNo());
				BinaryIO.writeInt4(out, tmp,
					Float.floatToIntBits((Float)value));
				break;
			case Double:
				BinaryIO.writeInt1(out, tmp, cls.getTypeNo());
				BinaryIO.writeLong(out, tmp,
					Double.doubleToLongBits((Long)value));
				break;
			case Date:
				BinaryIO.writeInt1(out, tmp, cls.getTypeNo());
				BinaryIO.writeLong(out, tmp,
					((Date)value).getTime());
				break;
			case String:
				BinaryIO.writeInt1(out, tmp, cls.getTypeNo());
				BinaryIO.writeString(out, tmp, (String)value);
				break;
			case Object:
				BinaryIO.writeInt1(out, tmp, cls.getTypeNo());
				((SessionStorage)value).save(out);
				break;
			default:
				// Stirng.
				BinaryIO.writeInt1(out, tmp,
					TypesClass.String.getTypeNo());
				BinaryIO.writeString(out, tmp, value.toString());
				break;
			}
		}
	}
}
