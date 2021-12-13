package quina.jdbc.storage;

import java.util.Date;

import quina.exception.QuinaException;
import quina.jdbc.QuinaDataSource;
import quina.jdbc.io.IoStatement;
import quina.storage.Storage;
import quina.util.BooleanUtil;
import quina.util.DateUtil;
import quina.util.NumberUtil;
import quina.util.collection.TypesClass;

/**
 * JDBCStorage.
 * RDBMSに対するStorageのI/O実装を行います.
 */
public class JDBCStorage implements Storage {
	// 対象データーソース.
	protected QuinaDataSource dataSource;
	
	// ストレージマネージャID.
	protected long managerId;
	
	// ストレージマネージャ名.
	protected String managerName;
	
	// 更新時間.
	protected long accessTime;
	
	/**
	 * コンストラクタ.
	 * @param ds QuinaDataSourceを設定します.
	 * @param manId 管理IDを設定します.
	 * @param manName 管理名を設定します.
	 * @param time 前回の更新時間を設定します.
	 */
	public JDBCStorage(
		QuinaDataSource ds, long manId, String manName, long time) {
		this.dataSource = ds;
		this.managerId = manId;
		this.managerName = manName;
		this.accessTime = time;
	}
	
	/**
	 * 感理IDを取得.
	 * @return long 感理IDが返却されます.
	 */
	protected long getManagerId() {
		return managerId;
	}
	
	/**
	 * 感理名を取得.
	 * @return String 感理名が返却されます.
	 */
	protected String getManagerName() {
		return managerName;
	}
	
	/**
	 * 最終アクセス時間を更新.
	 * @param time 更新時間を設定します.
	 */
	protected void setUpdateTime(long time) {
		accessTime = time;
	}
	
	@Override
	public long getUpdateTime() {
		return accessTime;
	}
	
	// storageが存在しない場合は処理しない.
	protected boolean isStorage(IoStatement ios) {
		// storageが存在する場合.
		if(JDBCStorageUtil.isStorage(ios, managerName)) {
			// 前回アクセスから一定時間を超えてる場合はアクセス時間を更新.
			long time = System.currentTimeMillis();
			if(accessTime +
				JDBCStorageConstants.FIXED_INTERVAL_TIME < time) {
				JDBCStorageUtil.updateStorage(ios, managerId, time);
				accessTime = time;
			}
			return true;
		}
		return false;
	}
	
	// keyの有効性確認.
	protected String checkKey(String key) {
		if(key == null || (key = key.trim()).isEmpty()) {
			throw new QuinaException(
				"The specified Storage element key has not been set.");
		}
		return key;
	}
	
	// storageの存在確認.
	protected void checkStorage(IoStatement ios) {
		// storageが存在する場合に要素追加.
		if(!isStorage(ios)) {
			// 存在しない場合は410エラー.
			// 410:ファイルが削除されたため、ほぼ永久的にWebページが
			//     存在しない.
			throw new QuinaException(410,
				"Storage with the specified name \"" + managerName +
				"\" has already been deleted.");
		}
	}
	
	@Override
	public void clear() {
		IoStatement.execute(dataSource, (ios) -> {
			// storage存在確認.
			checkStorage(ios);
			// storageが存在する場合に実行.
			JDBCStorageUtil.clearElement(ios, managerId);
			return null;
		});
	}
	
	// １つの要素を追加.
	private Storage setValue(
		String key, int type, Object value) {
		final Storage ret = this;
		return (Storage)IoStatement.execute(dataSource, (ios) -> {
			// キーチェック.
			final String eKey = checkKey(key);
			// storage存在確認.
			checkStorage(ios);
			// １つの要素を追加・更新.
			JDBCStorageUtil.putElement(
				ios, managerId, eKey, type,
				value == null ? null : String.valueOf(value));
			return ret;
		});
	}

	@Override
	public Storage set(String key, Boolean value) {
		return setValue(
			key, TypesClass.Boolean.getTypeNo(), value);
	}

	@Override
	public Storage set(String key, Byte value) {
		return setValue(
			key, TypesClass.Byte.getTypeNo(), value);
	}

	@Override
	public Storage set(String key, Short value) {
		return setValue(
			key, TypesClass.Short.getTypeNo(), value);
	}

	@Override
	public Storage set(String key, Integer value) {
		return setValue(
			key, TypesClass.Integer.getTypeNo(), value);
	}

	@Override
	public Storage set(String key, Long value) {
		return setValue(
			key, TypesClass.Long.getTypeNo(), value);
	}

	@Override
	public Storage set(String key, Float value) {
		return setValue(
			key, TypesClass.Float.getTypeNo(), value);
	}

	@Override
	public Storage set(String key, Double value) {
		return setValue(
			key, TypesClass.Double.getTypeNo(), value);
	}

	@Override
	public Storage set(String key, String value) {
		return setValue(
			key, TypesClass.String.getTypeNo(), value);
	}

	@Override
	public Storage set(String key, Date value) {
		return setValue(
			key, TypesClass.Date.getTypeNo(),
			value == null ? null : value.getTime());
	}
	
	// 指定名の要素を取得.
	private String getValue(String key) {
		return (String)IoStatement.execute(dataSource, (ios) -> {
			// キーチェック.
			final String eKey = checkKey(key);
			// storage存在確認.
			checkStorage(ios);
			// 要素内容を取得.
			return JDBCStorageUtil.getElement(ios, managerId, eKey);
		});
	}

	@Override
	public Boolean getBoolean(String key) {
		return BooleanUtil.parseBoolean(getValue(key));
	}

	@Override
	public Byte getByte(String key) {
		return NumberUtil.parseByte(getValue(key));
	}

	@Override
	public Short getShort(String key) {
		return NumberUtil.parseShort(getValue(key));
	}

	@Override
	public Integer getInteger(String key) {
		return NumberUtil.parseInt(getValue(key));
	}

	@Override
	public Long getLong(String key) {
		return NumberUtil.parseLong(getValue(key));
	}

	@Override
	public Float getFloat(String key) {
		return NumberUtil.parseFloat(getValue(key));
	}

	@Override
	public Double getDouble(String key) {
		return NumberUtil.parseDouble(getValue(key));
	}

	@Override
	public String getString(String key) {
		return getValue(key);
	}

	@Override
	public Date getDate(String key) {
		return DateUtil.parseDate(getValue(key));
	}

	@Override
	public TypesClass getType(String key) {
		return (TypesClass)IoStatement.execute(dataSource, (ios) -> {
			// キーチェック.
			final String eKey = checkKey(key);
			// storage存在確認.
			checkStorage(ios);
			// 要素タイプを取得.
			return JDBCStorageUtil.getElementType(ios, managerId, eKey);
		});
	}

	@Override
	public boolean contains(String key) {
		return (boolean)IoStatement.execute(dataSource, (ios) -> {
			// キーチェック.
			final String eKey = checkKey(key);
			// storage存在確認.
			checkStorage(ios);
			// 要素タイプを取得.
			return JDBCStorageUtil.isElement(ios, managerId, eKey);
		});
	}

	@Override
	public void remove(String key) {
		IoStatement.execute(dataSource, (ios) -> {
			// キーチェック.
			final String eKey = checkKey(key);
			// storage存在確認.
			checkStorage(ios);
			// 要素を削除.
			JDBCStorageUtil.deleteElement(ios, managerId, eKey);
			return null;
		});
	}

	@Override
	public int size() {
		return (int)IoStatement.execute(dataSource, (ios) -> {
			// storage存在確認.
			checkStorage(ios);
			// 要素を削除.
			return JDBCStorageUtil.elementLength(
				ios, managerId);
		});
	}
}
