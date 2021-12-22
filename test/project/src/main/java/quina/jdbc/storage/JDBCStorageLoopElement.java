package quina.jdbc.storage;

import quina.QuinaThreadStatus;
import quina.storage.MemoryStorageManager;
import quina.storage.StorageConstants;
import quina.worker.QuinaLoopElement;

/**
 * JDBCStorageManagerのタイムアウト監視する
 * LoopElement.
 */
final class JDBCStorageLoopElement
	implements QuinaLoopElement {
	
	// タイムアウト時間.
	protected long timeout;
	
	// 確認タイミング.
	protected long checkTiming;
	
	// MemoryStorageマネージャ.
	protected JDBCStorageManager manager;
	
	// 前回確認したタイムアウト値.
	protected long beforeTimeout;
	
	/**
	 * コンストラクタ.
	 * @param timeout 各StorageManagerのタイムアウト時間を
	 *                設定します.
	 * @param checkTiming LoopElementを実行するタイミングを
	 *                    設定します.
	 * @param manager JDBCStorageマネージャを設定します.
	 */
	protected JDBCStorageLoopElement(
		long timeout, long checkTiming,
		JDBCStorageManager manager) {
		this.timeout = StorageConstants.getTimeout(timeout);
		this.checkTiming = StorageConstants.getCheckTiming(checkTiming);
		this.manager = manager;
		this.beforeTimeout =
			System.currentTimeMillis() + this.checkTiming;
	}
	
	@Override
	public void execute(QuinaThreadStatus status)
		throws Throwable {
		final long nowTime = System.currentTimeMillis();
		// タイムアウトチェックを行わない場合.
		if(beforeTimeout > nowTime) {
			sleep();
			return;
		}
		// タイムアウト処理.
		executeTimeout(nowTime);
		// 次のチェックタイミングを設定.
		beforeTimeout = System.currentTimeMillis() + checkTiming;
	}
	
	/**
	 * タイムアウト処理を実行.
	 * @param time 現在の時間が設定されます.
	 */
	protected void executeTimeout(long nowTime) {
		
	}
}
