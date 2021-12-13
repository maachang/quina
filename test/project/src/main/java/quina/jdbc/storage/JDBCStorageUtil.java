package quina.jdbc.storage;

import quina.jdbc.QuinaDataSource;
import quina.jdbc.io.IoStatement;
import quina.jdbc.io.PrimaryKey;
import quina.jdbc.io.QueryColumns;
import quina.jdbc.io.QueryResult;
import quina.jdbc.io.QueryResultRow;
import quina.util.collection.TypesClass;

/**
 * JDBCStorageユーティリティ.
 */
final class JDBCStorageUtil {
	
	/**
	 * JDBCStorageの初期テーブル群を生成.
	 * @param ios IoStatementを設定します.
	 */
	public static final void initStorage(IoStatement ios) {
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
	}
	
	/**
	 * 新しいStorageを生成.
	 * @param ds 対象のDataSourceを設定します.
	 * @param ios IoStatementを設定します.
	 * @param name ストレージ名を設定します.
	 *             この値は一意のものである必要があります.
	 * @return JDBCStorage 新しく生成されたJDBCStorageが返却されます.
	 */
	public static final JDBCStorage createStorage(
		QuinaDataSource ds, IoStatement ios, String name) {
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
		return new JDBCStorage(
			ds, row.getLong("id")
			,row.getString("name")
			,row.getLong("update_time")
		);
	}
	
	/**
	 * 指定Storageを削除.
	 * @param ios IoStatementを設定します.
	 * @param manId Storage管理IDを設定します.
	 */
	public static final void deleteStorage(
		IoStatement ios, long manId) {
		// 指定IDのStorage管理情報を削除.
		ios.delete(
			JDBCStorageConstants.MANAGE_TABLE_NAME
			,manId);
		// 指定Storage管理IDを指定してStorage要素情報を削除.
		ios.delete(JDBCStorageConstants.ELEMENT_TABLE_NAME
			,"man_id", manId);
	}
	
	/**
	 * 指定Storageの更新時間を現在の時間にupdate.
	 * @param ios IoStatementを設定します.
	 * @param manId Storage管理IDを設定します.
	 * @return long 対象ののタイムアウト値が返却されます.
	 */
	public static final long updateStorage(IoStatement ios,
		long manId) {
		return updateStorage(
			ios, manId, System.currentTimeMillis());
	}
	
	/**
	 * 指定Storageの更新時間をupdate.
	 * @param ios IoStatementを設定します.
	 * @param manId Storage管理IDを設定します.
	 * @param time 更新時間を設定します.
	 * @return long 対象ののタイムアウト値が返却されます.
	 */
	public static final long updateStorage(
		IoStatement ios, long manId, long time) {
		ios.update(
			JDBCStorageConstants.MANAGE_TABLE_NAME
			,"id", manId
			,"update_time", time
		);
		return time;
	}
	
	/**
	 * 指定名のJDBCStorageを取得.
	 * @param ds QuinDataSourceを設定します.
	 * @param ios IoStatementを設定します.
	 * @param name ストレージ名を設定します.
	 * @return JDBCStorage Storage情報が返却されます.
	 */
	public static final JDBCStorage getStorage(
		QuinaDataSource ds, IoStatement ios, String name) {
		QueryResultRow row = ios.selectRow(
			JDBCStorageConstants.MANAGE_TABLE_NAME
			,PrimaryKey.of("name")
			,name);
		if(row == null) {
			return null;
		}
		return new JDBCStorage(
			ds, row.getLong("id")
			,row.getString("name")
			,row.getLong("update_time")
		);
	}
	
	/**
	 * 指定名のJDBCStorageを取得.
	 * @param ios IoStatementを設定します.
	 * @param name ストレージ名を設定します.
	 * @return boolean trueの場合は存在します.
	 */
	public static final boolean isStorage(
		IoStatement ios, String name) {
		QueryResult res = ios.selectSQL(
			JDBCStorageConstants.MANAGE_TABLE_NAME
			,"COUNT(id) as manager_length")
			.sql("where name=?")
			.params(name)
			.executeQuery();
		while(res.hasNext()) {
			return res.next().getInt(
				"manager_length") > 0;
		}
		return false;
	}
	
	/**
	 * 登録されているJDBCStorage数を取得.
	 * @param ios IoStatementを設定します.
	 * @return int 登録されているJDBCStorage数が返却されます.
	 */
	public static final int storageLength(IoStatement ios) {
		QueryResult res = ios.selectSQL(
			JDBCStorageConstants.MANAGE_TABLE_NAME
			,"COUNT(id) as manager_length")
			.executeQuery();
		while(res.hasNext()) {
			return res.next().getInt(
				"manager_length");
		}
		return 0;
	}
	
	/**
	 * 要素をput.
	 * @param ios IoStatementを設定します.
	 * @param manId Storage管理IDを設定します.
	 * @param name 要素名を設定します.
	 * @param type 要素タイプを設定します.
	 *             要素タイプはquina.util.collection.TypesClassの
	 *             getTypeNo()を設定.
	 * @param value 要素を設定します.
	 *              null要素の場合は"$n"が設定され、typeは
	 *              TypesClass.Null.getTypeNo()がセットされます.
	 */
	public static final void putElement(
		IoStatement ios, long manId, String name, int type,
		String value) {
		// valueがnullの場合.
		if(value == null) {
			value = "$n";
			type = TypesClass.Null.getTypeNo();
		}
		// 追加及び更新.
		ios.upsert(JDBCStorageConstants.ELEMENT_TABLE_NAME
			,PrimaryKey.of("man_id", "key_name")
			,"man_id", manId
			,"key_name", name
			,"val_type", type
			,"val", value
		);
	}
	
	/**
	 * 要素タイプを取得.
	 * @param ios IoStatementを設定します.
	 * @param manId Storage管理IDを設定します.
	 * @param name 要素名を設定します.
	 * @return TypesClass TypesClassが返却されます.
	 */
	public static final TypesClass getElementType(
		IoStatement ios, long manId, String name) {
		QueryResultRow row = ios.selectRow(
			JDBCStorageConstants.ELEMENT_TABLE_NAME
			,PrimaryKey.of("man_id", "key_name")
			,QueryColumns.of("val_type")
			,"man_id", manId
			,"key_name", name
		);
		if(row == null) {
			return null;
		}
		return TypesClass.getByTypeNo(
			row.getInt("val_type"));
	}
	
	/**
	 * 要素をget.
	 * @param ios IoStatementを設定します.
	 * @param manId Storage管理IDを設定します.
	 * @param name 要素名を設定します.
	 * @return String valueの内容が返却されます.
	 */
	public static final String getElement(
		IoStatement ios, long manId, String name) {
		QueryResultRow row = ios.selectRow(
			JDBCStorageConstants.ELEMENT_TABLE_NAME
			,PrimaryKey.of("man_id", "key_name")
			,QueryColumns.of("val_type", "val")
			,"man_id", manId
			,"key_name", name
		);
		// 存在しない場合.
		if(row == null) {
			return null;
		}
		// 型変換.
		TypesClass tc = TypesClass.getByTypeNo(
			row.getInt("val_type"));
		// 型変換がnullの場合.
		if(tc == TypesClass.Null) {
			return null;
		}
		// 文字列変換.
		return row.getString("val");
	}
	
	/**
	 * 要素の存在確認.
	 * @param ios IoStatementを設定します.
	 * @param manId Storage管理IDを設定します.
	 * @param name 要素名を設定します.
	 * @return boolean trueの場合、存在します.
	 */
	public static final boolean isElement(
		IoStatement ios, long manId, String name) {
		QueryResult res = ios.selectSQL(
			JDBCStorageConstants.ELEMENT_TABLE_NAME
			,"COUNT(id) AS element_length")
		.sql("where man_id=? and key_name=?")
		.params(manId, name)
		.executeQuery();
		while(res.hasNext()) {
			return res.next().getInt(
				"element_length") > 0;
		}
		return false;
	}
	
	/**
	 * 要素の削除処理.
	 * @param ios IoStatementを設定します.
	 * @param manId Storage管理IDを設定します.
	 * @param name 要素名を設定します.
	 */
	public static final void deleteElement(
		IoStatement ios, long manId, String name) {
		ios.delete(JDBCStorageConstants.ELEMENT_TABLE_NAME
			,PrimaryKey.of("man_id", "key_name")
			,"man_id", manId
			,"key_name", name
		);
	}
	
	/**
	 * 要素数を取得.
	 * @param ios IoStatementを設定します.
	 * @param manId Storage管理IDを設定します.
	 * @return int 要素数が返却されます.
	 */
	public static final int elementLength(
		IoStatement ios, long manId) {
		QueryResult res = ios.selectSQL(
			JDBCStorageConstants.ELEMENT_TABLE_NAME
			,"COUNT(id) AS element_length")
		.sql("where man_id=?")
		.params(manId)
		.executeQuery();
		while(res.hasNext()) {
			return res.next().getInt(
				"element_length");
		}
		return 0;
	}
	
	/**
	 * 要素をクリア.
	 * @param ios IoStatementを設定します.
	 * @param manId Storage管理IDを設定します.
	 */
	public static final void clearElement(
		IoStatement ios, long manId) {
		ios.delete(JDBCStorageConstants.ELEMENT_TABLE_NAME
			,PrimaryKey.of("man_id")
			,"man_id", manId
		);
	}
}
