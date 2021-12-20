package quina.net.nio.tcp;

import quina.util.AtomicNumber;
import quina.util.AtomicNumber64;
import quina.util.AtomicObject;
import quina.util.Flag;

/**
 * Nio定義.
 */
public final class NioConstants {
	protected NioConstants() {
	}

	/**
	 * NioのSelectorタイムアウト.
	 */
	public static final int SELECTOR_TIMEOUT = 1000;

	/**
	 * IPV4だけで処理するか : true.
	 */
	public static final boolean NET_IPV4_FLAG = true;

	/**
	 * DNS キャッシュ時間 : 300秒.
	 */
	public static final int NET_DNS_CACHE_SECOND = 300;

	/**
	 * DNS解決失敗ドメインの保有時間.
	 */
	public static final int ERROR_DNS_CACHE_TIME = 0;

	/**
	 * 最低限の長さのランダムファイル名長.
	 */
	public static final int MIN_TMP_NAME_COUNT = 8;

	/**
	 * 削除リトライ.
	 */
	public static final int DELETE_RETRY = 5;

	/**
	 * NioBufferサイズ.
	 */
	private static final int DEF_BUFFER_SIZE = 1024;

	/**
	 * ByteBufferサイズ.
	 */
	private static final int DEF_BYTE_BUFFER_LENGTH = 512;

	/**
	 * TcpI/OでのBody一時ファイル格納先ディレクトリ名.
	 */
	private static final String TCP_BODY_FILE_DIR = ".nio_body";

	/**
	 * ランダムファイル名長.
	 */
	private static final int TEMP_FILENAME_LENGTH = 20;

	// NioDebugMode.
	private static final Flag nioDebugMode = new Flag(false);

	// NioBufferサイズ.
	private static final AtomicNumber nioBufferSize = new AtomicNumber(
		DEF_BUFFER_SIZE);

	// ByteBufferサイズ.
	private static final AtomicNumber byteBufferLength = new AtomicNumber(
		DEF_BYTE_BUFFER_LENGTH);

	// TCPボディーファイル格納先フォルダ.
	private static final AtomicObject<String> tcpBodyFileDir = new AtomicObject<String>(
		NioConstants.TCP_BODY_FILE_DIR);

	// テンポラリファイル文字数.
	private static final AtomicNumber tempFileNameLength = new AtomicNumber(
		NioConstants.TEMP_FILENAME_LENGTH);
	
	/**
	 * NioDebugモードを取得.
	 * @return boolean trueの場合、デバッグモードです.
	 */
	public static final boolean isDebug() {
		return nioDebugMode.get();
	}

	/**
	 * NioDebugモードを設定.
	 * @param mode trueを設定した場合、デバッグモードです.
	 */
	public static final void setDebug(boolean mode) {
		NioConstants.nioDebugMode.set(mode);
	}

	/**
	 * NioBufferサイズ.
	 * @return nioBufferSize
	 */
	public static final int getBufferSize() {
		return nioBufferSize.get();
	}

	/**
	 * NioBufferサイズ.
	 * @param nioBufferSize セットする nioBufferSize
	 */
	public static final void setBufferSize(int nioBufferSize) {
		if(nioBufferSize < 512) {
			nioBufferSize = 512;
		}
		NioConstants.nioBufferSize.set(nioBufferSize);
	}

	/**
	 * ByteBufferサイズ.
	 * @return byteBufferLength
	 */
	public static final int getByteBufferLength() {
		return byteBufferLength.get();
	}

	/**
	 * ByteBufferサイズ.
	 * @param byteBufferLength セットする byteBufferLength
	 */
	public static final void setByteBufferLength(int byteBufferLength) {
		if(byteBufferLength < 512) {
			byteBufferLength = 512;
		}
		NioConstants.byteBufferLength.set(byteBufferLength);
	}

	/**
	 * TCPボディーファイル格納先フォルダ.
	 * @return tcpBodyFileDir
	 */
	public static final String getTcpBodyFileDir() {
		return tcpBodyFileDir.get();
	}

	/**
	 * TCPボディーファイル格納先フォルダ.
	 * @param tcpBodyFileDir セットする tcpBodyFileDir
	 */
	public static final void setTcpBodyFileDir(String tcpBodyFileDir) {
		NioConstants.tcpBodyFileDir.set(tcpBodyFileDir);
	}

	/**
	 * テンポラリファイル文字数.
	 * @return tempFileNameLength
	 */
	public static final int getTempFileNameLength() {
		return tempFileNameLength.get();
	}

	/**
	 * テンポラリファイル文字数.
	 * @param tempFileNameLength セットする tempFileNameLength
	 */
	public static final void setTempFileNameLength(int tempFileNameLength) {
		NioConstants.tempFileNameLength.set(tempFileNameLength);
	}
	
	// 無通信が続くタイムアウト値.
	private static final long DEF_TIMEOUT = 30000L;

	
	// 無通信が続くタイムアウト値.
	private static final AtomicNumber64 timeout = new AtomicNumber64(DEF_TIMEOUT);
	
	/**
	 * 無通信が続くタイムアウト値を設定.
	 * @param time 無通信が続くタイムアウト値を設定します.
	 */
	public static final void setTimeout(long time) {
		timeout.set(time);
	}
	
	/**
	 * 無通信が続くタイムアウト値を取得.
	 * @return long 無通信が続くタイムアウト値を返却します.
	 */
	public static final long getTimeout() {
		return timeout.get();
	}
	
	// デフォルトのタイムアウト監視に移行するタイム（ミリ秒）.
	private static final long DEF_DOUBT_TIME = 2500L;
	
	// タイムアウト監視に移行するタイム（ミリ秒）.
	private static final AtomicNumber64 doubtTime = new AtomicNumber64(DEF_DOUBT_TIME);
	
	/**
	 * タイムアウト監視に移行するタイム（ミリ秒）を設定.
	 * @param time タイムアウト監視に移行するタイム（ミリ秒）を設定します.
	 */
	public static final void setDoubtTime(long time) {
		doubtTime.set(time);
	}
	
	/**
	 * タイムアウト監視に移行するタイム（ミリ秒）を取得.
	 * @return long タイムアウト監視に移行するタイム（ミリ秒）が返却されます.
	 */
	public static final long getDoubtTime() {
		return doubtTime.get();
	}
}