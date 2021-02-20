package quina.logger;

import java.util.concurrent.atomic.AtomicReference;

/**
 * ログ定義.
 */
public class LogConstants {
	private LogConstants() {
	}

	// デフォルトログディレクトリ名.
	private static final String DEFAULT_DIR = "./log";

	/**
	 * システムログ名.
	 */
	public static final String SYSTEM_LOG = "system";

	/**
	 * デフォルトのログディレクトリ.
	 */
	private static final AtomicReference<String> logDirectory =
		new AtomicReference<String>(DEFAULT_DIR);

	/**
	 * デフォルトのログディレクトリを取得.
	 * @return
	 */
	public static final String getLogDirectory() {
		return logDirectory.get();
	}

	/**
	 * デフォルトのログディレクトリを設定.
	 * @param dir
	 */
	public static final void setLogDirectory(String dir) {
		while (!logDirectory.compareAndSet(logDirectory.get(), dir));
	}
}
