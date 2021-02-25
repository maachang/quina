package quina.net.nio.tcp.worker;

import quina.net.nio.tcp.NioAtomicValues.Number32;

/**
 * NioWorker定義.
 */
public class NioWorkerConstants {
	/** 最小ワーカースレッド数. **/
	public static final int MIN_WORKER_THREAD_LENGTH =
		java.lang.Runtime.getRuntime().availableProcessors() << 1;

	/** 最大ワーカースレッド数. **/
	public static final int MAX_WORKER_THREAD_LENGTH = 32768;

	/** 最小プーリング数. **/
	public static final int MIN_POOLING_MANAGE_LENGTH = 16;

	/** デフォルトプーリング数. **/
	public static final int DEF_POOLING_MANAGE_LENGTH = 256;

	/** 最大プーリング数. **/
	public static final int MAX_POOLING_MANAGE_LENGTH = 65535;

	// デフォルトのワーカースレッド数.
	private static final Number32 workerThreadLength =
		new Number32(MIN_WORKER_THREAD_LENGTH);

	// デフォルトのプーリング管理数.
	private static final Number32 poolingManageLength =
		new Number32(DEF_POOLING_MANAGE_LENGTH);

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
		workerThreadLength.set(len);
	}

	/**
	 * デフォルトのプーリング管理数を取得.
	 * @return
	 */
	public static final int getPoolingManageLength() {
		return poolingManageLength.get();
	}

	/**
	 * デフォルトのプーリング管理数を設定.
	 * @param len
	 */
	public static final void setPoolingManageLength(int len) {
		poolingManageLength.set(len);
	}
}
