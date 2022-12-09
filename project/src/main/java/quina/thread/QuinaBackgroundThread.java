package quina.thread;

import java.io.IOException;

import quina.exception.QuinaException;
import quina.util.collection.ObjectList;

/**
 * 対象要素群をループ実行しつづけるバックグラウンド用スレッド.
 */
public class QuinaBackgroundThread
	extends QuinaServiceThread<Object> {
	
	// ループ実行用の要素群.
	private ObjectList<QuinaBackgroundElement> loopList =
		new ObjectList<QuinaBackgroundElement>();
	
	/**
	 * ループ実行要素の登録.
	 * @param em ループ実行用の要素を設定します.
	 */
	public void regLoopElement(QuinaBackgroundElement em) {
		if(em == null) {
			throw new QuinaException(
				"The specified argument is null.");
		}
//		if(this.isStartupThread()) {
//			throw new QuinaException(
//				"Quina Loop Thread has already started.");
//		}
		// 既に登録済みの場合.
		int len = loopList.size();
		for(int i = 0; i < len; i ++) {
			if(loopList.get(i) == em) {
				return;
			}
		}
		loopList.add(em);
	}
	
	/**
	 * スレッドに処理を登録.
	 *
	 * @param em スレッド処理内容を設定します.
	 * @throws IOException
	 */
	@Override
	public void offer(Object value) {
		// 利用しない.
	}
	
	/**
	 * 登録されたスレッド処理内容を取得.
	 * @return T スレッド処理内容が返却されます.
	 */
	@Override
	protected Object poll() {
		// 利用しない.
		return null;
	}

	/**
	 * 実行処理.
	 * @param nullCall 実行条件を設定します.
	 *                 この値は常にnullです.
	 * @exception Throwable 例外.
	 */
	@Override
	protected void executeCall(Object nullCall)
		throws Throwable {
		// １つも登録されていない場合.
		final int len = loopList.size();
		if(len == 0) {
			// スレッド一時停止.
			if(!isStopThread()) {
				Thread.sleep(100L);
			}
			return;
		}
		// 実行処理.
		for(int i = 0; i < len; i ++) {
			if(isStopThread()) {
				return;
			}
			loopList.get(i).execute(this);
		}
	}
	
	/**
	 * 開始スレッドコール.
	 */
	protected void startThreadCall() {
		// １つも登録されていない場合.
		final int len = loopList.size();
		if(len == 0) {
			return;
		}
		// 実行処理.
		for(int i = 0; i < len; i ++) {
			loopList.get(i).startThreadCall();
		}
	}
	
	/**
	 * 終了スレッドコール.
	 */
	protected void endThreadCall() {
		// １つも登録されていない場合.
		final int len = loopList.size();
		if(len == 0) {
			return;
		}
		// 実行処理.
		for(int i = 0; i < len; i ++) {
			loopList.get(i).endThreadCall();
		}
	}
	
	/**
	 * 後始末実行.
	 */
	protected void cleanUpCall() {
		// １つも登録されていない場合.
		final int len = loopList.size();
		if(len == 0) {
			return;
		}
		// 実行処理.
		for(int i = 0; i < len; i ++) {
			loopList.get(i).cleanUpCall();
		}
	}
}
