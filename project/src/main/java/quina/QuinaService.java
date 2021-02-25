package quina;

/**
 * QuinaService.
 */
public interface QuinaService {

	/**
	 * コンフィグ情報を読み込む.
	 * @param configDir コンフィグディレクトリを設定します.
	 */
	public void readConfig(String configDir);

	/**
	 * QuinaInfoを取得.
	 * @return QuinaInfo QuinaInfoが返却されます.
	 */
	public QuinaInfo getInfo();

	/**
	 * 既にサービスが稼働/停止している場合はエラー返却.
	 * @param flg [true]の場合、開始中 [false]の場合、停止中の場合、
	 *            エラーが発生します.
	 */
	public void check(boolean flg);

	/**
	 * サービスが開始されているかチェック.
	 * @return boolena [true]の場合、開始しています.
	 */
	public boolean isStartService();

	/**
	 * サービスを開始.
	 */
	public void startService();

	/**
	 * サービスが開始完了したかチェック.
	 * @return boolean trueの場合、サービスは開始完了しています.
	 */
	default boolean isStarted() {
		return isStartService();
	}

	/**
	 * サービスの開始が完了するまで待機します.
	 * @return boolean [true]の場合、正しくサービス開始が行われました.
	 */
	default boolean waitToStartup() {
		return waitToStartup(-1L);
	}

	/**
	 * サービスの開始が完了するまで待機します.
	 * @param timeout タイムアウト値を設定します.
	 *                0以下を設定した場合は、無限に待ちます.
	 * @return boolean [true]の場合、正しくサービス開始が行われました.
	 */
	default boolean waitToStartup(long timeout) {
		return true;
	}

	/**
	 * サービスを停止.
	 */
	public void stopService();

	/**
	 * サービスが終了したかチェック.
	 * @return boolean trueの場合、サービスは終了しています.
	 */
	default boolean isExit() {
		return !isStartService();
	}

	/**
	 * サービスの停止が完了するまで待機します.
	 * @return boolean [true]の場合、正しくサービス停止が行われました.
	 */
	default boolean waitToExit() {
		return waitToExit(-1L);
	}

	/**
	 * サービスの停止が完了するまで待機します.
	 * @param timeout タイムアウト値を設定します.
	 *                0以下を設定した場合は、無限に待ちます.
	 * @return boolean [true]の場合、正しくサービス停止が行われました.
	 */
	default boolean waitToExit(long timeout) {
		return true;
	}
}
