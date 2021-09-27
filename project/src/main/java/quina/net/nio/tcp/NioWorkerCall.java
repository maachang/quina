package quina.net.nio.tcp;

import quina.worker.QuinaContext;
import quina.worker.QuinaWorkerCall;

/**
 * Nioから受信したデーターを処理するWorkerCall.
 */
public abstract class NioWorkerCall
	extends QuinaWorkerCall {
	
	// Nio要素.
	protected NioElement element;
	
	// 今回受信したバイナリデーター.
	protected byte[] receiveData;
	
	/**
	 * QuinaContextを設定.
	 * @param context 対象のQuinaContextを設定します.
	 */
	@Override
	public void setContext(QuinaContext context) {
		element.setContext(context);
	}
	
	/**
	 * QuinaContextを取得.
	 * @return QuinaContext 設定されてるQuinaContextが
	 *                      返却されます.
	 */
	@Override
	public QuinaContext getContext() {
		return element.getContext();
	}
	
	/**
	 * 設定されたワーカーNoを取得.
	 * @return int ワーカーNoを取得.
	 *             -1の場合割り当てられていません.
	 */
	@Override
	public int getWorkerNo() {
		// 対象NioElementのワーカー番号を取得.
		return element.getWorkerNo();
	}
	
	/**
	 * ワーカーNoを設定.
	 * @param no ワーカーNoを設定します.
	 */
	@Override
	public void setWorkerNo(int no) {
		// 対象NioElementのワーカー番号に設定.
		element.setWorkerNo(no);
	}
	
	/**
	 * 受信データを設定.
	 * @param element NioElementを設定します.
	 * @param receiveData 受信データを設定します.
	 */
	public void setReceiveData(
		NioElement element, byte[] receiveData) {
		this.element = element;
		this.receiveData = receiveData;
	}
	
	/**
	 * Nio要素を取得.
	 * @return NioElement Nio要素が返却されます.
	 */
	public NioElement getNioElement() {
		return element;
	}
	
	/**
	 * 受信データを取得.
	 * この処理により元の受信データは削除されます.
	 * @return byte[] 受信データが返却されます.
	 */
	public byte[] getReceiveData() {
		final byte[] ret = receiveData;
		receiveData = null;
		return ret;
	}
	
	/**
	 * 要素を破棄.
	 */
	@Override
	public void destroy(int no) {
		try {
			element.close();
		} catch(Exception e) {}
		element = null;
		receiveData = null;
	}

	/**
	 * 要素が既に破棄されているかチェック.
	 * @return boolean [true]の場合は既に破棄されています.
	 */
	@Override
	public boolean isDestroy(int no) {
		return element == null ||
			!element.isConnection();
	}
}
