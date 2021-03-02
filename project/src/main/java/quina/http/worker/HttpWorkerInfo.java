package quina.http.worker;

import quina.QuinaConfig;
import quina.QuinaInfo;
import quina.net.nio.tcp.worker.NioWorkerConstants;
import quina.net.nio.tcp.worker.NioWorkerElement;
import quina.net.nio.tcp.worker.NioWorkerThreadHandler;
import quina.util.collection.TypesClass;

/**
 * Httpワーカー定義.
 */
public class HttpWorkerInfo implements QuinaInfo {
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

	// ワーカースレッドハンドラー.
	private NioWorkerThreadHandler workerThreadHandler;

	// コンフィグ情報.
	private QuinaConfig config = new QuinaConfig(
		// ワーカースレッド管理サイズ.
		"workerThreadLength", TypesClass.Integer, NioWorkerConstants.getWorkerThreadLength()
	);

	/**
	 * コンストラクタ.
	 */
	public HttpWorkerInfo() {
		reset();
	}

	@Override
	public void reset() {
		config.clear();
		workerThreadHandler = new BlankWorkerThreadHandler();
	}

	@Override
	public QuinaConfig getQuinaConfig() {
		return config;
	}

	/**
	 * ワーカースレッド数を取得.
	 * @return
	 */
	public int getWorkerThreadLength() {
		return config.get("workerThreadLength").getInt();
	}

	/**
	 * ワーカースレッド数を設定.
	 * @param workerThreadLength
	 */
	public void setWorkerThreadLength(int workerThreadLength) {
		config.set("workerThreadLength", workerThreadLength);
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
