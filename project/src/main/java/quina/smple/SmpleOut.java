package quina.smple;

/**
 * Smple出力オブジェクト.
 */
public class SmpleOut {
	// バッファオブジェクト.
	private StringBuilder buf = new StringBuilder();
	
	/**
	 * 出力内容をクリア.
	 */
	public void clear() {
		buf = new StringBuilder();
	}
	
	/**
	 * 出力処理.
	 * @param o 出力情報を設定します.
	 * @return SmpleOut このオブジェクトが返却されます.
	 */
	public SmpleOut print(Object o) {
		buf.append(o);
		return this;
	}
	
	/**
	 * 改行出力処理.
	 * @param o 出力情報を設定します.
	 * @return SmpleOut このオブジェクトが返却されます.
	 */
	public SmpleOut println(Object o) {
		buf.append(o).append("\n");
		return this;
	}
	
	/**
	 * フォーマット出力処理.
	 * @param out String.formatに従った出力文字列を
	 *            設定します.
	 * @param args String.formatに従った出力内容を
	 *             設定します.
	 * @return
	 */
	public SmpleOut printf(String out, Object ... args) {
		buf.append(String.format(out, args));
		return this;
	}
	
	/**
	 * 処理結果を所得.
	 * @return String 処理結果が返却されます.
	 */
	public String resultOut() {
		return buf.toString();
	}
	
	@Override
	public String toString() {
		return buf.toString();
	}
}
