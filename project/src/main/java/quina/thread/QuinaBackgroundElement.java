package quina.thread;

/**
 * QuinaBackgroundThreadでループ実行する１つの要素.
 */
public interface QuinaBackgroundElement {
	
	/**
	 * Background実行.
	 * @param status QuinaThreadステータスを設定されます.
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
	
	// 一定期間待機.
	default void sleep() {
		try {
			Thread.sleep(5L);
		} catch(Exception e) {}
	}
}
