package quina.http.server.response;

import quina.component.ComponentType;
import quina.exception.QuinaException;
import quina.http.HttpElement;
import quina.http.MimeTypes;
import quina.http.Response;
import quina.http.server.furnishing.AbstractBaseSendResponse;

/**
 * 標準的なレスポンス実装.
 */
public class AnyResponseImpl
	extends AbstractResponse<AnyResponse>
	implements AnyResponse,
		AbstractBaseSendResponse<AnyResponse> {
	
	/**
	 * コンストラクタ.
	 */
	protected AnyResponseImpl() {}
	
	/***
	 * コンストラクタ.
	 * @param res レスポンスオブジェクトを設定します.
	 */
	public AnyResponseImpl(Response<?> res) {
		newResponse(res);
	}

	/**
	 * コンストラクタ.
	 * @param element Http要素を設定します.
	 * @param mimeTypes MimeType群を設定します.
	 */
	public AnyResponseImpl(
		HttpElement element, MimeTypes mimeTypes) {
		if(element == null || mimeTypes == null) {
			throw new QuinaException("The specified argument is null.");
		}
		this.element = element;
		this.mimeTypes = mimeTypes;
	}
	
	/**
	 * レスポンスオブジェクトを設定します.
	 * @return Response<?> HttpResponseが返却されます.
	 */
	@Override
	public Response<?> _getResponse() {
		return this;
	}

	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	public ComponentType getComponentType() {
		return AnyResponse.super.getComponentType();
	}

	/**
	 * 新しいデフォルトのResponseを取得.
	 * @param response レスポンスを設定します.
	 * @return Response<?> 新しいレスポンスが返却されます.
	 */
	public static final Response<?> newResponse(Response<?> response) {
		if(response == null) {
			throw new QuinaException("The specified response is null.");
		}
		final AbstractResponse<?> res = (AbstractResponse<?>)response;
		final HttpElement em = res.getElement();
		AbstractResponse<?> ret = new AnyResponseImpl(
			em, res.getMimeTypes());
		ret.setting(res);
		em.setResponse(ret);
		return ret;
	}
}
