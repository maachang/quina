package quina.net.nio.tcp.worker;

import quina.net.nio.tcp.NioAtomicValues.Number32;

/**
 * NioWorker定義.
 */
public class NioWorkerConstants {
	/** 最小ワーカースレッド数. **/
	public static final int MIN_WORKER_THREAD_LENGTH = 1;

	/** デフォルトのワーカースレッド数. **/
	public static final int DEF_WORKER_THREAD_LENGTH =
		(int)(java.lang.Runtime.getRuntime().availableProcessors() * 1.5);

	/** 最大ワーカースレッド数. **/
	public static final int MAX_WORKER_THREAD_LENGTH = 32768;

	/** 最小プーリング数. **/
	public static final int MIN_POOLING_MANAGE_LENGTH = 16;

	/** デフォルトプーリング数. **/
	public static final int DEF_POOLING_MANAGE_LENGTH = 256;

	/** 最大プーリング数. **/
	public static final int MAX_POOLING_MANAGE_LENGTH = 65535;

	// デフォルトのワーカースレッド数.
	// 認識されたCPUコア数の倍のワーカー数が初期値で指定されます.
	private static final Number32 workerThreadLength =
		new Number32(DEF_WORKER_THREAD_LENGTH);

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
		if(MIN_WORKER_THREAD_LENGTH < len) {
			len = MIN_WORKER_THREAD_LENGTH;
		} else if(MAX_WORKER_THREAD_LENGTH >= len) {
			len = MAX_WORKER_THREAD_LENGTH;
		}
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
		if(MIN_POOLING_MANAGE_LENGTH < len) {
			len = MIN_POOLING_MANAGE_LENGTH;
		} else if(MAX_POOLING_MANAGE_LENGTH >= len) {
			len = MAX_POOLING_MANAGE_LENGTH;
		}
		poolingManageLength.set(len);
	}
}
