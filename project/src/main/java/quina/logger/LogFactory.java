package quina.logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Logファクトリ.
 */
public class LogFactory {
	// 出力先ログ.
	private String outDir = null;

	// ログ出力レベル.
	// 0: trace, 1: debug, 2: info, 3: warn, 4: error, 5: fatal.
	private int outLogLevel = Log.DEBUG;

	// １ファイルの最大サイズ(1MByte).
	private long outFileSize = 0x00100000;

	// コンソール出力モード.
	private boolean outConsoleOut = true;

	// 個別のログ設定用のコンフィグ.
	private Map<String, Object> config = null;

	// デフォルトのログ名.
	private String defaultLogName = LogConstants.SYSTEM_LOG;

	// シングルトン.
	private static final LogFactory SNGL = new LogFactory();

	/**
	 * シングルトンオブジェクトを取得.
	 * @return LogFactory ログファクトリが返却されます.
	 */
	public static final LogFactory getInstance() {
		return SNGL;
	}

	/**
	 * コンストラクタ.
	 */
	private LogFactory() {
	}

	/**
	 * ログコンフィグの設定.
	 *
	 * @param level
	 *            出力させない基本ログレベルを設定します.
	 * @param console
	 *            コンソール出力を許可する場合[true]を設定します.
	 * @param fileSize
	 *            ログ分割をする基本ファイルサイズを設定します.
	 * @param dir
	 *            ログ出力先のディレクトリを設定します.
	 */
	public void config(Object level, boolean console, Long fileSize, String dir) {
		if (level != null) {
			if (level instanceof String) {
				level = strLogLevelByNumber((String) level);
			}
			outLogLevel = isNumeric(level) ? convertInt(level) : Log.DEBUG;
		}
		if (fileSize != null) {
			outFileSize = fileSize;
			outFileSize = outFileSize < 0 ? -1 : outFileSize;
		}
		if (dir != null && dir.length() != 0) {
			outDir = dir;
		}
		checkOutLogDir();
	}

	// ログ出力先を確定する.
	private final void checkOutLogDir() {
		if(outDir == null) {
			outDir = LogConstants.getLogDirectory();
		}
		int p = outDir.lastIndexOf("/");
		int pp = outDir.lastIndexOf("¥¥");
		int endPos = outDir.length() - 1;
		if(!(p == endPos || pp == endPos)) {
			if(p == -1) {
				outDir = outDir + "¥¥";
			} else {
				outDir = outDir + "/";
			}
		}
		// ログ出力先フォルダが存在しない場合は作成する.
		if (outDir != null && outDir.length() != 0) {
			final File odir = new File(outDir);
			if (!odir.isDirectory()) {
				odir.mkdirs();
			}
		}
	}

	/**
	 * ログコンフィグの設定.
	 *
	 * @param json
	 */
	public void config(Map<String, Object> json) {
		Object level = json.get("level");
		boolean console = true;
		Long fileSize = null;
		String dir = null;

		if (json.containsKey("console")) {
			Object n = ("" + json.get("console")).toLowerCase();
			console = !("false".equals(n) || "off".equals(n) || "f".equals(n));
		}
		if (json.containsKey("maxFileSize")) {
			if (isNumeric(json.get("maxFileSize"))) {
				fileSize = convertLong(json.get("maxFileSize"));
			}
		}
		if (json.containsKey("logDir")) {
			dir = "" + json.get("logDir");
		}
		config(level, console, fileSize, dir);

		// コンフィグ情報として保持.
		config = json;
	}

	/**
	 * デフォルトのログ名を設定.
	 * @param name デフォルトのログ名を設定します.
	 */
	public void setDefaultLogName(String name) {
		if(name == null || (name = name.trim()).length() == 0) {
			return;
		}
		defaultLogName = name;
	}

	/**
	 * デフォルトのログ名を取得.
	 * @return String デフォルトのログ名が返却されます.
	 */
	public String getDefaultLogName() {
		return defaultLogName;
	}

	/**
	 * デフォルトの設定ログレベルを取得.
	 *
	 * @return
	 */
	public int logLevel() {
		return outLogLevel;
	}

	/**
	 * デフォルトのログ出力先フォルダ名取得.
	 *
	 * @return
	 */
	public String logDir() {
		return outDir;
	}

	/**
	 * デフォルトの１つのログファイルサイズを取得.
	 *
	 * @return
	 */
	public long maxFileSize() {
		return outFileSize;
	}

	/**
	 * コンソール出力の許可取得.
	 * @return
	 */
	public boolean isConsole() {
		return outConsoleOut;
	}

	// ログ情報取得.
	public Log get() {
		return get(defaultLogName, null, null, null);
	}

	// ログ情報取得.
	public Log get(String name) {
		return get(name, null, null, null);
	}

	// ログ情報取得.
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Log get(String name, Object consoleOut, Object logLevel, Long fileSize) {
		if (name == null) {
			throw new NullPointerException();
		} else if (name.length() == 0) {
			throw new IllegalArgumentException();
		}
		// コンフィグ情報が存在する場合は、コンフィグ情報の内容を元に設定.
		if (config != null &&
			(consoleOut == null || logLevel == null || fileSize == null)) {
			Object o = config.get(name);
			if (o instanceof Map) {
				Map<String, Object> logConf = (Map) o;
				if (logLevel == null) {
					logLevel = logConf.get("logLevel");
					if (logLevel != null && isNumeric(logLevel)) {
						logLevel = convertInt(logLevel);
					}
				}
				if (consoleOut == null) {
					o = logConf.get("console");
					if(o != null) {
						String n = ("" + o).toLowerCase();
						consoleOut = "false".equals(n) || "off".equals(n) || "f".equals(n);
					}
				}
				if (fileSize == null) {
					o = logConf.get("fileSize");
					if (o != null && isNumeric(o)) {
						fileSize = convertLong(o);
					}
				}
			}
		}
		int lv = outLogLevel;
		boolean co = outConsoleOut;
		long fs = outFileSize;
		if (logLevel != null) {
			if (logLevel instanceof String) {
				logLevel = strLogLevelByNumber((String) logLevel);
			}
			lv = convertInt(logLevel);
		}
		if (fileSize != null) {
			fs = fileSize < 0 ? -1 : fileSize;
		}
		if (consoleOut != null) {
			if (consoleOut instanceof Boolean) {
				co = (Boolean)consoleOut;
			} else if(consoleOut instanceof Number) {
				co = ((Number)consoleOut).intValue() != 0;
			} else {
				String n = ("" + consoleOut).toLowerCase();
				co = "false".equals(n) || "off".equals(n) || "f".equals(n);
			}
		}
		return new BaseLog(name, lv, co, fs, outDir);
	}

	/**
	 * ログ実装.
	 */
	private static final class BaseLog implements Log {
		private String name;
		private String logDir;
		private boolean consoleOut;
		private int logLevel;
		private long maxFileSize;

		BaseLog(String n, int lv, boolean co, long fs, String ld) {
			name = n;
			logLevel = lv;
			consoleOut = co;
			maxFileSize = fs;
			logDir = ld;
		}

		@Override
		public boolean log(int level, Object... args) {
			switch(level) {
			case TRACE : trace(args); return true;
			case DEBUG : debug(args); return true;
			case INFO : info(args); return true;
			case WARN : warn(args); return true;
			case ERROR : error(args); return true;
			case FATAL : fatal(args); return true;
			}
			// ログレベルが不明な場合は、コンソール出力.
			if(consoleOut) {
				System.out.println(format("CONSOLE", args));
			}
			return true;
		}

		@Override
		public void trace(Object... args) {
			LogFactory.write(name, logLevel, consoleOut,
				maxFileSize, TRACE, logDir, args);
		}

		@Override
		public void debug(Object... args) {
			LogFactory.write(name, logLevel, consoleOut,
				maxFileSize, DEBUG, logDir, args);
		}

		@Override
		public void info(Object... args) {
			LogFactory.write(name, logLevel, consoleOut,
				maxFileSize, INFO, logDir, args);
		}

		@Override
		public void warn(Object... args) {
			LogFactory.write(name, logLevel, consoleOut,
				maxFileSize, WARN, logDir, args);
		}

		@Override
		public void error(Object... args) {
			LogFactory.write(name, logLevel, consoleOut,
				maxFileSize, ERROR, logDir, args);
		}

		@Override
		public void fatal(Object... args) {
			LogFactory.write(name, logLevel, consoleOut,
				maxFileSize, FATAL, logDir, args);
		}

		@Override
		public boolean isTraceEnabled() {
			return logLevel <= TRACE;
		}

		@Override
		public boolean isDebugEnabled() {
			return logLevel <= DEBUG;
		}

		@Override
		public boolean isInfoEnabled() {
			return logLevel <= INFO;
		}

		@Override
		public boolean isWarnEnabled() {
			return logLevel <= WARN;
		}

		@Override
		public boolean isErrorEnabled() {
			return logLevel <= ERROR;
		}

		@Override
		public boolean isFatalEnabled() {
			return logLevel <= FATAL;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return new StringBuilder("\"name\": \"").append(name)
				.append("\", \"logLevel\": \"").append(numberLogLevelByStr(logLevel))
				.append("\", \"fileSize\": ").append(maxFileSize)
				.append(", \"logDir\": \"").append(logDir).append("\"")
				.toString();
		}
	}

	// 数値変換可能かチェック.
	private static final boolean isNumeric(Object o) {
		if(o instanceof Number) {
			return true;
		}
		try {
			Long.parseLong(""+o);
		} catch(Exception e) {
			return false;
		}
		return true;
	}

	// int 変換.
	private static final int convertInt(Object o) {
		if(o instanceof Number) {
			return ((Number)o).intValue();
		}
		return Integer.parseInt(""+o);
	}

	// long 変換.
	private static final long convertLong(Object o) {
		if(o instanceof Number) {
			return ((Number)o).longValue();
		}
		return Long.parseLong(""+o);
	}

	// 日付情報を取得.
	@SuppressWarnings("deprecation")
	private static final String dateString(Date d) {
		String n;
		return new StringBuilder().append((d.getYear() + 1900)).append("-")
				.append("00".substring((n = "" + (d.getMonth() + 1)).length())).append(n).append("-")
				.append("00".substring((n = "" + d.getDate()).length())).append(n).toString();
	}

	// ログフォーマット情報を作成.
	@SuppressWarnings("deprecation")
	private static final String format(String type, Object[] args) {
		String n;
		Date d = new Date();
		StringBuilder buf = new StringBuilder();
		buf.append("[").append(d.getYear() + 1900).append("/")
				.append("00".substring((n = "" + (d.getMonth() + 1)).length())).append(n).append("/")
				.append("00".substring((n = "" + d.getDate()).length())).append(n).append(" ")
				.append("00".substring((n = "" + d.getHours()).length())).append(n).append(":")
				.append("00".substring((n = "" + d.getMinutes()).length())).append(n).append(":")
				.append("00".substring((n = "" + d.getSeconds()).length())).append(n).append(".")
				.append((n = "" + d.getTime()).substring(n.length() - 3)).append("] [").append(type).append("] ");

		Object o;
		String nx = "";
		int len = (args == null) ? 0 : args.length;
		for (int i = 0; i < len; i++) {
			if ((o = args[i]) instanceof Throwable) {
				buf.append("\n").append(getStackTrace((Throwable) o));
				nx = "\n";
			} else {
				buf.append(nx).append(o).append(" ");
				nx = "";
			}
		}
		return buf.append("\n").toString();
	}

	// stackTraceを文字出力.
	private static final String getStackTrace(Throwable t) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}

	// 文字指定のログレベルを、数値に変換.
	private static final int strLogLevelByNumber(String level) {
		level = level.toLowerCase();
		if("trace".equals(level)) {
			return Log.TRACE;
		} else if("debug".equals(level) || "dev".equals(level)) {
			return Log.DEBUG;
		} else if("info".equals(level) || "normal".equals(level)) {
			return Log.INFO;
		} else if("warn".equals(level) || "warning".equals(level)) {
			return Log.WARN;
		} else if("err".equals(level) || "error".equals(level)) {
			return Log.ERROR;
		} else if("fatal".equals(level)) {
			return Log.FATAL;
		}
		return Log.DEBUG;
	}

	// 対象のログレベルの数値を、文字変換.
	private static final String numberLogLevelByStr(int level) {
		switch (level) {
		case Log.TRACE:
			return "TRACE";
		case Log.DEBUG:
			return "DEBUG";
		case Log.INFO:
			return "INFO";
		case Log.WARN:
			return "WARN";
		case Log.ERROR:
			return "ERROR";
		case Log.FATAL:
			return "FATAL";
		}
		return "DEBUG";
	}

	// ファイル追加書き込み.
	private static final void appendFile(String name, String out) {
		FileOutputStream o = null;
		try {
			byte[] b = out.getBytes("UTF8");
			o = new FileOutputStream(name, true);
			o.write(b);
			o.close();
			o = null;
		} catch (Exception e) {
			if (o != null) {
				try {
					o.close();
				} catch (Exception ee) {
				}
			}
		}
	}

	// ログ出力処理.
	@SuppressWarnings("deprecation")
	private static final void write(final String name, final int logLevel, final boolean consoleOut,
			final long fileSize, final int typeNo, final String logDir, final Object... args) {
		// 指定されたログレベル以下はログ出力させない場合.
		if (typeNo < logLevel) {
			return;
		}
		// ログ出力先がない場合は作成.
		final File dir = new File(logDir);
		if (!dir.isDirectory()) {
			dir.mkdirs();
		}

		final String format = format(numberLogLevelByStr(typeNo), args);
		final String fileName = name + ".log";
		final File stat = new File(logDir + fileName);
		final Date date = new Date(stat.lastModified());
		final Date now = new Date();

		// ファイルサイズの最大値が設定されていて、その最大値が増える場合.
		// また、現在のログファイルの日付が、現在の日付と一致しない場合.
		if (stat.isFile() && ((fileSize > 0 && stat.length() + format.length() > fileSize) || ((date.getYear() & 31)
				| ((date.getMonth() & 31) << 9) | ((date.getDate() & 31) << 18)) != ((now.getYear() & 31)
						| ((now.getMonth() & 31) << 9) | ((now.getDate() & 31) << 18)))) {
			// 現在のログファイルをリネームして、新しいログファイルに移行する.
			int p, v;
			String n;
			int cnt = -1;
			File renameToStat = null;
			final String targetName = fileName + "." + dateString(date) + ".";

			// nameでjava内同期.
			final String sync = name.intern();
			synchronized (sync) {
				// 指定フォルダ内から、targetNameの条件とマッチするものを検索.
				String[] list = dir.list(new FilenameFilter() {
					public boolean accept(final File file, final String str) {
						return str.indexOf(targetName) == 0;
					}
				});
				// そこの一番高いカウント値＋１の値を取得.
				int len = (list == null) ? 0 : list.length;
				for (int i = 0; i < len; i++) {
					n = list[i];
					p = n.lastIndexOf(".");
					v = Integer.parseInt(n.substring(p + 1));
					if (cnt < v) {
						cnt = v;
					}
				}
				// 今回のファイルをリネーム.
				stat.renameTo(renameToStat = new File(logDir + targetName + (cnt + 1)));
			}

			// リネーム先ファイル名.
			final String tname = logDir + targetName + (cnt + 1);
			final File tstat = renameToStat;

			// gzip圧縮(スレッド実行).
			Thread t = new Thread() {
				public void run() {
					try {
						int len;
						byte[] b = new byte[1024];
						InputStream in = new BufferedInputStream(new FileInputStream(tname));
						OutputStream out = null;
						try {
							out = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(tname + ".gz")));
							while ((len = in.read(b)) != -1) {
								out.write(b, 0, len);
							}
							out.flush();
							out.close();
							out = null;
							in.close();
							in = null;
							tstat.delete();
						} catch (Exception e) {
						} finally {
							if (out != null) {
								try {
									out.close();
								} catch (Exception e) {
								}
							}
							if (in != null) {
								try {
									in.close();
								} catch (Exception e) {
								}
							}
						}
					} catch (Exception e) {
					}
				}
			};
			t.setDaemon(true);
			t.start();
			t = null;
		}
		// ログ出力.
		appendFile(logDir + fileName, format);
		if(consoleOut) {
			System.out.print(format);
		}
	}
}
