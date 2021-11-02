package quina.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import quina.Quina;
import quina.exception.QuinaException;
import quina.util.Alphabet;

/**.
 * QuinaProxyUtil.
 */
public final class QuinaProxyUtil {
	private QuinaProxyUtil() {}
	
	/** Connectionクラス名. **/
	protected static final String CONNECT_CLASS_NAME =
		QuinaConnection.class.getName();
	
	/** Statementクラス名. **/
	protected static final String STATEMENT_CLASS_NAME =
		QuinaStatement.class.getName();
	
	/** PreparedStatementクラス名. **/
	protected static final String PREPARED_STATEMENT_CLASS_NAME =
		QuinaPreparedStatement.class.getName();
	
	/** CallableStatementクラス名. **/
	protected static final String CALLABLE_STATEMENT_CLASS_NAME =
		QuinaCallableStatement.class.getName();
	
	/** ResultSetクラス名. **/
	protected static final String RESULT_SET_CLASS_NAME =
		QuinaResultSet.class.getName();
	
	/**
	 * QuinaProxyConnectionオブジェクトを取得.
	 * @param notProxy true の場合Poolingで処理しません.
	 * @param source QuinaDataSourceを設定します.
	 * @param base 対象のConnectionオブジェクトを設定します.
	 * @return QuinaProxyConnection オブジェクトが返却されます.
	 */
	protected static final QuinaConnection getConnection(
		boolean notProxy, QuinaDataSource source, Connection base) {
		return (QuinaConnection)Quina.get()
			.getProxyScopedManager().getObject(
				CONNECT_CLASS_NAME,
				notProxy, source, base);
	}
	
	/**
	 * QuinaProxyStatementオブジェクトを取得.
	 * @param conn Connectionオブジェクトを設定します.
	 * @param base 対象のStatementオブジェクトを設定します.
	 * @return QuinaProxyStatement オブジェクトが返却されます.
	 */
	protected static final QuinaStatement getStatement(
		QuinaConnection conn, Statement base) {
		return (QuinaStatement)Quina.get()
			.getProxyScopedManager().getObject(
				STATEMENT_CLASS_NAME,
				conn, base);
	}
	
	/**
	 * QuinaProxyPreparedStatementオブジェクトを取得.
	 * @param conn Connectionオブジェクトを設定します.
	 * @param base 対象のPreparedStatementオブジェクトを設定します.
	 * @return QuinaProxyPreparedStatement オブジェクトが返却されます.
	 */
	protected static final QuinaPreparedStatement getPreparedStatement(
		QuinaConnection conn, PreparedStatement base) {
		return (QuinaPreparedStatement)Quina.get()
			.getProxyScopedManager().getObject(
				PREPARED_STATEMENT_CLASS_NAME,
				conn, base);
	}
	
	/**
	 * QuinaProxyCallableStatementオブジェクトを取得.
	 * @param conn Connectionオブジェクトを設定します.
	 * @param base 対象のCallableStatementオブジェクトを設定します.
	 * @return QuinaProxyCallableStatement オブジェクトが返却されます.
	 */
	protected static final QuinaCallableStatement getCallableStatement(
		QuinaConnection conn, CallableStatement base) {
		return (QuinaCallableStatement)Quina.get()
			.getProxyScopedManager().getObject(
				CALLABLE_STATEMENT_CLASS_NAME,
				conn, base);
	}
	
	/**
	 * QuinaProxyCallableStatementオブジェクトを取得.
	 * @param conn Connectionオブジェクトを設定します.
	 * @param base 対象のCallableStatementオブジェクトを設定します.
	 * @return QuinaProxyCallableStatement オブジェクトが返却されます.
	 */
	protected static final QuinaResultSet getResultSet(
		AbstractQuinaProxyStatement stmt, ResultSet base) {
		return (QuinaResultSet)Quina.get()
			.getProxyScopedManager().getObject(
				RESULT_SET_CLASS_NAME,
			stmt, base);
	}
	
	// 文字列のURLParamsから指定したキーに対するValue位置を取得.
	private static final int[] urlParamValue(
		String urlParams, String key) {
		if(key == null || key.isEmpty()) {
			throw new QuinaException(
				"The specified Key information is empty.");
		}
		char c;
		int keyP = Alphabet.indexOf(urlParams, key);
		if(keyP == -1 ||
			((c = urlParams.charAt(keyP -1)) != ';' &&
			c != '?' && c != '&' && c != ' '
		)) {
			return null;
		}
		final int keyLen = key.length();
		final int len = urlParams.length();
		int eqP = -1;
		int endP = len;
		for(int i = keyP + keyLen ; i < len; i ++) {
			if((c = urlParams.charAt(i)) == '=') {
				if(eqP != -1) {
					return null;
				}
				eqP = keyP + keyLen + 1;
			} else if(c == ';'|| c == '&') {
				endP = i;
				break;
			}
		}
		if(eqP == -1) {
			return null;
		}
		return new int[] {eqP, endP};
	}
	
	/**
	 * 対象URLパラメータに指定Keyの条件が存在するか
	 * チェック.
	 * @param urlParams URLパラメータを設定します.
	 * @param key 存在確認をするKey情報を取得します.
	 * @return boolean trueの場合、存在します.
	 */
	public static final boolean eqURLParamsToKey(
		String urlParams, String key) {
		return urlParamValue(urlParams, key) != null;
		
	}
	
	/**
	 * 対象URLパラメータに指定KeyとValueが
	 * 存在するかチェック.
	 * @param urlParams URLパラメータを設定します.
	 * @param key 存在確認をするKey情報を取得します.
	 * @param value 存在確認をするValue情報を設定します.
	 * @return boolean trueの場合、存在します.
	 */
	public static final boolean eqURLParamsToKeyValue(
		String urlParams, String key, String value) {
		if(value == null || value.isEmpty()) {
			throw new QuinaException(
				"The specified Value information is empty.");
		}
		int[] valuePos = urlParamValue(urlParams, key);
		if(valuePos == null) {
			return false;
		}
		return Alphabet.eq(
			value, urlParams.substring(valuePos[0], valuePos[1]).trim());
	}
}
