package quina.net.nio.tcp.worker;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Nioワーカー要素プーリング管理.
 */
public class NioWorkerPoolingManager {
	private static final int MIN_MANAGE_LENGTH = 8;
	private static final int MAX_MANAGE_LENGTH = 65535;
	private int manageLength;
	private Queue<NioWorkerElement> pooling =
		new ConcurrentLinkedQueue<NioWorkerElement>();

	/**
	 * コンストラクタ.
	 * @param len プーリング管理する最大サイズを設定します.
	 */
	public NioWorkerPoolingManager(int len) {
		if(len < MIN_MANAGE_LENGTH) {
			len = MIN_MANAGE_LENGTH;
		} else if(len > MAX_MANAGE_LENGTH) {
			len = MAX_MANAGE_LENGTH;
		}
		manageLength = len;
	}

	/**
	 * プーリングオブジェクトをクリア.
	 */
	public void clear() {
		NioWorkerElement c;
		while((c = pooling.poll()) != null) {
			try {
				c.close();
			} catch(Exception e) {}
		}
	}

	/**
	 * プーリングオブジェクトを取得.
	 * @return NioWorkerElement プーリングオブジェクトが返却されます.
	 */
	public NioWorkerElement poll() {
		return pooling.poll();
	}

	/**
	 * 利用済みのオブジェクトをプーリングオブジェクトにセット.
	 * @param o プーリングさせるオブジェクトを設定します.
	 * @return boolean trueの場合、プーリング正しくされました.
	 */
	public boolean offer(NioWorkerElement o) {
		if(pooling.size() >= manageLength || o == null) {
			return false;
		}
		try {
			o.close();
		} catch(Exception e) {
			return false;
		}
		pooling.offer(o);
		return true;
	}

	/**
	 * プーリング管理数を取得.
	 * @return int プーリング管理数が返却されます.
	 */
	public int manageSize() {
		return manageLength;
	}

	/**
	 * 現在のプーリング数を取得.
	 * @return int 現在のプーリング数が返却されます.
	 */
	public int size() {
		return pooling.size();
	}
}
