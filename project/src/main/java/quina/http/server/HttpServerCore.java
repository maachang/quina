package quina.http.server;

import quina.Quina;
import quina.component.ComponentConstants;
import quina.component.ComponentManager;
import quina.component.ComponentType;
import quina.component.RegisterComponent;
import quina.component.error.ErrorComponent;
import quina.exception.CoreException;
import quina.exception.QuinaException;
import quina.http.HttpAnalysis;
import quina.http.HttpCustomAnalysisParams;
import quina.http.HttpElement;
import quina.http.HttpEmptySendResponse;
import quina.http.HttpStatus;
import quina.http.Method;
import quina.http.Params;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.AnyResponseImpl;
import quina.http.server.response.RESTfulResponseImpl;
import quina.http.server.response.ResponseUtil;
import quina.http.server.response.SyncResponseImpl;
import quina.logger.Log;
import quina.logger.LogFactory;
import quina.net.nio.tcp.NioSendData;
import quina.net.nio.tcp.NioUtil;
import quina.validate.Validation;

/**
 * HttpServerコンポーネント実行処理.
 */
public final class HttpServerCore {
	private HttpServerCore() {}
	
	// ログ出力.
	private static final Log LOG = LogFactory.getInstance().get();
	
	/**
	 * URLを指定してコンポーネントを実行.
	 * @param em Httpy要素を設定します.
	 * @param url URLを設定します.
	 * @param custom パラメータ解析のカスタム条件を設定します.
	 */
	public static final void execComponent(
		String url, HttpElement em,
		HttpCustomAnalysisParams custom) {
		// 接続が切断されてる場合は処理しない.
		if(!em.isConnection()) {
			return;
		}
		String[] urls;
		Params params;
		RegisterComponent comp;
		Validation validation;
		// requestを取得.
		HttpServerRequest req = (HttpServerRequest)em.getRequest();
		// 初回の場合レスポンスはnull.
		Response<?> res = em.getResponse();
		// methodがoptionかチェック.
		if(Method.OPTIONS.equals(req.getMethod())) {
			// Option送信.
			HttpServerCore.sendOptions(em, req);
			return;
		}
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
			comp = Quina.get().getRouter().get(url, urls, req.getMethod());
			url = null;
			// コンポーネントが取得できない場合.
			if(comp == null) {
				// エラー404返却.
				if(res == null) {
					res = new AnyResponseImpl(em, null);
				}
				res.setStatus(404);
				HttpServerCore.sendError(req, res, null);
				return;
			}
			// コンポーネントタイプを取得.
			final ComponentType ctype = comp.getType();
			// Elementにレスポンスがない場合.
			if(res == null) {
				switch(ctype.getAttributeType()) {
				// このコンポーネントは同期コンポーネントの場合.
				case ComponentConstants.ATTRIBUTE_SYNC:
					res = new SyncResponseImpl(em, ctype);
					break;
				// このコンポーネントはRESTful系の場合.
				case ComponentConstants.ATTRIBUTE_RESTFUL:
					res = new RESTfulResponseImpl(em, ctype);
					break;
				// Anyレスポンス.
				default:
					res = new AnyResponseImpl(em, ctype);
				}
				// レスポンスをセット.
				em.setResponse(res);
			}
			// HttpServerContextが作成可能な場合.
			if(!HttpServerContext.isCreate(em)) {
				// ここで生成できない場合は不具合か
				// 何らかの理由でコネクションが閉じてる
				// 場合があります.
				throw new QuinaException(
					"Failed to create HttpContext.");
			}
			
			// HttpServerContextが登録されてない場合.
			if(HttpServerContext.get() == null) {
				// HttpServerContextに登録.
				HttpServerContext.create(em);
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
			comp.call(req, res);
		} catch(HttpEmptySendResponse her) {
			// NioElementが閉じられてる場合は処理しない.
			if(!req.isConnection()) {
				return;
			}
			// HTTPレスポンスステータスが定義されている場合.
			if(her.getStatus() != 0) {
				// 空のレスポンス返却用例外に
				// 有効なHTTPステータスやメッセージが
				// 設定されている場合.
				res.setStatus(her.getStatus());
				res.setMessage(her.getMessage());
			}
			// 空のBodyレスポンスを返却.
			try {
				ResponseUtil.send((AbstractResponse<?>)res);
			} catch(Throwable t) {
				try {
					em.close();
				} catch(Exception e) {}
			}
				
		} catch(Throwable e) {
			// ワーニング以上のログ通知が認められてる場合.
			if(LOG.isWarnEnabled()) {
				int status = -1;
				boolean warnFlag = false;
				boolean errorFlag = false;
				// CoreExceptionで、ステータスが５００以下の場合は
				// エラー表示なし.
				if(e instanceof CoreException) {
					if((status = ((CoreException)e).getStatus()) >= 500) {
						errorFlag = true;
					} else {
						warnFlag = true;
					}
				// それ以外の例外の場合はエラー表示.
				} else {
					errorFlag = true;
				}
				// エラー表示の場合.
				if(errorFlag) {
					if(status >= 0) {
						LOG.error("# error (status: "
							+ status + " url: \"" + req.getUrl() + "\").", e);
					} else {
						LOG.error("# error (url: \"" + req.getUrl() + "\").", e);
					}
				// ワーニング表示の場合.
				} else if(warnFlag) {
					if(status >= 0) {
						LOG.warn("# warning (status: "
							+ status + " url: \"" + req.getUrl() + "\").");
					} else {
						LOG.warn("# warning (url: \"" + req.getUrl() + "\").");
					}
				}
			}
			// エラー返却.
			try {
				HttpServerCore.sendError(req, res, e);
			} catch(Throwable t) {
				// エラー返却失敗の場合は警告ーログを出力.
				if(LOG.isWarnEnabled()) {
					LOG.warn("Failed to send the error.", t);
				}
				try {
					em.close();
				} catch(Exception ee) {}
			}
		}
	}
	
	/**
	 * HttpErrorを送信.
	 * @param em 対象のHTTP要素を設定します.
	 */
	public static final void sendError(HttpElement em) {
		sendError(em.getRequest(), em.getResponse());
	}
	
	/**
	 * HttpErrorを送信.
	 * @param status 対象のHTTPステータスを設定します.
	 * @param em 対象のHTTP要素を設定します.
	 */
	public static final void sendError(
		int status, HttpElement em) {
		em.getResponse().setStatus(status);
		sendError(em.getRequest(), em.getResponse());
	}
	
	/**
	 * HttpErrorを送信.
	 * @param status 対象のHTTPステータスを設定します.
	 * @param em 対象のHTTP要素を設定します.
	 */
	public static final void sendError(
		HttpStatus status, HttpElement em) {
		em.getResponse().setStatus(status);
		sendError(em.getRequest(), em.getResponse());
	}
	
	/**
	 * HttpErrorを送信.
	 * @param status 対象のHTTPステータスを設定します.
	 * @param message 対象のHTTPステータスメッセージを設定します.
	 * @param em 対象のHTTP要素を設定します.
	 */
	public static final void sendError(
		int status, String message, HttpElement em) {
		em.getResponse().setStatus(status, message);
		sendError(em.getRequest(), em.getResponse());
	}
	
	/**
	 * HttpErrorを送信.
	 * @param status 対象のHTTPステータスを設定します.
	 * @param message 対象のHTTPステータスメッセージを設定します.
	 * @param em 対象のHTTP要素を設定します.
	 */
	public static final void sendError(
		HttpStatus status, String message, HttpElement em) {
		em.getResponse().setStatus(status, message);
		sendError(em.getRequest(), em.getResponse());
	}

	/**
	 * HttpErrorを送信.
	 * @param em 対象のHTTP要素を設定します.
	 * @param e 例外を設定します.
	 */
	public static final void sendError(
		HttpElement em, Throwable e) {
		sendError(em.getRequest(), em.getResponse(), e);
	}
	
	/**
	 * HttpErrorを送信.
	 * @param status 対象のHTTPステータスを設定します.
	 * @param em 対象のHTTP要素を設定します.
	 * @param e 例外を設定します.
	 */
	public static final void sendError(
		int status, HttpElement em, Throwable e) {
		em.getResponse().setStatus(status);
		sendError(em.getRequest(), em.getResponse(), e);
	}
	
	/**
	 * HttpErrorを送信.
	 * @param status 対象のHTTPステータスを設定します.
	 * @param em 対象のHTTP要素を設定します.
	 * @param e 例外を設定します.
	 */
	public static final void sendError(
		HttpStatus status, HttpElement em, Throwable e) {
		em.getResponse().setStatus(status);
		sendError(em.getRequest(), em.getResponse(), e);
	}
	
	/**
	 * HttpErrorを送信.
	 * @param status 対象のHTTPステータスを設定します.
	 * @param message 対象のHTTPステータスメッセージを設定します.
	 * @param em 対象のHTTP要素を設定します.
	 * @param e 例外を設定します.
	 */
	public static final void sendError(
		int status, String message, HttpElement em, Throwable e) {
		em.getResponse().setStatus(status, message);
		sendError(em.getRequest(), em.getResponse(), e);
	}
	
	/**
	 * HttpErrorを送信.
	 * @param status 対象のHTTPステータスを設定します.
	 * @param message 対象のHTTPステータスメッセージを設定します.
	 * @param em 対象のHTTP要素を設定します.
	 * @param e 例外を設定します.
	 */
	public static final void sendError(
		HttpStatus status, String message, HttpElement em,
		Throwable e) {
		em.getResponse().setStatus(status, message);
		sendError(em.getRequest(), em.getResponse(), e);
	}
	
	/**
	 * HttpErrorを送信.
	 * @param req HttpRequestを設定します.
	 * @param res httpResponseを設置します.
	 */
	public static final void sendError(
		Request req, Response<?> res) {
		sendError(req, res, null);
	}
	
	/**
	 * HttpErrorを送信.
	 * @param req HttpRequestを設定します.
	 * @param res httpResponseを設置します.
	 * @param e 例外を設定します.
	 */
	public static final void sendError(
		Request req, Response<?> res, Throwable e) {
		// NioElementが閉じられてる場合は処理しない.
		if(!req.isConnection()) {
			return;
		}
		// エラーコンポーネントを取得.
		ErrorComponent component =
			Quina.get().getRouter().getError(res.getStatusNo(), req.getUrl());
		// エラー実行.
		if(e == null) {
			// ステータスが４００未満の場合.
			if(res.getStatusNo() < 400) {
				// エラー５００をセット.
				res.setStatus(500);
			}
			// エラー出力.
			component.call(res.getStatusNo(), req, res);
		} else {
			// エラーメッセージを取得.
			String message = e.getMessage();
			// エラーメッセージが存在する場合.
			if(message != null) {
				// 余分な文字列は削除する.
				message = getErrorMessage(message);
				// Core例外の場合.
				if(e instanceof CoreException) {
					CoreException core = (CoreException)e;
					res.setStatus(core.getStatus(), message);
				// それ以外の例外の場合.
				} else {
					res.setStatus(500, message);
				}
			// エラーメッセージが存在しない場合.
			} else {
				// Core例外の場合.
				if(e instanceof CoreException) {
					res.setStatus(((CoreException)e).getStatus());
				// それ以外の例外の場合.
				} else {
					res.setStatus(500);
				}
			}
			// エラー出力.
			component.call(res.getStatusNo(), req, res, e);
		}
	}
	
	// 例外メッセージをhttpステータスメッセージに変換する.
	private static final String getErrorMessage(String message) {
		char c;
		StringBuilder buf = new StringBuilder();
		final int len = message.length();
		for(int i = 0; i < len; i ++) {
			c = message.charAt(i);
			if(c == '\"' || c =='\'' || c == '\r') {
				buf.append("");
			} else if(c == '\n' || c == '\t') {
				buf.append(" ");
			} else if(c == ':') {
				buf.append("");
			} else {
				buf.append(c);
			}
		}
		return buf.toString();
	}

	/**
	 * Option送信.
	 * @param em
	 * @param req
	 */
	public static final void sendOptions(HttpElement em, Request req) {
		// 接続が切断されてる場合は処理しない.
		if(!em.isConnection()) {
			return;
		}
		try {
			// optionのデータを取得.
			final NioSendData options = CreateResponseHeader.createOptionsHeader(
				true, false);
			// NioElementに送信データを登録.
			em.setSendData(options);
			// 送信開始.
			em.startWrite();
		} catch(Exception e) {
			// 例外の場合は要素をクローズして終了.
			try {
				NioUtil.closeNioElement(em);
			} catch(Exception ee) {}
			try {
				req.close();
			} catch(Exception ee) {}
		}
	}
}
