package quina.http.response;

import quina.http.HttpElement;
import quina.http.MimeTypes;

/**
 * 同期用のレスポンス.
 */
public class SyncResponse extends AbstractResponse<SyncResponse> {
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
