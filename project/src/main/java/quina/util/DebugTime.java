package quina.util;

/**
 * デバッグ用のタイム測定.
 */
public class DebugTime {
	private static final Flag debugMode = new Flag(false);
	private long first = -1L;
	private long time = -1L;

	/**
	 * デバッグモードを設定します.
	 * @param mode trueの場合、デバッグモードは有効です.
	 */
	public static final void debugMode(boolean mode) {
		debugMode.set(mode);
	}

	/**
	 * デバッグモードが有効かチェック.
	 * @return trueの場合、デバッグモードは有効です.
	 */
	public static final boolean isDebugMode() {
		return debugMode.get();
	}

	/**
	 * コンストラクタ.
	 */
	public DebugTime() {
	}

	/**
	 * タイム測定開始.
	 */
	public void start() {
		start(null, null);
	}

	/**
	 * タイム測定開始.
	 * @param head 出力ヘッド文字を設定します.
	 */
	public void start(String head) {
		start(head, null);
	}

	/**
	 * タイム測定開始.
	 * @param head 出力ヘッド文字を設定します.
	 * @param buf 出力先のStringBuilderを設定します.
	 */
	public void start(String head, StringBuilder buf) {
		if(isDebugMode()) {
			end(head, buf);
			long tm = System.currentTimeMillis();;
			if(first == -1L) {
				first = tm;
			}
			time = tm;
		}
	}

	/**
	 * タイム測定終了.
	 */
	public void end() {
		end(null, null);
	}

	/**
	 * タイム測定終了.
	 * @param head 出力ヘッド文字を設定します.
	 */
	public void end(String head) {
		end(head, null);
	}

	/**
	 * タイム測定終了.
	 * @param head 出力ヘッド文字を設定します.
	 * @param buf 出力先のStringBuilderを設定します.
	 */
	public void end(String head, StringBuilder buf) {
		if(isDebugMode()) {
			if(time > -1L) {
				if(head == null || head.isEmpty()) {
					head = "";
				}
				long n = System.currentTimeMillis() - time;
				time = -1L;
				if(buf != null) {
					buf.append(head).append(" time: ")
						.append(n).append("msec\n");
				} else {
					System.out.println(head + " time: " + n + "msec");
				}
			}
		}
	}

	/**
	 * 最初の開始からの時間終了.
	 */
	public void endAll() {
		endAll(null, null);
	}

	/**
	 * 最初の開始からの時間終了.
	 * @param head 出力ヘッド文字を設定します.
	 */
	public void endAll(String head) {
		endAll(head, null);
	}

	/**
	 * 最初の開始からの時間終了.
	 * @param head 出力ヘッド文字を設定します.
	 * @param buf 出力先のStringBuilderを設定します.
	 */
	public void endAll(String head, StringBuilder buf) {
		if(isDebugMode()) {
			if(time > -1L) {
				if(head == null || head.isEmpty()) {
					head = "";
				}
				long n = System.currentTimeMillis() - first;
				first = -1L;
				if(buf != null) {
					buf.append(head).append(" allTime: ")
						.append(n).append("msec\n");
				} else {
					System.out.println(head + " allTime: " + n + "msec");
				}
			}
		}
	}
}
