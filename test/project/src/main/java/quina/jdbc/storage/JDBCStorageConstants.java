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
		.append("id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY")
		.append(", name varchar(64)")
		.append(")")
		.toString();
	
	// Storage要素テーブル生成SQL.
	protected static final String ELEMENT_CREATE_TABLE_SQL =
		new StringBuilder("CREATE TABLE IF NOT EXISTS ")
		.append(ELEMENT_TABLE_NAME)
		.append(" (")
		.append("id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY")
		.append(", man_id BIGINT NOT NULL")
		.append(", key_name varchar(64) NOT NULL")
		.append(", val_type INT NOT NULL")
		.append(", val TEXT")
		.append(")")
		.toString();
	
	// Storage要素(man_id)インデックス生成SQL.
	protected static final String ELEMENT_CREATE_MAN_INDEX_SQL =
		new StringBuilder("CREATE INDEX IF NOT EXISTS ")
		.append(ELEMENT_TABLE_NAME).append("_MAN_IDX")
		.append(" ON ").append(ELEMENT_TABLE_NAME)
		.append("(man_id)")
		.toString();
	
	// Storage要素(man_id, key_name)UNIQUE インデックス生成SQL.
	protected static final String ELEMENT_CREATE_MAN_KEY_INDEX_SQL =
		new StringBuilder("CREATE UNIQUE INDEX IF NOT EXISTS ")
		.append(ELEMENT_TABLE_NAME).append("_MAN_KEY_IDX")
		.append(" ON ").append(ELEMENT_TABLE_NAME)
		.append("(man_id, key_name)")
		.toString();
	
	
	
}
