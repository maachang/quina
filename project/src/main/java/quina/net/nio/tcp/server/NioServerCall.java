package quina.net.nio.tcp.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import quina.net.nio.tcp.NioCall;
import quina.net.nio.tcp.NioElement;
import quina.net.nio.tcp.NioUtil;
import quina.net.nio.tcp.NioWorkerCall;

/**
 * NioServerコールバック.
 */
public abstract class NioServerCall implements NioCall {

	/**
	 * NioServerCoreが生成された時に呼び出されます.
	 */
	public void init() {
	}

	/**
	 * NioServerCoreのstartThread処理が呼ばれた時に呼び出されます.
	 */
	public void startThread() {
	}

	/**
	 * NioServerCoreのstopThread処理が呼ばれた時に呼び出されます.
	 */
	public void stopThread() {
	}

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
	public boolean initSocket(SocketChannel ch)
		throws IOException {
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
		return this.setSendData(em, buf);
	}

	/**
	 * SendData内容を送信する.
	 */
	protected boolean setSendData(NioElement em, ByteBuffer buf)
		throws IOException {
		return NioUtil.sendDataToWriteByteBuffer(em, buf);
	}
	
	/**
	 * NioWorkerCallを取得.
	 * @return NioWorkerCall NioWorkerCallが返却されます.
	 */
	protected abstract NioWorkerCall createNioWorkerCall();
}
