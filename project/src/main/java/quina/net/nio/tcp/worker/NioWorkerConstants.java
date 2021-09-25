package quina.net.nio.tcp.worker;

import quina.net.nio.tcp.NioAtomicValues.Number32;

/**
 * NioWorker定義.
 */
public class NioWorkerConstants {
	/** 最小ワーカースレッド数. **/
	public static final int MIN_WORKER_THREAD_LENGTH = 8;

	/** デフォルトのワーカースレッド数. **/
	public static final int DEF_WORKER_THREAD_LENGTH;
	// MIN_WORKER_THREAD_LENGTHより少ない数になる場合は
	// MIN_WORKER_THREAD_LENGTH をセット.
	static {
		final int len = (int)(java.lang.Runtime.getRuntime().
			availableProcessors() * 4);
		if(len < MIN_WORKER_THREAD_LENGTH) {
			DEF_WORKER_THREAD_LENGTH = MIN_WORKER_THREAD_LENGTH;
		} else {
			DEF_WORKER_THREAD_LENGTH = len;
		}
	}

	/** 最大ワーカースレッド数. **/
	public static final int MAX_WORKER_THREAD_LENGTH = 32768;

	// デフォルトのワーカースレッド数.
	// 認識されたCPUスレッド数の4倍のワーカー数が初期値で指定されます.
	private static final Number32 workerThreadLength =
		new Number32(DEF_WORKER_THREAD_LENGTH);

	/**
	 * デフォルトのワーカースレッド数を取得.
	 * @return
	 */
	public static final int getWorkerThreadLength() {
		return workerThreadLength.get();
	}

	/**
	 * デフォルトのワーカースレッド数を設定.
	 * @param len
	 */
	public static final void setWorkerThreadLength(int len) {
		if(MIN_WORKER_THREAD_LENGTH > len) {
			len = MIN_WORKER_THREAD_LENGTH;
		} else if(MAX_WORKER_THREAD_LENGTH < len) {
			len = MAX_WORKER_THREAD_LENGTH;
		}
		workerThreadLength.set(len);
	}
}
