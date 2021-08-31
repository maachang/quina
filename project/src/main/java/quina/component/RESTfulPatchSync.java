package quina.component;

import quina.http.HttpException;
import quina.http.Method;
import quina.http.Params;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.ResponseUtil;
import quina.http.server.response.SyncResponse;

/**
 * [同期]RESTfulメソッドPatch専用のComponent.
 */
@FunctionalInterface
public interface RESTfulPatchSync extends Component {
	/**
	 * 送信なしを示すオブジェクト.
	 */
	public static final Object NOSEND = SyncResponse.NOSEND;

	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	@Override
	default ComponentType getType() {
		return ComponentType.RESTfulPatchSync;
	}

	/**
	 * 対応HTTPメソッド定義を取得.
	 * @return int このコンポーネントが対応するHTTPメソッド定義が返却されます.
	 */
	@Override
	default int getMethod() {
		return ComponentConstants.HTTP_METHOD_PATCH;
	}

	/**
	 * コンポーネント実行処理.
	 * @param method HTTPメソッドが設定されます.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 */
	@Override
	default void call(Method method, Request req, Response<?> res) {
		if(method != Method.PATCH) {
			throw new HttpException(405,
				"The specified method: " + method + " cannot be used for this URL.");
		}
		final Object o = patch(req, (SyncResponse)res, req.getParams());
		// 送信なしを示す場合.
		if(NOSEND == o) {
			return;
		// 返却内容が存在する場合.
		} else {
			ResponseUtil.sendJSON((AbstractResponse<?>)res, o);
		}
	}

	/**
	 * PATCHメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res SyncResponseが設定されます.
	 * @param params パラメータが設定されます.
	 * @return Object 返却するRESTfulオブジェクトを設定します.
	 */
	public Object patch(Request req, SyncResponse res, Params params);
}
