package quina.util;

/**
 * 指定名に対するロックオブジェクトを取得.
 */
public class NamedLock {
	/**
	 * デフォルトのロック管理オブジェクト数.
	 */
	private static final int DEFAULT_LOCK_LENGTH = 256;

	private static final int MAX_LOCK_LENGTH = 65536;

	/**
	 * ロック管理オブジェクト.
	 */
	private final Object[] syncArray;

	/**
	 * null文字ロックオブジェクト.
	 */
	private final Object nullSync = new Object();

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
		size = BinaryUtil.bitMask(size);
		if(size > MAX_LOCK_LENGTH) {
			size = MAX_LOCK_LENGTH;
		}
		final Object[] o = new Object[size];
		for(int i = 0; i < size; i ++) {
			o[i] = new Object();
		}
		this.syncArray = o;
		this.mask = size - 1;
	}

	/**
	 * 名前を指定してロックオブジェクトを取得.
	 * @param name 名前を設定します.
	 * @return Object ロック用のオブジェクトが返却されます.
	 */
	public Object get(String name) {
		if(name == null) {
			return nullSync;
		}
		return syncArray[name.hashCode() & mask];
	}
}
