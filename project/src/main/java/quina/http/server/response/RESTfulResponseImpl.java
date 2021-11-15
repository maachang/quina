package quina.http.server.response;

import quina.component.ComponentType;
import quina.exception.QuinaException;
import quina.http.HttpElement;
import quina.http.Response;
import quina.http.server.furnishing.AbstractBaseSendResponse;

/**
 * RESTful用のレスポンス実装.
 */
public class RESTfulResponseImpl
	extends AbstractResponse<RESTfulResponse>
	implements RESTfulResponse,
		AbstractBaseSendResponse<RESTfulResponse>{

	/**
	 * コンストラクタ.
	 */
	protected RESTfulResponseImpl() {}
	
	/***
	 * コンストラクタ.
	 * @param response レスポンスオブジェクトを設定します.
	 */
	public RESTfulResponseImpl(Response<?> response) {
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
	 * @param componentType コンポーネントタイプを設定します.
	 */
	public RESTfulResponseImpl(HttpElement element, ComponentType componentType) {
		if(element == null) {
			throw new QuinaException("The specified argument is null.");
		}
		this.element = element;
		this.setSrcComponentType(componentType);
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
		return RESTfulResponse.super.getComponentType();
	}
}
