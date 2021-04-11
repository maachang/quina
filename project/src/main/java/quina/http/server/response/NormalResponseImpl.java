package quina.http.server.response;

import quina.component.ComponentType;
import quina.http.MimeTypes;
import quina.http.HttpElement;
import quina.http.Response;

/**
 * 標準的なレスポンス実装.
 */
public class NormalResponseImpl extends AbstractResponse<NormalResponse>
	implements NormalResponse {

	/***
	 * コンストラクタ.
	 * @param res レスポンスオブジェクトを設定します.
	 */
	@SuppressWarnings("rawtypes")
	public NormalResponseImpl(Response<?> res) {
		AbstractResponse r = (AbstractResponse)res;
		this.element = r.element;
		this.mimeTypes = r.mimeTypes;
	}

	/**
	 * コンストラクタ.
	 * @param element Http要素を設定します.
	 * @param mimeTypes MimeType群を設定します.
	 */
	public NormalResponseImpl(HttpElement element, MimeTypes mimeTypes) {
		this.element = element;
		this.mimeTypes = mimeTypes;
	}

	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	public ComponentType getComponentType() {
		return NormalResponse.super.getComponentType();
	}

	/**
	 * 新しいデフォルトのResponseを取得.
	 * @param response レスポンスを設定します.
	 * @return Response<?> 新しいレスポンスが返却されます.
	 */
	public static final Response<?> newResponse(Response<?> response) {
		final AbstractResponse<?> res = (AbstractResponse<?>)response;
		final HttpElement em = res.getElement();
		response = new NormalResponseImpl(em, res.getMimeTypes());
		em.setResponse(response);
		return response;
	}
}
