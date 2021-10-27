package quina;

import java.io.IOException;

import quina.util.Flag;
import quina.worker.QuinaWait;

/**
 * QuinaService用スレッド.
 */
public abstract class QuinaServiceThread<T>
	extends Thread {
	// スレッド停止フラグ.
	protected volatile boolean stopFlag = true;

	// スレッド開始完了フラグ.
	protected final Flag startThreadFlag = new Flag(false);

	// スレッド終了フラグ.
	protected final Flag endThreadFlag = new Flag(false);

	/**
	 * スレッドに処理を登録.
	 *
	 * @param em スレッド処理内容を設定します.
	 * @throws IOException
	 */
	public abstract void offer(T value);
	
	/**
	 * 登録されたスレッド処理内容を取得.
	 * @return T スレッド処理内容が返却されます.
	 */
	protected abstract T poll();
	
	/**
	 * ワーカーを開始する.
	 */
	public void startThread() {
		stopFlag = false;
		startThreadFlag.set(false);
		endThreadFlag.set(false);
		setDaemon(true);
		start();
	}

	/**
	 * ワーカーを停止する.
	 */
	public void stopThread() {
		stopFlag = true;
	}

	/**
	 * ワーカーが停止命令が既に出されているかチェック.
	 * @return
	 */
	public boolean isStopThread() {
		return stopFlag;
	}

	/**
	 * ワーカーが開始しているかチェック.
	 * @return
	 */
	public boolean isStartupThread() {
		return startThreadFlag.get();
	}

	/**
	 * ワーカーが終了しているかチェック.
	 * @return
	 */
	public boolean isExitThread() {
		return endThreadFlag.get();
	}

	/**
	 * スレッド開始完了まで待機.
	 * @param timeout タイムアウトのミリ秒を設定します.
	 *                0以下を設定した場合、無限に待ちます.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean awaitStartup() {
		return QuinaWait.await(-1L, startThreadFlag);
	}

	/**
	 * スレッド開始完了まで待機.
	 * @param timeout タイムアウトのミリ秒を設定します.
	 *                0以下を設定した場合、無限に待ちます.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean awaitStartup(long timeout) {
		return QuinaWait.await(timeout, startThreadFlag);
	}

	/**
	 * スレッド終了まで待機.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean awaitExit() {
		return QuinaWait.await(-1L, endThreadFlag);
	}

	/**
	 * スレッド終了まで待機.
	 * @param timeout タイムアウトのミリ秒を設定します.
	 *                0以下を設定した場合、無限に待ちます.
	 * @return boolean [true]の場合、正しく終了しました.
	 */
	public boolean awaitExit(long timeout) {
		return QuinaWait.await(timeout, endThreadFlag);
	}
	
	/**
	 * 開始スレッドコール.
	 */
	protected void startThreadCall() {
		
	}
	
	/**
	 * 終了スレッドコール.
	 */
	protected void endThreadCall() {
		
	}
	
	/**
	 * 実行処理.
	 * @param call 実行条件を設定します.
	 * @exception Throwable 例外.
	 */
	protected abstract void executeCall(T call)
		throws Throwable;
	
	
	/**
	 * エラースレッドコール.
	 * @param td エラー例外が設定されます.
	 */
	protected void errorThreadCall(T call, Throwable td) {
		
	}
	
	/**
	 * 後始末実行.
	 */
	protected void cleanUpCall() {
		
	}

	/**
	 * スレッド実行.
	 */
	public void run() {
		// スレッド開始呼び出し.
		try {
			startThreadCall();
		} catch(Exception e) {
		}
		
		T value = null;
		boolean endFlag = false;
		ThreadDeath td = null;
		startThreadFlag.set(true);
		while (!endFlag && !stopFlag) {
			// スレッド実行.
			try {
				// 処理対象の情報を取得.
				value = poll();
				// 処理実行.
				executeCall(value);
			} catch(Throwable t) {
				// スレッド中止.
				if (t instanceof InterruptedException) {
					endFlag = true;
				// threadDeathが発生した場合.
				} else if (t instanceof ThreadDeath) {
					endFlag = true;
					td = (ThreadDeath)t;
				}
				try {
					errorThreadCall(value, t);
				} catch(Exception e) {}
			}
		}
		// 後始末コール.
		try {
			cleanUpCall();
		} catch(Exception e) {
		}
		
		// スレッド終了呼び出し.
		try {
			endThreadCall();
		} catch(Exception e) {
		}
		
		// スレッド終了完了.
		endThreadFlag.set(true);
		if (td != null) {
			throw td;
		}
	}
}
