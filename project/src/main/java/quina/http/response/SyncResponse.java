package quina.http.response;

import quina.http.HttpElement;
import quina.http.MimeTypes;
import quina.http.Response;

/**
 * 同期用のレスポンス.
 */
public class SyncResponse extends AbstractResponse<SyncResponse> {
	/***
	 * コンストラクタ.
	 * @param res レスポンスオブジェクトを設定します.
	 */
	@SuppressWarnings("rawtypes")
	public SyncResponse(Response<?> res) {
		AbstractResponse r = (AbstractResponse)res;
		this.element = r.element;
		this.mimeTypes = r.mimeTypes;
	}

	/**
	 * コンストラクタ.
	 * @param element Http要素を設定します.
	 * @param mimeTypes MimeType群を設定します.
	 */
	public SyncResponse(HttpElement element, MimeTypes mimeTypes) {
		this.element = element;
		this.mimeTypes = mimeTypes;
	}
}
