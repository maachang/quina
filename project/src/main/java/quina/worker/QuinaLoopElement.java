package quina.worker;

import quina.QuinaThreadStatus;

/**
 * QuinaLoopThreadでループ実行する１つの要素.
 */
public interface QuinaLoopElement {
	
	/**
	 * Loop実行.
	 * @param status QuinaThreadステータスが設定されます.
	 * @exception Throwable 例外.
	 */
	public void execute(QuinaThreadStatus status)
		throws Throwable;
	
	/**
	 * 開始スレッドコール.
	 */
	default void startThreadCall() {
		
	}
	
	/**
	 * 終了スレッドコール.
	 */
	default void endThreadCall() {
		
	}
	
	/**
	 * 後始末実行.
	 */
	default void cleanUpCall() {
		
	}
}
