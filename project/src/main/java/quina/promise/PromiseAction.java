package quina.promise;

/**
 * Promiseアクション.
 */
public interface PromiseAction {
	/**
	 * 次の正常処理を実行します.
	 * @return PromiseAction オブジェクトが返却されます.
	 */
	public PromiseAction resolve();

	/**
	 * 次の正常処理を実行します.
	 * この処理を呼び出すと次のthen()やallways()を実行します.
	 * @param value 実行引数を設定します.
	 * @return PromiseAction オブジェクトが返却されます.
	 */
	public PromiseAction resolve(Object value);

	/**
	 * 次の異常系処理を実行します.
	 * この処理を呼び出すと次のerror()やallways()を実行します.
	 * @param value 実行引数を設定します.
	 * @return PromiseAction オブジェクトが返却されます.
	 */
	public PromiseAction reject(Object value);

	/**
	 * 処理を終わらせます.
	 * この処理を呼び出すと次のthen()やerror()やallways()で
	 * 定義された内容を無視してPromiseを終わらせます.
	 * @param value 実行引数を設定します.
	 * @return PromiseAction オブジェクトが返却されます.
	 */
	public PromiseAction exit(Object value);

	/**
	 * 現在のPromiseステータスを取得.
	 * @return PromiseStatus Promiseステータスが返却されます.
	 */
	public PromiseStatus getStatus();
}
