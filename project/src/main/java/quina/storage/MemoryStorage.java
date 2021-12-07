package quina.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import quina.exception.QuinaException;
import quina.util.BinaryIO;
import quina.util.BooleanUtil;
import quina.util.DateUtil;
import quina.util.NumberUtil;
import quina.util.StringUtil;
import quina.util.collection.TypesClass;
import quina.util.collection.TypesConstants;

/**
 * メモリストレージ情報.
 */
public class MemoryStorage implements Storage {
	
	// ストレージ管理.
	protected Map<String, Object> keyValue =
		new ConcurrentHashMap<String, Object>();
	
	/**
	 * コンストラクタ.
	 */
	public MemoryStorage() {}
	
	/**
	 * クリアー.
	 */
	public void clear() {
		keyValue.clear();
	}
	
	// 引数チェック.
	private static final void checkArgs(String key, Object value) {
		if(key == null || value == null ||
			(key = key.trim()).isEmpty()) {
			throw new QuinaException(
				"The specified argument is null.");
		}
	}
	
	/**
	 * 指定キー名に対して新しいStorageを生成します.
	 * @param key キー名を設定します.
	 * @return Storage 新しく生成されたStorageが返却されます.
	 */
	@Override
	public Storage makeStorage(String key) {
		Storage value = new MemoryStorage();
		checkArgs(key, value);
		keyValue.put(key, value);
		return value;
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage set(String key, Boolean value) {
		checkArgs(key, value);
		keyValue.put(key, value);
		return this;
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage set(String key, Byte value) {
		checkArgs(key, value);
		keyValue.put(key, value);
		return this;
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage set(String key, Short value) {
		checkArgs(key, value);
		keyValue.put(key, value);
		return this;
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage set(String key, Integer value) {
		checkArgs(key, value);
		keyValue.put(key, value);
		return this;
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage set(String key, Long value) {
		checkArgs(key, value);
		keyValue.put(key, value);
		return this;
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage set(String key, Float value) {
		checkArgs(key, value);
		keyValue.put(key, value);
		return this;
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage set(String key, Double value) {
		checkArgs(key, value);
		keyValue.put(key, value);
		return this;
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage set(String key, String value) {
		checkArgs(key, value);
		keyValue.put(key, value);
		return this;
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage set(String key, Date value) {
		checkArgs(key, value);
		keyValue.put(key, value);
		return this;
	}
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Boolean 要素が返却されます.
	 */
	@Override
	public Boolean getBoolean(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return BooleanUtil.parseBoolean(keyValue.get(key));
	}
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Byte 要素が返却されます.
	 */
	@Override
	public Byte getByte(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return NumberUtil.parseByte(keyValue.get(key));
	}
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Short 要素が返却されます.
	 */
	@Override
	public Short getShort(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return NumberUtil.parseShort(keyValue.get(key));
	}
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Integer 要素が返却されます.
	 */
	@Override
	public Integer getInteger(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return NumberUtil.parseInt(keyValue.get(key));
	}
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Long 要素が返却されます.
	 */
	@Override
	public Long getLong(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return NumberUtil.parseLong(keyValue.get(key));
	}
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Float 要素が返却されます.
	 */
	@Override
	public Float getFloat(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return NumberUtil.parseFloat(keyValue.get(key));
	}
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Double 要素が返却されます.
	 */
	@Override
	public Double getDouble(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return NumberUtil.parseDouble(keyValue.get(key));
	}

	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return String 要素が返却されます.
	 */
	@Override
	public String getString(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return StringUtil.parseString(keyValue.get(key));
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
		return DateUtil.parseDate(keyValue.get(key));
	}
	
	/**
	 * Storageオブジェクトを取得.
	 * @param key キー名を設定します.
	 * @return Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage getStorage(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		Object ret = keyValue.get(key);
		if(ret != null && ret instanceof Storage) {
			return (Storage)ret;
		}
		return null;
	}
	
	/**
	 * 要素のタイプを取得.
	 * @param key キー名を設定します.
	 * @return TypesClass 要素のタイプが返却されます.
	 *                    TypesClass.Objectの場合Storageです.
	 */
	@Override
	public TypesClass getType(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return null;
		}
		return getTypesClass(keyValue.get(key));
	}
	
	/**
	 * 要素存在確認.
	 * @param key キー名を設定します.
	 * @return boolean trueの場合、存在します.
	 */
	@Override
	public boolean contains(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return false;
		}
		return keyValue.containsKey(key);
	}
	
	/**
	 * 指定要素を削除.
	 * @param key キー名を設定します.
	 */
	@Override
	public void remove(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return;
		}
		keyValue.remove(key);
	}
	
	/**
	 * 要素数を取得.
	 * @return
	 */
	@Override
	public int size() {
		return keyValue.size();
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
		} else if(o instanceof Storage) {
			return TypesClass.Object;
		}
		return TypesClass.String;
	}
	
	/**
	 * Storageの読み込み.
	 * @param in 読み込み元のInputStreamを設定します.
	 * @throws IOException I/O例外.
	 */
	public void load(InputStream in)
		throws IOException {
		String key;
		Object value;
		TypesClass cls;
		MemoryStorage st;
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
					st = new MemoryStorage();
					st.load(in);
					value = st;
					break;
				default :
					// string.
					value = BinaryIO.readString(in, tmp);
					break;
			}
			keyValue.put(key, value);
			key = null; value = null;
		}
	}
	
	/**
	 * Storageの保存.
	 * @param out 保存先のOutputStreamを設定します.
	 * @throws IOException I/O例外.
	 */
	public void save(OutputStream out)
		throws IOException {
		Object value;
		TypesClass cls;
		final byte[] tmp = BinaryIO.createTmp();
		Entry<String, Object> e;
		Iterator<Entry<String, Object>> it =
			keyValue.entrySet().iterator();
		// storageLength.
		BinaryIO.writeSavingBinary(out, tmp, size());
		while(it.hasNext()) {
			e = it.next();
			value = e.getValue();
			// key.
			BinaryIO.writeString(out, tmp, e.getKey());
			e = null;
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
				((MemoryStorage)value).save(out);
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
