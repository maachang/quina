package quina.jdbc.storage;

import java.util.Date;

import quina.jdbc.QuinaDataSource;
import quina.storage.Storage;
import quina.util.collection.TypesClass;

/**
 * JDBCStorage.
 * RDBMSに対してStorageのI/O実装を行います.
 */
public class JDBCStorage implements Storage {
	// 対象データーソース.
	private QuinaDataSource dataSource;
	
	// ストレージマネージャID.
	private long managerId;
	
	// ストレージマネージャ名.
	private String managerName;
	
	// コンストラクタ.
	private JDBCStorage() {
		
	}
	
	/**
	 * コンストラクタ.
	 * @param ds
	 * @param manId
	 * @param manName
	 */
	public JDBCStorage(QuinaDataSource ds, long manId, String manName) {
		this.dataSource = ds;
		this.managerId = manId;
		this.managerName = manName;
	}

	@Override
	public void clear() {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public Storage makeStorage(String key) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Storage set(String key, Boolean value) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Storage set(String key, Byte value) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Storage set(String key, Short value) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Storage set(String key, Integer value) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Storage set(String key, Long value) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Storage set(String key, Float value) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Storage set(String key, Double value) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Storage set(String key, String value) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Storage set(String key, Date value) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Boolean getBoolean(String key) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Byte getByte(String key) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Short getShort(String key) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Integer getInteger(String key) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Long getLong(String key) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Float getFloat(String key) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Double getDouble(String key) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public String getString(String key) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Date getDate(String key) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Storage getStorage(String key) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public TypesClass getType(String key) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public boolean contains(String key) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public void remove(String key) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public int size() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

}
