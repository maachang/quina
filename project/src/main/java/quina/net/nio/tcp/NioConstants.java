package quina.net.nio.tcp;

import quina.net.nio.tcp.NioAtomicValues.Bool;
import quina.net.nio.tcp.NioAtomicValues.Number32;
import quina.net.nio.tcp.NioAtomicValues.Value;

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
	private static final Bool nioDebugMode = new Bool(false);

	// NioBufferサイズ.
	private static final Number32 nioBufferSize = new Number32(
		DEF_BUFFER_SIZE);

	// ByteBufferサイズ.
	private static final Number32 byteBufferLength = new Number32(
		DEF_BYTE_BUFFER_LENGTH);

	// TCPボディーファイル格納先フォルダ.
	private static final Value<String> tcpBodyFileDir = new Value<String>(
		NioConstants.TCP_BODY_FILE_DIR);

	// テンポラリファイル文字数.
	private static final Number32 tempFileNameLength = new Number32(
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

}