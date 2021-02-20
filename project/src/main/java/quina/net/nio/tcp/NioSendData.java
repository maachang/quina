package quina.net.nio.tcp;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Nio送信データ.
 */
public interface NioSendData extends Closeable {
	/**
	 * NioSendDataをコピー.
	 * この処理は「複数先に同じものを送信したい場合」に利用します.
	 * @return NioSendData コピーされたNioSendDataが返却されます.
	 */
	public NioSendData copy();

	/**
	 * データ取得.
	 * @param buf 対象のByteBufferを設定します.
	 * @return int 読み込まれたデータ数が返却されます.
	 *             -1 の場合EOFに達しました.
	 * @exception IOException I/O例外.
	 */
	public int read(ByteBuffer buf) throws IOException;

	/**
	 * NioSendData全体のデータ長を取得.
	 * @return long NioSendData全体のデータ長が返却されます.
	 */
	public long length();

	/**
	 * 残りデータ長を取得.
	 * @return long 残りのデータ長が返却されます.
	 */
	public long remaining();

	/**
	 * 残りデータが存在するかチェック.
	 * @return boolean true の場合残りデータがあります.
	 */
	public boolean hasRemaining();

	/**
	 * 情報が空かチェック.
	 * @return boolean true の場合空です.
	 */
	public boolean isEmpty();
}
