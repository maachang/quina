package quina.net.nio.tcp.worker;

import java.io.IOException;

import quina.net.nio.tcp.NioCall;
import quina.net.nio.tcp.NioElement;
import quina.net.nio.tcp.NioException;

/**
 * データ受信用ワーカー要素.
 * このオブジェクトはデータ受信処理の采配をNioWorkerThreadに委託する
 * ための要素です.
 *
 * この処理はNioPooringManagerで管理されて、
 * ＜例＞
 * // pool NioPoolingManager.
 * // em = NioElement.
 * // recvBin = Nioで受信したbyte[].
 * // nioMan = NioWorkerThredManager.
 * // call = NioCallを継承した実装コールバック.
 *
 * // NioWorkerThreadHandle.endWorkerElementで、
 * // NioReceiveWorkerElementをNioPoolingManagerでプーリングされた
 * // 実装されたもので実装する必要がある.
 * NioWorkerThreadHandle handle = new NioWorkerThreadXXXHandle(pool);
 *
 * // NioWorkerThreadManager生成.
 * NioWorkerThreadManager nioMan = new NioWorkerThreadManager(5, handle);
 *
 * // プーリングされた要素を取得.
 * NioReceiveWorkerElement rem = (NioReceiveWorkerElement)pool.poll();
 * if(rem == null) {
 *   // プーリング情報が無い場合は受信ワーカーを生成.
 *   rem = new NioReceiveWorkerElement(call);
 * }
 * // ワーカスレッドで実行に対して処理を行う今回の定義をセット.
 * rem.setReceiveData(em, recvBin);
 *
 * // プーリングマネージャに登録.
 * nioMan.push(em, rem);
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
