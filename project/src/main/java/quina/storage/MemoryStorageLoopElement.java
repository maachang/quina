package quina.storage;

import quina.QuinaThreadStatus;
import quina.worker.QuinaLoopElement;

/**
 * MemoryStorageManagerのタイムアウト監視する
 * LoopElement.
 */
public class MemoryStorageLoopElement
	implements QuinaLoopElement {
	
	// タイムアウト時間.
	protected long timeout;
	
	// 確認タイミング.
	protected long checkTiming;
	
	// MemoryStorageマネージャ.
	protected MemoryStorageManager manager;
	
	// 前回確認したタイムアウト値.
	protected long beforeTimeout;
	
	/**
	 * コンストラクタ.
	 * @param timeout 各StorageManagerのタイムアウト時間を
	 *                設定します.
	 * @param checkTiming LoopElementを実行するタイミングを
	 *                    設定します.
	 * @param manager MemoryStorageマネージャを設定します.
	 */
	protected MemoryStorageLoopElement(
		long timeout, long checkTiming,
		MemoryStorageManager manager) {
		this.timeout = StorageConstants.getTimeout(timeout);
		this.checkTiming = StorageConstants.getCheckTiming(checkTiming);
		this.manager = manager;
		this.beforeTimeout =
			System.currentTimeMillis() + this.checkTiming;
	}
	
	@Override
	public void execute(QuinaThreadStatus status)
		throws Throwable {
		final long time = System.currentTimeMillis();
		// タイムアウトチェックを行わない場合.
		if(beforeTimeout > time) {
			sleep();
			return;
		}
		// タイムアウトチェックを行う.
		String key;
		MemoryStorage ms;
		final int len = manager.size();
		for(int i = len - 1; i >= 0; i --) {
			// キー名を取得.
			if((key = manager.keyAt(i)) == null) {
				continue;
			}
			// Storageを取得.
			ms = (MemoryStorage)manager.getStorage(key);
			if(ms == null) {
				continue;
			}
			// タイムアウトの場合は削除.
			if(ms.getUpdateTime() + timeout < time) {
				manager.removeStorage(key);
			}
		}
		// 次のチェックタイミングを設定.
		beforeTimeout = System.currentTimeMillis() + checkTiming;
	}
}
