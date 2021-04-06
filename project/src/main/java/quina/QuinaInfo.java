package quina;

import quina.util.collection.IndexMap;

/**
 * Quina情報管理.
 */
public interface QuinaInfo {
	/**
	 * QuinaConfigを取得.
	 * @return QuinaConfig QuinaConfigが返却されます.
	 */
	public QuinaConfig getQuinaConfig();

	/**
	 * 情報の初期化.
	 */
	default void reset() {
		getQuinaConfig().clear();
	}

	/**
	 * コンフィグ情報を読み込む.
	 * @param configDir コンフィグファイル読み込み先のディレクトリを設定します.
	 */
	default void readConfig(String configDir) {
		final IndexMap<String, Object> json = QuinaUtil.loadConfig(configDir, this);
		if(json != null) {
			getQuinaConfig().setConfig(json);
		}
	}

	/**
	 * QuinaInfoの情報を文字列で出力.
	 * @return String 文字列が返却されます.
	 */
	default String outString() {
		StringBuilder out = new StringBuilder();
		out.append("QuinaInfo: ")
			.append(this.getClass().getName()).append("\n");
		getQuinaConfig().toString(out, 2);
		return out.toString();
	}
}
