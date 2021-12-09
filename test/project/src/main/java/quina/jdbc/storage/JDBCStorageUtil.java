package quina.jdbc.storage;

import quina.jdbc.QuinaDataSource;
import quina.jdbc.io.IoStatement;
import quina.jdbc.io.PrimaryKey;
import quina.jdbc.io.QueryResultRow;

/**
 * JDBCStorageユーティリティ.
 */
final class JDBCStorageUtil {
	
	/**
	 * JDBCStorageの初期テーブル群を生成.
	 * @param ds QuinDataSourceを設定します.
	 */
	public static final void initTable(QuinaDataSource ds) {
		IoStatement.execute(ds, (ios) -> {
			// storage管理テーブルを生成.
			ios.sql(JDBCStorageConstants.MANAGE_CREATE_TABLE_SQL);
			ios.executeUpdate();
			
			// storage管理テーブルにユニークインデックス(name)を生成.
			ios.sql(JDBCStorageConstants.MANAGE_CREATE_NAME_INDEX_SQL);
			ios.executeUpdate();
			
			// storage要素テーブルを生成.
			ios.sql(JDBCStorageConstants.ELEMENT_CREATE_TABLE_SQL);
			ios.executeUpdate();
			
			// storage要素テーブルにインデックス(man_id)を生成.
			ios.sql(JDBCStorageConstants.ELEMENT_CREATE_MAN_INDEX_SQL);
			ios.executeUpdate();
			
			// storage要素テーブルにユニークインデックス(man_id, key_name)を生成.
			ios.sql(JDBCStorageConstants.ELEMENT_CREATE_MAN_KEY_INDEX_SQL);
			ios.executeUpdate();
			
			return null;
		});
	}
	
	/**
	 * 新しいStorageを生成.
	 * @param ds QuinDataSourceを設定します.
	 * @param rootStorage Rootストレージの場合 trueを設定します.
	 * @param name ストレージ名を設定します.
	 *             この値は一意のものである必要があります.
	 * @return Long 対象ストレージのシーケンスIDが返却されます.
	 */
	public static final Long createStorage(
		QuinaDataSource ds, boolean rootStorage, String name) {
		return (Long)IoStatement.execute(ds, (ios) -> {
			final String tableName = JDBCStorageConstants.MANAGE_TABLE_NAME;
			// insert.
			ios.insertRow(tableName,
				"root_storage", rootStorage ? 1: 0,
				"name", name,
				"update_time", System.currentTimeMillis()
			);
			// コミット.
			ios.commit();
			// 生成されたidを取得.
			QueryResultRow row = ios.selectRow(
				tableName, PrimaryKey.of("name"), name);
			return row.getLong("id");
		});
	}
}
