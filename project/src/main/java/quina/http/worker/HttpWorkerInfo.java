package quina.http.worker;

import quina.QuinaConfig;
import quina.QuinaInfo;
import quina.net.nio.tcp.NioConstants;
import quina.net.nio.tcp.worker.NioWorkerConstants;
import quina.util.collection.TypesClass;

/**
 * Httpワーカー定義.
 */
public class HttpWorkerInfo implements QuinaInfo {
	// コンフィグ情報.
	private QuinaConfig config = new QuinaConfig(
		// ワーカースレッド管理サイズ.
		"workerThreadLength", TypesClass.Integer, NioWorkerConstants.getWorkerThreadLength(),
		// サーバープーリング管理サイズ.
		"serverPoolingManagerLength", TypesClass.Integer, NioWorkerConstants.getPoolingManageLength(),
		// クライアントプーリング管理サイズ.
		"clientPoolingManagerLength", TypesClass.Integer, NioWorkerConstants.getPoolingManageLength(),
		// 受信テンポラリバッファサイズ.
		"recvTmpBuffer", TypesClass.Integer, NioConstants.getByteBufferLength()
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
	 * サーバープーリングマネージャサイズを取得.
	 * @return
	 */
	public int getServerPoolingManagerLength() {
		return config.get("serverPoolingManagerLength").getInt();
	}

	/**
	 * サーバープーリングマネージャサイズを設定.
	 * @param serverPoolingManagerLength
	 */
	public void setServerPoolingManagerLength(int serverPoolingManagerLength) {
		config.set("serverPoolingManagerLength", serverPoolingManagerLength);
	}

	/**
	 * クライアントプーリングマネージャサイズを取得.
	 * @return
	 */
	public int getClientPoolingManagerLength() {
		return config.get("clientPoolingManagerLength").getInt();
	}

	/**
	 * クライアントプーリングマネージャサイズを設定.
	 * @param poolingManagerLength
	 */
	public void setClientPoolingManagerLength(int clientPoolingManagerLength) {
		config.set("clientPoolingManagerLength", clientPoolingManagerLength);
	}

	/**
	 * 受信テンポラリバッファサイズを取得.
	 * @return
	 */
	public int getRecvTmpBuffer() {
		return config.get("recvTmpBuffer").getInt();
	}

	/**
	 * 受信テンポラリバッファサイズを設定.
	 * @param recvTmpBuffer
	 */
	public void setRecvTmpBuffer(int recvTmpBuffer) {
		config.set("recvTmpBuffer", recvTmpBuffer);
	}

	@Override
	public String toString() {
		return outString();
	}
}
