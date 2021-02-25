package quina.net.nio.tcp.worker;

import quina.util.collection.ObjectList;

/**
 * Nioワーカースレッドサービス.
 */
public class NioWorkerService {
	// ワーカースレッドマネージャ.
	private NioWorkerThreadManager manager;

	// プーリングマネージャ群.
	private ObjectList<NioWorkerPoolingManager> poolings =
		new ObjectList<NioWorkerPoolingManager>();

	// NioWorker定義.
	private NioWorkerInfo info = new NioWorkerInfo();


}
