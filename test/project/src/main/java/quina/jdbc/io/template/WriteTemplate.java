package quina.jdbc.io.template;

import java.util.Map;

import quina.exception.QuinaException;
import quina.jdbc.io.QueryResult;
import quina.jdbc.io.DbUtil;
import quina.jdbc.io.PrimaryKey;
import quina.util.collection.IndexMap;
import quina.util.collection.ObjectList;

/**
 * 書き込みテンプレート.
 */
@SuppressWarnings("unchecked")
public interface WriteTemplate<T>
	extends BaseTemplate<T> {
	
	/**
	 * 更新実行.
	 * @param out out out[0]にQueryResultが返却されます.
	 * @param outCount outCount[0]に処理結果の件数が設定されます.
	 * @return T このオブジェクトが返却されます.
	 */
	public T executeUpdate(QueryResult[] out, long[] outCount);
	
	/**
	 * 更新実行.
	 * @return T このオブジェクトが返却されます.
	 */
	default T executeUpdate() {
		return executeUpdate(null, null);
	}
	
	/**
	 * 更新実行.
	 * @param out out[0]に処理結果の件数が設定されます.
	 * @return T このオブジェクトが返却されます.
	 */
	default T executeUpdate(long[] out) {
		return executeUpdate(null, out);
	}
	
	/**
	 * Update用のSQL文を生成.
	 * 使い方は以下のように使います.
	 * 
	 * IoStatement stmt = conn.ioStatement();
	 * stmt.params("age", 25, "name", "hoge")
	 *     .updateSQL("testTable")
	 *     .sql("where id=?")
	 *     .params(100);
	 *     .executeUpdate();
	 * 
	 * 以下のようなSQL文が実行されます.
	 * > updata testTable set age=25, name='hoge' where id=100;
	 * 
	 * @param tableName テーブル名を設定します.
	 * @param values 追加対象のkey=カラム名,value=要素の
	 *               Mapを設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T updateSQL(String tableName) {
		try {
			StringBuilder sqlBuf = clearSql();
			setParams(DbUtil.createUpdate(
				sqlBuf, tableName, getParams()));
			return (T)this;
		} catch(QuinaException qe) {
			// 登録されてたSQLとパラメータをクリア.
			clearSqlAndParmas();
			throw qe;
		} catch(Exception e) {
			// 登録されてたSQLとパラメータをクリア.
			clearSqlAndParmas();
			throw new QuinaException(e);
		}
	}
	
	/**
	 * Update用のSQL文を生成.
	 * 使い方は以下のように使います.
	 * 
	 * IoStatement stmt = conn.ioStatement();
	 * Map<String, Object> keyValues = new HashMap<>();
	 * keyValues.put("age", 25);
	 * keyValues.put("name", "hoge");
	 * stmt.updateSQL("testTable", keyValues)
	 *     .sql("where id=?")
	 *     .params(100);
	 *     .executeUpdate();
	 * 
	 * 以下のようなSQL文が実行されます.
	 * > updata testTable set age=25, name='hoge' where id=100;
	 * 
	 * @param tableName テーブル名を設定します.
	 * @param values 追加対象のkey=カラム名,value=要素の
	 *               Mapを設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T updateSQL(
		String tableName, Map<String, Object> values) {
		try {
			StringBuilder sqlBuf = clearSql();
			setParams(DbUtil.createUpdate(
				sqlBuf, getParams(), tableName, values));
			return (T)this;
		} catch(QuinaException qe) {
			// 登録されてたSQLとパラメータをクリア.
			clearSqlAndParmas();
			throw qe;
		} catch(Exception e) {
			// 登録されてたSQLとパラメータをクリア.
			clearSqlAndParmas();
			throw new QuinaException(e);
		}
	}
	
	/**
	 * Update用のSQL文を生成.
	 * 使い方は以下のように使います.
	 * 
	 * IoStatement stmt = conn.ioStatement();
	 * stmt.updateSQL("testTable",
	 *         "age", 25, "name", "hoge")
	 *     .sql("where id=?")
	 *     .params(100);
	 *     .executeUpdate();
	 * 
	 * 以下のようなSQL文が実行されます.
	 * > updata testTable set age=25, name='hoge' where id=100;
	 * 
	 * @param tableName テーブル名を設定します.
	 * @param values 追加対象のkey=カラム名,value=要素の
	 *               Mapを設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T updateSQL(
		String tableName, Object... values) {
		try {
			StringBuilder sqlBuf = clearSql();
			setParams(new ObjectList<Object>(values));
			setParams(DbUtil.createUpdate(
				sqlBuf, tableName, getParams()));
			return (T)this;
		} catch(QuinaException qe) {
			// 登録されてたSQLとパラメータをクリア.
			clearSqlAndParmas();
			throw qe;
		} catch(Exception e) {
			// 登録されてたSQLとパラメータをクリア.
			clearSqlAndParmas();
			throw new QuinaException(e);
		}
	}
	
	/**
	 * Delete用のSQL文を生成.
	 * 使い方は以下のように使います.
	 * 
	 * IoStatement stmt = conn.ioStatement();
	 * stmt.deleteSQL("testTable")
	 *     .sql("where id=?")
	 *     .params(100);
	 *     .executeUpdate();
	 * 
	 * 以下のようなSQL文が実行されます.
	 * > delete from testTable where id=100;
	 * 
	 * @param tableName テーブル名を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T deleteSQL(String tableName) {
		try {
			clearParmas();
			StringBuilder sqlBuf = clearSql();
			DbUtil.createDeleteSQL(sqlBuf, tableName);
			return (T)this;
		} catch(QuinaException qe) {
			// 登録されてたSQLとパラメータをクリア.
			clearSqlAndParmas();
			throw qe;
		} catch(Exception e) {
			// 登録されてたSQLとパラメータをクリア.
			clearSqlAndParmas();
			throw new QuinaException(e);
		}
	}
	
	/**
	 * 新規データを挿入.
	 * この処理の場合sql()呼び出しはせず以下のように実装します.
	 * 
	 * IoStatement stmt = conn.ioStatement();
	 * stmt.params("id", 100, "age", 25, "name", "hoge")
	 *     .insert("testTable")
	 *     .commit();
	 * 
	 * これにより"testTable"に対してid=100, age=25, name=hoge
	 * 内容がInsertされます.
	 * @param tableName テーブル名を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T insert(String tableName) {
		return insert(null, tableName);
	}
	
	/**
	 * 新規データを挿入.
	 * この処理の場合sql()呼び出しはせず以下のように実装します.
	 * 
	 * IoStatement stmt = conn.ioStatement();
	 * stmt.params("id", 100, "age", 25, "name", "hoge")
	 *     .insert(null, "testTable")
	 *     .commit();
	 * 
	 * これにより"testTable"に対してid=100, age=25, name=hoge
	 * 内容がInsertされます.
	 * @param out [0]に書き込みで発行された結果を返却します.
	 * @param tableName テーブル名を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T insert(
		QueryResult[] out, String tableName) {
		try {
			StringBuilder sqlBuf = clearSql();
			setParams(DbUtil.createInsert(
				sqlBuf, tableName, getParams()));
			executeUpdate(out, null);
			return (T)this;
		} finally {
			// 登録されてたSQLとパラメータをクリア.
			clearSqlAndParmas();
		}
	}
	
	/**
	 * 新規データを挿入.
	 * この処理の場合sql()呼び出しはせず以下のように実装します.
	 * 
	 * Map<String, Object> keyValues = new HashMap<>();
	 * keyValues.put("id", 100);
	 * keyValues.put("age", 25);
	 * keyValues.put("name", "hoge");
	 * IoStatement stmt = conn.ioStatement();
	 * stmt.insert("testTable", keyValues)
	 *     .commit();
	 * 
	 * これにより"testTable"に対してid=100, age=25, name=hoge
	 * 内容がInsertされます.
	 * @param tableName テーブル名を設定します.
	 * @param values 追加対象のkey=カラム名,value=要素の
	 *               Mapを設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T insert(
		String tableName, Map<String, Object> values) {
		return insert(null, tableName, values);
	}

	
	/**
	 * 新規データを挿入.
	 * この処理の場合sql()呼び出しはせず以下のように実装します.
	 * 
	 * Map<String, Object> keyValues = new HashMap<>();
	 * keyValues.put("id", 100);
	 * keyValues.put("age", 25);
	 * keyValues.put("name", "hoge");
	 * IoStatement stmt = conn.ioStatement();
	 * stmt.insert(null, "testTable", keyValues)
	 *     .commit();
	 * 
	 * これにより"testTable"に対してid=100, age=25, name=hoge
	 * 内容がInsertされます.
	 * @param out [0]に書き込みで発行された結果を返却します.
	 * @param tableName テーブル名を設定します.
	 * @param values 追加対象のkey=カラム名,value=要素の
	 *               Mapを設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T insert(QueryResult[] out, String tableName,
		Map<String, Object> values) {
		try {
			StringBuilder sqlBuf = clearSql();
			setParams(DbUtil.createInsert(
				sqlBuf, getParams(), tableName, values));
			executeUpdate(out, null);
			return (T)this;
		} finally {
			// 登録されてたSQLとパラメータをクリア.
			clearSqlAndParmas();
		}
	}
	
	/**
	 * 新規データを挿入.
	 * この処理の場合sql()呼び出しはせず以下のように実装します.
	 * 
	 * IoStatement stmt = conn.ioStatement();
	 * stmt.insert("testTable",
	 *     "id", 100, "age", 25, "name", "hoge")
	 *     .commit();
	 * 
	 * これにより"testTable"に対してid=100, age=25, name=hoge
	 * 内容がInsertされます.
	 * @param tableName テーブル名を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T insert(
		String tableName, Object... values) {
		return insert(null, tableName, values);
	}
	
	/**
	 * 新規データを挿入.
	 * この処理の場合sql()呼び出しはせず以下のように実装します.
	 * 
	 * IoStatement stmt = conn.ioStatement();
	 * stmt.insert(null, "testTable",
	 *     "id", 100, "age", 25, "name", "hoge")
	 *     .commit();
	 * 
	 * これにより"testTable"に対してid=100, age=25, name=hoge
	 * 内容がInsertされます.
	 * @param out [0]に書き込みで発行された結果を返却します.
	 * @param tableName テーブル名を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T insert(
		QueryResult[] out, String tableName, Object... values) {
		try {
			StringBuilder sqlBuf = clearSql();
			setParams(new ObjectList<Object>(values));
			setParams(DbUtil.createInsert(
				sqlBuf, tableName, getParams()));
			executeUpdate(out, null);
			return (T)this;
		} finally {
			// 登録されてたSQLとパラメータをクリア.
			clearSqlAndParmas();
		}
	}
	
	/**
	 * PrimaryKeyを設定して１行情報を更新.
	 * @param tableName 対象のテーブル名を設定します.
	 * @param primaryKey 対象のPrimaryKeyを設定します.
	 * @param values Insert or UpdateするKeyValue条件を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T update(String tableName, PrimaryKey primaryKey,
		Map<String, Object> values) {
		// 更新処理.
		updateSQL(tableName, values);
		// primaryKeyに対するwhere文を生成.
		DbUtil.wherePrimaryKeys(
			getSql(), getParams(), primaryKey, values);
		// 実行処理.
		executeUpdate();
		return (T)this;
	}
	
	/**
	 * PrimaryKeyを設定して１行情報を更新.
	 * @param tableName 対象のテーブル名を設定します.
	 * @param primaryKey 対象のPrimaryKeyを設定します.
	 * @param values Insert or UpdateするKeyValue条件を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T update(String tableName, PrimaryKey primaryKey,
		Object... values) {
		// 更新処理.
		updateSQL(tableName, values);
		// primaryKeyに対するwhere文を生成.
		DbUtil.wherePrimaryKeys(
			getSql(), getParams(), primaryKey,
			DbUtil.getPrimaryKey(primaryKey, values));
		// 実行処理.
		executeUpdate();
		return (T)this;
	}
	
	/**
	 * PrimaryKeyを設定して１行情報を更新.
	 * @param tableName 対象のテーブル名を設定します.
	 * @param primaryKey 対象のPrimaryKeyを設定します.
	 * @param values Insert or UpdateするKeyValue条件を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T update(String tableName, String primaryKey,
		Map<String, Object> values) {
		return update(tableName, new PrimaryKey(primaryKey), values);
	}
	
	/**
	 * PrimaryKeyを設定して１行情報を更新.
	 * @param tableName 対象のテーブル名を設定します.
	 * @param primaryKey 対象のPrimaryKeyを設定します.
	 * @param values Insert or UpdateするKeyValue条件を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T update(String tableName, String primaryKey,
		Object... values) {
		return update(tableName, new PrimaryKey(primaryKey), values);
	}
	
	/**
	 * PrimaryKeyを設定して１行情報を更新.
	 * またPrimaryKeyは"id"が指定されます.
	 * @param tableName 対象のテーブル名を設定します.
	 * @param values Insert or UpdateするKeyValue条件を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T update(String tableName, Map<String, Object> values) {
		return update(tableName, new PrimaryKey("id"), values);
	}
	
	/**
	 * PrimaryKeyを設定して１行情報を更新.
	 * またPrimaryKeyは"id"が指定されます.
	 * @param tableName 対象のテーブル名を設定します.
	 * @param values Insert or UpdateするKeyValue条件を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T update(String tableName, Object... values) {
		return update(tableName, new PrimaryKey("id"), values);
	}
	
	/**
	 * 指定PrimaryKeyの条件が存在する場合は更新処理を行い
	 * 存在しない場合は注入処理を行います.
	 * @param tableName 対象のテーブル名を設定します.
	 * @param primaryKey 対象のPrimaryKeyを設定します.
	 * @param values Insert or UpdateするKeyValue条件を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T upsert(String tableName, PrimaryKey primaryKey,
		Map<String, Object> values) {
		// 指定PrimaryKeyの内容が存在するかチェック.
		if(DbUtil.isPrimaryKeyByRow(
			this, tableName, primaryKey, values)) {
			// Update.
			update(tableName, primaryKey, values);
		} else {
			// 注入.
			insert(tableName, values);
		}
		return (T)this;
	}
	
	/**
	 * 指定PrimaryKeyの条件が存在する場合は更新処理を行い
	 * 存在しない場合は注入処理を行います.
	 * @param tableName 対象のテーブル名を設定します.
	 * @param primaryKey 対象のPrimaryKeyを設定します.
	 * @param values Insert or UpdateするKeyValue条件を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T upsert(
		String tableName, PrimaryKey primaryKey, Object... values) {
		// 指定PrimaryKeyの内容が存在するかチェック.
		if(DbUtil.isPrimaryKeyByRow(
			this, tableName, primaryKey, values)) {
			// Update.
			update(tableName, primaryKey, values);
		} else {
			// 注入.
			insert(tableName, values);
		}
		return (T)this;
	}
	
	/**
	 * 指定PrimaryColumnの条件が存在する場合は更新処理を行い
	 * 存在しない場合は注入処理を行います.
	 * @param tableName 対象のテーブル名を設定します.
	 * @param primaryKey 対象のPrimaryKeyを設定します.
	 * @param values Insert or UpdateするKeyValue条件を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T upsert(String tableName, String primaryKey,
		Map<String, Object> values) {
		return upsert(tableName, new PrimaryKey(primaryKey), values);
	}
	
	/**
	 * 指定PrimaryColumnの条件が存在する場合は更新処理を行い
	 * 存在しない場合は注入処理を行います.
	 * @param tableName 対象のテーブル名を設定します.
	 * @param primaryKey 対象のPrimaryKeyを設定します.
	 * @param values Insert or UpdateするKeyValue条件を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T upsert(
		String tableName, String primaryKey, Object... values) {
		return upsert(tableName, new PrimaryKey(primaryKey), values);
	}
	
	/**
	 * 指定PrimaryColumnの条件が存在する場合は更新処理を行い
	 * 存在しない場合は注入処理を行います.
	 * またPrimaryKeyは"id"が指定されます.
	 * @param tableName 対象のテーブル名を設定します.
	 * @param values Insert or UpdateするKeyValue条件を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T upsert(String tableName, Map<String, Object> values) {
		return upsert(tableName, new PrimaryKey("id"), values);
	}
	
	/**
	 * 指定PrimaryColumnの条件が存在する場合は更新処理を行い
	 * 存在しない場合は注入処理を行います.
	 * またPrimaryKeyは"id"が指定されます.
	 * @param tableName 対象のテーブル名を設定します.
	 * @param values Insert or UpdateするKeyValue条件を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T upsert(
		String tableName, Object... values) {
		return upsert(tableName, new PrimaryKey("id"), values);
	}
	
	/**
	 * PrimaryKeyを設定して削除.
	 * @param tableName テーブル名を設定します.
	 * @param primaryKey プライマリキー群を設定します.
	 * @param values プライマリキーのvalue群を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T delete(
		String tableName, PrimaryKey primaryKey, Object... values) {
		deleteSQL(tableName);
		setParams(new ObjectList<Object>());
		DbUtil.wherePrimaryKeys(getSql(), getParams(), primaryKey, values);
		executeUpdate(null, null);
		return (T)this;
	}
	
	/**
	 * PrimaryKeyを設定して削除.
	 * @param tableName テーブル名を設定します.
	 * @param primaryKey プライマリキーを設定します.
	 * @param value プライマリキーのvalueを設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T delete(
		String tableName, String primaryKey, Object value) {
		return delete(tableName, new PrimaryKey(primaryKey), value);
	}
	
	/**
	 * PrimaryKeyを設定して削除.
	 * PrimaryKeyを"id"として設定します.
	 * @param tableName テーブル名を設定します.
	 * @param value プライマリキーのvalueを設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T delete(String tableName, Object value) {
		return delete(tableName, new PrimaryKey("id"), value);
	}
}
