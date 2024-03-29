package quina.shutdown;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * シャットダウンコールバック定義.
 * サーバーのシャットダウンが実施された場合にシャットダウンを
 * 行います.
 */
public abstract class ShutdownCall {

	/**
	 * シャットダウン実行フラグ.
	 */
	protected AtomicBoolean isShutdown = new AtomicBoolean(false);

	/**
	 * シャットダウンが完了済みかチェック.
	 *
	 * @return boolean [true]の場合、既に実行されました.
	 */
	public boolean isShutdown() {
		return isShutdown.get();
	}

	/**
	 * シャットダウン完了として呼び出す.
	 * @return boolean 前回の条件が返却されます.
	 */
	protected boolean successShutdown() {
		boolean ret = isShutdown.get();
		while(!isShutdown.compareAndSet(ret, true)) {
			ret = isShutdown.get();
		}
		return ret;
	}

	/**
	 * シャットダウンを実行するコールバックメソッド.
	 */
	public abstract void call();

}
