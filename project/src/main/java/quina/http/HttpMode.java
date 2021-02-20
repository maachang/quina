package quina.http;

/**
 * HTTPモード.
 */
public enum HttpMode {
	/**
	 * Httpサーバーモード.
	 */
	Server("server", true),
	/**
	 * Httpクライアントモード.
	 */
	Client("client", false);

	private String name;
	private boolean serverMode;

	private HttpMode(String name, boolean serverMode) {
		this.name = name;
		this.serverMode = serverMode;
	}

	/**
	 * モード名を取得.
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * サーバモードかチェック.
	 * @return
	 */
	public boolean isServer() {
		return serverMode;
	}
}
