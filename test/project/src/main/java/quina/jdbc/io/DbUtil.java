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

import quina.util.BooleanUtil;
import quina.util.DateUtil;
import quina.util.NumberUtil;
import quina.util.StringUtil;

/**
 * DBUtil.
 */
public class DbUtil {
	private DbUtil() {}
	
	/**
	 * PreparedStatementパラメータをセット.
	 * 
	 * @param pre    対象のステートメントを設定します.
	 * @param meta   パラメータメタデータを設定します.
	 * @param params 対象のパラメータを設定します.
	 */
	public static final void preParams(
		final PreparedStatement pre, final ParameterMetaData meta, final Object... params)
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
		Object data = null;
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
}
