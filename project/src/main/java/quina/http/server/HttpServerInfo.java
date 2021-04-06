package quina.http.server;

import quina.QuinaConfig;
import quina.QuinaInfo;
import quina.QuinaUtil;
import quina.http.EditMimeTypes;
import quina.http.HttpCustomAnalysisParams;
import quina.net.nio.tcp.NioConstants;
import quina.net.nio.tcp.server.NioServerConstants;
import quina.util.collection.IndexMap;
import quina.util.collection.TypesClass;

/**
 * Httpサーバ定義.
 */
public class HttpServerInfo implements QuinaInfo {
	// mimeConfigファイル名.
	private static final String MIME_CONFIG_FILE = "mime";

	// コンフィグ情報.
	private final QuinaConfig config = new QuinaConfig(
		// ByteBufferサイズ.
		"byteBufferLength", TypesClass.Integer, NioConstants.getByteBufferLength(),
		// Socket送信バッファ長.
		"sendBuffer", TypesClass.Integer, NioServerConstants.getSendBuffer(),
		// Socket受信バッファ長
		"recvBuffer", TypesClass.Integer, NioServerConstants.getRecvBuffer(),
		// KeepAlive
		"keepAlive", TypesClass.Boolean, NioServerConstants.isKeepAlive(),
		// TcpNoDeley.
		"tcpNoDeley", TypesClass.Boolean, NioServerConstants.isTcpNoDeley(),
		// サーバーソケットBindポート.
		"bindPort", TypesClass.Integer, HttpServerConstants.getBindServerSocketPort(),
		// サーバーソケットBindアドレス.
		"bindAddress", TypesClass.String, null,
		// サーバーソケット最大接続数.
		"backLog", TypesClass.Integer, NioServerConstants.getBacklog(),
		// サーバーソケット受信バッファ長.
		"serverRecvBuffer", TypesClass.Integer, NioServerConstants.getRecvBuffer(),
		// エラー４０４返却に対するjson返信モード.
		"errorResultJsonMode", TypesClass.Boolean, HttpServerConstants.isErrorResltJsonMode()
	);

	// カスタムなPostBody解析.
	private HttpCustomAnalysisParams custom = null;
	// MimeTypes.
	private EditMimeTypes mimeTypes = null;

	/**
	 * コンストラクタ.
	 */
	public HttpServerInfo() {
		reset();
	}

	@Override
	public void reset() {
		config.clear();
		custom = null;
		mimeTypes = new EditMimeTypes();
	}

	@Override
	public QuinaConfig getQuinaConfig() {
		return config;
	}

	/**
	 * ByteBufferのサイズを取得.
	 * @return
	 */
	public int getByteBufferLength() {
		return config.get("byteBufferLength").getInt();
	}

	/**
	 * ByteBufferサイズをセット.
	 * @param byteBufferLength
	 */
	public void setByteBufferLength(int byteBufferLength) {
		config.set("byteBufferLength", byteBufferLength);
	}

	/**
	 * Socket送信バッファサイズを取得.
	 * @return
	 */
	public int getSendBuffer() {
		return config.get("sendBuffer").getInt();
	}

	/**
	 * Socket送信バッファサイズを設定.
	 * @param sendBuffer
	 */
	public void setSendBuffer(int sendBuffer) {
		config.set("sendBuffer", sendBuffer);
	}

	/**
	 * Socket受診バッファサイズを取得.
	 * @return
	 */
	public int getRecvBuffer() {
		return config.get("recvBuffer").getInt();
	}

	/**
	 * Socket受診バッファサイズを設定
	 * @param recvBuffer
	 */
	public void setRecvBuffer(int recvBuffer) {
		config.set("recvBuffer", recvBuffer);
	}

	/**
	 * SocketのKeepAliveを取得.
	 * @return
	 */
	public boolean isKeepAlive() {
		return config.get("keepAlive").getBool();
	}

	/**
	 * SocketのKeepAliveを設定.
	 * @param keepAlive
	 */
	public void setKeepAlive(boolean keepAlive) {
		config.set("keepAlive", keepAlive);
	}

	/**
	 * SocketのTcpNoDeleyを取得.
	 * @return
	 */
	public boolean isTcpNoDeley() {
		return config.get("tcpNoDeley").getBool();
	}

	/**
	 * SocketのTcpNoDeleyを設定.
	 * @param tcpNoDeley
	 */
	public void setTcpNoDeley(boolean tcpNoDeley) {
		config.set("tcpNoDeley", tcpNoDeley);
	}

	/**
	 * ServerSocketのBindポートを取得.
	 * @return
	 */
	public int getBindPort() {
		return config.get("bindPort").getInt();
	}

	/**
	 * ServerSocketのBindポートを設定.
	 * @param bindPort
	 */
	public void setBindPort(int bindPort) {
		config.set("bindPort", bindPort);
	}

	/**
	 * ServerSocketのBindアドレスを取得.
	 * @return
	 */
	public String getBindAddress() {
		return config.get("bindAddress").getString();
	}

	/**
	 * ServerSocketのBindアドレスを設定.
	 * @param bindAddress
	 */
	public void setBindAddress(String bindAddress) {
		config.set("bindAddress", bindAddress);
	}

	/**
	 * サーバー接続最大数を取得.
	 * @return
	 */
	public int getBackLog() {
		return config.get("backLog").getInt();
	}

	/**
	 * サーバー接続最大数を設定.
	 * @param backLog
	 */
	public void setBackLog(int backLog) {
		config.set("backLog", backLog);
	}

	/**
	 * サーバー受信バッファを取得.
	 * @return
	 */
	public int getServerRecvBuffer() {
		return config.get("serverRecvBuffer").getInt();
	}

	/**
	 * サーバー受信バッファを設定.
	 * @param serverRecvBuffer
	 */
	public void setServerRecvBuffer(int serverRecvBuffer) {
		config.set("serverRecvBuffer", serverRecvBuffer);
	}

	/**
	 * エラー４０４等の返却でJSON返却するか取得.
	 * @return
	 */
	public boolean isErrorResultJsonMode() {
		return config.get("errorResultJsonMode").getBool();
	}

	/**
	 * エラー４０４等の返却でJSON返却するか設定.
	 * @param mode
	 */
	public void setErrorResultJsonMode(boolean mode) {
		config.set("errorResultJsonMode", mode);
	}

	/**
	 * Httpリクエストのパラメータ解析カスタム処理を取得.
	 * @return
	 */
	public HttpCustomAnalysisParams getCustom() {
		return custom;
	}

	/**
	 * Httpリクエストのパラメータ解析カスアム処理を設定.
	 * @param custom
	 */
	public void setCustom(HttpCustomAnalysisParams custom) {
		this.custom = custom;
	}

	/**
	 * MimeTypeを取得.
	 * @return
	 */
	public EditMimeTypes getMimeTypes() {
		return mimeTypes;
	}

	/**
	 * MimeTypeを設定.
	 * @param mimeTypes
	 */
	public void setMimeTypes(EditMimeTypes mimeTypes) {
		if(mimeTypes == null) {
			mimeTypes = new EditMimeTypes();
		}
		this.mimeTypes = mimeTypes;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		// このオブジェクト内容を出力.
		config.toString(buf, 2);
		if(mimeTypes != null) {
			// mimeType内容を出力.
			buf.append("\n").append("  *mimeType:\n");
			this.mimeTypes.toString(buf, 4);
		}
		return buf.toString();
	}

	@Override
	public void readConfig(String configDir) {
		// コンフィグ情報を読み込む.
		QuinaInfo.super.readConfig(configDir);
		// mimeTypeのコンフィグ読み込み.
		IndexMap<String, Object> json = QuinaUtil.loadJson(
			configDir, MIME_CONFIG_FILE);
		// jsonが取得できた場合.
		if(json != null) {
			mimeTypes.setMimeTypes(json);
		}
	}
}
