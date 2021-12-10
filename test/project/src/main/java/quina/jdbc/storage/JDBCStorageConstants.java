package quina.jdbc.storage;

/**
 * JDBCストレージ定義.
 */
public class JDBCStorageConstants {
	
	// Storageテーブル基本名.
	protected static final String BASE_TABLE_NAME = "QUINA_JDBC_STORAGE";

	// Storage管理テーブル名.
	protected static final String MANAGE_TABLE_NAME = BASE_TABLE_NAME + "_MAN";
	
	// Storage各要素テーブル名.
	protected static final String ELEMENT_TABLE_NAME = BASE_TABLE_NAME + "_ELM";
	
	// Storage管理テーブル生成SQL.
	protected static final String MANAGE_CREATE_TABLE_SQL =
		new StringBuilder("CREATE TABLE IF NOT EXISTS ")
		.append(MANAGE_TABLE_NAME)
		.append(" (")
		// man_id
		.append("id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY")
		// storageName.
		.append(", name VARCHAR(96) NOT NULL")
		// storage更新時間.
		.append(", update_time BIGINT NOT NULL")
		.append(")")
		.toString();
	
	// Storage管理(name)ユニークインデックス生成SQL.
	protected static final String MANAGE_CREATE_NAME_INDEX_SQL =
		new StringBuilder("CREATE UNIQUE INDEX IF NOT EXISTS ")
		.append(MANAGE_TABLE_NAME).append("_NAME_IDX")
		.append(" ON ").append(MANAGE_TABLE_NAME)
		.append(" (name)")
		.toString();
	
	// Storage要素テーブル生成SQL.
	protected static final String ELEMENT_CREATE_TABLE_SQL =
		new StringBuilder("CREATE TABLE IF NOT EXISTS ")
		.append(ELEMENT_TABLE_NAME)
		.append(" (")
		// sequence id.
		.append("id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY")
		// MANAGE_TABLE_NAME の id.
		.append(", man_id BIGINT NOT NULL")
		// 要素名.
		.append(", key_name VARCHAR(64) NOT NULL")
		// 要素タイプはquina.util.collection.TypesClassのgetTypeNo()が
		// セットされる.
		.append(", val_type INT NOT NULL")
		// 要素内容
		// (val_type定義値を元にStringで内容を解釈する).
		.append(", val TEXT")
		.append(")")
		.toString();
	
	// Storage要素(man_id)インデックス生成SQL.
	protected static final String ELEMENT_CREATE_MAN_INDEX_SQL =
		new StringBuilder("CREATE INDEX IF NOT EXISTS ")
		.append(ELEMENT_TABLE_NAME).append("_MAN_IDX")
		.append(" ON ").append(ELEMENT_TABLE_NAME)
		.append(" (man_id)")
		.toString();
	
	// Storage要素(man_id, key_name)ユニークインデックス生成SQL.
	protected static final String ELEMENT_CREATE_MAN_KEY_INDEX_SQL =
		new StringBuilder("CREATE UNIQUE INDEX IF NOT EXISTS ")
		.append(ELEMENT_TABLE_NAME).append("_MAN_KEY_IDX")
		.append(" ON ").append(ELEMENT_TABLE_NAME)
		.append(" (man_id, key_name)")
		.toString();
	
	
	
}
