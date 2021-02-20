package quina.net.nio.tcp.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import quina.net.nio.tcp.NioCall;
import quina.net.nio.tcp.NioElement;
import quina.net.nio.tcp.NioUtil;

/**
 * NioServerコールバック.
 */
public abstract class NioServerCall implements NioCall {
	/**
	 * Accept処理.
	 *
	 * @param em
	 *            対象のBaseNioElementオブジェクトが設定されます.
	 * @return boolean [true]の場合、正常に処理されました.
	 * @exception IOException
	 *                IO例外.
	 */
	public boolean accept(NioElement em)
		throws IOException {
		return true;
	}

	/**
	 * nio開始処理.
	 *
	 * @return boolean [true]の場合、正常に処理されました.
	 */
	@Override
	public boolean startNio() {
		return true;
	}

	/**
	 * nio終了処理.
	 */
	@Override
	public void endNio() {
	}

	/**
	 * 新しい通信要素を生成.
	 *
	 * @return NioElement 新しい通信要素が返却されます.
	 */
	@Override
	public abstract NioElement createElement();

	/**
	 * ソケットの初期処理.
	 * @param ch
	 * @return
	 * @throws IOException
	 */
	@Override
	public boolean initSocket(SocketChannel ch) throws IOException {
		return true;
	}

	/**
	 * エラーハンドリング.
	 *
	 * @param e エラー用の例外オブジェクトを設定されます.
	 */
	@Override
	public void error(Throwable e) {
	}

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
	@Override
	public boolean send(NioElement em, ByteBuffer buf)
		throws IOException {
		return setSendData(em, buf);
	}

	/**
	 * SendData内容を送信する.
	 */
	protected boolean setSendData(NioElement em, ByteBuffer buf)
		throws IOException {
		return NioUtil.sendDataToWriteByteBuffer(em, buf);
	}

	/**
	 * Receive処理.
	 *
	 * @param o
	 *            ワーカースレッドNoに紐づくオブジェクトが設定されます.
	 * @param em
	 *            対象のNioElementオブジェクトが設定されます.
	 * @param recvBin
	 *            今回受信されたデータが設定されます.
	 * @return boolean [true]の場合、正常に処理されました.
	 * @exception IOException
	 *                IO例外.
	 */
	@Override
	public abstract boolean receive(Object o, NioElement em, byte[] rcvBin)
		throws IOException;
}
