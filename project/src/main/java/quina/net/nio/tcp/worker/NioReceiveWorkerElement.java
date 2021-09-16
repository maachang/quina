package quina.net.nio.tcp.worker;

import java.io.IOException;

import quina.net.nio.tcp.NioCall;
import quina.net.nio.tcp.NioElement;
import quina.net.nio.tcp.NioException;

/**
 * データ受信用ワーカー要素.
 * このオブジェクトはデータ受信処理の采配をNioWorkerThreadに委託する
 * ための要素です.
 */
public class NioReceiveWorkerElement implements WorkerElement {
	private NioCall call;
	private NioElement element;
	private byte[] recvBinary;

	protected NioReceiveWorkerElement() {}

	/**
	 * コンストラクタ.
	 * @param call NioCallを設定します.
	 */
	public NioReceiveWorkerElement(NioCall call) {
		this.call = call;
	}

	/**
	 * NioCallを取得.
	 * @return NioCall NioCallが返却されます.
	 */
	public NioCall getCall() {
		return call;
	}

	/**
	 * 受信情報をワーカースレッドに橋渡し.
	 * @param em NioElement要素を設定します.
	 * @param b 今回Nioで受信したバイナリを設定します.
	 */
	public void setReceiveData(NioElement em, byte[] b) {
		this.element = em;
		this.recvBinary = b;
	}

	@Override
	public void close() throws IOException {
		this.element = null;
		this.recvBinary = null;
	}

	@Override
	public void destroy() {
		if(this.element != null) {
			try {
				this.element.close();
			} catch(Exception e) {}
		}
		this.call = null;
		this.recvBinary = null;
	}

	@Override
	public boolean isDestroy() {
		return call == null;
	}

	@Override
	public boolean call(Object o) {
		if(call == null) {
			return false;
		}
		try {
			return call.receive(o, this.element, this.recvBinary);
		} catch(NioException ne) {
			throw ne;
		} catch(Exception e) {
			throw new NioException(e);
		}
	}
}
