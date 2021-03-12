package quina.http.server;

import java.io.IOException;

import quina.Quina;
import quina.QuinaException;
import quina.component.ComponentConstants;
import quina.component.ComponentManager;
import quina.component.ComponentType;
import quina.component.RegisterComponent;
import quina.http.HttpAnalysis;
import quina.http.HttpCustomAnalysisParams;
import quina.http.HttpElement;
import quina.http.HttpException;
import quina.http.HttpMode;
import quina.http.Method;
import quina.http.MimeTypes;
import quina.http.Params;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.DefaultResponse;
import quina.http.server.response.Forward;
import quina.http.server.response.RESTfulResponse;
import quina.http.server.response.Redirect;
import quina.http.server.response.ResponseUtil;
import quina.http.server.response.SyncResponse;
import quina.logger.Log;
import quina.logger.LogFactory;
import quina.net.nio.tcp.NioElement;
import quina.net.nio.tcp.NioSendData;
import quina.net.nio.tcp.server.NioServerCall;
import quina.validate.Validation;

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

	// エラー４０４等のレスポンスをJson返却するモード.
	private boolean defaultResultJsonMode;

	/**
	 * コンストラクタ.
	 * @param custom HttpRequestでのBodyの解釈をするオブジェクトを設定します.
	 * @param mimeTypes mimeTypesを設定します.
	 * @param defaultResultJsonMode エラー４０４等のレスポンスを
	 *                              Json返却するモードを設定します.
	 */
	public HttpServerCall(HttpCustomAnalysisParams custom, MimeTypes mimeTypes,
		boolean defaultResultJsonMode) {
		this.custom = custom;
		this.mimeTypes = mimeTypes;
		this.defaultResultJsonMode = defaultResultJsonMode;
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
	public boolean receive(Object wkObject, NioElement em, byte[] rcvBin)
		throws IOException {
		final byte[] tmpBuf = (byte[])wkObject;
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
				execComponent(null, (HttpElement)em);
				// 処理終了.
				return true;
			}
		}
	}

	/**
	 * URLを指定してコンポーネントを実行.
	 * @param em
	 * @param url
	 */
	private final void execComponent(String url, HttpElement em) {
		String[] urls;
		Params params;
		RegisterComponent comp;
		Validation validation;
		// requestを取得.
		HttpServerRequest req = (HttpServerRequest)em.getRequest();
		// レスポンスはnull.
		Response<?> res = em.getResponse();
		// methodがoptionかチェック.
		if(Method.OPTIONS.equals(req.getMethod())) {
			// Option送信.
			sendOptions(em, req);
		}
		// エラー４０４等のレスポンスをJson返却するモード.
		boolean json = defaultResultJsonMode;
		try {
			// urlが指定されてない場合はURLを取得.
			if(url == null) {
				url = req.getUrl();
			// URLが存在する場合は作成されたRequestのURLを変更してコピー処理.
			} else {
				req = new HttpServerRequest(req, url);
				em.setRequest(req);
			}
			// urlを[/]でパース.
			urls = ComponentManager.getUrls(url);
			// URLに対するコンテンツ取得.
			comp = Quina.router().get(url, urls);
			url = null;
			// コンポーネントが取得された場合.
			if(comp != null) {
				// コンポーネントタイプを取得.
				final ComponentType ctype = comp.getType();
				// RESTfulか取得.
				json = ctype.isRESTful();
				// Elementにレスポンスがない場合.
				if(res == null) {
					switch(ctype.getAttributeType()) {
					// このコンポーネントは同期コンポーネントの場合.
					case ComponentConstants.ATTRIBUTE_SYNC:
						res = new SyncResponse(em, mimeTypes);
					// このコンポーネントはRESTful系の場合.
					case ComponentConstants.ATTRIBUTE_RESTFUL:
						res = new RESTfulResponse(em, mimeTypes);
					// デフォルトレスポンス.
					default:
						res = new DefaultResponse(em, mimeTypes);
					}
					// レスポンスをセット.
					em.setResponse(res);
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
				// validationが存在する場合はValidation処理.
				if((validation = comp.getValidation()) != null) {
					// validation実行.
					params = validation.execute(req, req.getParams());
					// 新しく生成されたパラメータを再セット.
					req.setParams(params);
				}
				// コンポーネント実行.
				comp.call(req.getMethod(), req, res);
			} else {
				// エラー404返却.
				sendError(404, json, req, defaultRespones(em, mimeTypes), null);
			}
		} catch(Forward fwd) {
			// フォワード処理.
			sendForward(fwd, em);
		} catch(Redirect red) {
			// リダイレクト処理.
			sendRedirect(red, defaultRespones(em, mimeTypes));
		} catch(QuinaException qe) {
			// QuinaExceptionの場合は、そのステータスを踏襲する.
			sendError(qe.getStatus(), json, req, defaultRespones(em, mimeTypes), qe);
			// qe.printStackTrace();
		} catch(Exception e) {
			// その他例外の場合は５００エラー.
			sendError(500, json, req, defaultRespones(em, mimeTypes), e);
			//e.printStackTrace();
		}
	}

	// フォワード処理.
	private final void sendForward(Forward forward, HttpElement em) {
		// URLを指定して再実行.
		execComponent(forward.getPath(), em);
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
	private static final void sendOptions(HttpElement em, HttpServerRequest req) {
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

	// リダイレクト送信.
	private static final void sendRedirect(Redirect redirect, Response<?> res) {
		// HTTPステータスを設定.
		res.setStatus(redirect.getHttpStatus());
		// リダイレクト先を設定.
		res.getHeader().put("Location", redirect.getLocation());
		// 0バイトデータを設定.
		ResponseUtil.send((AbstractResponse<?>)res);
	}

	// HttpErrorを送信.
	private static final void sendError(
		int state, boolean json, Request req, Response<?> res, Throwable e) {
		// json返却の場合.
		if(json) {
			// JSON返却条件を設定.
			res.setContentType("application/json");
		} else {
			// HTML返却条件を設定.
			res.setContentType("text/html");
		}
		// エラー実行.
		if(e == null) {
			Quina.router().getError()
				.call(state, json, req, res);
		} else {
			Quina.router().getError()
				.call(state, json, req, res, e);
		}
	}
}
