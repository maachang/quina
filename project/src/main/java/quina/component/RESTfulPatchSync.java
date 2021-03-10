package quina.component;

import quina.http.HttpException;
import quina.http.Method;
import quina.http.Params;
import quina.http.Request;
import quina.http.Response;
import quina.http.response.ResponseUtil;
import quina.http.response.SyncResponse;

/**
 * [同期]RESTfulzメソッドPatch専用のComponent.
 */
public abstract class RESTfulPatchSync extends AbstractValidationComponent<RESTfulPatchSync>
	implements Component {
	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	@Override
	public ComponentType getType() {
		return ComponentType.RESTfulPatchSync;
	}

	/**
	 * コンポーネント実行処理.
	 * @param method HTTPメソッドが設定されます.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 */
	@Override
	public void call(Method method, Request req, Response<?> res) {
		if(method != Method.PATCH) {
			throw new HttpException(405,
				"The specified method: " + method + " cannot be used for this URL.");
		}
		ResponseUtil.sendJSON((SyncResponse)res,
			patch(req, (SyncResponse)res, execute(req)));
	}

	/**
	 * PATCHメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res SyncResponseが設定されます.
	 * @param params パラメータが設定されます.
	 * @return Object 返却するRESTfulオブジェクトを設定します.
	 */
	public abstract Object patch(Request req, SyncResponse res, Params params);
}
