package quina.net.nio.tcp.worker;

/**
 * Nioワーカースレッドハンドラ.
 */
public interface NioWorkerThreadHandler {

	/**
	 * ワーカースレッド初期化時の呼び出し処理。
	 * @param len ワーカースレッド数が設定されます.
	 */
	public void initWorkerThreadManager(int len);

	/**
	 * ワーカスレッド開始時の呼び出し.
	 * @param no ワーカースレッドNoが設定されます.
	 */
	public void startThreadCall(int no);

	/**
	 * ワーカスレッド終了時の呼び出し.
	 * @param no ワーカースレッドNoが設定されます.
	 */
	public void endThreadCall(int no);

	/**
	 * ワーカ実行時のエラー呼び出し.
	 * @param no ワーカースレッドNoが設定されます.
	 * @param t 例外(Throwable)が設定されます.
	 */
	public void errorCall(int no, Throwable t);

	/**
	 * ワーカースレッドNoに紐づくオブジェクトを取得.
	 * @return
	 */
	default Object getWorkerThreadObject(int no) {
		return null;
	}

	/**
	 * ワーカー要素の利用終了時の呼び出し処理.
	 * ここで、ワーカーがプーリング化したい場合は、その実装を行う.
	 * @param em 利用済みのワーカー要素が返却されます.
	 */
	public void endWorkerElement(WorkerElement em);
}
