package quina.http.worker;

import quina.QuinaInfo;
import quina.net.nio.tcp.worker.NioWorkerConstants;
import quina.net.nio.tcp.worker.NioWorkerElement;
import quina.net.nio.tcp.worker.NioWorkerThreadHandler;

/**
 * Httpワーカー定義.
 */
public class HttpWorkerInfo implements QuinaInfo {
	// ワーカースレッド管理サイズ.
	private int workerThreadLength;
	// ワーカースレッドハンドラー.
	private NioWorkerThreadHandler workerThreadHandler;

	// 空のワーカースレッドハンドラ.
	private static final class BlankWorkerThreadHandler implements NioWorkerThreadHandler {
		@Override
		public void initWorkerThreadManager(int len) {
		}

		@Override
		public void startThreadCall(int no) {
		}

		@Override
		public void endThreadCall(int no) {
		}

		@Override
		public void errorCall(int no, Throwable t) {
		}

		@Override
		public void endWorkerElement(NioWorkerElement em) {
		}
	}

	/**
	 * コンストラクタ.
	 */
	public HttpWorkerInfo() {
		reset();
	}

	@Override
	public void reset() {
		// デフォルト値を初期化.
		workerThreadLength = NioWorkerConstants.getWorkerThreadLength();
		workerThreadHandler = new BlankWorkerThreadHandler();
	}

	/**
	 * ワーカースレッド数を取得.
	 * @return
	 */
	public int getWorkerThreadLength() {
		return workerThreadLength;
	}

	/**
	 * ワーカースレッド数を設定.
	 * @param workerThreadLength
	 */
	public void setWorkerThreadLength(int workerThreadLength) {
		this.workerThreadLength = workerThreadLength;
	}

	/**
	 * ワーカースレッドハンドラを取得.
	 * @return
	 */
	public NioWorkerThreadHandler getWorkerThreadHandler() {
		return workerThreadHandler;
	}

	/**
	 * ワーカースレッドハンドラを設定.
	 * @param workerThreadHandler
	 */
	public void setWorkerThreadHandler(NioWorkerThreadHandler workerThreadHandler) {
		this.workerThreadHandler = workerThreadHandler;
	}

	@Override
	public String toString() {
		return outString();
	}
}
