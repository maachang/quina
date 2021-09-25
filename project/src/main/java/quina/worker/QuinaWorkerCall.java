package quina.worker;

/**
 * Quinaコール.
 */
public abstract class QuinaWorkerCall {
	// コンテキスト.
	protected QuinaContext context;
	// 要素.
	protected Object value;
	
	/**
	 * QuinaContextを設定.
	 * @param context 対象のコンテキストを設定します.
	 */
	public void setContext(QuinaContext context) {
		this.context = context.copy();
	}
	
	/**
	 * QuinaContextを取得.
	 * @return QuinaContext 対象のコンテキストを取得します.
	 */
	protected QuinaContext getContext() {
		return context;
	}
	
	/**
	 * 要素をセット.
	 * @param value 設定する要素を設定します.
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
	/**
	 * 要素を取得.
	 * @return Object 要素が返却されます.
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * 要素を破棄.
	 */
	public abstract void destroy();

	/**
	 * 要素が既に破棄されているかチェック.
	 * @return boolean [true]の場合は既に破棄されています.
	 */
	public abstract boolean isDestroy();
	
	/**
	 * 対象要素の開始時の呼び出し.
	 */
	public void startCall() {
	}
	
	/**
	 * 対象要素の実行時の呼び出し.
	 * @return boolean falseの場合実行処理は失敗しました.
	 */
	public abstract boolean executeCall();
	
	/**
	 * 対象要素の終了時の呼び出し.
	 */
	public void endCall() {
	}
	
	/**
	 * 対象要素実行時でエラーが発生した場合の呼び出し.
	 * @param t 例外(Throwable)が設定されます.
	 */
	public void errorCall(Throwable t) {
	}
	
	/**
	 * 設定されたワーカーNoを取得.
	 * @return int ワーカーNoを取得.
	 *             -1の場合割り当てられていません.
	 */
	public int getWorkerNo() {
		return -1;
	}
	
	/**
	 * ワーカーNoを設定.
	 * @param no ワーカーNoを設定します.
	 */
	public void setWorkerNo(int no) {
	}
	
	/**
	 * ワーカー要素用のユニークIDを取得.
	 * @return int ユニークIDを取得します.
	 */
	public abstract int getId();
}
