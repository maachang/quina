package quina.promise;

/**
 * Promise要素.
 *
 * Promiseのステータスと返却Value情報を持ちます.
 */
public class PromiseValue {
	private PromiseStatus status;
	private Object value;

	/**
	 * コンストラクタ.
	 * @param status ステータスを設定します.
	 * @param value Valueを設定します.
	 */
	protected PromiseValue(PromiseStatus status, Object value) {
		this.status = status;
		this.value = value;
	}

	/**
	 * ステータスを取得.
	 * @return PromiseStatus ステータスが返却されます.
	 */
	public PromiseStatus getStatus() {
		return status;
	}

	/**
	 * Valueを取得.
	 * @return Object Valueが返却されます.
	 */
	public Object getValue() {
		return value;
	}
}
