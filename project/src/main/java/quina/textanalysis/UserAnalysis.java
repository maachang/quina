package quina.textanalysis;

/**
 * ユーザ定義解析処理.
 */
@FunctionalInterface
public interface UserAnalysis {
	/**
	 * ユーザー定義の解析処理.
	 * @param ts TextScriptAnalysisを設定します.
	 * @return boolean trueの場合処理を完了しました.
	 */
	public boolean analysis(TextScript ts);
}
