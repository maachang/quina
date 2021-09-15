package quina.net.nio.tcp.worker;

import java.io.Closeable;

/**
 * Nioワーカ要素.
 * NioWorkerThreadで実行されるワーカー要素です.
 */
public interface WorkerElement extends Closeable {
	/**
	 * ワーカー要素を破棄.
	 */
	public void destroy();

	/**
	 * ワーカー要素が既に破棄されているかチェック.
	 * @return boolean [true]の場合は既に破棄されています.
	 */
	public boolean isDestroy();

	/**
	 * ワーカー要素を実行.
	 * @param o ワーカースレッド特有のオブジェクトがセットされます.
	 * @return boolean [true]の場合、ワーカー要素が正しく実行されました.
	 */
	public boolean call(Object o);
}
