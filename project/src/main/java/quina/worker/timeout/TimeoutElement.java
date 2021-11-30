package quina.worker.timeout;

public interface TimeoutElement {
	
	/**
	 * タイムアウト監視登録.
	 * @return boolean trueの場合既に登録してます.
	 */
	public boolean regTimeout();
	
	/**
	 * タイムを取得.
	 * @return long 過去にアクセスされた
	 *              System.currentTimeMillis()値が
	 *              返却されます.
	 */
	public long getTime();

}
