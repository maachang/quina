package quina.jdbc.io.template;

import java.util.List;

import quina.exception.QuinaException;
import quina.jdbc.io.DbUtil;
import quina.jdbc.io.PrimaryKey;
import quina.jdbc.io.QueryColumns;
import quina.jdbc.io.QueryResult;
import quina.jdbc.io.QueryResultRow;
import quina.util.collection.ObjectList;

/**
 * 読み込みテンプレート.
 */
@SuppressWarnings("unchecked")
public interface ReadTemplate<T>
	extends BaseTemplate<T> {
	
	/**
	 * Query実行.
	 * @return QueryResult Query実行結果が返却されます.
	 */
	public QueryResult executeQuery();

	/**
	 * Select用のSQL文を生成.
	 * 使い方は以下のように使います.
	 * 
	 * IoStatement stmt = conn.ioStatement();
	 * QueryResult res = stmt
	 *     .selectSQL("testTable", "age", "name")
	 *     .sql("where id=?")
	 *     .params(100);
	 *     .executeQuery();
	 * 
	 * 以下のようなSQL文が実行されます.
	 * > select age, name from testTable where id=100;
	 * 
	 * @param tableName テーブル名を設定します.
	 * @param columns select文で取得するカラム名群を設定します.
	 * @return IoStaTtement このオブジェクトが返却されます.
	 */
	default T selectSQL(
		String tableName, String... columns) {
		try {
			clearParmas();
			StringBuilder sqlBuf = clearSql();
			DbUtil.createSelectSQL(
				sqlBuf, tableName, columns);
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
	 * PrimaryKeyを設定して１行情報を取得.
	 * @param tableName テーブル名を設定します.
	 * @param primaryKey プライマリキー群を設定します.
	 * @param columns クエリーカラム群を設定します.
	 * @param values プライマリキーのvalue群を設定します.
	 * @return QueryResultRow １行情報が返却されます.
	 *                     存在しない場合 null が返却されます.
	 */
	default QueryResultRow selectRow(
		String tableName, PrimaryKey primaryKey, QueryColumns columns,
		Object... values) {
		QueryResultRow ret = null;
		if(columns == null || columns.isEmpty()) {
			selectSQL(tableName);
		} else {
			selectSQL(tableName, columns.get());
		}
		setParams(new ObjectList<Object>());
		DbUtil.wherePrimaryKeys(getSql(), getParams(), primaryKey, values);
		QueryResult res = executeQuery();
		if(res.hasNext()) {
			ret = res.next().getCopy();
		}
		try {
			res.close();
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
		return ret;
	}
	
	/**
	 * PrimaryKeyを設定して１行情報を取得.
	 * @param tableName テーブル名を設定します.
	 * @param primaryKey プライマリキーを設定します.
	 * @param columns クエリーカラム群を設定します.
	 * @param value プライマリキーのvalueを設定します.
	 * @return QueryResultRow １行情報が返却されます.
	 *                     存在しない場合 null が返却されます.
	 */
	default QueryResultRow selectRow(
		String tableName, String primaryKey, QueryColumns columns,
		Object value) {
		return selectRow(tableName, new PrimaryKey(primaryKey),
			columns, value);
	}
	
	/**
	 * PrimaryKeyを設定して１行情報を取得.
	 * PrimaryKeyを"id"として設定します.
	 * @param tableName テーブル名を設定します.
	 * @param columns クエリーカラム群を設定します.
	 * @param value プライマリキーのvalueを設定します.
	 * @return QueryResultRow １行情報が返却されます.
	 *                     存在しない場合 null が返却されます.
	 */
	default QueryResultRow selectRow(String tableName, QueryColumns columns,
		Object value) {
		return selectRow(tableName, new PrimaryKey("id"), columns, value);
	}
	
	/**
	 * PrimaryKeyを設定して１行情報を取得.
	 * @param tableName テーブル名を設定します.
	 * @param primaryKey プライマリキー群を設定します.
	 * @param values プライマリキーのvalue群を設定します.
	 * @return QueryResultRow １行情報が返却されます.
	 *                     存在しない場合 null が返却されます.
	 */
	default QueryResultRow selectRow(
		String tableName, PrimaryKey primaryKey, Object... values) {
		return selectRow(tableName, primaryKey,
			null, values);
	}
	
	/**
	 * PrimaryKeyを設定して１行情報を取得.
	 * @param tableName テーブル名を設定します.
	 * @param primaryKey プライマリキーを設定します.
	 * @param value プライマリキーのvalueを設定します.
	 * @return QueryResultRow １行情報が返却されます.
	 *                     存在しない場合 null が返却されます.
	 */
	default QueryResultRow selectRow(
		String tableName, String primaryKey, Object value) {
		return selectRow(tableName, new PrimaryKey(primaryKey),
			null, value);
	}
	
	/**
	 * PrimaryKeyを設定して１行情報を取得.
	 * PrimaryKeyを"id"として設定します.
	 * @param tableName テーブル名を設定します.
	 * @param value プライマリキーのvalueを設定します.
	 * @return QueryResultRow １行情報が返却されます.
	 *                     存在しない場合 null が返却されます.
	 */
	default QueryResultRow selectRow(String tableName, Object value) {
		return selectRow(tableName, new PrimaryKey("id"), null, value);
	}
	
	/**
	 * PrimaryKeyを設定して１行情報を取得.
	 * @param out 出力先のListを設定します.
	 * @param tableName テーブル名を設定します.
	 * @param primaryKey プライマリキー群を設定します.
	 * @param columns クエリーカラム群を設定します.
	 * @param values プライマリキーのvalue群を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T selectRow(
		List<?> out, String tableName, PrimaryKey primaryKey,
		QueryColumns columns, Object... values) {
		QueryResultRow row = selectRow(tableName, primaryKey,
			columns, values);
		if(row != null) {
			((List<Object>)out).add(row);
		}
		return (T)this;
	}
	
	/**
	 * PrimaryKeyを設定して１行情報を取得.
	 * @param out 出力先のListを設定します.
	 * @param tableName テーブル名を設定します.
	 * @param primaryKey プライマリキーを設定します.
	 * @param columns クエリーカラム群を設定します.
	 * @param value プライマリキーのvalueを設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T selectRow(
		List<?> out, String tableName, String primaryKey,
		QueryColumns columns, Object value) {
		return selectRow(out, tableName, new PrimaryKey(primaryKey),
			columns, value);
	}
	
	/**
	 * PrimaryKeyを設定して１行情報を取得.
	 * PrimaryKeyを"id"として設定します.
	 * @param out 出力先のListを設定します.
	 * @param tableName テーブル名を設定します.
	 * @param columns クエリーカラム群を設定します.
	 * @param value プライマリキーのvalueを設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T selectRow(
		List<?> out, String tableName, QueryColumns columns,
		Object value) {
		return selectRow(out, tableName, new PrimaryKey("id"),
			columns, value);
	}
	
	/**
	 * PrimaryKeyを設定して１行情報を取得.
	 * @param tableName テーブル名を設定します.
	 * @param primaryKey プライマリキー群を設定します.
	 * @param values プライマリキーのvalue群を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T selectRow(
		List<?> out, String tableName, PrimaryKey primaryKey,
		Object... values) {
		return selectRow(out, tableName, primaryKey, null, values);
	}
	
	/**
	 * PrimaryKeyを設定して１行情報を取得.
	 * @param out 出力先のListを設定します.
	 * @param tableName テーブル名を設定します.
	 * @param primaryKey プライマリキーを設定します.
	 * @param value プライマリキーのvalueを設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T selectRow(
		List<?> out, String tableName, String primaryKey,
		Object value) {
		return selectRow(out, tableName, new PrimaryKey(primaryKey),
			null, value);
	}
	
	/**
	 * PrimaryKeyを設定して１行情報を取得.
	 * PrimaryKeyを"id"として設定します.
	 * @param out 出力先のListを設定します.
	 * @param tableName テーブル名を設定します.
	 * @param value プライマリキーのvalueを設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	default T selectRow(
		List<?> out, String tableName, Object value) {
		return selectRow(out, tableName, new PrimaryKey("id"), null, value);
	}
}
