package quina.shutdown;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

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

	// sun.misc.Signalを使うと「警告」が出るので、それを回避する対応.
	// また将来的にsun.misc.Signalが廃止された時に回避する対応.
	private static final void setupSunMiscSignal_INT(Object o) {
		// Signal.handle(new Signal("INT"), sig -> System.exit(0));
		// コレと同じ対応をリフレクションで実施.

		// Check that sun.misc.SignalHandler and sun.misc.Signal exists
		try {
			final Class<?> signalClass = Class.forName("sun.misc.Signal");
			final Class<?> signalHandlerClass = Class.forName("sun.misc.SignalHandler");
			// Implement signal handler
			final Object signalHandler = Proxy.newProxyInstance(o.getClass().getClassLoader(),
				new Class<?>[]{signalHandlerClass}, new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args)
						throws Throwable {
						// only method we are proxying is handle()
						System.exit(0);
						return null;
					}
				});
			// Register the signal handler, this code is equivalent to:
			// Signal.handle(new Signal("INT"), signalHandler);
			signalClass.getMethod("handle", signalClass, signalHandlerClass)
				.invoke(null, signalClass.getConstructor(String.class).newInstance("INT"),
					signalHandler);
		} catch (ClassNotFoundException cnfe) {
			// sun.misc Signal handler classes don't exist
		} catch (Exception e) {
			// Ignore this one too, if the above failed, the signal API is
			// incompatible with what we're expecting
		}
	}


	// シグナル登録フラグ.
	private static final AtomicBoolean regSignalFlag = new AtomicBoolean(false);

	// シグナル登録.
	// これを行わないとctrl-cなどでシャットダウンフックが検知されない.
	public static final void registerSignal(Object o) {
		// シグナルは１度だけ登録.
		boolean flg = regSignalFlag.get();
		while(!regSignalFlag.compareAndSet(flg, true)) {
			flg = regSignalFlag.get();
		}
		if(!flg) {
			// Register a signal handler for Ctrl-C that runs the shutdown hooks
			//Signal.handle(new Signal("INT"), sig -> System.exit(0));
			// sun.misc.Signalを使うと「警告」が出るので、回避対応.
			setupSunMiscSignal_INT(o);
		}
	}

	/**
	 * シャットダウン処理の監視を開始.
	 */
	public void startShutdown() {
		synchronized(info.getSync()) {
			info.checkStart();
			registerSignal(this);
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
			runCall();
		}

		/**
		 * 実行処理.
		 */
		public void runCall() {
			final int retry = info.getRetry();
			final ShutdownCallManager cman = info.getCallManager();
			while(!stopFlag) {
				try {
					// シャットダウンコネクションを検知した場合.
					if(checkShutdown(retry)) {
						// コールマネージャを実行.
						cman.runCall();
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
					SendShutdown.eqShutdownConnection(packet, shutdownToken)) {
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
