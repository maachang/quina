package quina.http.server.response;

import quina.component.ComponentType;
import quina.exception.QuinaException;
import quina.http.HttpElement;
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
	 * @param response レスポンスオブジェクトを設定します.
	 */
	public AnyResponseImpl(Response<?> response) {
		if(response == null) {
			throw new QuinaException("The specified response is null.");
		}
		final AbstractResponse<?> res = (AbstractResponse<?>)response;
		super.setting(res);
		super.getElement().setResponse(this);
	}

	/**
	 * コンストラクタ.
	 * @param element Http要素を設定します.
	 */
	public AnyResponseImpl(HttpElement element) {
		if(element == null) {
			throw new QuinaException("The specified argument is null.");
		}
		this.element = element;
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
}
