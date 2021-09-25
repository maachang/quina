package quina.worker;

/**
 * QuinaWorker呼び出しに対する１つのハンドル.
 */
public interface QuinaWorkerCallHandler {
	/**
	 * 紐付けたいQuinaCall.getId()と同じIdを取得.
	 * @return Integer 対象となるQuinaCallのIdが返却されます.
	 */
	default Integer targetId() {
		return null;
	}
	
	/**
	 * ワーカーマネージャ初期化時の呼び出し処理。
	 * @param len ワーカースレッド数が設定されます.
	 */
	default void initWorkerCall(int len) {
	}

	/**
	 * １つのワーカスレッド開始時の呼び出し.
	 * @param no ワーカースレッドNoが設定されます.
	 */
	default void startThreadCall(int no) {
	}

	/**
	 * １つのワーカスレッド終了時の呼び出し.
	 * @param no ワーカースレッドNoが設定されます.
	 */
	default void endThreadCall(int no) {
	}
	
	/**
	 * 要素を破棄.
	 * @param no ワーカースレッドNoが設定されます.
	 * @param em QuinaWorkerElementを設定します.
	 */
	default void destroy(int no, QuinaWorkerCall em) {
		em.destroy();
	}

	/**
	 * 要素が既に破棄されているかチェック.
	 * @param no ワーカースレッドNoが設定されます.
	 * @param em QuinaWorkerElementを設定します.
	 * @return boolean [true]の場合は既に破棄されています.
	 */
	default boolean isDestroy(int no, QuinaWorkerCall em) {
		return em.isDestroy();
	}
	
	/**
	 * 対象要素の開始時の呼び出し.
	 * @param no ワーカースレッドNoが設定されます.
	 * @param em QuinaWorkerElementを設定します.
	 */
	default void startCall(int no, QuinaWorkerCall em) {
		em.startCall();
	}
	
	/**
	 * 対象要素の終了時の呼び出し.
	 * @param no ワーカースレッドNoが設定されます.
	 * @param em QuinaWorkerElementを設定します.
	 */
	default void endCall(int no, QuinaWorkerCall em) {
		em.endCall();
	}

	/**
	 * 対象要素の実行時の呼び出し.
	 * @param no ワーカースレッドNoが設定されます.
	 * @param em QuinaWorkerElementを設定します.
	 * @return boolean falseの場合実行処理は失敗しました.
	 */
	default boolean executeCall(int no, QuinaWorkerCall em) {
		return em.executeCall();
	}
}
