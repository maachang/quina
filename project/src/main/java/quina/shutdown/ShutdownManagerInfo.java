package quina.shutdown;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * シャットダウンマネージャ情報.
 */
public class ShutdownManagerInfo {
	/**
	 * setXXX が呼び出されたかチェック.
	 */
	private boolean setFlag = false;

	/**
	 * シャットダウンマネージャが起動中かチェック.
	 */
	private boolean startFlag = false;

	/**
	 * 同期オブジェクト.
	 */
	private final Object sync = new Object();

	/**
	 * シャットダウンリトライ数.
	 */
	private int retry = ShutdownConstants.getRetry();

	/**
	 * シャットダウン待ちコネクションのポート番号.
	 */
	private int bindPort = ShutdownConstants.getPort();

	/**
	 * シャットダウントークン.
	 */
	private byte[] token = ShutdownConstants.getShutdownToken();

	/**
	 * シャットダウンコールマネージャ.
	 */
	private ShutdownCallManager callManager = null;

	/**
	 * コンフィグ定義が行われているかチェック.
	 * @return boolean [true]の場合、コンフィグ定義が行われています.
	 */
	public boolean isConfig() {
		synchronized(sync) {
			return setFlag;
		}
	}

	// シャットダウンフックが既に開始している場合はエラー.
	protected final void checkStart() {
		synchronized(sync) {
			if(startFlag) {
				throw new ShutdownException("The shutdown hook has already started.");
			}
		}
	}

	// ObjectをIntに変換.
	private static final int getInt(int src, Object... o) {
		int len = o == null ? 0 : o.length;
		for(int i = 0; i < len; i ++) {
			if(o[i] != null) {
				try {
					return Integer.parseInt(""+o[i]);
				} catch(Exception e) {
				}
			}
		}
		return src;
	}

	// Objectを文字列に変換.
	private static final String getString(String src, Object... o) {
		int len = o == null ? 0 : o.length;
		for(int i = 0; i < len; i ++) {
			if(o[i] != null && o[i] instanceof String) {
				return (String)o[i];
			}
		}
		return src;
	}

	/**
	 * シャットダウンコンフィグを設定.
	 * @param json json定義を設定します.
	 * @return ShutdownManagerInfo ShutdownManagerInfoオブジェクトが返却されます.
	 */
	public ShutdownManagerInfo config(Map<String, Object> json) {
		synchronized(sync) {
			checkStart();
			setRetry(getInt(this.retry, json.get("retry")));
			setBindPort(getInt(this.bindPort, json.get("bindPort"), json.get("port")));
			setToken(getString(ShutdownConstants.DEFAULT_TOKEN,
				json.get("token"), json.get("shutdownToken")));
			return this;
		}
	}

	/**
	 * シャットダウンコールを登録.
	 * @param call シャットダウンコールを設定します.
	 * @return ShutdownManagerInfo ShutdownManagerInfoオブジェクトが返却されます.
	 */
	public ShutdownManagerInfo register(ShutdownCall call) {
		synchronized(sync) {
			checkStart();
			if(callManager == null) {
				callManager = new ShutdownCallManager(sync);
			}
			callManager.add(call);
			return this;
		}
	}

	/**
	 * 登録済みのシャットダウンコールを削除.
	 * @param call 削除するシャットダウンコールを設定します.
	 * @return boolean trueの場合削除されました.
	 */
	public boolean remove(ShutdownCall call) {
		synchronized(sync) {
			checkStart();
			if(callManager != null) {
				final int len = callManager.size();
				for(int i = 0; i < len; i ++) {
					if(callManager.get(i) == call) {
						if(callManager.remove(i) != null) {
							return true;
						}
						return false;
					}
				}
			}
			return false;
		}
	}

	/**
	 * シャットダウンコネクションを受信するUDPポート番号を取得します.
	 * @return int UDPポート番号が返却されます.
	 */
	public int getBindPort() {
		synchronized(sync) {
			return bindPort;
		}
	}

	/**
	 * シャットダウンコネクションを受信するUDPポート番号を設定します.
	 * @param port UDPポート番号を設定します.
	 * @return ShutdownManagerInfo ShutdownManagerInfoオブジェクトが返却されます.
	 */
	public ShutdownManagerInfo setBindPort(int port) {
		synchronized(sync) {
			checkStart();
			if (port <= 0 || port > 65535) {
				port = ShutdownConstants.getPort();
			}
			bindPort = port;
			setFlag = true;
			return this;
		}
	}

	/**
	 * 返信リトライ数を取得.
	 * @return int 返信リトライ数が返却されます.
	 */
	public int getRetry() {
		synchronized(sync) {
			return retry;
		}
	}

	/**
	 * 返信リトライ数を設定.
	 * @param retry 返信リトライ数を設定します.
	 * @return ShutdownManagerInfo ShutdownManagerInfoオブジェクトが返却されます.
	 */
	public ShutdownManagerInfo setRetry(int retry) {
		synchronized(sync) {
			checkStart();
			if(retry <= 0 || retry > ShutdownConstants.MAX_RETRY) {
				if(retry <= 0) {
					retry = 1;
				} else {
					retry = ShutdownConstants.MAX_RETRY;
				}
			}
			this.retry = retry;
			setFlag = true;
			return this;
		}
	}

	/**
	 * トークンを取得.
	 * @return トークンが返却されます.
	 */
	public byte[] getToken() {
		synchronized(sync) {
			return token;
		}
	}

	/**
	 * トークンを設定.
	 * @param token トークンを設定します.
	 * @return ShutdownManagerInfo ShutdownManagerInfoオブジェクトが返却されます.
	 */
	public ShutdownManagerInfo setToken(String token) {
		synchronized(sync) {
			checkStart();
			this.token = ShutdownConstants.createShutdownToken(token);
			setFlag = true;
			return this;
		}
	}

	/**
	 * シャットダウンマネージャが開始済みかチェック.
	 * @return boolean trueの場合、開始しています.
	 */
	public boolean isStart() {
		synchronized(sync) {
			return startFlag;
		}
	}

	/**
	 * シャットダウンマネージャ開始の有無を設定.
	 * @param flg [true]の場合開始扱いになります.
	 */
	protected void setStart(boolean flg) {
		synchronized(sync) {
			startFlag = flg;
		}
	}

	/**
	 * 同期オブジェクトを取得.
	 * @return
	 */
	protected Object getSync() {
		return sync;
	}

	/**
	 * コールマネージャを取得.
	 * @return ShutdownCallManager コールマネージャが返却されます.
	 */
	protected ShutdownCallManager getCallManager() {
		synchronized(sync) {
			return callManager;
		}
	}

	/**
	 * シャットダウンコールマネージャ.
	 */
	protected static final class ShutdownCallManager extends Thread {
		private Object sync;
		private List<ShutdownCall> callList = new ArrayList<ShutdownCall>();

		/**
		 * コンストラクタ.
		 */
		public ShutdownCallManager(Object sync) {
			super();
			this.setPriority(Thread.MAX_PRIORITY);
			this.setDaemon(false);
			this.sync = sync;
		}

		/**
		 * シャットダウンコールを追加.
		 * @param c シャットダウンコールを設定します.
		 * @return SHook このオブジェクトが返却されます.
		 */
		public ShutdownCallManager add(ShutdownCall c) {
			synchronized(sync) {
				if(callList != null) {
					callList.add(c);
				}
				return this;
			}
		}

		/**
		 * シャットダウンコールを取得.
		 * @param no 対象の項番を設定します.
		 * @return ShutdownCall シャットダウンコールが返却されます.
		 */
		public ShutdownCall get(int no) {
			synchronized(sync) {
				if(callList != null) {
					return callList.get(no);
				}
				return null;
			}
		}

		/**
		 * シャットダウンコールを削除.
		 * @param no 対象の項番を設定します.
		 * @return ShutdownCall シャットダウンコールが返却されます.
		 */
		public ShutdownCall remove(int no) {
			synchronized(sync) {
				if(callList != null) {
					return callList.remove(no);
				}
				return null;
			}
		}

		/**
		 * シャットダウンコール登録数を取得.
		 * @return int 登録数が返却されます.
		 */
		public int size() {
			synchronized(sync) {
				if(callList != null) {
					return callList.size();
				}
				return 0;
			}
		}

		/**
		 * シャットダウン実行.
		 */
		public void run() {
			ShutdownCall c;
			List<ShutdownCall> clist = null;
			synchronized(sync) {
				clist = callList;
				callList = null;
			}
			try {
				final int len = clist == null ? 0 : clist.size();
				for(int i = 0; i < len; i ++) {
					c = clist.get(i);
					// シャットダウン完了.
					if(!c.successShutdown()) {
						// 前回シャットダウン処理が実施されてない場合.
						try {
							// シャットダウンコールを実行.
							c.call();
						} catch(Throwable t) {}
					}
				}
			} finally {
				synchronized(sync) {
					callList = clist;
				}
			}
		}
	}

}
