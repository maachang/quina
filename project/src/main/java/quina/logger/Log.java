package quina.logger;

/**
 * ログインターフェイス.
 */
public interface Log {
	/**
	 * 出力ログレベルを設定してログを出力.
	 * @param level 出力対象のログレベルを設定します.
	 * @param args 出力内容を設定します.
	 * @return boolean [true]の場合は正しく処理されました.
	 */
	default boolean log(LogLevel level, Object... args) {
		switch(level) {
		case TRACE : trace(args); return true;
		case DEBUG : debug(args); return true;
		case INFO : info(args); return true;
		case WARNING : warn(args); return true;
		case WARN : warn(args); return true;
		case ERROR : error(args); return true;
		case FATAL : fatal(args); return true;
		case CONSOLE: console(args); return true;
		}
		return false;
	}

	/**
	 * 出力ログレベルを設定してログ出力が可能かチェック.
	 * @param level 出力対象のログレベルを設定します.
	 * @return boolean [true]の場合は出力可能です.
	 */
	default boolean isEnabled(LogLevel level) {
		switch(level) {
		case TRACE : return isTraceEnabled();
		case DEBUG : return isDebugEnabled();
		case INFO : return isInfoEnabled();
		case WARNING : return isWarnEnabled();
		case WARN : return isWarnEnabled();
		case ERROR : return isErrorEnabled();
		case FATAL : return isFatalEnabled();
		case CONSOLE: return isFatalEnabled();
		}
		return false;
	}

	/**
	 * traecログを出力.
	 * @param args 出力内容を設定します.
	 */
	public void trace(Object... args);

	/**
	 * debugログを出力.
	 * @param args 出力内容を設定します.
	 */
	public void debug(Object... args);

	/**
	 * infoログを出力.
	 * @param args 出力内容を設定します.
	 */
	public void info(Object... args);

	/**
	 * warnログを出力.
	 * @param args 出力内容を設定します.
	 */
	public void warn(Object... args);

	/**
	 * errorログを出力.
	 * @param args 出力内容を設定します.
	 */
	public void error(Object... args);

	/**
	 * fatalログを出力.
	 * @param args 出力内容を設定します.
	 */
	public void fatal(Object... args);

	/**
	 * consoleログを出力.
	 * @param args 出力内容を設定します.
	 */
	public void console(Object... args);

	/**
	 * traceログが出力可能かチェック.
	 * @return boolean [true]の場合は出力可能です.
	 */
	public boolean isTraceEnabled();

	/**
	 * debugログが出力可能かチェック.
	 * @return boolean [true]の場合は出力可能です.
	 */
	public boolean isDebugEnabled();

	/**
	 * infoログが出力可能かチェック.
	 * @return boolean [true]の場合は出力可能です.
	 */
	public boolean isInfoEnabled();

	/**
	 * warnログが出力可能かチェック.
	 * @return boolean [true]の場合は出力可能です.
	 */
	public boolean isWarnEnabled();

	/**
	 * errorログが出力可能かチェック.
	 * @return boolean [true]の場合は出力可能です.
	 */
	public boolean isErrorEnabled();

	/**
	 * fatalログが出力可能かチェック.
	 * @return boolean [true]の場合は出力可能です.
	 */
	public boolean isFatalEnabled();

	/**
	 * consoleログが出力可能かチェック.
	 * @return boolean [true]の場合は出力可能です.
	 */
	public boolean isConsoleEnabled();

	/**
	 * 定義されているログ定義名を取得.
	 * @return String 定義されているログの定義名が返却されます.
	 */
	public String getName();

	/**
	 * 定義されているログ定義を取得.
	 * @return LogDefineElement 定義されているログ定義が返却されます.
	 */
	public LogDefineElement getDefineElement();
}
