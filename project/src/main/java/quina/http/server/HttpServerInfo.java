package quina.http.server;

import quina.QuinaInfo;
import quina.http.EditMimeTypes;
import quina.http.HttpCustomAnalysisParams;
import quina.net.nio.tcp.NioConstants;
import quina.net.nio.tcp.server.NioServerConstants;

/**
 * Httpサーバ定義.
 */
public class HttpServerInfo implements QuinaInfo {
	// ByteBufferサイズ.
	private int byteBufferLength;
	// Socket送信バッファ長.
	private int sendBuffer;
	// Socket受信バッファ長
	private int recvBuffer;
	// KeepAlive
	private boolean keepAlive;
	// TcpNoDeley.
	private boolean tcpNoDeley;
	// サーバーソケットBindポート.
	private int bindPort;
	// サーバーソケットBindアドレス.
	private String bindAddress;
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
		// デフォルト値を初期化.
		byteBufferLength = NioConstants.getByteBufferLength();
		sendBuffer = NioServerConstants.getSendBuffer();
		recvBuffer = NioServerConstants.getRecvBuffer();
		keepAlive = NioServerConstants.isKeepAlive();
		tcpNoDeley = NioServerConstants.isTcpNoDeley();
		bindPort = HttpServerConstants.getBindServerSocketPort();
		bindAddress = null;
		custom = null;
		mimeTypes = new EditMimeTypes();
	}

	/**
	 * ByteBufferのサイズを取得.
	 * @return
	 */
	public int getByteBufferLength() {
		return byteBufferLength;
	}

	/**
	 * ByteBufferサイズをセット.
	 * @param byteBufferLength
	 */
	public void setByteBufferLength(int byteBufferLength) {
		this.byteBufferLength = byteBufferLength;
	}

	/**
	 * Socket送信バッファサイズを取得.
	 * @return
	 */
	public int getSendBuffer() {
		return sendBuffer;
	}

	/**
	 * Socket送信バッファサイズを設定.
	 * @param sendBuffer
	 */
	public void setSendBuffer(int sendBuffer) {
		this.sendBuffer = sendBuffer;
	}

	/**
	 * Socket受診バッファサイズを取得.
	 * @return
	 */
	public int getRecvBuffer() {
		return recvBuffer;
	}

	/**
	 * Socket受診バッファサイズを設定
	 * @param recvBuffer
	 */
	public void setRecvBuffer(int recvBuffer) {
		this.recvBuffer = recvBuffer;
	}

	/**
	 * SocketのKeepAliveを取得.
	 * @return
	 */
	public boolean isKeepAlive() {
		return keepAlive;
	}

	/**
	 * SocketのKeepAliveを設定.
	 * @param keepAlive
	 */
	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	/**
	 * SocketのTcpNoDeleyを取得.
	 * @return
	 */
	public boolean isTcpNoDeley() {
		return tcpNoDeley;
	}

	/**
	 * SocketのTcpNoDeleyを設定.
	 * @param tcpNoDeley
	 */
	public void setTcpNoDeley(boolean tcpNoDeley) {
		this.tcpNoDeley = tcpNoDeley;
	}

	/**
	 * ServerSocketのBindポートを取得.
	 * @return
	 */
	public int getBindPort() {
		return bindPort;
	}

	/**
	 * ServerSocketのBindポートを設定.
	 * @param bindPort
	 */
	public void setBindPort(int bindPort) {
		this.bindPort = bindPort;
	}

	/**
	 * ServerSocketのBindアドレスを取得.
	 * @return
	 */
	public String getBindAddress() {
		return bindAddress;
	}

	/**
	 * ServerSocketのBindアドレスを設定.
	 * @param bindAddress
	 */
	public void setBindAddress(String bindAddress) {
		this.bindAddress = bindAddress;
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
		return outString();
	}
}
