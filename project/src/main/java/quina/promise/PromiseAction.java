package quina.promise;

import quina.http.furnishing.BaseSendResponse;
import quina.http.furnishing.EmptySendResponse;
import quina.http.furnishing.ErrorSendResponse;
import quina.http.furnishing.FileSendResponse;
import quina.http.furnishing.InputStreamSendResponse;
import quina.http.furnishing.JsonSendResponse;
import quina.http.furnishing.MemorySendResponse;

/**
 * Promiseアクション.
 */
public interface PromiseAction<T> extends BaseSendResponse<T>,
	EmptySendResponse<T>, MemorySendResponse<T>,
	JsonSendResponse<T>, InputStreamSendResponse<T>,
	FileSendResponse<T>, ErrorSendResponse<T> {
	/**
	 * 次の正常処理を実行します.
	 * @return T オブジェクトが返却されます.
	 */
	public T resolve();

	/**
	 * 次の正常処理を実行します.
	 * この処理を呼び出すと次のthen()やallways()を実行します.
	 * @param value 実行引数を設定します.
	 * @return T オブジェクトが返却されます.
	 */
	public T resolve(Object value);

	/**
	 * 次の異常系処理を実行します.
	 * この処理を呼び出すと次のerror()やallways()を実行します.
	 * @param value 実行引数を設定します.
	 * @return T オブジェクトが返却されます.
	 */
	public T reject(Object value);

	/**
	 * 処理を終わらせます.
	 * この処理を呼び出すと次のthen()やerror()やallways()で
	 * 定義された内容を無視してPromiseを終わらせます.
	 * @param value 実行引数を設定します.
	 * @return T オブジェクトが返却されます.
	 */
	public T exit(Object value);

	/**
	 * 現在のPromiseステータスを取得.
	 * @return PromiseStatus Promiseステータスが返却されます.
	 */
	public PromiseStatus getStatus();
}
