package quina.http.server;

import java.io.IOException;

import quina.http.CsMode;
import quina.http.HttpElement;
import quina.logger.Log;
import quina.logger.LogFactory;
import quina.net.nio.tcp.NioElement;
import quina.net.nio.tcp.NioWorkerCall;
import quina.net.nio.tcp.server.NioServerCall;

/**
 * HttpサーバNio用コール.
 */
public class HttpServerNioCall extends NioServerCall {
	// ログ出力.
	private static final Log log = LogFactory.getInstance().get();
	
	// HttpServerService.
	private final HttpServerService service;

	/**
	 * コンストラクタ.
	 * @param server HttpServerServiceを設定します.
	 */
	public HttpServerNioCall(HttpServerService service) {
		this.service = service;
	}

	/**
	 * NioServerCoreが生成された時に呼び出されます.
	 */
	@Override
	public void init() {
		if(log.isInfoEnabled()) {
			log.info("### init HttpServerCall.");
		}
	}

	/**
	 * NioServerCoreのstartThread処理が呼ばれた時に呼び出されます.
	 */
	@Override
	public void startThread() {
		if(log.isInfoEnabled()) {
			log.info("### startThread HttpServerCall.");
		}
	}

	/**
	 * NioServerCoreのstopThread処理が呼ばれた時に呼び出されます.
	 */
	@Override
	public void stopThread() {
		if(log.isInfoEnabled()) {
			log.info("### stopThread HttpServerCall.");
		}
	}

	/**
	 * nio開始処理.
	 *
	 * @return boolean [true]の場合、正常に処理されました.
	 */
	@Override
	public boolean startNio() {
		if(log.isInfoEnabled()) {
			log.info("### started HttpServerCall.");
		}
		return true;
	}

	/**
	 * nio終了処理.
	 */
	@Override
	public void endNio() {
		if(log.isInfoEnabled()) {
			log.info("### end HttpServerCall.");
		}
	}

	/**
	 * エラーハンドリング.
	 *
	 * @param e エラー用の例外オブジェクトを設定されます.
	 */
	@Override
	public void error(Throwable e) {
		if(log.isWarnEnabled()) {
			log.warn("### error", e);
		}
	}

	/**
	 * Accept処理.
	 *
	 * @param em
	 *            対象のBaseNioElementオブジェクトが設定されます.
	 * @return boolean [true]の場合、正常に処理されました.
	 * @exception IOException
	 *                IO例外.
	 */
	@Override
	public boolean accept(NioElement em)
		throws IOException {
		if(log.isTraceEnabled()) {
			log.trace("### accept: " + em.getRemoteAddress());
		}
		return true;
	}

	/**
	 * Nio要素を生成.
	 */
	@Override
	public NioElement createElement() {
		HttpElement ret = new HttpElement(CsMode.Server);
		// ElementTimeoutに登録.
		service.pushTimeoutElement(ret);
		return ret;
	}

	/**
	 * NioWorkerCallを取得.
	 * @return NioWorkerCall NioWorkerCallが返却されます.
	 */
	@Override
	protected NioWorkerCall createNioWorkerCall() {
		return new HttpServerWorkerCall();
	}
}
