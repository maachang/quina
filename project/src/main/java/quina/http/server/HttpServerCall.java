package quina.http.server;

import java.io.IOException;

import quina.Quina;
import quina.QuinaException;
import quina.component.ComponentManager;
import quina.component.RegisterComponent;
import quina.http.HttpAnalysis;
import quina.http.HttpCustomPostParams;
import quina.http.HttpElement;
import quina.http.HttpMode;
import quina.http.MimeTypes;
import quina.http.Params;
import quina.http.Request;
import quina.http.Response;
import quina.net.nio.tcp.NioElement;
import quina.net.nio.tcp.server.NioServerCall;

/**
 * Httpサーバコール.
 */
public class HttpServerCall extends NioServerCall {
	// カスタムなPostBody解析.
	private HttpCustomPostParams custom = null;
	// MimeTypes.
	private MimeTypes mimeTypes = null;

	/**
	 * コンストラクタ.
	 * @param custom HttpRequestでのBodyの解釈をするオブジェクトを設定します.
	 * @param mimeTypes mimeTypesを設定します.
	 */
	public HttpServerCall(HttpCustomPostParams custom, MimeTypes mimeTypes) {
		this.custom = custom;
		this.mimeTypes = mimeTypes;
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
		HttpServerRequest req;
		HttpServerResponse res;
		Params params;
		RegisterComponent comp;
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
				req = (HttpServerRequest)hem.getRequest();
				res = (HttpServerResponse)hem.getResponse();
				if(res == null) {
					res = new HttpServerResponse(hem, mimeTypes);
					hem.setResponse(res);
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
						}
						// コンポーネント実行.
						comp.call(req.getMethod(), req, res);
					} else {
						// エラー404返却.
						sendError(404, req, res, null);
					}
				} catch(QuinaException qe) {
					sendError(qe.getStatus(), req, res, qe);
				} catch(Exception e) {
					sendError(500, req, res, e);
				}
				// 処理終了.
				return true;
			}
		}
	}

	// HttpErrorを送信.
	private static final void sendError(int state, Request req, Response res, Throwable e) {
		// エラー実行.
		Quina.router().getError()
			.call(state, req, res, e);
	}
}
