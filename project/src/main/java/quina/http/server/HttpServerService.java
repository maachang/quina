package quina.http.server;

import java.nio.channels.ServerSocketChannel;

import quina.QuinaException;
import quina.QuinaInfo;
import quina.QuinaService;
import quina.http.worker.HttpWorkerService;
import quina.net.nio.tcp.NioUtil;
import quina.net.nio.tcp.server.NioServerCore;
import quina.util.Flag;

/**
 * HttpServerサービス.
 */
public class HttpServerService implements QuinaService {
	// Nioサーバコア.
	private NioServerCore core;

	// HttpServer定義.
	private HttpServerInfo info = new HttpServerInfo();

	// HttpWorkerService.
	private HttpWorkerService httpWorkerService = null;

	// サービス開始フラグ.
	private final Flag startFlag = new Flag(false);

	/**
	 * コンストラクタ.
	 * @param httpWorkerService HttpWorkerServceを設定します.
	 */
	public HttpServerService(HttpWorkerService httpWorkerService) {
		this.httpWorkerService = httpWorkerService;
	}

	@Override
	public synchronized void readConfig(String configDir) {
		info.readConfig(configDir);
	}


	@Override
	public void check(boolean flg) {
		if(startFlag.get() == flg) {
			if(flg) {
				throw new QuinaException(this.getClass().getName() + " has already started.");
			}
			throw new QuinaException(this.getClass().getName() + " is already stopped.");
		}
	}

	@Override
	public synchronized boolean isStartService() {
		return startFlag.get();
	}

	@Override
	public synchronized void startService() {
		// HttpWorkerServiceが開始していない場合はエラー.
		if(!httpWorkerService.isStarted()) {
			throw new QuinaException("HttpWorkerService is not started.");
		}
		// 一度起動している場合はエラー.
		if(startFlag.setToGetBefore(true)) {
			throw new QuinaException(this.getClass().getName() + " has already started.");
		}
		ServerSocketChannel server = null;
		try {
			// サーバーコール生成.
			final HttpServerCall call = new HttpServerCall(info.getCustom(), info.getMimeTypes());
			// サーバーソケット作成.
			server = NioUtil.createServerSocketChannel(
				info.getBindAddress(), info.getBindPort(), info.getBackLog(), info.getServerRecvBuffer());
			// サーバーコア生成.
			this.core = new NioServerCore(info.getByteBufferLength(), info.getSendBuffer(),
				info.getRecvBuffer(), info.isKeepAlive(), info.isTcpNoDeley(),
				server, call, httpWorkerService.getServerPoolingManager(),
				httpWorkerService.getNioWorkerThreadManager());
			// サーバスレッド開始.
			this.core.startThread();
		} catch(QuinaException qe) {
			stopService();
			if(server != null) {
				try {server.close();} catch(Exception ee) {}
			}
			throw qe;
		} catch(Exception e) {
			stopService();
			if(server != null) {
				try {server.close();} catch(Exception ee) {}
			}
			throw new QuinaException(e);
		}
	}

	@Override
	public synchronized boolean isStarted() {
		if(core != null) {
			return core.isStartupThread();
		}
		return false;
	}

	@Override
	public synchronized boolean waitToStartup(long timeout) {
		if(core != null) {
			return core.waitToStartup();
		}
		return true;
	}

	@Override
	public synchronized void stopService() {
		// 停止処理.
		if(core != null) {
			core.stopThread();
			core = null;
		}
		startFlag.set(false);
	}

	@Override
	public synchronized boolean isExit() {
		if(core != null) {
			return core.isExitThread();
		}
		return true;
	}

	@Override
	public synchronized boolean waitToExit(long timeout) {
		if(core != null) {
			return core.waitToExit();
		}
		return true;
	}

	@Override
	public synchronized QuinaInfo getInfo() {
		return info;
	}
}
