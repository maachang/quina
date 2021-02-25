package quina.http.server;

import quina.net.nio.tcp.server.NioServerCore;

/**
 * HttpServerサービス.
 */
public class HttpServerService {
	// Nioサーバコア.
	private NioServerCore core;

	// HttpServer実行処理.
	private HttpServerCall call;

	// HttpServer定義.
	private HttpServerInfo info = new HttpServerInfo();



}
