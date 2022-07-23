package quina.thread;

/**
 * QuinaLoopManager.
 */
public interface QuinaLoopManager {
	/**
	 * ループ実行要素の登録.
	 * @param em ループ実行用の要素を設定します.
	 */
	public void regLoopElement(QuinaLoopElement em);
}
