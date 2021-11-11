package quina.jdbc.io;

import quina.exception.QuinaException;

/**
 * PrimaryKey群.
 */
public class PrimaryKey {
	// Primaryキー群.
	private final String[] keys;
	
	// コンストラクタ.
	protected PrimaryKey() {
		keys = new String[0];
	}
	
	/**
	 * コンストラクタ.
	 * @param keys PrimaryKey群を設定します.
	 */
	public PrimaryKey(String... keys) {
		if(keys == null || keys.length == 0) {
			throw new QuinaException("Primary Key is not set.");
		}
		this.keys = keys;
	}
	
	/**
	 * PrimaryKey群を設定.
	 * @param keys PrimaryKey群を設定します.
	 */
	public static final PrimaryKey of(String... keys) {
		return new PrimaryKey(keys);
	}
	
	/**
	 * 定義されたPrimaryKey群を取得.
	 * @return String[] PrimaryKey群が返却されます.
	 */
	public String[] getKeys() {
		return keys;
	}
	
	/**
	 * 指定項番のキー名を取得.
	 * @param no 対象の項番を設定します.
	 * @return String キー名が返却されます.
	 */
	public String getKey(int no) {
		return keys[no];
	}
	
	/**
	 * キー数を取得.
	 * @return int キー数が返却されます.
	 */
	public int size() {
		return keys.length;
	}
}
