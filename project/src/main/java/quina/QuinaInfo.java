package quina;

/**
 * Quina情報管理.
 */
public interface QuinaInfo {
	/**
	 * 情報の初期化.
	 */
	public void reset();

	/**
	 * コンフィグ情報を読み込む.
	 * @param configDir コンフィグファイル読み込み先のディレクトリを設定します.
	 */
	default void readConfig(String configDir) {
		QuinaUtil.readConfig(configDir, this);
	}

	/**
	 * QuinaInfoの情報を文字列で出力.
	 * @return String 文字列が返却されます.
	 */
	default String outString() {
		StringBuilder out = new StringBuilder();
		out.append("QuinaInfo: ")
			.append(this.getClass().getName()).append("\n");
		QuinaUtil.toString(out, 2, this);
		return out.toString();
	}
}
