package quina.http.server;

import quina.Quina;
import quina.exception.CoreException;
import quina.http.HttpElement;
import quina.http.HttpException;
import quina.http.MimeTypes;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.NormalResponse;
import quina.http.server.response.NormalResponseImpl;
import quina.http.server.response.RESTfulResponseImpl;
import quina.http.server.response.SyncResponseImpl;
import quina.net.nio.tcp.NioSendData;

/**
 * HttpServerユーティリティ.
 */
public final class HttpServerUtil {
	private HttpServerUtil() {}

	/**
	 * デフォルトレスポンスに作り変える.
	 * @param res
	 * @return
	 */
	public static final Response<?> defaultResponse(Response<?> res) {
		return (Response<?>)NormalResponseImpl.newResponse(res);
	}

	/**
	 * デフォルトレスポンスに作り変える.
	 * @param em
	 * @param mimeTypes
	 * @return
	 */
	public static final Response<?> defaultResponse(HttpElement em, MimeTypes mimeTypes) {
		Response<?> res = new NormalResponseImpl(em, mimeTypes);
		em.setResponse(res);
		return res;
	}

	/**
	 * デフォルトレスポンスに作り変える.
	 * @param em
	 * @param mimeTypes
	 * @param res
	 * @return
	 */
	public static final Response<?> defaultResponse(
		HttpElement em, MimeTypes mimeTypes, Response<?> res) {
		if(res == null) {
			res = new NormalResponseImpl(em, mimeTypes);
			em.setResponse(res);
			return res;
		} else {
			return (Response<?>)NormalResponseImpl.newResponse(res);
		}
	}

	/**
	 * RESTfulレスポンスに作り変える.
	 * @param res
	 * @return
	 */
	public static final Response<?> RESTfulResponse(Response<?> res) {
		return (Response<?>)RESTfulResponseImpl.newResponse(res);
	}

	/**
	 * RESTfulレスポンスに作り変える.
	 * @param em
	 * @param mimeTypes
	 * @return
	 */
	public static final Response<?> RESTfulResponse(HttpElement em, MimeTypes mimeTypes) {
		Response<?> res = new RESTfulResponseImpl(em, mimeTypes);
		em.setResponse(res);
		return res;
	}

	/**
	 * RESTfulレスポンスに作り変える.
	 * @param em
	 * @param mimeTypes
	 * @param res
	 * @return
	 */
	public static final Response<?> RESTfulResponse(
		HttpElement em, MimeTypes mimeTypes, Response<?> res) {
		if(res == null) {
			res = new RESTfulResponseImpl(em, mimeTypes);
			em.setResponse(res);
			return res;
		} else {
			return (Response<?>)RESTfulResponseImpl.newResponse(res);
		}
	}

	/**
	 * 同期レスポンスに作り変える.
	 * @param res
	 * @return
	 */
	public static final Response<?> syncResponse(Response<?> res) {
		return (Response<?>)SyncResponseImpl.newResponse(res);
	}

	/**
	 * 同期レスポンスに作り変える.
	 * @param em
	 * @param mimeTypes
	 * @return
	 */
	public static final Response<?> syncResponse(HttpElement em, MimeTypes mimeTypes) {
		Response<?> res = new SyncResponseImpl(em, mimeTypes);
		em.setResponse(res);
		return res;
	}

	/**
	 * 同期レスポンスに作り変える.
	 * @param em
	 * @param mimeTypes
	 * @param res
	 * @return
	 */
	public static final Response<?> syncResponse(
		HttpElement em, MimeTypes mimeTypes, Response<?> res) {
		if(res == null) {
			res = new SyncResponseImpl(em, mimeTypes);
			em.setResponse(res);
			return res;
		} else {
			return (Response<?>)SyncResponseImpl.newResponse(res);
		}
	}

	/**
	 * HttpErrorを送信.
	 * @param req HttpRequestを設定します.
	 * @param res httpResponseを設置します.
	 */
	public static final void sendError(Request req, Response<?> res) {
		sendError(((AbstractResponse<?>)res).getComponentType().isRESTful(), req, res, null);
	}

	/**
	 * HttpErrorを送信.
	 * @param req HttpRequestを設定します.
	 * @param res httpResponseを設置します.
	 * @param e 例外を設定します.
	 */
	public static final void sendError(Request req, Response<?> res, Throwable e) {
		sendError(((AbstractResponse<?>)res).getComponentType().isRESTful(), req, res, e);
	}

	/**
	 * HttpErrorを送信.
	 * @param json [true]の場合はjson形式でエラー返却します.
	 * @param req HttpRequestを設定します.
	 * @param res httpResponseを設置します.
	 * @param e 例外を設定します.
	 */
	public static final void sendError(boolean json, Request req, Response<?> res, Throwable e) {
		// json返却の場合.
		if(json) {
			// JSON返却条件を設定.
			res.setContentType("application/json");
		} else {
			// HTML返却条件を設定.
			res.setContentType("text/html");
		}
		// ResponseがNormalResponseでない場合は変換.
		if(!(res instanceof NormalResponse)) {
			res = defaultResponse(res);
		}
		// エラー実行.
		if(e == null) {
			// ステータスが４００未満の場合.
			if(res.getStatusNo() < 400) {
				// エラー５００をセット.
				res.setStatus(500);
			}
			// エラー出力.
			Quina.router().getError()
				.call(res.getStatusNo(), json, req, res);
		} else {
			// Nio例外の場合.
			if(e instanceof CoreException) {
				CoreException core = (CoreException)e;
				res.setStatus(core.getStatus(), core.getMessage());
			// それ以外の例外の場合.
			} else {
				res.setStatus(500, e.getMessage());
			}
			Quina.router().getError()
				.call(res.getStatusNo(), json, req, (NormalResponse)res, e);
		}
	}

	/**
	 * Option送信.
	 * @param em
	 * @param req
	 */
	public static final void sendOptions(HttpElement em, HttpServerRequest req) {
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
}
