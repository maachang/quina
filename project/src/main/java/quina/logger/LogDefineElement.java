package quina.logger;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import quina.util.Env;

/**
 * ログ定義要素.
 */
public class LogDefineElement {
	// 出力可能なログレベル.
	private LogLevel logLevel;

	// ログ１ファイルの書き込みファイルサイズ.
	private Long logSize;

	// コンソール出力モード.
	private Boolean consoleOut;

	// 出力先ログディレクトリ.
	private String directory;

	// 設定確定フラグ.
	private final AtomicBoolean finalizedFlag = new AtomicBoolean(false);

	// 書き込みロックオブジェクト.
	private final Object sync = new Object();

	/**
	 * コンストラクタ.
	 */
	public LogDefineElement() {
		set();
	}

	/**
	 * コンストラクタ.
	 * @param defaultInfo デフォルトの定義を設定します.
	 */
	public LogDefineElement(LogDefineElement defaultInfo) {
		set(defaultInfo);
	}

	/**
	 * コンストラクタ.
	 * @param map 定義条件が格納されたMapを設定します.
	 */
	public LogDefineElement(Map<String, Object> map) {
		set(map).set();
	}

	/**
	 * コンストラクタ.
	 * @param level 出力させない基本ログレベルを設定します.
	 * @param cons コンソール出力を許可する場合[true]を設定します.
	 * @param fileSize ログ分割をする基本ファイルサイズを設定します.
	 * @param directory ログ出力先のディレクトリを設定します.
	 */
	public LogDefineElement(
		Object level, Boolean cons, Long fileSize, String directory) {
		set(level, cons, fileSize, directory).set();
	}

	/**
	 * コンストラクタ.
	 * @param defaultInfo デフォルトの定義を設定します.
	 * @param map 定義条件が格納されたMapを設定します.
	 */
	public LogDefineElement(LogDefineElement defaultInfo,
		Map<String, Object> map) {
		set(map).set(defaultInfo);
	}

	/**
	 * コンストラクタ.
	 * @param defaultInfo デフォルトの定義を設定します.
	 * @param level 出力させない基本ログレベルを設定します.
	 * @param cons コンソール出力を許可する場合[true]を設定します.
	 * @param fileSize ログ分割をする基本ファイルサイズを設定します.
	 * @param directory ログ出力先のディレクトリを設定します.
	 */
	public LogDefineElement(LogDefineElement defaultInfo,
		Object level, Boolean cons, Long fileSize, String directory) {
		set(level, cons, fileSize, directory).set(defaultInfo);
	}

	// ログ定義確定済みの場合はエラー出力.
	protected final void checkFinalized() {
		if(finalizedFlag.get()) {
			throw new LogDefineException("The log definition has been finalized.");
		}
	}

	/**
	 * ログ定義を確定させる.
	 * @return boolean [true]の場合、今回の処理で確定できました.
	 *                 [false]の場合は、既に確定済みです.
	 */
	protected final boolean confirm() {
		// 確定済みにする.
		boolean flg = false;
		while(!finalizedFlag.compareAndSet((flg = finalizedFlag.get()), true));
		// 既に確定済みの場合.
		if(flg) {
			return false;
		}
		// null定義を初期セット.
		defaultDefine();
		// ログ出力先フォルダを確定する.
		directory = checkOutLogDir(directory);
		return true;
	}

	// ログ出力先のフォルダを確定する.
	protected static final String checkOutLogDir(String outDir) {
		String retDir = outDir;
		if(retDir == null) {
			retDir = LogConstants.getLogDirectory();
		}
		int p = retDir.lastIndexOf("/");
		int pp = retDir.lastIndexOf("¥¥");
		int endPos = retDir.length() - 1;
		if(!(p == endPos || pp == endPos)) {
			if(p == -1) {
				retDir = retDir + "¥¥";
			} else {
				retDir = retDir + "/";
			}
		}
		// ログ出力先フォルダが存在しない場合は作成する.
		if (retDir != null && retDir.length() != 0) {
			final File odir = new File(retDir);
			if (!odir.isDirectory()) {
				odir.mkdirs();
			}
		}
		return retDir;
	}

	// 初期定義を行います.
	private final void defaultDefine() {
		if(directory == null) {
			directory = LogConstants.getLogDirectory();
		}
		if(logLevel == null) {
			logLevel = LogConstants.getLogLevel();
		}
		if(logSize == null) {
			logSize = LogConstants.getLogSize();
		}
		if(consoleOut == null) {
			consoleOut = LogConstants.getConsoleOut();
		}
	}

	/**
	 * 確定済みかチェック.
	 * @return
	 */
	public boolean isFinalized() {
		return finalizedFlag.get();
	}

	/**
	 * デフォルト条件を設定.
	 * @return
	 */
	public LogDefineElement set() {
		defaultDefine();
		return this;
	}

	/**
	 * LogDefineElementを設定.
	 * @param defaultInfo
	 * @return
	 */
	public LogDefineElement set(LogDefineElement defaultInfo) {
		if(defaultInfo != null) {
			setDirectory(defaultInfo.getDirectory());
			setLogLevel(defaultInfo.getLogLevel());
			setLogSize(defaultInfo.getLogSize());
			setConsoleOut(defaultInfo.isConsoleOut());
		}
		defaultDefine();
		return this;
	}

	/**
	 * Map形式でログ定義を設定.
	 * @param map
	 * @return
	 */
	public LogDefineElement set(Map<String, Object> map) {
		checkFinalized();
		Object n;
		String dir = null;
		LogLevel lv = null;
		Long size = null;
		Boolean console = null;
		if(map.containsKey("level")) {
			lv = LogLevel.convertLogLevel(map.get("level"));
		}
		if (map.containsKey("console")) {
			n = map.get("console");
			if(n instanceof Boolean) {
				console = (Boolean)n;
			} else if(n instanceof Number) {
				console = ((Number)n).intValue() != 0;
			} else {
				n = ("" + n).trim().toLowerCase();
				if("false".equals(n) || "off".equals(n) || "f".equals(n)) {
					console = false;
				} else if("true".equals(n) || "on".equals(n) || "t".equals(n)) {
					console = true;
				}
			}
		}
		if (map.containsKey("maxFileSize")) {
			n = map.get("maxFileSize");
			if(n instanceof Number) {
				size = ((Number)n).longValue();
			} else if (LogFactory.isNumeric(n)) {
				try {
					size = LogFactory.convertLong(n);
				} catch(Exception e) {
					size = null;
				}
			}
		}
		if (map.containsKey("logDir")) {
			dir = "" + map.get("logDir");
		} else if (map.containsKey("logDirectory")) {
			dir = "" + map.get("logDirectory");
		}
		setLogLevel(lv);
		setLogSize(size);
		setConsoleOut(console);
		setDirectory(dir);
		defaultDefine();
		return this;
	}

	/**
	 * 直接指定で設定.
	 *
	 * @param level 出力させない基本ログレベルを設定します.
	 * @param cons コンソール出力を許可する場合[true]を設定します.
	 * @param fileSize ログ分割をする基本ファイルサイズを設定します.
	 * @param directory ログ出力先のディレクトリを設定します.
	 */
	public LogDefineElement set(
		Object level, Boolean cons, Long fileSize, String directory) {
		checkFinalized();
		String dir = null;
		LogLevel lv = null;
		Long size = null;
		Boolean console = null;
		if(level != null) {
			lv = LogLevel.convertLogLevel(level);
		}
		if(cons != null) {
			console = cons;
		}
		if (fileSize != null) {
			size = fileSize;
		}
		if (directory != null && !directory.isEmpty()) {
			dir = directory;
		}
		setLogLevel(lv);
		setLogSize(size);
		setConsoleOut(console);
		setDirectory(dir);
		defaultDefine();
		return this;
	}

	/**
	 * 出力先ログディレクトリを取得.
	 * @return
	 */
	public String getDirectory() {
		return directory;
	}

	/**
	 * 出力先ログディレクトリを設定.
	 * @param directory
	 * @return
	 */
	public LogDefineElement setDirectory(String directory) {
		checkFinalized();
		if(directory != null && !directory.isEmpty()) {
			this.directory = Env.path(directory);
		}
		return this;
	}

	/**
	 * 出力可能なログレベルを取得.
	 * @return
	 */
	public LogLevel getLogLevel() {
		return logLevel;
	}

	/**
	 * 出力可能なログレベルを設定.
	 * @param logLevel
	 * @return
	 */
	public LogDefineElement setLogLevel(LogLevel logLevel) {
		checkFinalized();
		if(logLevel != null) {
			this.logLevel = logLevel;
		}
		return this;
	}

	/**
	 * ログ１ファイルの書き込みファイルサイズを取得.
	 * @return
	 */
	public Long getLogSize() {
		return logSize;
	}

	/**
	 * ログ１ファイルの書き込みファイルサイズを設定.
	 * @param logSize
	 * @return
	 */
	public LogDefineElement setLogSize(Long logSize) {
		checkFinalized();
		if(logSize != null && logSize > 0L) {
			this.logSize = logSize;
		}
		return this;
	}

	/**
	 * コンソール出力モードを取得.
	 * @return
	 */
	public Boolean isConsoleOut() {
		return consoleOut;
	}

	/**
	 * コンソール出力モードを設定.
	 * @param consoleOut
	 * @return
	 */
	public LogDefineElement setConsoleOut(Boolean consoleOut) {
		checkFinalized();
		if(consoleOut != null) {
			this.consoleOut = consoleOut;
		}
		return this;
	}

	/**
	 * 書き込み同期オブジェクトを取得.
	 * @return
	 */
	public Object getSync() {
		return sync;
	}

	/**
	 * 内容を文字列で出力.
	 * @param buf
	 * @return
	 */
	public StringBuilder toString(StringBuilder buf) {
		buf.append("\"logLevel\": \"").append(logLevel)
			.append("\", \"fileSize\": ").append(logSize)
			.append("\", \"consoleOut\": ").append(consoleOut)
			.append(", \"logDir\": \"").append(directory).append("\"");
		return buf;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		return toString(buf).toString();
	}
}
