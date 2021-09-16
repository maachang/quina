package quina;

import quina.exception.QuinaException;

/**
 * QuinaService.
 */
public interface QuinaService {

	/**
	 * コンフィグ情報を読み込む.
	 * @param configDir コンフィグディレクトリを設定します.
	 */
	public void loadConfig(String configDir);

	/**
	 * QuinaConfigを取得.
	 * @return QuinaConfig QuinaConfigが返却されます.
	 */
	public QuinaConfig getConfig();

	/**
	 * サービスの状態チェック.
	 * @param mode [true]を指定した場合、開始中の場合、
	 *             エラーが発生します.
	 *             [false]を指定した場合、停止中の場合、
	 *             エラーが発生します.
	 */
	default void checkService(boolean mode) {
		// 指定したフラグ条件と開始フラグが一致した場合.
		if(isStartService() == mode) {
			if(mode) {
				throw new QuinaException(
					this.getClass().getName() +
					" service has already started.");
			}
			throw new QuinaException(
				this.getClass().getName() +
				" service is already stopped.");
		}
	}


	/**
	 * サービス開始処理[startService()]が実行されたかチェック.
	 * @return boolena [true]の場合、サービス開始処理が呼び出されています.
	 */
	public boolean isStartService();

	/**
	 * サービスを開始処理.
	 */
	public void startService();

	/**
	 * サービスが起動完了したかチェック.
	 * @return boolean trueの場合、サービスは起動完了しています.
	 */
	default boolean isStarted() {
		return isStartService();
	}

	/**
	 * サービスが起動完了するまで待機します.
	 * @return boolean [true]の場合、正しくサービスの起動完了が確認されました.
	 */
	default boolean awaitStartup() {
		return awaitStartup(-1L);
	}

	/**
	 * サービスが起動完了するまで待機します.
	 * @param timeout タイムアウト値を設定します.
	 *                0以下を設定した場合は、無限に待ちます.
	 * @return boolean [true]の場合、正しくサービスの起動完了が確認されました.
	 */
	default boolean awaitStartup(long timeout) {
		return true;
	}

	/**
	 * サービスを停止.
	 */
	public void stopService();

	/**
	 * サービスの終了が完了したかチェック.
	 * @return boolean trueの場合、サービスの終了が完了しています.
	 */
	default boolean isExit() {
		return !isStartService();
	}

	/**
	 * サービスの終了が完了するまで待機します.
	 * @return boolean [true]の場合、正しくサービス終了が完了しました.
	 */
	default boolean awaitExit() {
		return awaitExit(-1L);
	}

	/**
	 * サービスの終了が完了するまで待機します.
	 * @param timeout タイムアウト値を設定します.
	 *                0以下を設定した場合は、無限に待ちます.
	 * @return boolean [true]の場合、正しくサービス終了が完了しました.
	 */
	default boolean awaitExit(long timeout) {
		return true;
	}
}
