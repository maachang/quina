package quina.worker;

/**
 * Quinaコール.
 */
public abstract class QuinaWorkerCall {
	// スレッド項番.
	protected int threadNo = -1;
	
	/**
	 * 設定されたワーカーNoを取得.
	 * @return int ワーカーNoを取得.
	 *             -1の場合割り当てられていません.
	 */
	public int getWorkerNo() {
		return threadNo;
	}
	
	/**
	 * ワーカーNoを設定.
	 * @param no ワーカーNoを設定します.
	 */
	public void setWorkerNo(int no) {
		this.threadNo = no;
	}
	
	/**
	 * 要素を破棄.
	 * @param no 対象のスレッドNoを設定します.
	 */
	public abstract void destroy(int no);

	/**
	 * 要素が既に破棄されているかチェック.
	 * @param no 対象のスレッドNoを設定します.
	 * @return boolean [true]の場合は既に破棄されています.
	 */
	public abstract boolean isDestroy(int no);
	
	/**
	 * 対象要素の開始時の呼び出し.
	 * @param no 対象のスレッドNoを設定します.
	 */
	public void startCall(int no) {
	}
	
	/**
	 * 対象要素の実行時の呼び出し.
	 * @param no 対象のスレッドNoを設定します.
	 * @return boolean falseの場合実行処理は失敗しました.
	 */
	public abstract boolean executeCall(int no);
	
	/**
	 * 対象要素の終了時の呼び出し.
	 * @param no 対象のスレッドNoを設定します.
	 */
	public void endCall(int no) {
	}
	
	/**
	 * 対象要素実行時でエラーが発生した場合の呼び出し.
	 * @param no 対象のスレッドNoを設定します.
	 * @param t 例外(Throwable)が設定されます.
	 */
	public void errorCall(int no, Throwable t) {
	}
	
	/**
	 * ワーカー要素用のユニークIDを取得.
	 * @return int ユニークIDを取得します.
	 */
	public abstract int getId();
}
