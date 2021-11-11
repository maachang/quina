package quina.jdbc.io;

import quina.exception.QuinaException;

/**
 * QueryColumns群.
 */
public class QueryColumns {
	// カラム群.
	private final String[] keys;
	
	// コンストラクタ.
	protected QueryColumns() {
		keys = new String[0];
	}
	
	/**
	 * コンストラクタ.
	 * @param keys QueryColumns群を設定します.
	 */
	public QueryColumns(String... keys) {
		if(keys == null || keys.length == 0) {
			throw new QuinaException("Primary Key is not set.");
		}
		this.keys = keys;
	}
	
	/**
	 * QueryColumns群を設定.
	 * @param keys QueryColumns群を設定します.
	 */
	public static final QueryColumns of(String... keys) {
		return new QueryColumns(keys);
	}
	
	/**
	 * 定義されたQueryColumns群を取得.
	 * @return String[] QueryColumns群が返却されます.
	 */
	public String[] get() {
		return keys;
	}
	
	/**
	 * 指定項番カラム名を取得.
	 * @param no 対象の項番を設定します.
	 * @return String カラム名が返却されます.
	 */
	public String getColumns(int no) {
		return keys[no];
	}
	
	/**
	 * カラム数を取得.
	 * @return int カラム数が返却されます.
	 */
	public int size() {
		return keys.length;
	}
}
