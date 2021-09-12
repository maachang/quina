package quina.logger;

/**
 * ログレベル.
 */
public enum LogLevel {
	// 1: trace, 2: debug, 3: info, 4: warn, 5: error, 6: fatal, 9: console.
	// デバッグトレースを出力.
	TRACE(1, "TRACE"),
	// デバッグログを出力.
	DEBUG(2, "DEBUG"),
	// 情報ログを出力.
	INFO(3, "INFO"),
	// 警告ログを出力.
	WARN(4, "WARN"),
	// 警告ログを出力.
	WARNING(4, "WARN"),
	// エラーログを出力.
	ERROR(5, "ERROR"),
	// 致命的エラーログを出力.
	FATAL(6, "FATAL"),
	// コンソールを出力.
	CONSOLE(9, "CONSOLE");

	private int level;
	private String name;

	/**
	 * コンストラクタ.
	 * @param level ログレベルを設定します.
	 * @param name ログレベル名を設定します.
	 */
	private LogLevel(int level, String name) {
		this.level = level;
		this.name = name;
	}

	/**
	 * ログレベルを取得します.
	 * @return
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * ログレベル名を取得します.
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 指定ログレベル番号からログレベルを取得.
	 * 0: trace, 1: debug, 2: info, 3: warn, 4: error, 5: fatal.
	 * @param level
	 * @return
	 */
	public static final LogLevel getLogLevel(int level) {
		switch(level) {
		case 1: return LogLevel.TRACE;
		case 2: return LogLevel.DEBUG;
		case 3: return LogLevel.INFO;
		case 4: return LogLevel.WARN;
		case 5: return LogLevel.ERROR;
		case 6: return LogLevel.FATAL;
		case 9: return LogLevel.CONSOLE;
		}
		// 判別不可の場合はnull返却.
		return null;
	}

	/**
	 * 指定ログレベル文字列からログレベルを取得.
	 * @param level
	 * @return
	 */
	public static final LogLevel getLogLevel(String level) {
		String lowName = level.toLowerCase();
		if("trace".equals(lowName)) {
			return LogLevel.TRACE;
		} else if("debug".equals(lowName) || "dev".equals(lowName)) {
			return LogLevel.DEBUG;
		} else if("info".equals(lowName) || "normal".equals(lowName)) {
			return LogLevel.INFO;
		} else if("warn".equals(lowName) || "warning".equals(lowName)) {
			return LogLevel.WARN;
		} else if("err".equals(lowName) || "error".equals(lowName)) {
			return LogLevel.ERROR;
		} else if("fatal".equals(lowName)) {
			return LogLevel.FATAL;
		} else if("console".equals(lowName)) {
			return LogLevel.CONSOLE;
		}
		// 判別不可の場合はnull返却.
		return null;
	}

	/**
	 * オブジェクトからログレベルに変換.
	 * @param level
	 * @return
	 */
	public static final LogLevel convertLogLevel(Object level) {
		if (level == null) {
			return null;
		}
		if(level instanceof LogLevel) {
			return (LogLevel)level;
		} else if(level instanceof Number) {
			return LogLevel.getLogLevel(((Number)level).intValue());
		} else if(LogUtil.isNumeric(level)) {
			try {
				LogLevel.getLogLevel(LogUtil.convertInt(level));
			} catch(Exception e) {
				return null;
			}
		} else {
			return LogLevel.getLogLevel("" + level);
		}
		return null;
	}

	/**
	 * Levelの値の大なり小なり同一の判別.
	 * @param v 比較先のLogLevelを設定します.
	 * @return int このオブジェクトが指定されたオブジェクトより小さい場合は負の整数、
	 *             等しい場合はゼロ、大きい場合は正の整数
	 */
	public int checkMinMaxEquals(LogLevel v) {
		return level - v.getLevel();
	}

	@Override
	public String toString() {
		return name;
	}
}
