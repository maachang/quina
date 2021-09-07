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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

/**
 * Logファクトリ.
 */
public class LogFactory {
	// デフォルト定義名.
	private static final String DEFAULT_NAME = "default";

	// デフォルトのログ名.
	private String defaultLogName = LogConstants.SYSTEM_LOG;

	// 基本ログ定義.
	private LogDefineElement defaultLogDefine = new LogDefineElement();

	// ログ定義情報.
	private final Map<String, LogDefineElement> manager = new HashMap<String, LogDefineElement>();

	// コンフィグ処理が呼び出されたかチェック.
	private boolean configFlag = false;

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
	 * ログコンフィグ設定を完了させる.
	 * @return LogFactory 
	 */
	public synchronized LogFactory fixConfig() {
		configFlag = true;
		return this;
	}
	
	/**
	 * コンフィグの設定が完了済みかチェック.
	 * @return
	 */
	public synchronized boolean isFixConfig() {
		return configFlag;
	}

	/**
	 * ログコンフィグの設定.
	 * @param json json定義を設定します.
	 * @return true の場合、読み込みに成功しました.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized boolean loadConfig(Map<String, Object> json) {
		/**
		 * jsonのコンフィグ定義内容説明.
		 *
		 * ２つの定義方法がある。
		 *
		 * (1)１つは「共通定義」の方法.
		 * {
		 *   default: デフォルトのログ定義名を設定します
		 *            (未定義の場合は "system" が割り当てられる.
		 *   level: ログレベル(TRACE~FATALまで).
		 *   console: コンソール出力モード(true/false).
		 *   maxFileSize: １つのログ出力サイズ(byte).
		 *   logDir 出力先のログディレクトリ.
		 * }
		 *
		 * (2)もう１つはログ定義名単位で定義する方法.
		 * {
		 *   default : {
		 *     default: デフォルトのログ定義名を設定します
		 *              (未定義の場合は "system" が割り当てられる.
		 *     level: ログレベル(TRACE~FATALまで).
		 *     console: コンソール出力モード(true/false).
		 *     maxFileSize: １つのログ出力サイズ(byte).
		 *     logDir 出力先のログディレクトリ.
		 *   },
		 *   「ログ定義名」 : {
		 *     level: ログレベル(TRACE~FATALまで).
		 *     console: コンソール出力モード(true/false).
		 *     maxFileSize: １つのログ出力サイズ(byte).
		 *     logDir 出力先のログディレクトリ.
		 *   }
		 *   ・
		 *   ・
		 *   ・
		 * }
		 *
		 * 共通定義では、どのログ定義名にも適用されます.
		 *
		 * ログ定義の場合は[default]の定義は必須で、それ以外
		 * ログ定義名に対しての個別定義が可能です.
		 */
		
		// 既にコンフィグ情報が読み込まれた場合.
		if(configFlag) {
			return false;
		}

		// 最初にdefault定義を取得.
		Map<String, Object> v;
		LogDefineElement em;
		Object n = json.get(DEFAULT_NAME);
		// (2)の条件の場合.
		if(n instanceof Map) {
			em = new LogDefineElement(defaultLogDefine, v = (Map)n);
			defaultLogDefine = em;
			n = v.get(DEFAULT_NAME);
		// (1)の条件の場合.
		} else {
			em = new LogDefineElement(defaultLogDefine, json);
			defaultLogDefine = em;
			n = json.get(DEFAULT_NAME);
		}
		boolean ret = false;
		// defaultログ定義名が存在する場合.
		if(n != null && n instanceof String) {
			// デフォルトログ名の変更.
			setDefaultLogName(((String)n).trim());
			ret = true;
		}
		// default条件をセット.
		manager.put(defaultLogName, defaultLogDefine);
		n = null; v = null;

		// 各種ログ定義条件を取得.
		String name;
		Entry<String, Object> e;
		Iterator<Entry<String, Object>> it = json.entrySet().iterator();
		while(it.hasNext()) {
			e = it.next();
			// value が Map条件のものを取得.
			if(e.getValue() instanceof Map) {
				// デフォルト条件のものは「無視」する.
				if(DEFAULT_NAME.equals(name = e.getKey())) {
					continue;
				}
				// Map条件を取得.
				v = (Map)e.getValue();
				// 定義条件を読み込む.
				em = new LogDefineElement(defaultLogDefine, v);
				// LogFactoryに登録.
				_register(name, em);
				ret = true;
			}
		}
		return ret;
	}
	
	/**
	 * ログコンフィグを設定してコンフィグをFixする.
	 * @param json json定義を設定します.
	 * @return true の場合、読み込みに成功しました.
	 */
	public synchronized boolean loadConfigByFix(Map<String, Object> json) {
		boolean ret = loadConfig(json);
		if(ret) {
			// 読み込みが成功した場合はFixする.
			fixConfig();
		}
		return ret;
	}

	
	/**
	 * ログ定義を登録.
	 *
	 * @param level
	 *            出力させない基本ログレベルを設定します.
	 * @param console
	 *            コンソール出力を許可する場合[true]を設定します.
	 * @param fileSize
	 *            ログ分割をする基本ファイルサイズを設定します.
	 * @param dir
	 *            ログ出力先のディレクトリを設定します.
	 * @return LogFactory
	 *            LogFactoryオブジェクトが返却されます.
	 */
	public synchronized LogFactory register(Object level, boolean console,
		Long fileSize, String dir) {
		return _register(defaultLogName,
			new LogDefineElement(
				defaultLogDefine, level, console, fileSize, dir));
	}

	/**
	 * ログ定義を登録.
	 *
	 * @param name
	 *            ログ定義名を設定します.
	 * @param level
	 *            出力させない基本ログレベルを設定します.
	 * @param console
	 *            コンソール出力を許可する場合[true]を設定します.
	 * @param fileSize
	 *            ログ分割をする基本ファイルサイズを設定します.
	 * @param dir
	 *            ログ出力先のディレクトリを設定します.
	 * @return LogFactory
	 *            LogFactoryオブジェクトが返却されます.
	 */
	public synchronized LogFactory register(String name,
		Object level, boolean console, Long fileSize, String dir) {
		return _register(name,
			new LogDefineElement(
				defaultLogDefine, level, console, fileSize, dir));
	}

	/**
	 * ログ定義を登録.
	 * @param name ログ定義名を設定します.
	 * @return LogFactory LogFactoryオブジェクトが返却されます.
	 */
	public synchronized LogFactory register(String name) {
		return _register(name, new LogDefineElement(defaultLogDefine));
	}

	/**
	 * ログ定義を登録.
	 * @param element ログ定義要素を設定します.
	 * @return LogFactory LogFactoryオブジェクトが返却されます.
	 */
	public synchronized LogFactory register(LogDefineElement element) {
		return _register(defaultLogName, new LogDefineElement(element));
	}

	/**
	 * ログ定義を登録.
	 * @param name ログ定義名を設定します.
	 * @param element ログ定義要素を設定します.
	 * @return LogFactory LogFactoryオブジェクトが返却されます.
	 */
	public synchronized LogFactory register(String name, LogDefineElement element) {
		return _register(name, new LogDefineElement(element));
	}

	/**
	 * ログ定義を登録.
	 * @param name ログ定義名を設定します.
	 * @param element ログ定義要素を設定します.
	 * @return LogFactory LogFactoryオブジェクトが返却されます.
	 */
	private synchronized LogFactory _register(String name, LogDefineElement element) {
		if(name == null || element == null) {
			throw new NullPointerException();
		} else if((name = name.trim()).isEmpty()) {
			throw new IllegalArgumentException();
		}
		// 既にコンフィグ情報が読み込まれた場合.
		if(configFlag) {
			return this;
		}
		// デフォルトのログ定義名でない、ログ定義の場合.
		if(!defaultLogName.equals(name)) {
			// 新しい定義要素として登録する.
			manager.put(name, element);
		// デフォルトのログ定義の場合.
		} else {
			// デフォルトのログ定義を置き換える.
			defaultLogDefine = element;
			manager.put(defaultLogName, defaultLogDefine);
		}
		return this;
	}

	/**
	 * デフォルトのログ定義情報を取得.
	 * @return LogDefineElement ログ定義情報が返却されます.
	 */
	public synchronized LogDefineElement getLogDefineElement() {
		return defaultLogDefine;
	}

	/**
	 * ログ定義名を設定して、ログ定義情報を取得.
	 * @param name ログ定義名を設定します.
	 * @return LogDefineElement ログ定義情報が返却されます.
	 */
	public synchronized LogDefineElement getLogDefineElement(String name) {
		if(name == null || (name = name.trim()).isEmpty()) {
			return null;
		}
		return manager.get(name);
	}

	/**
	 * ログ定義名で登録済みかチェック.
	 * @param name ログ定義名を設定します.
	 * @return boolean [true]の場合登録されています.
	 */
	public synchronized boolean isLogDefineElement(String name) {
		if(name == null || (name = name.trim()).isEmpty()) {
			return false;
		}
		return manager.containsKey(name);
	}

	/**
	 * 登録されたログ定義数を取得.
	 * @return int ログ定義数が返却されます.
	 */
	public synchronized int getLogDefineSize() {
		return manager.size();
	}

	/**
	 * 登録されたログ定義名群を取得.
	 * @return String[] ログ定義名群が返却されます.
	 */
	public synchronized String[] getLogDefineNames() {
		int cnt = 0;
		int len = manager.size();
		String[] ret = new String[len];
		Iterator<String> it = manager.keySet().iterator();
		while(it.hasNext()) {
			ret[cnt ++] = it.next();
		}
		return ret;
	}

	/**
	 * デフォルトのログ名を設定.
	 * @param name デフォルトのログ名を設定します.
	 * @return boolean [true]の場合、正しく設定できました.
	 */
	public synchronized boolean setDefaultLogName(String name) {
		// デフォルトのログ情報が確定済みの場合は名前の変更は不可.
		// 指定名がnullか空の場合も変更できない.
		if(defaultLogDefine.isFinalized() ||
			name == null || (name = name.trim()).isEmpty()) {
			return false;
		}
		// 置き換え前のデフォルトのログ名をbeforeで退避.
		final String before = defaultLogName;
		// 今回の定義名をセット.
		defaultLogName = name;
		// 前回のデフォルト定義を削除.
		manager.remove(before);
		// 新しい条件を再セット.
		manager.put(defaultLogName, defaultLogDefine);
		return true;
	}

	/**
	 * デフォルトのログ名を取得.
	 * @return String デフォルトのログ名が返却されます.
	 */
	public synchronized String getDefaultLogName() {
		return defaultLogName;
	}

	/**
	 * デフォルトのログ情報取得.
	 * @return Log ログオブジェクトが返却されます.
	 */
	public synchronized Log get() {
		return new BaseLog(defaultLogName, defaultLogDefine);
	}

	/**
	 * 個別に定義されたログ情報取得.
	 * @param name 個別に定義されたログ定義名を設定します.
	 * @return Log ログオブジェクトが返却されます.
	 */
	public Log get(String name) {
		return get(name, null);
	}
	
	/**
	 * デフォルトのログ情報取得.
	 * @return Log ログオブジェクトが返却されます.
	 */
	public static final Log log() {
		return LogFactory.getInstance().get();
	}
	
	/**
	 * 個別に定義されたログ情報取得.
	 * @param name 個別に定義されたログ定義名を設定します.
	 * @return Log ログオブジェクトが返却されます.
	 */
	public static final Log log(String name) {
		return LogFactory.getInstance().get(name);
	}
	

	/**
	 * 個別に定義されたログ情報取得.
	 * @param name 個別に定義されたログ定義名を設定します.
	 * @param element 対象のログ定義要素を設定します.
	 * @return Log ログオブジェクトが返却されます.
	 */
	public synchronized Log get(String name, LogDefineElement element) {
		if (name == null) {
			throw new NullPointerException();
		} else if ((name = name.trim()).isEmpty()) {
			throw new IllegalArgumentException();
		}
		// ログ定義名とログ定義が存在する場合.
		if(element != null) {
			return new BaseLog(name, element);
		}
		LogDefineElement em;
		// 定義されているデフォルト名と一致しない場合.
		if(!defaultLogName.equals(name)) {
			// 既に登録されている場合.
			if((em = manager.get(name)) != null) {
				return new BaseLog(name, em);
			}
			// デフォルトのログ定義を複製して、その名前のログ定義を作成.
			em = new LogDefineElement(defaultLogDefine);
			manager.put(name, em);
			return new BaseLog(name, em);
		}
		// デフォルトのログ定義の呼び出し.
		return new BaseLog(defaultLogName, defaultLogDefine);
	}

	/**
	 * ログ実装.
	 */
	private static final class BaseLog implements Log {
		private String name;
		private LogDefineElement element;

		/**
		 * コンストラクタ.
		 * @param name
		 * @param element
		 */
		protected BaseLog(String name, LogDefineElement element) {
			this.name = name;
			this.element = element;

			// ログ確定処理が行われてない場合.
			if(!element.isFinalized()) {
				// ログオブジェクトの利用を確定する.
				element.confirm();
			}
		}

		/**
		 * ログ出力.
		 * @param level
		 * @param args
		 */
		@Override
		public boolean log(LogLevel level, Object... args) {
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
			// ログレベルが不明な場合は、コンソール出力.
			console(args);
			return true;
		}

		/**
		 * トレース出力.
		 * @param args
		 */
		@Override
		public void trace(Object... args) {
			LogFactory.write(name, element, LogLevel.TRACE, args);
		}

		/**
		 * デバッグ出力.
		 * @param args
		 */
		@Override
		public void debug(Object... args) {
			LogFactory.write(name, element, LogLevel.DEBUG, args);
		}

		/**
		 * 情報出力.
		 * @param args
		 */
		@Override
		public void info(Object... args) {
			LogFactory.write(name, element, LogLevel.INFO, args);
		}

		/**
		 * 警告出力.
		 * @param args
		 */
		@Override
		public void warn(Object... args) {
			LogFactory.write(name, element, LogLevel.WARN, args);
		}

		/**
		 * エラー出力.
		 * @param args
		 */
		@Override
		public void error(Object... args) {
			LogFactory.write(name, element, LogLevel.ERROR, args);
		}

		/**
		 * 致命的エラー出力.
		 * @param args
		 */
		@Override
		public void fatal(Object... args) {
			LogFactory.write(name, element, LogLevel.FATAL, args);
		}

		/**
		 * コンソール出力.
		 * @param args
		 */
		@Override
		public void console(Object... args) {
			if(element.isConsoleOut()) {
				System.out.println(format(LogLevel.CONSOLE, args));
			}
		}

		/**
		 * トレース出力が許可されている場合.
		 * @return boolean
		 */
		@Override
		public boolean isTraceEnabled() {
			return element.getLogLevel()
					.checkMinMaxEquals(LogLevel.TRACE) <= 0;
		}

		/**
		 * デバッグ出力が許可されている場合.
		 * @return boolean
		 */
		@Override
		public boolean isDebugEnabled() {
			return element.getLogLevel()
					.checkMinMaxEquals(LogLevel.DEBUG) <= 0;
		}

		/**
		 * 情報出力が許可されている場合.
		 * @return boolean
		 */
		@Override
		public boolean isInfoEnabled() {
			return element.getLogLevel()
					.checkMinMaxEquals(LogLevel.INFO) <= 0;
		}

		/**
		 * 例外出力が許可されている場合.
		 * @return boolean
		 */
		@Override
		public boolean isWarnEnabled() {
			return element.getLogLevel()
					.checkMinMaxEquals(LogLevel.WARN) <= 0;
		}

		/**
		 * エラー出力が許可されている場合.
		 * @return boolean
		 */
		@Override
		public boolean isErrorEnabled() {
			return element.getLogLevel()
					.checkMinMaxEquals(LogLevel.ERROR) <= 0;
		}

		/**
		 * 致命的エラー出力が許可されている場合.
		 * @return boolean
		 */
		@Override
		public boolean isFatalEnabled() {
			return element.getLogLevel()
					.checkMinMaxEquals(LogLevel.FATAL) <= 0;
		}

		/**
		 * コンソール出力が許可されている場合.
		 * @return boolean
		 */
		@Override
		public boolean isConsoleEnabled() {
			if(element.isConsoleOut()) {
				return element.getLogLevel()
						.checkMinMaxEquals(LogLevel.CONSOLE) <= 0;
			}
			return false;
		}

		/**
		 * 定義されているログ定義名を取得.
		 * @return String
		 */
		@Override
		public String getName() {
			return name;
		}

		/**
		 * 定義されているログ定義を取得.
		 * @return
		 */
		public LogDefineElement getDefineElement() {
			return element;
		}

		/**
		 * 文字列を出力.
		 * @return String
		 */
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder("\"name\": \"")
				.append(name).append("\", ");
			return element.toString(buf).toString();
		}
	}

	// 数値変換可能かチェック.
	protected static final boolean isNumeric(Object o) {
		if(o instanceof Number) {
			return true;
		}
		return isNumeric("" + o);
	}

	/**
	 * 指定文字列が数値かチェック.
	 * @param s 文字列を設定します.
	 * @return trueの場合、文字列です.
	 */
	protected static final boolean isNumeric(String s) {
		if(s == null || s.isEmpty()) {
			return false;
		}
		char c;
		int off = 0;
		int dot = 0;
		final int len = s.length();
		if(s.charAt(0) == '-') {
			off = 1;
		}
		for (int i = off; i < len; i++) {
			if (!((c = s.charAt(i)) == '.' || (c >= '0' && c <= '9'))) {
				return false;
			} else if(c == '.') {
				dot ++;
				if(dot > 1) {
					return false;
				}
			}
		}
		return true;
	}

	// int 変換.
	protected static final int convertInt(Object o) {
		if(o instanceof Number) {
			return ((Number)o).intValue();
		}
		return Integer.parseInt(""+o);
	}

	// long 変換.
	protected static final long convertLong(Object o) {
		if(o instanceof Number) {
			return ((Number)o).longValue();
		}
		return Long.parseLong(""+o);
	}

	// 日付情報を取得.
	@SuppressWarnings("deprecation")
	protected static final String dateString(Date d) {
		String n;
		return new StringBuilder().append((d.getYear() + 1900)).append("-")
				.append("00".substring((n = "" + (d.getMonth() + 1)).length())).append(n).append("-")
				.append("00".substring((n = "" + d.getDate()).length())).append(n).toString();
	}

	// ログフォーマット情報を作成.
	@SuppressWarnings("deprecation")
	private static final String format(LogLevel type, Object[] args) {
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
	private static final String getStackTrace(final Throwable t) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}

	// ファイル追加書き込み.
	private static final void appendFile(final String name, final String out) {
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
	private static final void write(final String name, final LogDefineElement element,
		final LogLevel typeNo, final Object... args) {
		final LogLevel logLevel = element.getLogLevel();
		// 指定されたログレベル以下はログ出力させない場合.
		if (typeNo.checkMinMaxEquals(logLevel) < 0) {
			return;
		}
		final boolean consoleOut = element.isConsoleOut();
		final long fileSize = element.getLogSize();
		final String logDir = element.getDirectory();
		// ログ出力先がない場合は作成.
		final File dir = new File(logDir);
		if (!dir.isDirectory()) {
			dir.mkdirs();
		}

		final String format = format(typeNo, args);
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
			final String tname;

			// 条件によって新しいファイルに移行する場合はロック処理.
			synchronized(element.getSync()) {
				final String targetName = fileName + "." + dateString(date) + ".";
				// 指定フォルダ内から、targetNameの条件とマッチするものを検索.
				String[] list = dir.list(new FilenameFilter() {
					public boolean accept(final File file, final String str) {
						return str.indexOf(targetName) == 0;
					}
				});
				// そこの一番高いカウント値＋１の値を取得.
				String s;
				int len = (list == null) ? 0 : list.length;
				for (int i = 0; i < len; i++) {
					n = list[i];
					p = n.lastIndexOf(".");
					s = n.substring(p + 1);
					if(isNumeric(s)) {
						v = Integer.parseInt(s);
						if (cnt < v) {
							cnt = v;
						}
					}
				}
				// 今回のファイルをリネーム.
				tname = logDir + targetName + (cnt + 1);
				renameToStat = new File(tname);
				stat.renameTo(renameToStat);
			}
			// gzip変換.
			toGzip(tname, renameToStat);
			renameToStat = null;
		}

		// ログ出力.
		// 書き込みロック.
		final String outLogFile = logDir + fileName;
		synchronized(element.getSync()) {
			appendFile(outLogFile, format);
		}
		// コンソール出力が許可されている場合.
		if(consoleOut) {
			// ログ情報をコンソールアウト.
			System.out.print(format);
		}
	}

	// ファイルをGZIP変換.
	protected static final void toGzip(String name, File stat) {
		final String tname = name;
		final File tstat = stat;
		// gzip圧縮(スレッド実行).
		final Thread t = new Thread() {
			public void run() {
				InputStream in = null;
				try {
					int len;
					byte[] b = new byte[4096];
					in = new BufferedInputStream(
						new FileInputStream(tname));
					OutputStream out = null;
					try {
						// gzipで圧縮する.
						out = new GZIPOutputStream(new BufferedOutputStream(
							new FileOutputStream(tname + ".gz")));
						while ((len = in.read(b)) != -1) {
							out.write(b, 0, len);
						}
						out.flush();
						out.close();
						out = null;
						in.close();
						in = null;
						// ファイル削除.
						tstat.delete();
					} catch (Exception e) {
					} finally {
						if (out != null) {
							try {
								out.close();
							} catch (Exception e) {}
						}
					}
				} catch (Exception e) {
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (Exception e) {}
					}
				}
			}
		};
		t.setDaemon(false);
		t.start();
	}
}
