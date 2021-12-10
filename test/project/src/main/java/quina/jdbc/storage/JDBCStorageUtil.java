package quina.jdbc.storage;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import quina.exception.QuinaException;
import quina.jdbc.QuinaDataSource;
import quina.jdbc.io.IoStatement;
import quina.jdbc.io.PrimaryKey;
import quina.jdbc.io.QueryResult;
import quina.jdbc.io.QueryResultRow;

/**
 * JDBCStorageユーティリティ.
 */
final class JDBCStorageUtil {
	
	/**
	 * JDBCStorageの初期テーブル群を生成.
	 * @param ds QuinDataSourceを設定します.
	 */
	public static final void initStorage(
		QuinaDataSource ds) {
		IoStatement.execute(ds, (ios) -> {
			// storage管理テーブルを生成.
			ios.sql(JDBCStorageConstants
				.MANAGE_CREATE_TABLE_SQL);
			ios.executeUpdate();
			
			// storage管理テーブルにユニーク
			// インデックス(name)を生成.
			ios.sql(JDBCStorageConstants
				.MANAGE_CREATE_NAME_INDEX_SQL);
			ios.executeUpdate();
			
			// storage要素テーブルを生成.
			ios.sql(JDBCStorageConstants
				.ELEMENT_CREATE_TABLE_SQL);
			ios.executeUpdate();
			
			// storage要素テーブルにインデックス
			// (man_id)を生成.
			ios.sql(JDBCStorageConstants
				.ELEMENT_CREATE_MAN_INDEX_SQL);
			ios.executeUpdate();
			
			// storage要素テーブルにユニーク
			// インデックス(man_id, key_name)を生成.
			ios.sql(JDBCStorageConstants
				.ELEMENT_CREATE_MAN_KEY_INDEX_SQL);
			ios.executeUpdate();
			
			return null;
		});
	}
	
	/**
	 * 新しいStorageを生成.
	 * @param ios IoStatementを設定します.
	 * @param name ストレージ名を設定します.
	 *             この値は一意のものである必要があります.
	 * @return Long ストレージ管理のシーケンスIDが返却されます.
	 */
	public static final Long createStorage(
		IoStatement ios, String name) {
		final String tableName = JDBCStorageConstants
			.MANAGE_TABLE_NAME;
		// insert.
		ios.insert(tableName
			,"name", name
			,"update_time", System.currentTimeMillis()
		);
		// コミット.
		ios.commit();
		// insertされたidを取得.
		QueryResultRow row = ios.selectRow(
			tableName, PrimaryKey.of("name"), name);
		return row.getLong("id");
	}
	
	/**
	 * 指定Storageを削除.
	 * @param ios IoStatementを設定します.
	 * @param manId Storage管理IDを設定します.
	 */
	public static final void deleteStorage(
		IoStatement ios, long manId) {
		deleteStorage(ios, true, manId);
	}
	
	/**
	 * 指定Storageを削除.
	 * @param ios IoStatementを設定します.
	 * @param rlementFlag 対象のStorage要素も
	 *                    削除する場合は true.
	 * @param manId Storage管理IDを設定します.
	 */
	public static final void deleteStorage(
		IoStatement ios, boolean elementFlag, long manId) {
		// 指定IDのStorage管理情報を削除.
		ios.delete(
			JDBCStorageConstants.MANAGE_TABLE_NAME
			,manId);
		// Storage要素を削除する場合.
		if(elementFlag) {
			// 指定Storage管理IDを指定してStorage要素情報を削除.
			ios.delete(JDBCStorageConstants.ELEMENT_TABLE_NAME
				,"man_id", manId);
		}
	}
	
	/**
	 * 指定Storageの更新時間をupdate.
	 * @param ios IoStatementを設定します.
	 * @param manId Storage管理IDを設定します.
	 * @param time 更新時間を設定します.
	 */
	public static final void updateStorage(
		IoStatement ios, long manId, long time) {
		ios.update(
			JDBCStorageConstants.MANAGE_TABLE_NAME
			,"id", manId
			,"update_time", time
		);
	}
	
	/**
	 * JDBCStorage群をロード.
	 * @param out ロードされたJDBCStorage群を格納する先を設定します.
	 * @param ds QuinDataSourceを設定します.
	 */
	public static final void loadStorage(
		Map<String, JDBCStorage> out, QuinaDataSource ds) {
		IoStatement.execute(ds, (ios) -> {
			// 一覧を取得.
			QueryResult res = ios.selectSQL(
				JDBCStorageConstants.MANAGE_TABLE_NAME)
				.executeQuery();
			// 取得した内容をJDBCStorageとして取得.
			QueryResultRow row;
			JDBCStorage storage;
			while(res.hasNext()) {
				row = res.next();
				storage = new JDBCStorage(
					ds, row.getLong("id")
					,row.getString("name")
					,row.getLong("update_time")
				);
				out.put(storage.getManagerName(), storage);
			}
			return null;
		});
	}
	
	/**
	 * メモリ上で管理しているROOTのStorageをRDBMSと同期.
	 * @param ds QuinDataSourceを設定します.
	 * @param man JDBCStorageを管理しているオブジェクトを設定します.
	 */
	public static final void syncRootStorage(
		QuinaDataSource ds, Map<String, JDBCStorage> man) {
		IoStatement.execute(ds, (ios) -> {
			// 一覧を取得.
			QueryResult res = ios.selectSQL(
				JDBCStorageConstants.MANAGE_TABLE_NAME)
				.executeQuery();
			// 既存の一覧を取得.
			Set<String> befores = new HashSet<String>();
			Iterator<String> it = man.keySet().iterator();
			while(it.hasNext()) {
				befores.add(it.next());
			}
			it = null;
			// 取得した内容をJDBCStorageとして取得.
			JDBCStorage storage;
			QueryResultRow row;
			while(res.hasNext()) {
				row = res.next();
				// RDBMSに格納されてるJDBCStorage名を取得.
				storage = man.get(row.getString("name"));
				// 存在しない場合は新規作成してメモリに追加.
				if(storage == null) {
					storage = new JDBCStorage(
						ds, row.getLong("id")
						,row.getString("name")
						,row.getLong("update_time")
					);
					man.put(storage.getManagerName(), storage);
				// 既存のものの場合はタイマー更新.
				} else {
					storage.setUpdateTime(row.getLong("update_time"));
				}
				// 今回RDBMS上で処理できたものは、ManagerNameから削除.
				befores.remove(storage.getManagerName());
			}
			// メモリ上にあってRDBMS上に無いものは削除.
			it = befores.iterator();
			while(it.hasNext()) {
				man.remove(it.next());
			}
			return null;
		});
	}
	
	/**
	 * メモリ上のJDBCStorageとRDBMS側の内容の同期を取る.
	 * @param ds QuinDataSourceを設定します.
	 * @param storage 更新したいJDBCStorageを設定します.
	 * @return 更新されたJDBCStorageが返却されます.
	 */
	public static final JDBCStorage syncStorage(
		QuinaDataSource ds, JDBCStorage storage) {
		return (JDBCStorage)IoStatement.execute(ds, (ios) -> {
			QueryResultRow row = ios.selectRow(
				JDBCStorageConstants.MANAGE_TABLE_NAME, storage.getManagerId());
			if(!storage.getManagerName().equals(row.getString("name"))) {
				throw new QuinaException(
					"JDBC Storage with the specified name (\"" +
					row.getString("name") + "\") does not exist.");
			}
			storage.setUpdateTime(row.getLong("update_time"));
			return storage;
		});
	}
}
