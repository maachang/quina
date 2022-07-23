package quina.util;

import quina.thread.QuinaWait;

/**
 * ２段階フラグ.
 * 
 * 対象の処理を１度だけ呼び出して初期化処理を
 * スレッドセーフで行いたい場合に利用します.
 * 
 * <例>
 * 
 * private final TwoStepsFlag executeFlag = new TwoStepsFlag();
 * public void execute() throws Exception {
 *   try {
 *     // 既に実行済み.
 *     if(!executeFlag.start()) {
 *       return;
 *     }
 *     
 *     // ......
 *     // ....
 *     // ..
 *     // .
 *     
 *     // 処理成功.
 *     if(!executeFlag.success()) {
 *       // 成功設定に失敗した場合.
 *       throw new Exception("....");
 *     }
 *   } catch(Exception e) {
 *     // 処理失敗.
 *     executeFlag.failure();
 *     throw e;
 *   }
 * }
 */
public class TwoStepsFlag {
	
	// 成功失敗情報.
	// 0: 未実行.
	// 1: 実行開始.
	// 2: 成功.
	// 3: 失敗.
	private final AtomicNumber status = new AtomicNumber(0);
	
	// wait.
	private final QuinaWait wait = new QuinaWait();
	
	/**
	 * 未実行かチェック.
	 * @return boolean true の場合未実行です.
	 */
	public boolean isUntreated() {
		// 成功(2)以外は未実行.
		return status.get() != 2;
	}
	
	/**
	 * 実行済みかチェック.
	 * @return boolean true の場合実行済みです.
	 */
	public boolean isExecuted() {
		// 成功(2)の場合.
		return status.get() == 2;
	}
	
	/**
	 * 実行開始.
	 * @return boolean true の場合は実行中で、
	 *                 false の場合は実行済みです.
	 */
	public boolean start() {
		// 現状"未実行"(0)で"実行開始"(1)に
		// 変更できなかった場合.
		// または、現状"失敗"(3)で"実行開始"(1)に
		// 変更できなかった場合.
		if(!status.compareAndSet(0, 1)) {
			// 処理
			while(true) {
				switch(status.get()) {
				// 現在のステータスが"実行開始"(1)の場合.
				// (他のスレッドで実行中(1)である場合).
				case 1:
					// 一定時間待機.
					wait.await(500L);
					break;
				// ステータスが"実行成功"(2)の場合
				// 実行済みとして返却.
				case 2:
					return false;
				// ステータスが"実行失敗"(3)の場合
				// ステータスを"実行中"(1)に変更して
				// 未実行として返却.
				case 3:
					if(status.compareAndSet(3, 1)) {
						// 実行中として返却.
						return true;
					}
					break;
				}
			}
		}
		// 実行中として返却.
		return true;
	}
	
	/**
	 * 実行の成功を通知.
	 * @return boolean true の場合、成功結果が正しく
	 *                 反映されました.
	 */
	public boolean success() {
		// 実行中(1)の場合のみ成功(2)をセット.
		if(status.compareAndSet(1, 2)) {
			// シグナル.
			wait.signal();
			return true;
		}
		return false;
	}
	
	/**
	 * 強制的な成功通知.
	 */
	public void forcedSuccess() {
		// 前回が成功(2)以外の場合のみセット.
		if(status.put(2) != 2) {
			// シグナル.
			wait.signal();
		}
	}
	
	/**
	 * 実行の失敗通知.
	 */
	public void failure() {
		// 成功(2)と失敗(3)以外の場合のみ設定.
		if(status.compareAndSet(0, 3) ||
			status.compareAndSet(1, 3)) {
			// シグナル.
			wait.signal();
		}
	}
}
