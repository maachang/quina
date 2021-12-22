package quina.jdbc.storage;

import quina.exception.QuinaException;
import quina.jdbc.QuinaDataSource;
import quina.jdbc.io.IoStatement;
import quina.storage.Storage;
import quina.storage.StorageManager;

/**
 * JDBCStorageマネージャ.
 */
public class JDBCStorageManager
	implements StorageManager {
	
	// 対象データーソース.
	protected QuinaDataSource dataSource;
	
	/**
	 * コンストラクタ.
	 * @param ds QuinaDataSourceを設定します.
	 */
	public JDBCStorageManager(QuinaDataSource ds) {
		IoStatement.execute(ds, (ios) -> {
			// Storageテーブル初期化処理.
			JDBCStorageUtil.initStorage(ios);
			return null;
		});
		this.dataSource = ds;
	}
	
	/**
	 * QuinaDataSourceを取得.
	 * @return QuinaDataSource QuinaDataSourceが返却されます.
	 */
	protected QuinaDataSource getQuinaDataSource() {
		return dataSource;
	}
	
	// 名前チェック.
	private String checkName(String name) {
		if(name == null || (name = name.trim()).isEmpty()) {
			throw new QuinaException(
				"The specified Storage name has not been set.");
		}
		return name;
	}

	@Override
	public Storage createStorage(String name) {
		return (Storage)IoStatement.execute(dataSource, (ios) -> {
			final String sname = checkName(name);
			if(JDBCStorageUtil.isStorage(ios, sname)) {
				// 既に存在している場合.
				throw new QuinaException(
					"Storage \"" + sname +
					"\" with the specified name already exists.");
			}
			return JDBCStorageUtil.createStorage(
				dataSource, ios, sname);
		});
	}

	@Override
	public void removeStorage(String name) {
		IoStatement.execute(dataSource, (ios) -> {
			final String sname = checkName(name);
			// RDBMSからJDBCStorageを取得.
			JDBCStorage js = JDBCStorageUtil.getStorage(
				dataSource, ios, sname);
			// 取得できた場合削除処理.
			if(js != null) {
				JDBCStorageUtil.deleteStorage(ios,
					js.getManagerId());
			}
			return null;
		});
	}

	@Override
	public Storage getStorage(String name) {
		return (Storage)IoStatement.execute(dataSource, (ios) -> {
			final String sname = checkName(name);
			// JDBCStorageを取得.
			JDBCStorage js = JDBCStorageUtil.getStorage(
				dataSource, ios, sname);
			// 存在する場合.
			if(js != null) {
				// 前回アクセスから一定時間を超えてる場合はアクセス時間を更新.
				long time = System.currentTimeMillis();
				if(js.getUpdateTime() +
					JDBCStorageConstants.FIXED_INTERVAL_TIME < time) {
					JDBCStorageUtil.updateStorage(ios, js.managerId, time);
					ios.commit();
					js.setUpdateTime(time);
				}
			}
			return js;
		});
	}

	@Override
	public boolean isStorage(String name) {
		return (boolean)IoStatement.execute(dataSource, (ios) -> {
			final String sname = checkName(name);
			return JDBCStorageUtil.isStorage(
				ios, sname);
		});
	}

	@Override
	public int size() {
		return (int)IoStatement.execute(dataSource, (ios) -> {
			return JDBCStorageUtil.storageLength(ios);
		});
	}
}
