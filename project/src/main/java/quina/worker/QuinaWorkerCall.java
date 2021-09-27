package quina.worker;

/**
 * Quinaコール.
 */
public abstract class QuinaWorkerCall {
	// QuinaContext.
	private QuinaContext context;
	
	/**
	 * QuinaContextを設定.
	 * @param context 対象のQuinaContextを設定します.
	 */
	public void setContext(QuinaContext context) {
		this.context = context;
	}
	
	/**
	 * QuinaContextを取得.
	 * @return QuinaContext 設定されてるQuinaContextが
	 *                      返却されます.
	 */
	public QuinaContext getContext() {
		return context;
	}
	
	/**
	 * 設定されたワーカーNoを取得.
	 * @return int ワーカーNoを取得.
	 *             -1の場合割り当てられていません.
	 */
	public int getWorkerNo() {
		// このIDはある程度長く滞在するオブジェクトと
		// 連動させる必要がある.
		return -1;
	}
	
	/**
	 * ワーカーNoを設定.
	 * @param no ワーカーNoを設定します.
	 */
	public void setWorkerNo(int no) {
		// このIDはある程度長く滞在するオブジェクトと
		// 連動させる必要がある.
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
