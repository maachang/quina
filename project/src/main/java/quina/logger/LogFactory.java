package quina.logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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
	
	// ログ出力用ワーカー.
	private final LogWriteWorker logWriteWorker = new LogWriteWorker();

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
		logWriteWorker.startThread();
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
	public synchronized LogFactory register(
		String name, LogDefineElement element) {
		return _register(name, new LogDefineElement(element));
	}

	/**
	 * ログ定義を登録.
	 * @param name ログ定義名を設定します.
	 * @param element ログ定義要素を設定します.
	 * @return LogFactory LogFactoryオブジェクトが返却されます.
	 */
	private synchronized LogFactory _register(
		String name, LogDefineElement element) {
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
		return new BaseLog(defaultLogName, defaultLogDefine,
			logWriteWorker);
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
			return new BaseLog(name, element, logWriteWorker);
		}
		LogDefineElement em;
		// 定義されているデフォルト名と一致しない場合.
		if(!defaultLogName.equals(name)) {
			// 既に登録されている場合.
			if((em = manager.get(name)) != null) {
				return new BaseLog(name, em, logWriteWorker);
			}
			// デフォルトのログ定義を複製して、その名前のログ定義を作成.
			em = new LogDefineElement(defaultLogDefine);
			manager.put(name, em);
			return new BaseLog(name, em, logWriteWorker);
		}
		// デフォルトのログ定義の呼び出し.
		return new BaseLog(defaultLogName, defaultLogDefine,
			logWriteWorker);
	}
	
	/**
	 * ログ書き込みワーカーの停止.
	 * ※注意: この処理を呼び出すとログ書き込みが行われなく
	 *         なります.
	 */
	public void stopLogWriteWorker() {
		logWriteWorker.stopThread();
	}
	
	/**
	 * ログ書き込みワーカーが終了したかチェック.
	 * @return boolean true の場合、停止します.
	 */
	public boolean isExitLogWriteWorker() {
		return logWriteWorker.isExitThread();
	}

	/**
	 * ログ実装.
	 */
	private static final class BaseLog implements Log {
		private final String name;
		private final LogDefineElement element;
		private final LogWriteWorker logWriteWorker;

		/**
		 * コンストラクタ.
		 * @param name
		 * @param element
		 * @param logWriteWorker
		 */
		protected BaseLog(String name, LogDefineElement element,
			LogWriteWorker logWriteWorker) {
			this.name = name;
			this.element = element;
			this.logWriteWorker = logWriteWorker;

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
			logWriteWorker.push(name, element, LogLevel.TRACE, args);
		}

		/**
		 * デバッグ出力.
		 * @param args
		 */
		@Override
		public void debug(Object... args) {
			logWriteWorker.push(name, element, LogLevel.DEBUG, args);
		}

		/**
		 * 情報出力.
		 * @param args
		 */
		@Override
		public void info(Object... args) {
			logWriteWorker.push(name, element, LogLevel.INFO, args);
		}

		/**
		 * 警告出力.
		 * @param args
		 */
		@Override
		public void warn(Object... args) {
			logWriteWorker.push(name, element, LogLevel.WARN, args);
		}

		/**
		 * エラー出力.
		 * @param args
		 */
		@Override
		public void error(Object... args) {
			logWriteWorker.push(name, element, LogLevel.ERROR, args);
		}

		/**
		 * 致命的エラー出力.
		 * @param args
		 */
		@Override
		public void fatal(Object... args) {
			logWriteWorker.push(name, element, LogLevel.FATAL, args);
		}

		/**
		 * コンソール出力.
		 * @param args
		 */
		@Override
		public void console(Object... args) {
			if(element.isConsoleOut()) {
				System.out.println(LogWriteWorker.format(LogLevel.CONSOLE, args));
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
}
