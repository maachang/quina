package quina.http;

/**
 * Client/Serverモード.
 */
public enum CsMode {
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

	private CsMode(String name, boolean serverMode) {
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
