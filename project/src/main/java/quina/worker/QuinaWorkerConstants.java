package quina.worker;

import quina.util.AtomicNumber;

public class QuinaWorkerConstants {
	/** 最小ワーカー数. **/
	public static final int MIN_WORKER_LENGTH = 16;

	/** デフォルトのワーカー数. **/
	public static final int DEF_WORKER_LENGTH;
	// MIN_WORKER_THREAD_LENGTHより少ない数になる場合は
	// MIN_WORKER_THREAD_LENGTH をセット.
	static {
		final int len = (int)(java.lang.Runtime.getRuntime().
			availableProcessors() * 8);
		if(len < MIN_WORKER_LENGTH) {
			DEF_WORKER_LENGTH = MIN_WORKER_LENGTH;
		} else {
			DEF_WORKER_LENGTH = len;
		}
	}

	/** 最大ワーカー数. **/
	public static final int MAX_WORKER_LENGTH = 32768;

	// デフォルトのワーカー数.
	// 認識されたCPUスレッド数の4倍のワーカー数が初期値で指定されます.
	private static final AtomicNumber workerLength =
		new AtomicNumber(DEF_WORKER_LENGTH);

	/**
	 * デフォルトのワーカー数を取得.
	 * @return
	 */
	public static final int getWorkerLength() {
		return workerLength.get();
	}

	/**
	 * デフォルトのワーカー数を設定.
	 * @param len
	 */
	public static final void setWorkerThreadLength(int len) {
		if(MIN_WORKER_LENGTH > len) {
			len = MIN_WORKER_LENGTH;
		} else if(MAX_WORKER_LENGTH <= len) {
			len = MAX_WORKER_LENGTH;
		}
		workerLength.set(len);
	}
}
