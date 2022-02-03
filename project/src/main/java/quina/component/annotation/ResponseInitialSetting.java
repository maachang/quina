package quina.component.annotation;

import quina.http.HttpCharset;
import quina.http.HttpStatus;
import quina.http.MediaType;
import quina.http.Response;

/**
 * レスポンス初期設定要素.
 * 
 * response関連のanntotationでComponentに設定された初期設定値を
 * 保管し、リクエスト毎にResponseに値を設定する要素です.
 */
public class ResponseInitialSetting {
	// Httpステータス情報.
	private HttpStatus status;
	// Httpステータスメッセージ.
	private String message;
	
	// ContentTypeのMimeType.
	private MediaType mimeType;
	// ContentTypeのCharset.
	private HttpCharset charset;
	
	// HttpHeader群.
	private String[] headers;
	
	// Gzipモード.
	private Boolean gzip;
	// Cacheモード.
	private Boolean cache;
	// CrossDomainモード.
	private Boolean cors;
	
	/**
	 * コンストラクタ.
	 * @param status ステータスAnnotationを設定.
	 * @param contentType ContentTypeAnnotationを設定.
	 * @param headers HeaderAnnotationの配列を設定.
	 * @param responseSwitch レスポンスモードAnnotationを設定.
	 * @param gzipSwitch レスポンスのGzipモードをAnnotation設定.
	 * @param cacheSwitch レスポンスのCacheモードをAnnotation設定.
	 * @param corsSwitch レスポンスのCrossドメインモードをAnnotation設定.
	 */
	protected ResponseInitialSetting(Status status, ContentType contentType,
		Header[] headers, ResponseSwitch responseSwitch, GzipSwitch gzipSwitch,
		CacheSwitch cacheSwitch, CorsSwitch corsSwitch) {
		// ステータスの設定.
		if(status != null) {
			this.status = status.status();
			if(status.message() != null && !status.message().isEmpty()) {
				this.message = status.message();
			}
		}
		// ContentTypeの設定.
		if(contentType != null) {
			this.mimeType = contentType.type();
			if(contentType.charset() != HttpCharset.NONE) {
				this.charset = contentType.charset();
			}
		}
		
		// Header群の設定.
		if(headers != null && headers.length > 0) {
			final int len = headers.length;
			final String[] hs = new String[len * 2];
			for(int i = 0, j = 0; i < len; i ++) {
				hs[j ++] = headers[i].key();
				hs[j ++] = headers[i].value();
			}
			this.headers = hs;
		}
		
		// ResponseSwitchの設定.
		if(responseSwitch != null) {
			this.gzip = responseSwitch.gzip().getMode();
			this.cache = responseSwitch.cache().getMode();
			this.cors = responseSwitch.cors().getMode();
		}
		// gzipSwitchの設定.
		if(gzipSwitch != null) {
			this.gzip = gzipSwitch.value();
		}
		// cacheSwitchの設定.
		if(cacheSwitch != null) {
			this.cache = cacheSwitch.value();
		}
		// corsSwitchの設定.
		if(corsSwitch != null) {
			this.cors = corsSwitch.value();
		}
	}
	
	/**
	 * レスポンス情報に情報設定.
	 * @param response 設定対象のレスポンス情報を設定します.
	 */
	public void setResponse(Response<?> response) {
		if(status != null) {
			if(message != null) {
				response.setStatus(status, message);
			} else {
				response.setStatus(status);
			}
		}
		if(mimeType != null) {
			if(charset != null) {
				response.setContentType(mimeType, charset);
			} else {
				response.setContentType(mimeType);
			}
		}
		if(headers != null) {
			final quina.http.Header h = response.getHeader();
			final int len = headers.length;
			for(int i = 0; i < len; i += 2) {
				h.put(headers[i], headers[i + 1]);
			}
		}
		if(gzip != null) {
			response.setGzip(gzip);
		}
		if(cache != null) {
			response.setCacheMode(cache);
		}
		if(cors != null) {
			response.setCors(cors);
		}
	}
}
