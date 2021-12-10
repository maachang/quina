package quina.jdbc.io;

/**
 * QueryColumns群.
 * select a, b, c from xxx;
 * このようなSQLを生成する場合、
 * 
 * ReadTemplate.selectSQL("xxx",
 *   QueryColumns.of("a", "b", "c").get());
 * 
 * とした形でQueryColumnsを利用します.
 */
public class QueryColumns {
	// 空の文字配列.
	private static final String[] BLANK_STRING_ARRAY = new String[0];
	
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
			keys = BLANK_STRING_ARRAY;
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
	
	/**
	 * この情報が空か取得.
	 * @return boolean trueの場合、空です.
	 */
	public boolean isEmpty() {
		return keys.length == 0;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		int len = keys.length;
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				buf.append(", ");
			}
			buf.append(keys[i]);
		}
		return buf.toString();
	}
}
