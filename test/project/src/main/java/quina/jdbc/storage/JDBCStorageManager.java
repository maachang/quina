package quina.jdbc.storage;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import quina.exception.QuinaException;
import quina.jdbc.QuinaDataSource;
import quina.jdbc.QuinaJDBCService;
import quina.jdbc.io.IoStatement;
import quina.storage.Storage;
import quina.storage.StorageManager;
import quina.util.Flag;

/**
 * JDBCStorageマネージャ.
 */
public class JDBCStorageManager
	implements StorageManager {
	
	// 対象データーソース.
	protected String dataSourceName;
	
	// 対象データーソース.
	protected QuinaDataSource dataSource;
	
	// 初期化フラグ.
	protected final Flag initFlag = new Flag(false);
	
	// 破棄フラグ.
	protected final Flag destroyFlag = new Flag(false);
	
	// ロックオブジェクト.
	protected final Lock lock = new ReentrantLock();
	
	/**
	 * コンストラクタ.
	 * @param dsName JDBCStorage情報を展開する
	 *               データソース名を設定します.
	 *               nullや空の場合は、DataSourceの
	 *               デフォルト名で処理します.
	 */
	public JDBCStorageManager(String dsName) {
		if(dsName == null ||
			(dsName = dsName.trim()).isEmpty()) {
			dsName = null;
		}
		this.dataSourceName = dsName;
	}
	
	/**
	 * 初期化処理.
	 * Storageテーブルの初期化は実際の実行処理で呼び出す.
	 */
	protected QuinaDataSource init() {
		// 既に初期化済みの場合.
		if(initFlag.get()) {
			return dataSource;
		}
		lock.lock();
		try {
			// 既に初期化済みの場合.
			if(initFlag.get()) {
				return dataSource;
			}
			// QuinaJDBCServiceを取得.
			QuinaJDBCService service = QuinaJDBCService.getService();
			String name = dataSourceName;
			// データソース名が設定されていない場合.
			if(name == null) {
				// デフォルトのデータソース名をセット.
				name = service.getDefaultDataSourceName();
			}
			// データーソースを取得.
			final QuinaDataSource ds = service.getDataSource(name);
			// データーソースの取得に失敗した場合.
			if(ds == null) {
				// 例外.
				throw new QuinaException(
					"The specified data source (\"" +
					name + "\") does not exist. ");
			}
			// 確定したデーターソースと名前をセット.
			// Storageテーブル初期化処理.
			IoStatement.execute(ds, (ios) -> {
				JDBCStorageUtil.initStorage(ios);
				ios.commit();
				return null;
			});
			// 確定内容をセット.
			dataSource = ds;
			dataSourceName = name;
			initFlag.set(true);
		} finally {
			lock.unlock();
		}
		return dataSource;
	}
	
	/**
	 * オブジェクトを破棄.
	 */
	protected void destroy() {
		// 破棄フラグをON.
		destroyFlag.set(true);
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
		// 破棄済みの場合は処理しない.
		if(destroyFlag.get()) {
			return null;
		}
		final String sname = checkName(name);
		return (Storage)IoStatement.execute(init(), (ios) -> {
			if(JDBCStorageUtil.isStorage(ios, sname)) {
				// 既に存在している場合エラー.
				throw new QuinaException(
					"Storage \"" + sname +
					"\" with the specified name already exists.");
			}
			// 生成処理.
			return JDBCStorageUtil.createStorage(
				dataSource, ios, sname);
		});
	}

	@Override
	public void removeStorage(String name) {
		// 破棄済みの場合は処理しない.
		if(destroyFlag.get()) {
			return;
		}
		final String sname = checkName(name);
		IoStatement.execute(init(), (ios) -> {
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
		// 破棄済みの場合は処理しない.
		if(destroyFlag.get()) {
			return null;
		}
		final String sname = checkName(name);
		return (Storage)IoStatement.execute(init(), (ios) -> {
			// JDBCStorageを取得.
			JDBCStorage ret = JDBCStorageUtil.getStorage(
				dataSource, ios, sname);
			// 存在する場合.
			if(ret != null) {
				// 前回アクセスから一定時間を超えてる場合はアクセス時間を更新.
				long time = System.currentTimeMillis();
				if(ret.getUpdateTime() +
					JDBCStorageConstants.FIXED_INTERVAL_TIME < time) {
					// アクセス時間を更新.
					JDBCStorageUtil.updateStorage(ios, ret.managerId, time);
					ios.commit();
					// 管理オブジェクトにタイムアウトセット.
					ret.setUpdateTime(time);
				}
			}
			return ret;
		});
	}

	@Override
	public boolean isStorage(String name) {
		// 破棄済みの場合は処理しない.
		if(destroyFlag.get()) {
			return false;
		}
		final String sname = checkName(name);
		return (boolean)IoStatement.execute(init(), (ios) -> {
			// Storage名存在を取得.
			return JDBCStorageUtil.isStorage(
				ios, sname);
		});
	}

	@Override
	public int size() {
		// 破棄済みの場合は処理しない.
		if(destroyFlag.get()) {
			return 0;
		}
		return (int)IoStatement.execute(init(), (ios) -> {
			// 長さを取得.
			return JDBCStorageUtil.storageLength(ios);
		});
	}
	
	/**
	 * QuinaDataSourceを取得.
	 * @return QuinaDataSource QuinaDataSourceが返却されます.
	 */
	protected QuinaDataSource getQuinaDataSource() {
		// 破棄済みの場合は処理しない.
		if(destroyFlag.get()) {
			return null;
		}
		// 初期化.
		return init();
	}
	
	/**
	 * 格納先のDataSource名を取得.
	 * @return String データソース名が返却されます.
	 */
	public String getDataSourceName() {
		// 破棄済みの場合は処理しない.
		if(destroyFlag.get()) {
			return null;
		}
		// 初期化.
		init();
		return dataSourceName;
	}
}
