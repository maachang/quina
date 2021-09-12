package quina.logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import quina.annotation.log.LogConfig;

/**
 * ログ定義要素.
 */
public class LogDefineElement {
	// ID管理.
	private static final AtomicInteger idMan = new AtomicInteger(0);
	
	// 出力可能なログレベル.
	private LogLevel logLevel;

	// ログ１ファイルの書き込みファイルサイズ.
	private Long logSize;

	// コンソール出力モード.
	private Boolean consoleOut;

	// 出力先ログディレクトリ.
	private String directory;
	
	// ログ定義ID.
	private int id;
	
	// ログ書き込み中オブジェクト.
	private OutputStream out;
	
	// ログFlashフラグ.
	private boolean flushOutFlag = false;

	// 設定確定フラグ.
	private final AtomicBoolean finalizedFlag = new AtomicBoolean(false);

	/**
	 * コンストラクタ.
	 */
	public LogDefineElement() {
		set();
		setId();
	}

	/**
	 * コンストラクタ.
	 * @param defaultInfo デフォルトの定義を設定します.
	 */
	public LogDefineElement(LogDefineElement defaultInfo) {
		set(defaultInfo);
		setId();
	}

	/**
	 * コンストラクタ.
	 * @param map 定義条件が格納されたMapを設定します.
	 */
	public LogDefineElement(Map<String, Object> map) {
		set(map).set();
		setId();
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
		setId();
	}
	
	/**
	 * コンストラクタ.
	 * @param config LogConfigアノテーションを設定します.
	 */
	public LogDefineElement(LogConfig config) {
		set(config);
		setId();
	}

	/**
	 * コンストラクタ.
	 * @param defaultInfo デフォルトの定義を設定します.
	 * @param map 定義条件が格納されたMapを設定します.
	 */
	public LogDefineElement(LogDefineElement defaultInfo,
		Map<String, Object> map) {
		set(defaultInfo).set(map);
		setId();
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
		Object level, Boolean cons, Object fileSize, String directory) {
		set(defaultInfo).set(level, cons, fileSize, directory);
		setId();
	}
	
	/**
	 * コンストラクタ.
	 * @param defaultInfo デフォルトの定義を設定します.
	 * @param config LogConfigアノテーションを設定します.
	 */
	public LogDefineElement(LogDefineElement defaultInfo,
		LogConfig config) {
		set(defaultInfo).set(config);
		setId();
	}
	
	// ログ定義確定済みの場合はエラー出力.
	protected final void checkFinalized() {
		if(finalizedFlag.get()) {
			throw new LogException("The log definition has been finalized.");
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
	 * @return LogDefineElement このオブジェクトが返却されます.
	 */
	private LogDefineElement set() {
		defaultDefine();
		return this;
	}

	/**
	 * LogDefineElementを設定.
	 * @param defaultInfo
	 * @return LogDefineElement このオブジェクトが返却されます.
	 */
	private LogDefineElement set(LogDefineElement defaultInfo) {
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
	 * @return LogDefineElement このオブジェクトが返却されます.
	 */
	private LogDefineElement set(Map<String, Object> map) {
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
			} else if (LogUtil.isNumeric(n)) {
				try {
					size = LogUtil.convertLong(n);
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
	 * @return LogDefineElement このオブジェクトが返却されます.
	 */
	private LogDefineElement set(
		Object level, Boolean cons, Object fileSize, String directory) {
		checkFinalized();
		String dir = null;
		LogLevel lv = null;
		Boolean console = null;
		if(level != null) {
			lv = LogLevel.convertLogLevel(level);
		}
		if(cons != null) {
			console = cons;
		}
		if (directory != null && !directory.isEmpty()) {
			dir = directory;
		}
		setLogLevel(lv);
		setLogSize(fileSize);
		setConsoleOut(console);
		setDirectory(dir);
		defaultDefine();
		return this;
	}
	
	/**
	 * LogConfigアノテーションで設定.
	 * @param config LogConfigアノテーションを設定します.
	 * @return LogDefineElement このオブジェクトが返却されます.
	 */
	private LogDefineElement set(LogConfig config) {
		checkFinalized();
		setConsoleOut(config.console());
		if(!config.size().isEmpty()) {
			setLogSize(parseCapacityByLong(config.size()));
		}
		if(!config.directory().isEmpty()) {
			setDirectory(config.directory());
		}
		setLogLevel(config.level());
		defaultDefine();
		return this;
	}
	
	/**
	 * このLogDefineElementのIDを設定します.
	 * @return LogDefineElement このオブジェクトが返却されます.
	 */
	private LogDefineElement setId() {
		id = idMan.getAndIncrement();
		return this;
	}
	
	/**
	 * このLogDefineElementのIDを取得します.
	 * @return int IDが返却されます.
	 */
	public int getId() {
		return id;
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
			this.directory = envPath(directory);
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
	 * ログの１ファイルの書き込みファイルサイズを取得.
	 * @return
	 */
	public Long getLogSize() {
		return logSize;
	}

	/**
	 * ログファイルの書き込みファイルサイズを設定.
	 * @param logSize
	 * @return
	 */
	public LogDefineElement setLogSize(Object logSize) {
		checkFinalized();
		Long size = null;
		if(logSize instanceof Number) {
			size = ((Number)logSize).longValue();
		} else {
			size = parseCapacityByLong(logSize.toString());
		}
		if(size != null && size > 0L) {
			this.logSize = size;
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
	 * ログ書き込み.
	 * @param fileName ログファイルを設定します.
	 * @param message ログ出力内容を設定します.
	 * @return
	 */
	protected LogDefineElement writeLog(String fileName, String message) {
		try {
			if(out == null) {
				out = new BufferedOutputStream(
					new FileOutputStream(directory + fileName));
			}
			out.write(message.getBytes("UTF8"));
			flushOutFlag = true;
		} catch(Exception e) {
		}
		return this;
	}
	
	/**
	 * キャッシュ書き込みされてるログ内容を書き込む.
	 * @return
	 */
	protected LogDefineElement flushLog() {
		boolean f = flushOutFlag;
		flushOutFlag = false;
		if(out != null && f) {
			try {
				out.flush();
			} catch(Exception e) {
			}
		}
		return this;
	}
	
	/**
	 * ログをクローズ.
	 * @return
	 */
	protected LogDefineElement closeLog() {
		flushOutFlag = false;
		try {
			if(out != null) {
				out.close();
				out = null;
			}
		} catch(Exception e) {
		}
		return this;
	}

	/**
	 * 内容を文字列で出力.
	 * @param buf
	 * @return
	 */
	public StringBuilder toString(StringBuilder buf) {
		buf.append("\"logLevel\": \"").append(logLevel)
			.append("\", \"logSize\": ").append(logSize)
			.append("\", \"consoleOut\": ").append(consoleOut)
			.append(", \"directory\": \"").append(directory).append("\"");
		return buf;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		return toString(buf).toString();
	}
	
	/**
	 * 容量を指定する文字列からlong値に変換.
	 * 以下のように キロ,メガ,ギガ,テラ のような単位を
	 * long値に変換します.
	 * 
	 * "1024" = 1,024.
	 * "1k" = 1,024.
	 * "1m" = 1,048,576.
	 * "1g" = 1,073,741,824.
	 * "1t" = 1,099,511,627,776.
	 * "1p" = 1,125,899,906,842,624.
	 * 
	 * @param num 対象のサイズの文字列を設定します.
	 * @return long 変換されたLong値が返却されます.
	 */
	private static final long parseCapacityByLong(String num) {
		int lastPos = num.length() - 1;
		String c = num.substring(lastPos).toLowerCase();
		if(c.equals("k")) {
			return Long.parseLong(num.substring(0, lastPos)) * 1024L;
		} else if(c.equals("m")) {
			return Long.parseLong(num.substring(0, lastPos)) * 1048576L;
		} else if(c.equals("g")) {
			return Long.parseLong(num.substring(0, lastPos)) * 1073741824L;
		} else if(c.equals("t")) {
			return Long.parseLong(num.substring(0, lastPos)) * 1099511627776L;
		} else if(c.equals("p")) {
			return Long.parseLong(num.substring(0, lastPos)) * 1125899906842624L;
		}
		return Long.parseLong(num);
	}
	
	/**
	 * 環境変数のパスを含んだ条件を取得.
	 * 環境変数は２つの条件で設定が出来ます.
	 * /xxx/${HOME}/yyy/zzz.txt
	 * /xxx/%HOME%/yyy/zzz.txt
	 *
	 * @param path 対象の環境変数定義を含んだパスを設定します.
	 * @return String 定義された環境変数が適用されたパスが返却されます.
	 */
	public static final String envPath(String path) {
		final List<int[]> posList = new ArrayList<int[]>();
		char c;
		int len = path.length();
		int start = 0;
		int type = -1;
		for(int i = 0; i < len; i ++) {
			c = path.charAt(i);
			if(type != -1) {
				// $.../ or $...[END]
				if(type == 0 && (c == '/' || i + 1 == len)) {
					// $.../
					if(c == '/') {
						posList.add(new int[] {type, start, i});
					// $...[END]
					} else {
						posList.add(new int[] {type, start, i + 1});
					}
					type = -1;
				// ${...}
				} else if(type == 1 && c == '}') {
					posList.add(new int[] {type, start, i});
					type = -1;
				// %...%
				} else if(type == 2 && c == '%') {
					posList.add(new int[] {type, start, i});
					type = -1;
				}
			} else if(c == '$') {
				if(i + 1 < len && path.charAt(i + 1) == '{') {
					type = 1; // ${...}
					start = i;
					i ++;
				} else {
					type = 0; // $...
					start = i;
				}
			} else if(c == '%') {
				type = 2; // %...%
				start = i;
			}
		}
		if(posList.size() == 0) {
			return path;
		}
		int[] plst;
		String envSrc, envDest;
		int first = 0;
		int s, e;
		len = posList.size();
		final StringBuilder buf = new StringBuilder();
		for(int i = 0; i < len; i ++) {
			plst = posList.get(i);
			type = plst[0]; // type.
			s = plst[1]; // 開始位置.
			e = plst[2]; // 終了位置.
			plst = null;
			// $...
			if(type == 0) {
				start = s;
			// ${...}
			} else if(type == 1) {
				start = s + 1;
			// %...%
			} else if(type == 2) {
				start = s;
			}
			// 環境変数名.
			envSrc = path.substring(start + 1, e);
			// 環境変数名を変換.
			envDest = System.getenv(envSrc);
			buf.append(path.substring(first, s));
			if(envDest != null) {
				// 取得した環境変数をセット.
				buf.append(envDest);
			} else {
				// 取得できない場合はエラー出力.
				throw new LogException("Information for environment variable \"" +
					envSrc + "\" does not exist.");
			}
			envSrc = null; envDest = null;
			// $... の場合は${...} や %...% と違い終端が無いことを示す定義.
			first = (type == 0) ? e : e + 1;
		}
		buf.append(path.substring(first));
		return buf.toString();
	}
}
