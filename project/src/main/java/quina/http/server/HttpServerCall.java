package quina.http.server;

import java.io.IOException;

import quina.Quina;
import quina.QuinaException;
import quina.component.ComponentManager;
import quina.component.ComponentType;
import quina.component.RegisterComponent;
import quina.http.HttpAnalysis;
import quina.http.HttpCustomAnalysisParams;
import quina.http.HttpElement;
import quina.http.HttpException;
import quina.http.HttpMode;
import quina.http.HttpRequest;
import quina.http.Method;
import quina.http.MimeTypes;
import quina.http.Params;
import quina.http.Request;
import quina.http.Response;
import quina.http.response.DefaultResponse;
import quina.http.response.RESTfulResponse;
import quina.http.response.SyncResponse;
import quina.logger.Log;
import quina.logger.LogFactory;
import quina.net.nio.tcp.NioElement;
import quina.net.nio.tcp.NioSendData;
import quina.net.nio.tcp.server.NioServerCall;

/**
 * Httpサーバコール.
 */
public class HttpServerCall extends NioServerCall {
	// ログ出力.
	private static final Log LOG = LogFactory.getInstance().get();

	// カスタムなPostBody解析.
	private HttpCustomAnalysisParams custom = null;

	// MimeTypes.
	private MimeTypes mimeTypes = null;

	/**
	 * コンストラクタ.
	 * @param custom HttpRequestでのBodyの解釈をするオブジェクトを設定します.
	 * @param mimeTypes mimeTypesを設定します.
	 */
	public HttpServerCall(HttpCustomAnalysisParams custom, MimeTypes mimeTypes) {
		this.custom = custom;
		this.mimeTypes = mimeTypes;
	}

	/**
	 * NioServerCoreが生成された時に呼び出されます.
	 */
	@Override
	public void init() {
		LOG.info("### init HttpServerCall.");
	}

	/**
	 * NioServerCoreのstartThread処理が呼ばれた時に呼び出されます.
	 */
	@Override
	public void startThread() {
		LOG.info("### startThread HttpServerCall.");
	}

	/**
	 * NioServerCoreのstopThread処理が呼ばれた時に呼び出されます.
	 */
	@Override
	public void stopThread() {
		LOG.info("### stopThread HttpServerCall.");
	}

	/**
	 * nio開始処理.
	 *
	 * @return boolean [true]の場合、正常に処理されました.
	 */
	@Override
	public boolean startNio() {
		LOG.info("### started HttpServerCall.");
		return true;
	}

	/**
	 * nio終了処理.
	 */
	@Override
	public void endNio() {
		LOG.info("### end HttpServerCall.");
	}

	/**
	 * エラーハンドリング.
	 *
	 * @param e エラー用の例外オブジェクトを設定されます.
	 */
	@Override
	public void error(Throwable e) {
		if(LOG.isWarnEnabled()) {
			LOG.warn("### error", e);
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
		if(LOG.isTraceEnabled()) {
			LOG.trace("### accept: " + em.getRemoteAddress());
		}
		return true;
	}

	/**
	 * Nio要素を生成.
	 */
	@Override
	public NioElement createElement() {
		HttpElement ret = new HttpElement(HttpMode.Server, mimeTypes);
		return ret;
	}

	/**
	 * Http向けのデータ受信処理を実装.
	 * @param o ワーカースレッドNoに紐づくオブジェクトが設定されます.
	 * @param em 対象のNio要素が設定されます.
	 * @param rcvBin 受信されたバイナリが設定されます.
	 * @return boolean trueの場合、正常に処理されました.
	 */
	@Override
	public boolean receive(Object o, NioElement em, byte[] rcvBin)
		throws IOException {
		HttpRequest req;
		Response<?> res;
		Params params;
		RegisterComponent comp;
		ComponentType ctype;
		final byte[] tmpBuf = (byte[])o;
		final HttpElement hem = (HttpElement)em;
		while(true) {
			switch(hem.getState()) {
			// リクエストヘッダを受信中.
			case STATE_RECEIVING_HEADER:
				// リクエストオブジェクトを作成.
				if(HttpServerAnalysis.getRequest(hem, null, rcvBin)) {
					// リクエストオブジェクト作成完了.
					continue;
				}
				// 完了していないので、次の受信処理で行う.
				return true;
			// リクエストヘッダ受信完了.
			case STATE_END_RECV_HTTP_HEADER:
			// contentLengthに準じたBody受信.
			case STATE_RECV_BODY:
			// chunkedに準じたBody受信.
			case STATE_RECV_CHUNKED_BODY:
				// Body読み込み.
				if(HttpAnalysis.receiveBody(
					tmpBuf, hem, hem.getRequest().getContentLength(), rcvBin)) {
					// 受診処理が完了したので次の処理に移行.
					continue;
				}
				// body読み込みが完了してない場合はfalseが返却されるので、
				// 次の受信処理を引き続き行う.
				return true;
			// 受信完了.
			case STATE_END_RECV:
				// requestを取得.
				req = (HttpRequest)hem.getRequest();
				// methodがoptionかチェック.
				if(Method.OPTIONS.equals(req.getMethod())) {
					// Option送信.
					sendOptions(hem, req);
					return true;
				}
				try {
					// urlを取得.
					String url = req.getUrl();
					// urlを[/]でパース.
					String[] urls = ComponentManager.getUrls(url);
					// URLに対するコンテンツ取得.
					comp = Quina.router().get(url, urls);
					url = null;
					// コンポーネントが取得された場合.
					if(comp != null) {
						// レスポンスを取得.
						res = hem.getResponse();
						// Elementにレスポンスがない場合.
						if(res == null) {
							// コンポーネントタイプを取得.
							ctype = comp.getType();
							// このコンポーネントは同期コンポーネントの場合.
							if(ctype.isSync()) {
								res = new SyncResponse(hem, mimeTypes);
							// このコンポーネントはRESTful系の場合.
							} else if(ctype.isRESTful()) {
								res = new RESTfulResponse(hem, mimeTypes);
							// デフォルトレスポンス.
							} else {
								res = new DefaultResponse(hem, mimeTypes);
							}
							// レスポンスをセット.
							hem.setResponse(res);
						}
						// パラメータを取得.
						params = HttpAnalysis.convertParams(req, custom);
						// URLパラメータ条件が存在する場合.
						if(comp.isUrlParam()) {
							// パラメータが存在しない場合は生成.
							if(params == null) {
								params = new Params();
							}
							// URLパラメータを解析.
							comp.getUrlParam(params, urls);
						}
						urls = null;
						// パラメータが存在する場合はリクエストセット.
						if(params != null) {
							req.setParams(params);
						// パラメータが存在しない場合は空のパラメータをセット.
						} else {
							req.setParams(new Params(0));
						}
						// コンポーネント実行.
						comp.call(req.getMethod(), req, res);
					} else {
						// エラー404返却.
						sendError(404, req, defaultRespones(hem, mimeTypes), null);
					}
				} catch(QuinaException qe) {
					// QuinaExceptionの場合は、そのステータスを踏襲する.
					sendError(qe.getStatus(), req, defaultRespones(hem, mimeTypes), qe);
				} catch(Exception e) {
					// その他例外の場合は５００エラー.
					sendError(500, req, defaultRespones(hem, mimeTypes), e);
				}
				// 処理終了.
				return true;
			}
		}
	}

	// デフォルトのResponseを取得.
	private static final Response<?> defaultRespones(HttpElement em, MimeTypes mimeTypes) {
		Response<?> res = em.getResponse();
		if(res == null) {
			res = new DefaultResponse(em, mimeTypes);
			em.setResponse(res);
		}
		return res;
	}

	// Option送信.
	private static final void sendOptions(HttpElement em, HttpRequest req) {
		try {
			// optionのデータを取得.
			final NioSendData options = CreateResponseHeader.createOptionsHeader(true, false);
			// NioElementに送信データを登録.
			em.setSendData(options);
			// 送信開始.
			em.startWrite();
		} catch(Exception e) {
			// 例外の場合は要素をクローズして終了.
			try {
				em.close();
			} catch(Exception ee) {}
			try {
				req.close();
			} catch(Exception ee) {}
			if(e instanceof HttpException) {
				throw (HttpException)e;
			}
			throw new HttpException(e);
		}
	}

	// HttpErrorを送信.
	private static final void sendError(int state, Request req, Response<?> res, Throwable e) {
		// レスポンス情報をリセットして、デフォルトレスポンスに変換する.
		final Response<?> response;
		// 指定レスポンスがDefaultResponseの場合はリセット.
		if(res instanceof DefaultResponse) {
			response = (DefaultResponse)res;
			response.reset();
		// 指定レスポンスがDefaultResponseでない場合は作り直す.
		} else {
			response = new DefaultResponse(res);
		}
		// エラー実行.
		if(e == null) {
			Quina.router().getError()
				.call(state, req, response);
		} else {
			Quina.router().getError()
				.call(state, req, response, e);
		}
	}
}
