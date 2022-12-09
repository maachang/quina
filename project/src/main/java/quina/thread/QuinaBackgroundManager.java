package quina.thread;

/**
 * QuinaBackgroundManager.
 */
public interface QuinaBackgroundManager {
	/**
	 * ループ実行要素の登録.
	 * @param em ループ実行用の要素を設定します.
	 */
	public void regLoopElement(QuinaBackgroundElement em);
}
