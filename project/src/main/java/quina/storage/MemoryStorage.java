package quina.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.locks.ReadWriteLock;

import quina.exception.QuinaException;
import quina.util.BinaryIO;
import quina.util.BooleanUtil;
import quina.util.DateUtil;
import quina.util.NumberUtil;
import quina.util.StringUtil;
import quina.util.collection.IndexKeyValueList;
import quina.util.collection.TypesClass;
import quina.util.collection.TypesConstants;

/**
 * メモリストレージ情報.
 */
public class MemoryStorage implements Storage {
	
	// マネージャオブジェクト.
	protected MemoryStorageManager manager;
	
	// このオブジェクトの管理名.
	protected String managerName;
	
	// ストレージ管理.
	protected IndexKeyValueList<String, Object> keyValue =
		new IndexKeyValueList<String, Object>();
	
	// 更新時間.
	protected long accessTime;
	
	// Read/Writeロック.
	protected final ReadWriteLock lock;
	
	/**
	 * コンストラクタ.
	 * @param man 対象のManagerオブジェクトを設定します.
	 * @param name 対象の管理名を設定します.
	 */
	public MemoryStorage(
		MemoryStorageManager man, String name) {
		this.manager = man;
		this.managerName = name;
		this.lock = man.lock;
		this.accessTime = System.currentTimeMillis();
	}
	
	/**
	 * 最終アクセス時間を更新.
	 * @param time 更新時間を設定します.
	 */
	protected void setUpdateTime(long time) {
		lock.writeLock().lock();
		try {
			accessTime = time;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * 最終アクセス時間を取得.
	 * @return long 最終アクセス時間が返却されます.
	 */
	@Override
	public long getUpdateTime() {
		lock.readLock().lock();
		try {
			return accessTime;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * クリアー.
	 */
	@Override
	public void clear() {
		lock.writeLock().lock();
		try {
			keyValue.clear();
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	// 引数チェック.
	private static final String checkArgs(
		String key, Object value) {
		if(key == null || value == null ||
			(key = key.trim()).isEmpty()) {
			throw new QuinaException(
				"The specified argument is null.");
		}
		return key;
	}
	
	// storageの存在確認.
	protected void checkStorage() {
		// storageが存在する場合に要素追加.
		if(!manager.isStorage(managerName)) {
			// 存在しない場合は410エラー.
			// 410:ファイルが削除されたため、ほぼ永久的にWebページが
			//     存在しない.
			throw new QuinaException(410,
				"Storage with the specified name \"" + managerName +
				"\" has already been deleted.");
		}
	}
	
	// アクセス時間を更新.
	protected final void updateTime() {
		accessTime = System.currentTimeMillis();
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage set(String key, Boolean value) {
		lock.writeLock().lock();
		try {
			key = checkArgs(key, value);
			checkStorage();
			keyValue.put(key, value);
			updateTime();
			return this;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage set(String key, Byte value) {
		lock.writeLock().lock();
		try {
			key = checkArgs(key, value);
			checkStorage();
			keyValue.put(key, value);
			updateTime();
			return this;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage set(String key, Short value) {
		lock.writeLock().lock();
		try {
			key = checkArgs(key, value);
			checkStorage();
			keyValue.put(key, value);
			updateTime();
			return this;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage set(String key, Integer value) {
		lock.writeLock().lock();
		try {
			key = checkArgs(key, value);
			checkStorage();
			keyValue.put(key, value);
			updateTime();
			return this;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage set(String key, Long value) {
		lock.writeLock().lock();
		try {
			key = checkArgs(key, value);
			checkStorage();
			keyValue.put(key, value);
			updateTime();
			return this;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage set(String key, Float value) {
		lock.writeLock().lock();
		try {
			key = checkArgs(key, value);
			checkStorage();
			keyValue.put(key, value);
			updateTime();
			return this;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage set(String key, Double value) {
		lock.writeLock().lock();
		try {
			key = checkArgs(key, value);
			checkStorage();
			keyValue.put(key, value);
			updateTime();
			return this;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage set(String key, String value) {
		lock.writeLock().lock();
		try {
			key = checkArgs(key, value);
			checkStorage();
			keyValue.put(key, value);
			updateTime();
			return this;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	@Override
	public Storage set(String key, Date value) {
		lock.writeLock().lock();
		try {
			key = checkArgs(key, value);
			checkStorage();
			keyValue.put(key, value);
			updateTime();
			return this;
		} finally {
			lock.writeLock().unlock();
		}
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
		lock.readLock().lock();
		try {
			checkStorage();
			updateTime();
			return BooleanUtil.parseBoolean(keyValue.get(key));
		} finally {
			lock.readLock().unlock();
		}
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
		lock.readLock().lock();
		try {
			checkStorage();
			updateTime();
			return NumberUtil.parseByte(keyValue.get(key));
		} finally {
			lock.readLock().unlock();
		}
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
		lock.readLock().lock();
		try {
			checkStorage();
			updateTime();
			return NumberUtil.parseShort(keyValue.get(key));
		} finally {
			lock.readLock().unlock();
		}
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
		lock.readLock().lock();
		try {
			checkStorage();
			updateTime();
			return NumberUtil.parseInt(keyValue.get(key));
		} finally {
			lock.readLock().unlock();
		}
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
		lock.readLock().lock();
		try {
			checkStorage();
			updateTime();
			return NumberUtil.parseLong(keyValue.get(key));
		} finally {
			lock.readLock().unlock();
		}
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
		lock.readLock().lock();
		try {
			checkStorage();
			updateTime();
			return NumberUtil.parseFloat(keyValue.get(key));
		} finally {
			lock.readLock().unlock();
		}
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
		lock.readLock().lock();
		try {
			checkStorage();
			updateTime();
			return NumberUtil.parseDouble(keyValue.get(key));
		} finally {
			lock.readLock().unlock();
		}
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
		lock.readLock().lock();
		try {
			checkStorage();
			updateTime();
			return StringUtil.parseString(keyValue.get(key));
		} finally {
			lock.readLock().unlock();
		}
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
		lock.readLock().lock();
		try {
			checkStorage();
			updateTime();
			return DateUtil.parseDate(keyValue.get(key));
		} finally {
			lock.readLock().unlock();
		}
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
		lock.readLock().lock();
		try {
			checkStorage();
			updateTime();
			return getTypesClass(keyValue.get(key));
		} finally {
			lock.readLock().unlock();
		}
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
		lock.readLock().lock();
		try {
			checkStorage();
			updateTime();
			return keyValue.containsKey(key);
		} finally {
			lock.readLock().unlock();
		}
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
		lock.writeLock().lock();
		try {
			checkStorage();
			updateTime();
			keyValue.remove(key);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * 要素数を取得.
	 * @return
	 */
	@Override
	public int size() {
		lock.readLock().lock();
		try {
			checkStorage();
			updateTime();
			return keyValue.size();
		} finally {
			lock.readLock().unlock();
		}
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
		final byte[] tmp = BinaryIO.createTmp();
		// 更新時間.
		accessTime = BinaryIO.readLong(in, tmp);
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
	protected void save(OutputStream out)
		throws IOException {
		TypesClass cls;
		final byte[] tmp = BinaryIO.createTmp();
		// 更新時間.
		BinaryIO.writeLong(out, tmp, accessTime);
		// storageLength.
		final int len = keyValue.size();
		BinaryIO.writeSavingBinary(out, tmp, len);
		String key;
		Object value;
		for(int i = 0; i < len; i ++) {
			key = keyValue.keyAt(i);
			value = keyValue.valueAt(i);
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
