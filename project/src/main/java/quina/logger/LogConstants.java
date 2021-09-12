package quina.logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ログ定義.
 */
public class LogConstants {
	private LogConstants() {
	}

	// デフォルトの出力可能なログレベル.
	// 0: trace, 1: debug, 2: info, 3: warn, 4: error, 5: fatal.
	private static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.INFO;

	// デフォルトのログ１ファイルの書き込みファイルサイズ(5MByte).
	private static final long DEFAULT_OUT_FILE_SIZE = 0x00100000 * 5;

	// デフォルトのコンソール出力モード.
	private static final boolean DEFAULT_CONSOLE_OUT = true;


	// デフォルトの出力先ログディレクトリを設定.
	private static final String DEFAULT_DIR = "./log";

	/**
	 * デフォルトのシステムログ名.
	 */
	public static final String SYSTEM_LOG = "system";

	// 基本設定の出力可能なログレベル.
	// 0: trace, 1: debug, 2: info, 3: warn, 4: error, 5: fatal, 9: console.
	private static final AtomicReference<LogLevel> logLevel =
		new AtomicReference<LogLevel>(DEFAULT_LOG_LEVEL);

	// 基本設定のログ１ファイルの書き込みファイルサイズ
	private static final AtomicLong fileSize =
		new AtomicLong(DEFAULT_OUT_FILE_SIZE);

	// 基本設定のコンソール出力モード.
	private static final AtomicBoolean consoleOut =
		new AtomicBoolean(DEFAULT_CONSOLE_OUT);

	// 基本設定の出力先ログディレクトリ.
	private static final AtomicReference<String> logDirectory =
		new AtomicReference<String>(DEFAULT_DIR);

	/**
	 * 基本設定の出力可能なログレベルを取得.
	 * @return
	 */
	public static final LogLevel getLogLevel() {
		return logLevel.get();
	}

	/**
	 * 基本設定の出力可能なログレベルを設定.
	 * @param level
	 */
	public static final void setLogLevel(LogLevel level) {
		if(level == null) {
			while (!logLevel.compareAndSet(logLevel.get(), LogLevel.DEBUG));
			return;
		}
		while (!logLevel.compareAndSet(logLevel.get(), level));
	}

	/**
	 * 基本設定のログ１ファイルの書き込みファイルサイズを取得.
	 * @return
	 */
	public static final long getLogSize() {
		return fileSize.get();
	}

	/**
	 * 基本設定のログ１ファイルの書き込みファイルサイズを設定.
	 * @param size
	 */
	public static final void setLogSize(long size) {
		while (!fileSize.compareAndSet(fileSize.get(), size));
	}

	/**
	 * 基本設定のコンソール出力モードを取得.
	 * @return
	 */
	public static final boolean getConsoleOut() {
		return consoleOut.get();
	}

	/**
	 * 基本設定のコンソール出力モードを取得.
	 * @param out
	 */
	public static final void setConsoleout(boolean out) {
		while (!consoleOut.compareAndSet(consoleOut.get(), out));
	}

	/**
	 * 基本設定の出力可能なログレベルを取得.
	 * @return
	 */
	public static final String getLogDirectory() {
		return logDirectory.get();
	}

	/**
	 * 基本設定の出力可能なログレベルを設定.
	 * @param dir
	 */
	public static final void setLogDirectory(String dir) {
		while (!logDirectory.compareAndSet(logDirectory.get(), dir));
	}
}
