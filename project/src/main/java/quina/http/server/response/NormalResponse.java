package quina.http.server.response;

import quina.component.ComponentType;
import quina.http.HttpElement;
import quina.http.MimeTypes;
import quina.http.Response;
import quina.http.furnishing.EmptySendResponse;
import quina.http.furnishing.ErrorSendResponse;
import quina.http.furnishing.FileSendResponse;
import quina.http.furnishing.InputStreamSendResponse;
import quina.http.furnishing.JsonSendResponse;
import quina.http.furnishing.MemorySendResponse;

/**
 * 標準的なレスポンス.
 */
public class NormalResponse extends AbstractResponse<NormalResponse>
	implements EmptySendResponse<NormalResponse>, MemorySendResponse<NormalResponse>,
		JsonSendResponse<NormalResponse>, InputStreamSendResponse<NormalResponse>,
		FileSendResponse<NormalResponse>, ErrorSendResponse<NormalResponse> {
	/***
	 * コンストラクタ.
	 * @param res レスポンスオブジェクトを設定します.
	 */
	@SuppressWarnings("rawtypes")
	public NormalResponse(Response<?> res) {
		AbstractResponse r = (AbstractResponse)res;

		this.element = r.element;
		this.mimeTypes = r.mimeTypes;
	}

	/**
	 * コンストラクタ.
	 * @param element Http要素を設定します.
	 * @param mimeTypes MimeType群を設定します.
	 */
	public NormalResponse(HttpElement element, MimeTypes mimeTypes) {
		this.element = element;
		this.mimeTypes = mimeTypes;
	}

	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	public ComponentType getComponentType() {
		return ComponentType.NORMAL;
	}

	/**
	 * 新しいデフォルトのResponseを取得.
	 * @param response レスポンスを設定します.
	 * @return Response<?> 新しいレスポンスが返却されます.
	 */
	public static final Response<?> newResponse(Response<?> response) {
		final AbstractResponse<?> res = (AbstractResponse<?>)response;
		final HttpElement em = res.getElement();
		response = new NormalResponse(em, res.getMimeTypes());
		em.setResponse(response);
		return response;
	}
}
