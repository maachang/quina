package quina.net.nio.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Nioコール.
 */
public interface NioCall {
	/**
	 * nio開始処理.
	 *
	 * @return boolean [true]の場合、正常に処理されました.
	 */
	public boolean startNio();

	/**
	 * nio終了処理.
	 */
	public void endNio();

	/**
	 * 新しい通信要素を生成.
	 *
	 * @return NioElement 新しい通信要素が返却されます.
	 */
	public NioElement createElement();

	/**
	 * ソケットの初期処理.
	 * @param ch
	 * @return
	 * @throws IOException
	 */
	public boolean initSocket(SocketChannel ch) throws IOException;

	/**
	 * エラーハンドリング.
	 *
	 * @param e エラー用の例外オブジェクトを設定されます.
	 */
	public void error(Throwable e);

	/**
	 * Send処理.
	 *
	 * @param em
	 *            対象のBaseNioElementオブジェクトが設定されます.
	 * @param buf
	 *            対象のByteBufferを設定します.
	 * @return boolean [true]の場合、正常に処理されました.
	 * @exception IOException
	 *                IO例外.
	 */
	public boolean send(NioElement em, ByteBuffer buf)
		throws IOException;
}
