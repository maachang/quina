package quina.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 名前に対するロックオブジェクトを取得.
 */
public class NamedLock {
	/**
	 * デフォルトのロック管理オブジェクト数.
	 */
	private static final int DEFAULT_LOCK_LENGTH = 256;

	/**
	 * 最小ロック管理オブジェクト数.
	 */
	private static final int MIN_LOCK_LENGTH = 32;
	
	/**
	 * 最大ロック管理オブジェクト数.
	 */
	private static final int MAX_LOCK_LENGTH = 65536;

	/**
	 * ロック管理オブジェクト.
	 */
	private final Lock[] syncArray;

	/**
	 * null文字ロックオブジェクト.
	 */
	private final Lock nullSync = new ReentrantLock();

	/**
	 * ロック管理マスク値.
	 */
	private final int mask;

	/**
	 * コンストラクタ.
	 */
	public NamedLock() {
		this(DEFAULT_LOCK_LENGTH);
	}

	/**
	 * コンストラクタ
	 * @param size ロックオブジェクト数を設定します.
	 *             この値は２の倍数に丸められます.
	 */
	public NamedLock(int size) {
		if(size <= 0) {
			size = DEFAULT_LOCK_LENGTH;
		}
		if(size < MIN_LOCK_LENGTH) {
			size = MIN_LOCK_LENGTH;
		} else if(size > MAX_LOCK_LENGTH) {
			size = MAX_LOCK_LENGTH;
		}
		size = BinaryUtil.bitMask(size);
		final Lock[] o = new Lock[size];
		for(int i = 0; i < size; i ++) {
			o[i] = new ReentrantLock();
		}
		this.syncArray = o;
		this.mask = size - 1;
	}

	/**
	 * 名前を指定してロックオブジェクトを取得.
	 * @param name 名前を設定します.
	 * @return Lock ロック用のオブジェクトが返却されます.
	 */
	public Lock get(String name) {
		if(name == null) {
			return nullSync;
		}
		// 文字列のHashコードでロックオブジェクトを取得.
		return syncArray[name.hashCode() & mask];
	}
}
