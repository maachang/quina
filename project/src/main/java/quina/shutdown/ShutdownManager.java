package quina.shutdown;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import quina.shutdown.ShutdownManagerInfo.ShutdownCallManager;

/**
 * シャットダウン管理オブジェクト.
 */
public class ShutdownManager {

	/**
	 * シャットダウンコネクション監視.
	 */
	private ShutdownConnectionMoniter connectMon = null;

	/**
	 * シャットダウンマネージャ情報.
	 */
	private ShutdownManagerInfo info = new ShutdownManagerInfo();

	/**
	 * コンストラクタ.
	 */
	public ShutdownManager() {
	}

	/**
	 * シャットダウンマネージャ情報を取得.
	 * @return ShutdownManagerInfo シャットダウンマネージャ情報が返却されます.
	 */
	public ShutdownManagerInfo getInfo() {
		synchronized(info.getSync()) {
			return info;
		}
	}

	/**
	 * シャットダウン処理の監視を開始.
	 */
	public void startShutdown() {
		synchronized(info.getSync()) {
			info.checkStart();
			ShutdownCallManager cman = info.getCallManager();
			// シャットダウンコールが存在するかチェック.
			if(cman == null || cman.size() <= 0) {
				throw new ShutdownException("Shutdown call is not registered.");
			}
			try {
				// シャットダウンコネクションマネージャを生成して実行.
				connectMon = new ShutdownConnectionMoniter(info);
				connectMon.startThread();

				// シャットダウンフックに登録.
				startShutdownHook(cman);
				info.setStart(true);
			} catch(Exception e) {
				stopShutdown();
			}
		}
	}

	/**
	 * シャットダウン処理の監視が開始されている場合は停止させます.
	 * @return
	 */
	public boolean stopShutdown() {
		synchronized(info.getSync()) {
			ShutdownCallManager cman = info.getCallManager();
			if(connectMon == null ||
				cman == null || cman.size() <= 0) {
				return false;
			}
			// シャットダウンコネクションをストップ.
			if(connectMon != null) {
				connectMon.stopThread();
				connectMon = null;
			}
			// シャットダウンフック登録解除.
			try {
				stopShutdownHook(cman);
			} catch(Exception ee) {}
			info.setStart(false);
			return true;
		}
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
	protected static final boolean eqShutdownConnection(
		final DatagramPacket packet, final byte[] token) {
		if (packet.getLength() == token.length) {
			final int len = packet.getLength();
			final byte[] bin = packet.getData();
			for (int i = 0; i < len; i++) {
				if ((token[i] & 0x000000ff) != (bin[i] & 0x000000ff)) {
					return false;
				}
			}
			return true;
		}
		return false;
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
		 * シャットダウンマネージャ情報.
		 */
		private ShutdownManagerInfo info = null;

		/**
		 * シャットダウンコネクションを受信するUDP.
		 */
		private DatagramSocket connection = null;

		/**
		 * 停止フラグ.
		 */
		private volatile boolean stopFlag = false;

		/**
		 * コンストラクタ.
		 * @param info
		 */
		public ShutdownConnectionMoniter(ShutdownManagerInfo info) {
			super();
			this.setDaemon(true);
			createConnetion(info.getBindPort());
			this.info = info;
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
			final int retry = info.getRetry();
			final ShutdownCallManager cman = info.getCallManager();
			while(!stopFlag) {
				try {
					// シャットダウンコネクションを検知した場合.
					if(checkShutdown(retry)) {
						// コールマネージャを実行.
						cman.run();
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
		 * @param retry 送信リトライ数を設定します.
		 */
		private final boolean checkShutdown(int retry) {
			final byte[] shutdownToken = info.getToken();
			try {
				// 受信パケットを生成.
				final DatagramPacket packet = new DatagramPacket(recvBuffer, recvBuffer.length);
				// 受信待ち.
				connection.receive(packet);
				// 受信されたら、シャットダウンコネクション情報と一致するかチェック.
				if (packet.getLength() == shutdownToken.length &&
					eqShutdownConnection(packet, shutdownToken)) {
					// 一致する場合は、相手側にシャットダウンコネクションを返信.
					int srcPort = packet.getPort();
					for(int i = 0; i < retry; i ++) {
						connection.send(
							new DatagramPacket(shutdownToken, 0, shutdownToken.length,
								InetAddress.getByName(ShutdownConstants.LOCAL_ADDRESS),
								srcPort));
						try {
							Thread.sleep(50L);
						} catch(Exception e) {}
					}
					return true;
				}
			} catch (Throwable e) {
				// タイムアウト等.
			}
			return false;
		}
	}
}
