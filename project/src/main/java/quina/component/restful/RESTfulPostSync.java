package quina.component.restful;

import quina.component.Component;
import quina.component.ComponentConstants;
import quina.component.ComponentType;
import quina.http.Method;
import quina.http.Params;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.ResponseUtil;
import quina.http.server.response.SyncResponse;
import quina.http.server.response.SyncResponseImpl;

/**
 * [同期]RESTfulメソッドPost専用のComponent.
 */
@FunctionalInterface
public interface RESTfulPostSync extends Component {
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
		return ComponentType.RESTfulPostSync;
	}

	/**
	 * 対応HTTPメソッド定義を取得.
	 * @return int このコンポーネントが対応するHTTPメソッド定義が返却されます.
	 */
	@Override
	default int getMethod() {
		return ComponentConstants.HTTP_METHOD_POST;
	}

	/**
	 * コンポーネント実行処理.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 */
	@Override
	default void call(Request req, Response<?> res) {
		if(req.getMethod() != Method.POST) {
			ComponentConstants.httpError405(req);
		}
		// ResponseがSyncResponseでない場合は変換.
		if(!(res instanceof SyncResponse)) {
			res = new SyncResponseImpl(res);
		}
		final Object o = post(req, (SyncResponse)res, req.getParams());
		// 送信なしを示す場合.
		if(NOSEND == o) {
			return;
		// 返却内容が存在する場合.
		} else {
			ResponseUtil.sendJSON((AbstractResponse<?>)res, o);
		}
	}

	/**
	 * POSTメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res SyncResponseが設定されます.
	 * @param params パラメータが設定されます.
	 * @return Object 返却するRESTfulオブジェクトを設定します.
	 */
	public Object post(Request req, SyncResponse res, Params params);
}
