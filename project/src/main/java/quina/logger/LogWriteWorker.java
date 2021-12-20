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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;

/**
 * ログ書き込みを行うワーカースレッド.
 */
final class LogWriteWorker extends Thread {
	
	// 1つのLogワーカー要素.
	private static final class LogWorkerElement {
		protected String name;
		protected LogDefineElement element;
		protected LogLevel typeNo;
		protected Object[] args;
		
		protected LogWorkerElement(String name, LogDefineElement element,
			LogLevel typeNo, Object[] args) {
			this.name = name;
			this.element = element;
			this.typeNo = typeNo;
			this.args = args;
		}
	}
	
	// boolean Atomic 管理.
	private static final class Bool {
		private final AtomicInteger ato = new AtomicInteger(0);
		public Bool(final boolean n) {
			ato.set(n ? 1 : 0);
		}
		public final boolean get() {
			return ato.get() == 1;
		}
		public final void set(final boolean n) {
			while (!ato.compareAndSet(ato.get(), n ? 1 : 0));
		}
	}
	
	// waitObject.
	private static class WaitObject {
		// ロックオブジェクト.
		private final Lock sync = new ReentrantLock();
		private final Condition con = sync.newCondition();

		/**
		 * コンストラクタ.
		 */
		public WaitObject() {
		}

		/**
		 * 指定時間待機.
		 *
		 * @param timeout
		 *            ミリ秒での待機時間を設定します. [0]を設定した場合、無限待機となります.
		 * @return boolean [true]が返された場合、復帰条件が設定されました.
		 */
		public final boolean await(long time) {
			sync.lock();
			try {
				return con.await(time, TimeUnit.MILLISECONDS);
			} catch(Exception e) {
				return false;
			} finally {
				sync.unlock();
			}
		}

		/**
		 * 待機中のスレッドを１つ起動.
		 */
		public final void signal() {
			sync.lock();
			try {
				con.signal();
			} catch(Exception e) {
			} finally {
				sync.unlock();
			}
		}
	}
	
	// LogUtf8変換バッファ管理.
	private static final class Utf8Buffer {
		// 初期バッファ長.
		private int initBufLength;
		// 再評価カウント.
		private int revaluationCount;
		
		// 対象バッファ.
		private byte[] buf;
		// 現在までに書き込まれたバイナリ数.
		private int allLength;
		// 現在までの書き込み回数.
		private int count;
		
		/**
		 * コンストラクタ.
		 * @param revaluationCount バッファサイズの再評価カウント値を設定します.
		 * @param initBufLength 初期バッファサイズを設定します.
		 */
		public Utf8Buffer(int revaluationCount, int initBufLength) {
			this.revaluationCount = revaluationCount;
			this.initBufLength = initBufLength;
			this.buf = new byte[initBufLength];
			this.allLength = 0;
			this.count = 0;
		}
		
		/**
		 * バッファ情報を取得.
		 * @return byte バッファ情報が返却されます.
		 */
		public byte[] buffer() {
			return buf;
		}
		
		/**
		 * バッファ情報に文字列をUTF8変換.
		 * @param str 文字列を設定します.
		 * @return int 変換されたバッファ長が返却されます.
		 *             -1 の場合変換に失敗しました.
		 */
		public int convert(String str) {
			final int strLen = str.length();
			int ret = -1;
			try {
				// 変換.
				ret = toUtf8(buf, 0, str, 0, strLen);
			} catch(Exception e) {
				try {
					// 変換に失敗した場合、バッファサイズを
					// 増やして再実行.
					byte[] b = new byte[strLen << 2];
					ret = toUtf8(b, 0, str, 0, strLen);
					buf = b;
				} catch(Exception ee) {
					// 失敗した場合は処理しない.
					return -1;
				}
			}
			// 書き込みサイズとカウントを追加.
			allLength += ret;
			count ++;
			// カウントが再評価カウントに達した場合.
			if(count > revaluationCount) {
				// 現状のバッファサイズが初期バッファ
				// サイズの場合.
				if(buf.length <= initBufLength) {
					// 再作成はしない.
					allLength = 0;
					count = 0;
				// 再評価が必要な場合.
				} else {
					// これまでの平均書き込み数を取得.
					int newLen = allLength / count;
					// 再評価サイズが初期バッファ
					// サイズ以下の場合.
					if(newLen <= initBufLength) {
						// 初期バッファサイズをセット.
						newLen = initBufLength;
					}
					// 再生成.
					buf = new byte[newLen];
					allLength = 0;
					count = 0;
				}
			}
			return ret;
		}
	}
	
	/**
	 * LogDefineの管理情報.
	 */
	private static final class LogDefineList {
		private static final int DEF_LEN = 8;
		private LogDefineElement[] list = null;
		private int[] idList = null;
		private int idListLength = 0;
		
		/**
		 * コンストラクタ.
		 */
		public LogDefineList() {
			list = new LogDefineElement[DEF_LEN];
			idList = new int[DEF_LEN];
		}
		
		/**
		 * 要素設定.
		 * @param em
		 */
		public void put(LogDefineElement em) {
			final int id = em.getId();
			if(list.length <= id) {
				LogDefineElement[] tmp = new LogDefineElement[id << 1];
				System.arraycopy(list, 0, tmp, 0, list.length);
				list = tmp;
			}
			if(list[id] == null) {
				if(idList.length <= idListLength + 1) {
					int[] tmp = new int[idList.length << 1];
					System.arraycopy(idList, 0, tmp, 0, idListLength);
					idList = tmp;
				}
				idList[idListLength ++] = id;
				list[id] = em;
			}
		}
		
		/**
		 * 登録LogDefineElement数を取得.
		 * @return
		 */
		public int size() {
			return idListLength;
		}
		
		/**
		 * 指定項番から登録されてるLogDefineElementを取得.
		 * @param no
		 * @return
		 */
		public LogDefineElement getLogDefineElement(int no) {
			return list[idList[no]];
		}
		
	}
	
	// waitタイムアウト値.
	private static final int TIMEOUT = 1000;

	// waitObject.
	private final WaitObject waitObject = new WaitObject();
	
	// Nioワーカー要素キュー.
	private final Queue<LogWorkerElement> queue =
		new ConcurrentLinkedQueue<LogWorkerElement>() ;
	
	// スレッド停止フラグ.
	private final Bool stopFlag = new Bool(true);

	// スレッド開始完了フラグ.
	private final Bool startThreadFlag = new Bool(false);

	// スレッド終了フラグ.
	private final Bool endThreadFlag = new Bool(false);
	
	/**
	 * ワーカースレッドに処理を登録.
	 *
	 * @param name 対象のログ名を設定します.
	 * @param element 対象のログ要素を設定します.
	 * @param typeNo ログレベルを設定します.
	 * @param args 出力ログ情報を設定します.
	 */
	public void push(final String name, final LogDefineElement element,
			final LogLevel typeNo, final Object... args) {
		queue.offer(new LogWorkerElement(name, element, typeNo, args));
		waitObject.signal();
	}

	/**
	 * ワーカーを開始する.
	 */
	public void startThread() {
		stopFlag.set(false);
		startThreadFlag.set(false);
		endThreadFlag.set(false);
		setDaemon(true);
		start();
	}

	/**
	 * ワーカーを停止する.
	 */
	public void stopThread() {
		stopFlag.set(true);
	}

	/**
	 * ワーカーが停止命令が既に出されているかチェック.
	 * @return
	 */
	public boolean isStopThread() {
		return stopFlag.get();
	}

	/**
	 * ワーカーが開始しているかチェック.
	 * @return
	 */
	public boolean isStartupThread() {
		return startThreadFlag.get();
	}

	/**
	 * ワーカーが終了しているかチェック.
	 * @return
	 */
	public boolean isExitThread() {
		return endThreadFlag.get();
	}

	/**
	 * スレッド実行.
	 */
	public void run() {
		final ThreadDeath td = execute();
		// スレッド終了完了.
		endThreadFlag.set(true);
		if (td != null) {
			throw td;
		}
	}

	/**
	 * ワーカースレッド実行処理.
	 */
	protected final ThreadDeath execute() {
		int i, len;
		final Utf8Buffer utf8Buf = new Utf8Buffer(
			LogConstants.getUt8BufferRevaluatio(),
			LogConstants.getUt8BufferLength());
		LogDefineElement logEm = null;
		LogWorkerElement em = null;
		ThreadDeath ret = null;
		boolean endFlag = false;
		final LogDefineList logDefineList = new LogDefineList();

		// スレッド開始完了.
		startThreadFlag.set(true);
		while (!endFlag && !stopFlag.get()) {
			try {
				while (!endFlag && !stopFlag.get()) {
					// 実行ワーカー要素を取得.
					if ((em = queue.poll()) == null) {
						// Flush処理.
						len = logDefineList.size();
						for(i = 0; i < len; i ++) {
							logEm = logDefineList.getLogDefineElement(i);
							if(logEm != null) {
								logEm.flushLog();
							}
						}
						waitObject.await(TIMEOUT);
						continue;
					}
					try {
						// ログ出力.
						write(utf8Buf, em.name, em.element, em.typeNo,
							em.args);
						// ファイルOpenしたLogDefineElementをセット.
						logDefineList.put(em.element);
					} catch(Exception e) {}
					em = null;
				}
			} catch (Throwable to) {
				// スレッド中止.
				if (to instanceof InterruptedException) {
					endFlag = true;
				// threadDeathが発生した場合.
				} else if (to instanceof ThreadDeath) {
					endFlag = true;
					ret = (ThreadDeath) to;
				}
			}
		}
		// ワーカースレッド処理後、残ったログを出力.
		while (true) {
			if ((em = queue.poll()) == null) {
				// Close処理.
				len = logDefineList.size();
				for(i = 0; i < len; i ++) {
					// LogDefineListに管理されてるLogDefineElementの
					// OutputStreamをクローズする.
					logEm = logDefineList.getLogDefineElement(i);
					if(logEm != null) {
						logEm.closeLog();
					}
				}
				break;
			}
			try {
				// ログ出力.
				write(utf8Buf, em.name, em.element, em.typeNo,
					em.args);
				// ファイルOpenしたLogDefineElementをセット.
				logDefineList.put(em.element);
			} catch(Exception e) {}
			em = null;
		}
		return ret;
	}
	
	// ログ出力処理.
	@SuppressWarnings("deprecation")
	private static final void write(Utf8Buffer utf8Buf,
		final String name, final LogDefineElement element,
		final LogLevel typeNo, final Object[] args) {
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
		if (stat.isFile() && (
			(fileSize > 0 && stat.length() + format.length() > fileSize) ||
			((date.getYear() & 31) | ((date.getMonth() & 31) << 9) | ((date.getDate() & 31) << 18))
			!=
			((now.getYear() & 31) | ((now.getMonth() & 31) << 9) | ((now.getDate() & 31) << 18))
		)) {
			// 現在のログファイルをリネームして、新しいログファイルに移行する.
			int p, v;
			String n;
			int cnt = -1;
			File renameToStat = null;
			final String tname;
			
			// 対象のelementをclose.
			element.closeLog();
			
			// リネーム名を生成.
			final String targetName = fileName + "." + LogUtil.dateString(date) + ".";
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
				if(LogUtil.isNumeric(s)) {
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
			// gzip変換.
			toGzip(tname, renameToStat);
			renameToStat = null;
		}
		
		// UTF8文字列変換.
		int utf8Len = utf8Buf.convert(format);
		
		// ログ出力.
		if(utf8Len > 0) {
			element.writeLog(fileName, utf8Buf.buffer(), utf8Len);
		}
		// コンソール出力が許可されている場合.
		if(consoleOut) {
			// ログ情報をコンソールアウト.
			System.out.print(format);
		}
		return;
	}
	
	// ログフォーマット情報を作成.
	@SuppressWarnings("deprecation")
	protected static final String format(LogLevel type, Object[] args) {
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

	// ファイルをGZIP変換.
	private static final void toGzip(String name, File stat) {
		final String tname = name;
		final File tstat = stat;
		// gzip圧縮(スレッド実行).
		final Thread t = new Thread() {
			public void run() {
				InputStream in = null;
				try {
					int len;
					byte[] b = new byte[4096];
					in = new BufferedInputStream(new FileInputStream(tname));
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
	
	/**
	 * UTF8文字列をバイナリ変換.
	 * @param out 受け取るバイナリ情報を設定します.
	 *            この値は len の４倍の長さを設定します.
	 * @param oOff 受け取るバイナリのオフセット値を設定します.
	 * @param value 対象の文字列を設定します.
	 * @param off 文字列のオフセット値を設定します.
	 * @param len 文字列の長さを設定します.
	 * @return int 変換したバイナリ長が返却されます.
	 * @exception Exception 例外.
	 */
	private static final int toUtf8(final byte[] out, final int oOff,
		final String value, final int off, final int len)
		throws Exception {
		if (value == null || len == 0) {
			return 0;
		}
		int c;
		int o = oOff;
		for (int i = 0; i < len; i++) {
			c = (int) value.charAt(off + i);

			// サロゲートペア処理.
			if (c >= 0xd800 && c <= 0xdbff) {
				c = 0x10000 + (((c - 0xd800) << 10) |
					((int) value.charAt(off + i + 1) - 0xdc00));
				i++;
			}

			if ((c & 0xffffff80) == 0) {
				out[o ++] = (byte)c;
			} else if (c < 0x800) {
				out[o ++] = (byte) ((c >> 6) | 0xc0);
				out[o ++] = (byte) ((c & 0x3f) | 0x80);
			} else if (c < 0x10000) {
				out[o ++] = (byte) ((c >> 12) | 0xe0);
				out[o ++] = (byte) (((c >> 6) & 0x3f) | 0x80);
				out[o ++] = (byte) ((c & 0x3f) | 0x80);
			} else {
				out[o ++] = (byte) ((c >> 18) | 0xf0);
				out[o ++] = (byte) (((c >> 12) & 0x3f) | 0x80);
				out[o ++] = (byte) (((c >> 6) & 0x3f) | 0x80);
				out[o ++] = (byte) ((c & 0x3f) | 0x80);
			}
		}
		return o - oOff;
	}
}
