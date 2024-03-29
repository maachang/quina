package quina.promise;

/**
 * Promiseオブジェクト内部状態
 */
public enum PromiseStatus {
	/**
	 * 保留.
	 */
	Pending(PromiseStatus.PENDING, "pending"),
	/**
	 * 成功.
	 */
	Fulfilled(PromiseStatus.FULFILLED, "fulfilled"),
	/**
	 * 失敗.
	 */
	Rejected(PromiseStatus.REJECTED, "rejected");

	// 保留ステータス番号.
	private static final int PENDING = 0;
	// 成功ステータス番号.
	private static final int FULFILLED = 1;
	// 失敗ステータス番号.
	private static final int REJECTED = 2;

	private int status;
	private String name;

	/**
	 * コンストラクタ.
	 * @param status Promise内部状態のステータス数値を設定します.
	 * @param name Promise内部状態のステータス名を設定します.
	 */
	private PromiseStatus(int status, String name) {
		this.status = status;
		this.name = name;
	}

	/**
	 * romise内部状態のステータス数値を取得.
	 * @return
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Promise内部状態のステータス名を取得.
	 * @return
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * 指定ステータス番号からPromiseStatusを取得.
	 * @param status
	 * @return
	 */
	public static final PromiseStatus getPromiseStatus(int status) {
		switch(status) {
		case PENDING: return Pending;
		case FULFILLED: return Fulfilled;
		case REJECTED: return Rejected;
		}
		return null;
	}
}
