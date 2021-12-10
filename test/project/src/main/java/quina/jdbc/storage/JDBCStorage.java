package quina.jdbc.storage;

import java.util.Date;

import quina.jdbc.QuinaDataSource;
import quina.storage.Storage;
import quina.util.AtomicNumber64;
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
	
	// 更新時間.
	private final AtomicNumber64 updateTime;
	
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
		this.updateTime = new AtomicNumber64(time);
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
	
	protected void setUpdateTime(long time) {
		updateTime.set(time);
	}
	
	protected long getUpdateTime() {
		return updateTime.get();
	}
	
	
}
