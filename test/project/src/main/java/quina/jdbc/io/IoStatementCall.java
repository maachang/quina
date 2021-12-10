package quina.jdbc.io;

import java.sql.SQLException;

/**
 * Read/Write用ステートメントコール.
 */
public interface IoStatementCall {
	
	/**
	 * 実行処理.
	 * @param ios I/OStatementがセットされます.
	 * @return Object 処理結果を返却します.
	 * @exception SQLException SQL例外.
	 */
	public Object execute(IoStatement ios)
		throws SQLException;
}
