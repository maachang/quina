package quina.net.nio.tcp;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * 受信Bodyデータの出力処理.
 */
public interface NioRecvBody extends Closeable {
	/**
	 * binaryで書き込み.
	 * @param bin
	 * @return
	 * @throws IOException
	 */
	public int write(byte[] bin)
		throws IOException;

	/**
	 * binaryで書き込み.
	 * @param bin
	 * @param len
	 * @return
	 * @throws IOException
	 */
	public int write(byte[] bin, int len)
		throws IOException;

	/**
	 * binaryで書き込み.
	 * @param bin
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 */
	public int write(byte[] bin, int off, int len)
		throws IOException;

	/**
	 * ByteBufferで書き込み.
	 * @param buf
	 * @return
	 * @throws IOException
	 */
	public int write(ByteBuffer buf) throws IOException;

	/**
	 * 現在の受信データ長を取得.
	 * @return
	 */
	public long remaining();

	/**
	 * 受信データが存在するかチェック.
	 * @return boolan true の場合受信情報は存在します.
	 */
	public boolean hasRemaining();

	/**
	 * 書き込み処理を終了.
	 */
	public void exitWrite();

	/**
	 * 書き込み終了処理が行われたかチェック.
	 * @return boolean [true]の場合、書き込みは終了しています.
	 */
	public boolean isExitWrite();

	/**
	 * 受信される予定のBody取得データ長を取得.
	 * @return
	 */
	public long getLength();

	/**
	 * 読み込みInputStreamを作成.
	 * @return InputStream InputStreamが返却されます.
	 * @exception IOException I/O例外.
	 */
	public InputStream getInputStream() throws IOException;
}
