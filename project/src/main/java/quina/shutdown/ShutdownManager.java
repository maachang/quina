package quina.shutdown;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * シャットダウン管理オブジェクト.
 */
public class ShutdownManager {
	/**
	 * シャットダウン待ちコネクションのポート番号.
	 */
	private int bindPort = ShutdownConstants.getPort();

	/**
	 * シャットダウンコールマネージャ.
	 */
	private ShutdownCallManager callManager = null;

	/**
	 * シャットダウンコネクション監視.
	 */
	private ShutdownConnectionMoniter connectMon = null;

	/**
	 * シャットダウンマネージャが起動中かチェック.
	 */
	private boolean startFlag = false;

	/**
	 * コンストラクタ.
	 */
	public ShutdownManager() {
	}

	/**
	 * シャットダウンマネージャが開始済みかチェック.
	 * @return boolean trueの場合、開始しています.
	 */
	public synchronized boolean isStart() {
		return startFlag;
	}

	// シャットダウンフックが既に開始している場合はエラー.
	private final void checkStart() {
		if(startFlag) {
			throw new ShutdownException("The shutdown hook has already started.");
		}
	}

	/**
	 * シャットダウンコールを登録.
	 * @param call シャットダウンコールを設定します.
	 * @return ShutdownManager ShutdownManagerオブジェクトが返却されます.
	 */
	public synchronized ShutdownManager register(ShutdownCall call) {
		checkStart();
		if(callManager == null) {
			callManager = new ShutdownCallManager();
		}
		callManager.add(call);
		return this;
	}

	/**
	 * シャットダウンコネクションを受信するUDPポート番号を取得します.
	 * @return int UDPポート番号が返却されます.
	 */
	public synchronized int getBindPort() {
		return bindPort;
	}

	/**
	 * シャットダウンコネクションを受信するUDPポート番号を設定します.
	 * @param port UDPポート番号を設定します.
	 * @return ShutdownManager ShutdownManagerオブジェクトが返却されます.
	 */
	public synchronized ShutdownManager setBindPort(int port) {
		checkStart();
		if (port <= 0 || port > 65535) {
			port = ShutdownConstants.getPort();
		}
		bindPort = port;
		return this;
	}

	/**
	 * シャットダウン処理の監視を開始.
	 */
	public synchronized void startShutdown() {
		checkStart();
		// シャットダウンコールが存在するかチェック.
		if(callManager == null || callManager.size() <= 0) {
			throw new ShutdownException("Shutdown call is not registered.");
		}
		try {
			// シャットダウンコネクションマネージャを生成して実行.
			connectMon = new ShutdownConnectionMoniter(bindPort, callManager);
			connectMon.startThread();

			// シャットダウンフックに登録.
			startShutdownHook(callManager);
			startFlag = true;
		} catch(Exception e) {
			stopShutdown();
		}
	}

	/**
	 * シャットダウン処理の監視が開始されている場合は停止させます.
	 * @return
	 */
	public synchronized boolean stopShutdown() {
		if(connectMon == null ||
			callManager == null || callManager.size() <= 0) {
			return false;
		}
		// シャットダウンコネクションをストップ.
		if(connectMon != null) {
			connectMon.stopThread();
			connectMon = null;
		}
		// シャットダウンフック登録解除.
		try {
			stopShutdownHook(callManager);
		} catch(Exception ee) {}
		startFlag = false;
		return true;
	}


	// シャットダウンフックを開始.
	private static final void startShutdownHook(Thread t) {
		Runtime.getRuntime().addShutdownHook(t);
	}

	// シャットダウンフックを解除.
	private static final void stopShutdownHook(Thread t) {
		Runtime.getRuntime().removeShutdownHook(t);
	}

	/**
	 * シャットダウンコネクションデータが受信されたかチェック.
	 */
	protected static final boolean eqShutdownConnection(final DatagramPacket packet) {
		if (packet.getLength() == ShutdownConstants.SHUTDOWN_BINARY.length) {
			final int len = packet.getLength();
			final byte[] bin = packet.getData();
			final byte[] sbin = ShutdownConstants.SHUTDOWN_BINARY;
			for (int i = 0; i < len; i++) {
				if ((sbin[i] & 0x000000ff) != (bin[i] & 0x000000ff)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * シャットダウンコールマネージャ.
	 */
	private static final class ShutdownCallManager extends Thread {
		private List<ShutdownCall> callList = new ArrayList<ShutdownCall>();

		/**
		 * コンストラクタ.
		 */
		public ShutdownCallManager() {
			super();
			this.setPriority(Thread.MAX_PRIORITY);
			this.setDaemon(false);
		}

		/**
		 * シャットダウンコールを追加.
		 * @param c シャットダウンコールを設定します.
		 * @return SHook このオブジェクトが返却されます.
		 */
		public ShutdownCallManager add(ShutdownCall c) {
			callList.add(c);
			return this;
		}

		/**
		 * シャットダウンコール登録数を取得.
		 * @return int 登録数が返却されます.
		 */
		public int size() {
			return callList.size();
		}

		/**
		 * シャットダウン実行.
		 */
		public void run() {
			ShutdownCall c;
			final int len = callList.size();
			for(int i = 0; i < len; i ++) {
				c = callList.get(i);
				if (!c.isShutdown()) {
					try {
						c.call();
					} catch(Throwable t) {}
				}
			}
		}
	}

	/**
	 * シャットダウンコネクション受信管理.
	 */
	private static final class ShutdownConnectionMoniter extends Thread {
		/**
		 * 受信タイムアウト待ち.
		 */
		private static final int RECEIVE_TIMEOUT = 50;

		/**
		 * 受信バッファ.
		 */
		private final byte[] recvBuffer = new byte[512];

		/**
		 * シャットダウンコネクションを受信するUDP.
		 */
		private DatagramSocket connection = null;

		/**
		 * シャットダウンコールマネージャ.
		 */
		private ShutdownCallManager callManager = null;

		/**
		 * 停止フラグ.
		 */
		private volatile boolean stopFlag = false;

		/**
		 * コンストラクタ.
		 * @param port
		 */
		public ShutdownConnectionMoniter(int port, ShutdownCallManager man) {
			super();
			this.setDaemon(true);
			createConnetion(port);
			callManager = man;
		}

		/**
		 * スレッド開始処理.
		 */
		public void startThread() {
			this.start();
		}

		/**
		 * スレッド停止処理.
		 */
		public void stopThread() {
			this.stopFlag = true;
		}

		/**
		 * 実行処理.
		 */
		public void run() {
			while(!stopFlag) {
				try {
					// シャットダウンコネクションを検知した場合.
					if(checkShutdown()) {
						// コールマネージャを実行.
						callManager.run();
						stopFlag = true;
						// このスレッドも終わらせる.
						return;
					}
				} catch(Throwable t) {
				}
			}
		}

		// 外部からのシャットダウンフックを検知する受信コネクションの生成.
		private final void createConnetion(int bindPort) {
			try {
				connection = new DatagramSocket(null);
				connection.setSoTimeout(RECEIVE_TIMEOUT);
				connection.setReuseAddress(true);
				connection.bind(new InetSocketAddress(
					InetAddress.getByName(ShutdownConstants.LOCAL_ADDRESS),
					bindPort));
			} catch(Exception e) {
				throw new ShutdownException(e);
			}
		}

		/**
		 * シャットダウンコネクションが受信されたかチェック.
		 */
		private final boolean checkShutdown() {
			try {
				// 受信パケットを生成.
				final DatagramPacket packet = new DatagramPacket(recvBuffer, recvBuffer.length);
				// 受信待ち.
				connection.receive(packet);
				// 受信されたら、シャットダウンコネクション情報と一致するかチェック.
				if (packet.getLength() == ShutdownConstants.SHUTDOWN_BINARY.length &&
					eqShutdownConnection(packet)) {
					// 一致する場合は、相手側にシャットダウンコネクションを返信.
					int srcPort = packet.getPort();
					connection.send(
						new DatagramPacket(ShutdownConstants.SHUTDOWN_BINARY, 0,
							ShutdownConstants.SHUTDOWN_BINARY.length,
							InetAddress.getByName(ShutdownConstants.LOCAL_ADDRESS), srcPort));
					return true;
				}
			} catch (Throwable e) {
				// タイムアウト等.
			}
			return false;
		}
	}
}
