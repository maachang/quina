package quina.http.server;

import java.io.IOException;

import quina.http.CsMode;
import quina.http.HttpAnalysis;
import quina.http.HttpCustomAnalysisParams;
import quina.http.HttpElement;
import quina.http.MimeTypes;
import quina.logger.Log;
import quina.logger.LogFactory;
import quina.net.nio.tcp.NioElement;
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
		HttpElement ret = new HttpElement(CsMode.Server, mimeTypes);
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
	public final void execComponent(String url, HttpElement em) {
		HttpServerUtil.execComponent(url, em, mimeTypes, custom);
	}
}
