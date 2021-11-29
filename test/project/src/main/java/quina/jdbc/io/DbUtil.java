package quina.jdbc.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import quina.exception.QuinaException;
import quina.jdbc.io.template.BaseTemplate;
import quina.util.BooleanUtil;
import quina.util.DateUtil;
import quina.util.NumberUtil;
import quina.util.StringUtil;
import quina.util.collection.ObjectList;

/**
 * DBUtil.
 */
public final class DbUtil {
	private DbUtil() {}
	
	/**
	 * PreparedStatementパラメータをセット.
	 * 
	 * @param pre    対象のステートメントを設定します.
	 * @param meta   パラメータメタデータを設定します.
	 * @param params 対象のパラメータを設定します.
	 */
	public static final void preParams(
		final PreparedStatement pre, final ParameterMetaData meta, final Object[] params)
		throws Exception {
		int len = params.length;
		for (int i = 0; i < len; i++) {
			putParam(i + 1, pre, meta, params[i]);
		}
	}

	// 1つのパラメータセット.
	private static final void putParam(
		final int no, final PreparedStatement pre, final ParameterMetaData meta, Object v)
		throws Exception {
		// ParameterMetaDataがサポートしていない場合.
		if (meta == null) {

			// nullの場合、タイプが不明なので、無作法だがsetObjectにNULLをセット.
			if (v == null) {
				pre.setObject(no, null);
			} else if (v instanceof Boolean) {
				boolean b = ((Boolean) v).booleanValue();
				pre.setBoolean(no, b);
			} else if (v instanceof String) {
				pre.setString(no, (String) v);
			} else if (v instanceof Integer) {
				pre.setInt(no, (Integer) v);
			} else if (v instanceof Long) {
				pre.setLong(no, (Long) v);
			} else if (v instanceof Float) {
				pre.setFloat(no, (Float) v);
			} else if (v instanceof Double) {
				pre.setDouble(no, (Double) v);
			} else if (v instanceof BigDecimal) {
				pre.setBigDecimal(no, (BigDecimal) v);
			} else if (v instanceof java.util.Date) {
				if (v instanceof java.sql.Timestamp) {
					pre.setTimestamp(no, (java.sql.Timestamp) v);
				} else if (v instanceof java.sql.Time) {
					pre.setTime(no, (java.sql.Time) v);
				} else if (v instanceof java.sql.Date) {
					pre.setDate(no, (java.sql.Date) v);
				} else {
					pre.setTimestamp(no, new java.sql.Timestamp(
						((java.util.Date) v).getTime()));
				}
			} else if (v instanceof byte[]) {
				pre.setBytes(no, (byte[]) v);
			} else {
				pre.setObject(no, v);
			}

			return;
		}

		// ParameterMetaDataがサポートされている場合.
		int type = meta.getParameterType(no);

		// 情報がnullの場合はこちらのほうが行儀がよいのでこのように処理する.
		if (v == null) {
			pre.setNull(no, type);
			return;
		}

		// タイプ別で処理をセット.
		switch (type) {
		case Types.BOOLEAN:
			if (v instanceof Boolean) {
				pre.setBoolean(no, (Boolean) v);
			} else if(NumberUtil.isNumeric(v)) {
				pre.setBoolean(no, NumberUtil.parseInt(v) == 1);
			} else {
				pre.setBoolean(no, BooleanUtil.parseBoolean(v));
			}
			break;
		case Types.BIT:
		case Types.TINYINT:
		case Types.SMALLINT:
			if (v instanceof Boolean) {
				pre.setInt(no, (((Boolean) v).booleanValue()) ?
					1 : 0);
			} else {
				pre.setInt(no, NumberUtil.parseInt(v));
			}
			break;
		case Types.INTEGER:
		case Types.BIGINT:
			if (v instanceof Boolean) {
				pre.setLong(no, (((Boolean) v).booleanValue()) ?
					1 : 0);
			} else if (v instanceof java.util.Date) {
				pre.setLong(no, ((java.util.Date) v).getTime());
			} else {
				pre.setLong(no, NumberUtil.parseLong(v));
			}
			break;
		case Types.FLOAT:
		case Types.REAL:
			if (v instanceof Boolean) {
				pre.setFloat(no, (((Boolean) v).booleanValue()) ?
					1.0f : 0.0f);
			} else if (v instanceof Float) {
				pre.setFloat(no, (Float) v);
			} else {
				pre.setFloat(no, NumberUtil.parseFloat(v));
			}
			break;
		case Types.DOUBLE:
			if (v instanceof Boolean) {
				pre.setDouble(no, (((Boolean) v).booleanValue()) ?
					1.0d : 0.0d);
			} else if (v instanceof Double) {
				pre.setDouble(no, (Double) v);
			} else {
				pre.setDouble(no, NumberUtil.parseDouble(v));
			}
			break;
		case Types.NUMERIC:
		case Types.DECIMAL:
			if (v instanceof Boolean) {
				pre.setBigDecimal(no, new BigDecimal(
					(((Boolean) v).booleanValue()) ? "1.0" : "0.0"));
			} if (v instanceof BigDecimal) {
				pre.setBigDecimal(no, (BigDecimal) v);
			} else {
				pre.setBigDecimal(no, new BigDecimal(
					NumberUtil.parseDouble(v).toString()));
			}
			break;
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
		case Types.NCHAR:
		case Types.NVARCHAR:
		case Types.DATALINK:
			if (v instanceof String) {
				pre.setString(no, (String) v);
			} else if(NumberUtil.isNumeric(v)) {
				// javascriptの場合、1 と設定しても 1.0 となるので、その場合は整数でセット.
				final Long n = NumberUtil.parseLong(v);
				if(NumberUtil.parseDouble(n).equals(NumberUtil.parseDouble(v))) {
					pre.setString(no, StringUtil.parseString(n));
				} else {
					pre.setString(no, StringUtil.parseString(v));
				}
			} else {
				pre.setString(no, StringUtil.parseString(v));
			}
			break;
		case Types.DATE:
			if (v instanceof java.sql.Date) {
				pre.setDate(no, (java.sql.Date) v);
			} else {
				pre.setDate(no, new java.sql.Date(
					DateUtil.parseDate(v).getTime()));
			}
			break;
		case Types.TIME:
			if (v instanceof java.sql.Time) {
				pre.setTime(no, (java.sql.Time) v);
			} else {
				pre.setTime(no, DateUtil.parseTime(v));
			}
			break;
		case Types.TIMESTAMP:
			if (v instanceof java.sql.Timestamp) {
				pre.setTimestamp(no, (java.sql.Timestamp) v);
			} else {
				pre.setTimestamp(no, new java.sql.Timestamp(
						DateUtil.parseDate(v).getTime()));
			}
			break;
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
		case Types.BLOB:
			if (v instanceof byte[]) {
				pre.setBytes(no, (byte[]) v);
			} else if (v instanceof String) {
				pre.setBytes(no, ((String) v).getBytes("UTF8"));
			} else {
				pre.setBytes(no, ("" + v).getBytes("UTF8"));
			}
			break;
		case Types.JAVA_OBJECT:
			pre.setObject(no, v);
			break;
		default:
			pre.setObject(no, v);
			break;
		}
	}

	/**
	 * 結果のカラム情報を取得.
	 * 
	 * @param result 対象の結果オブジェクトを設定します.
	 * @param type   対象のSQLタイプを設定します.
	 * @param no     対象の項番を設定します. この番号は１から開始されます.
	 */
	public static final Object getResultColumn(
		final ResultSet result, final int type, final int no)
		throws Exception {
		if (result.getObject(no) == null) {
			return null;
		}
		//System.out.println(
		//	"no: " + no + " type: " + type + " result: " + result.getObject(no));
		Object data = result.getObject(no);
		switch (type) {
		case Types.BOOLEAN:
			data = result.getBoolean(no);
			break;
		case Types.BIT:
		case Types.TINYINT:
			data = (int)(((Byte) result.getByte(no)).byteValue());
			break;
		case Types.SMALLINT:
			data = result.getInt(no);
			break;
		case Types.INTEGER:
			data = result.getLong(no);
			break;
		case Types.BIGINT:
			data = result.getLong(no);
			break;
		case Types.FLOAT:
		case Types.REAL:
			data = result.getFloat(no);
			break;
		case Types.DOUBLE:
			data = result.getDouble(no);
			break;
		case Types.NUMERIC:
		case Types.DECIMAL:
			data = result.getBigDecimal(no);
			break;
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
		case Types.NCHAR:
		case Types.NVARCHAR:
			data = result.getString(no);
			break;
		case Types.DATE:
			data = result.getDate(no);
			break;
		case Types.TIME:
			data = result.getTime(no);
			break;
		case Types.TIMESTAMP:
			data = result.getTimestamp(no);
			break;
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
			data = result.getBytes(no);
			break;
		case Types.BLOB:
			data = result.getBlob(no);
			break;
		case Types.DATALINK:
			data = result.getString(no);
			break;
		case Types.STRUCT:// 未サポート.
		case Types.CLOB:// 未サポート.
		case Types.NCLOB:// 未サポート.
		case Types.REF:// 未サポート.
			break;
		}
		// blob.
		if (data instanceof Blob) {
			InputStream b = null;
			ByteArrayOutputStream bo = null;
			try {
				b = new BufferedInputStream(((Blob) data).getBinaryStream());
				bo = new ByteArrayOutputStream();
				byte[] bin = new byte[4096];
				int len;
				while (true) {
					if ((len = b.read(bin)) <= -1) {
						break;
					}
					if (len > 0) {
						bo.write(bin, 0, len);
					}
				}
				b.close();
				b = null;
				data = bo.toByteArray();
				bo.close();
				bo = null;
			} finally {
				if(b != null) {
					try {
						b.close();
					} catch(Exception e) {}
				}
				if(bo != null) {
					try {
						bo.close();
					} catch(Exception e) {}
				}
			}
		}
		return data;
	}
	
	/**
	 * ObjectListに対して指定Valuesのデータをクリアしてセット.
	 * @param params ObjectListを設定します.
	 * @param values ObjectListをクリアして設定する
	 *               Value群を設定します.
	 * @return ObjectList<Object> ObjectListが返却されます.
	 */
	public static final ObjectList<Object> clearAndSetAll(
		ObjectList<Object> params, Object[] values) {
		if(params == null) {
			params = new ObjectList<Object>(values);
		} else {
			params.clearAndSetAll(values);
		}
		return params;
	}
	
	/**
	 * Insert用のSQLを作成.
	 * @param out SQL出力先のStringBuiderを設定します.
	 * @param table テーブル名を設定します.
	 * @param columns insert対象のColumn名群を設定します.
	 */
	public static final void createInsert(
		StringBuilder out, String table, String... columns) {
		if(table == null || (table = table.trim()).isEmpty()) {
			throw new QuinaException("The table name is not set.");
		} else if(columns == null || columns.length == 0) {
			throw new QuinaException("The column name is not set.");
		}
		final int len = columns.length;
		out.append("insert into ")
			.append(table).append("(");
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				out.append(", ");
			}
			out.append(columns[i]);
		}
		out.append(") ").append("values (");
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				out.append(", ");
			}
			out.append("?");
		}
		out.append(")");
	}

	
	/**
	 * Insert用のSQLを作成してパラメータセット.
	 * @param out SQL出力先のStringBuiderを設定します.
	 * @param table テーブル名を設定します.
	 * @param params パラメータを設定します.
	 *               column, value, column, value...
	 *               で設定します.
	 *               この処理で出力先のパラメータにもなります.
	 */
	public static final ObjectList<Object> createInsert(
		StringBuilder out, String table, ObjectList<Object> params) {
		if(table == null || (table = table.trim()).isEmpty()) {
			throw new QuinaException("The table name is not set.");
		} else if(params == null || params.size() == 0) {
			throw new QuinaException(
				"No key and value conditions have been set. ");
		}
		int cnt = 0;
		final int len = params.size();
		final Object[] values = new Object[len >> 1];
		out.append("insert into ")
			.append(table).append("(");
		for(int i = 0; i < len; i += 2) {
			if(i != 0) {
				out.append(", ");
			}
			out.append(params.get(i));
			values[cnt ++] = params.get(i + 1);
		}
		out.append(") ").append("values (");
		for(int i = 0; i < len; i += 2) {
			if(i != 0) {
				out.append(", ");
			}
			out.append("?");
		}
		out.append(")");
		// パラメータに新規セット.
		return clearAndSetAll(params, values);
	}
	
	/**
	 * Insert用のSQLを作成してパラメータセット.
	 * @param out SQL出力先のStringBuiderを設定します.
	 * @param params 出力先のパラメータを設定します.
	 * @param table テーブル名を設定します.
	 * @param data Update対象の内容を設定します.
	 */
	public static final ObjectList<Object> createInsert(
		StringBuilder out, ObjectList<Object> params, String table,
		Map<String, Object> data) {
		if(table == null || (table = table.trim()).isEmpty()) {
			throw new QuinaException("The table name is not set.");
		} else if(data == null || data.size() == 0) {
			throw new QuinaException(
				"No key and value conditions have been set.");
		}
		int cnt = 0;
		final int len = data.size();
		final Object[] values = new Object[len];
		Entry<String, Object> e;
		Iterator<Entry<String, Object>> itr = data.entrySet().iterator();
		out.append("insert into ")
			.append(table).append("(");
		while(itr.hasNext()) {
			e = itr.next();
			if(cnt != 0) {
				out.append(", ");
			}
			out.append(e.getKey());
			values[cnt ++] = e.getValue();
		}
		itr = null; e = null;
		out.append(") ").append("values (");
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				out.append(", ");
			}
			out.append("?");
		}
		out.append(")");
		// パラメータに新規セット.
		return clearAndSetAll(params, values);
	}
	
	/**
	 * Update用のSQLを作成してパラメータセット.
	 * @param out SQL出力先のStringBuiderを設定します.
	 * @param table テーブル名を設定します.
	 * @param params パラメータを設定します.
	 *               column, value, column, value...
	 *               で設定します.
	 *               この処理で出力先のパラメータにもなります.
	 */
	public static final ObjectList<Object> createUpdate(
		StringBuilder out, String table, ObjectList<Object> params) {
		if(table == null || (table = table.trim()).isEmpty()) {
			throw new QuinaException("The table name is not set.");
		} else if(params == null || params.size() == 0) {
			throw new QuinaException(
				"No key and value conditions have been set.");
		}
		int cnt = 0;
		final int len = params.size();
		final Object[] values = new Object[len >> 1];
		out.append("update ").append(table).append(" set ");
		for(int i = 0; i < len; i += 2) {
			if(i != 0) {
				out.append(", ");
			}
			out.append(params.get(i)).append("=?");
			values[cnt ++] = params.get(i + 1);
		}
		// パラメータに新規セット.
		return clearAndSetAll(params, values);
	}
	
	/**
	 * Update用のSQLを作成してパラメータセット.
	 * @param out SQL出力先のStringBuiderを設定します.
	 * @param params 出力先のパラメータを設定します.
	 * @param table テーブル名を設定します.
	 * @param data Update対象の内容を設定します.
	 */
	public static final ObjectList<Object> createUpdate(
		StringBuilder out, ObjectList<Object> params, String table,
		Map<String, Object> data) {
		if(table == null || (table = table.trim()).isEmpty()) {
			throw new QuinaException("The table name is not set.");
		} else if(data == null || data.size() == 0) {
			throw new QuinaException(
				"No key and value conditions have been set.");
		}
		int cnt = 0;
		final int len = data.size();
		final Object[] values = new Object[len];
		Entry<String, Object> e;
		Iterator<Entry<String, Object>> itr = data.entrySet().iterator();
		out.append("update ").append(table).append(" set ");
		while(itr.hasNext()) {
			e = itr.next();
			if(cnt != 0) {
				out.append(", ");
			}
			out.append(e.getKey()).append("=?");
			values[cnt ++] = e.getValue();
		}
		itr = null; e = null;
		// パラメータに新規セット.
		return clearAndSetAll(params, values);
	}
	
	/**
	 * Select用のSQL文を作成.
	 * @param out SQL出力先のStringBuiderを設定します.
	 * @param table テーブル名を設定します.
	 * @param columns 取得カラム名群を設定します.
	 */
	public static final void createSelectSQL(
		StringBuilder out, String table, String... columns) {
		if(table == null || (table = table.trim()).isEmpty()) {
			throw new QuinaException("The table name is not set.");
		} else if(columns == null || columns.length == 0) {
			columns = new String[] {"*"};
		}
		final int len = columns.length;
		out.append("select ");
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				out.append(", ");
			}
			out.append(columns[i]);
		}
		out.append(" from ").append(table);
	}
	
	/**
	 * Delete用のSQL文を作成.
	 * @param out SQL出力先のStringBuiderを設定します.
	 * @param table テーブル名を設定します.
	 */
	public static final void createDeleteSQL(StringBuilder out,
		String table) {
		if(table == null || (table = table.trim()).isEmpty()) {
			throw new QuinaException("The table name is not set.");
		}
		out.append("delete from ").append(table);
	}
	
	/**
	 * Query実行.
	 * @param bt 対象のBaseTemplateを設定します.
	 * @return QueryResult Query実行結果が返却されます.
	 */
	public static final QueryResult executeQuery(
		BaseTemplate<?> bt) {
		// クローズチェック.
		bt.checkClose();
		// 実行可能かチェック.
		bt.checkExecute();
		try {
			// QueryResultを取得.
			return (QueryResult)bt.executeStatement(true);
		} finally {
			// 登録されてたSQLとパラメータをクリア.
			bt.clearSqlAndParmas();
		}
	}
	
	/**
	 * 更新実行.
	 * @param outCount outCount[0]に処理結果の件数が設定されます.
	 * @param bt 対象のBaseTemplateを設定します.
	 * @return IoStatement このオブジェクトが返却されます.
	 */
	public static final QueryResult executeUpdate(
		long[] out, BaseTemplate<?> bt) {
		// クローズチェック.
		bt.checkClose();
		// 実行可能かチェック.
		bt.checkExecute();
		try {
			// 書き込み処理系を実行.
			if(out != null && out.length > 0) {
				out[0] = (Long)bt.executeStatement(false);
			} else {
				bt.executeStatement(false);
			}
			return bt.getGeneratedKeys();
		} finally {
			// 登録されてたSQLとパラメータをクリア.
			bt.clearSqlAndParmas();
		}
	}

	
	/**
	 * primaryKeyのWhere条件を生成.
	 * @param buf SQL用StringBuilderを設定します.
	 * @param params PreparedStatement用パラメータを設定します.
	 * @param primaryKey PrimaryKeyを設定します.
	 * @param values KeyValue群を設定します.
	 */
	public static final void wherePrimaryKeys(
		StringBuilder buf, ObjectList<Object> params,
		PrimaryKey primaryKey, Map<String, Object> values) {
		final int len = primaryKey.size();
		for(int i = 0; i < len; i ++) {
			if(!values.containsKey(primaryKey.getKey(i))) {
				throw new QuinaException(
					"The specified PrimaryKey \"" +
					primaryKey.getKey(i) +
					"\" is not set in values.");
			}
			if(i != 0) {
				buf.append(" and ");
			}
			buf.append(" where ").append(primaryKey.getKey(i)).append("=?");
			params.add(values.get(primaryKey.getKey(i)));
		}
	}
	
	/**
	 * primaryKeyのWhere条件を生成.
	 * @param buf SQL用StringBuilderを設定します.
	 * @param params PreparedStatement用パラメータを設定します.
	 * @param primaryKey PrimaryKeyを設定します.
	 * @param values PrimaryKeyに対するValue群を設定します.
	 */
	public static final void wherePrimaryKeys(
		StringBuilder buf, ObjectList<Object> params,
		PrimaryKey primaryKey, Object... values) {
		final int len = primaryKey.size();
		if(values == null || values.length != len) {
			throw new QuinaException(
				"The defined number of PrimaryKeys and the number of" +
				" values array do not match. ");
		}
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				buf.append(" and ");
			}
			buf.append(" where ").append(primaryKey.getKey(i)).append("=?");
			params.add(values[i]);
		}
	}

	
	/**
	 * 指定primaryKeyに対する行情報が存在するかチェック.
	 * @param wt WriteTemplateを設定します.
	 * @param table テーブル名を設定します.
	 * @param primaryKey PrimaryKeyを設定します.
	 * @param values KeyValue群を設定します.
	 * @return boolean trueの場合、情報は存在します.
	 */
	public static final boolean isPrimaryKeyByRow(
		BaseTemplate<?> wt, String table,
		PrimaryKey primaryKey, Map<String, Object> values) {
		try {
			StringBuilder buf = wt.clearSql();
			buf.append("select ");
			// select文を生成.
			final int len = primaryKey.size();
			if(len == 1) {
				buf.append("count(")
					.append(primaryKey.getKey(0))
					.append(") as rowsCount ");
			} else {
				buf.append("count(*) as rowsCount ");
			}
			buf.append("from ").append(table);
			ObjectList<Object> params = new ObjectList<Object>();
			wt.setParams(params);
			// primaryKeyに対するwhere文を生成.
			wherePrimaryKeys(buf, params, primaryKey, values);
			// 実行処理.
			QueryResult res = executeQuery(wt);
			// 存在するかチェック.
			if(res.hasNext()) {
				// カウントが存在する場合.
				return res.next().getInt("rowsCount") > 0;
			}
			// 存在しない場合.
			return false;
		} finally {
			wt.clearSqlAndParmas();
		}
	}
}
