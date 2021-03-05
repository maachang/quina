package quina.component;

import quina.http.Method;
import quina.http.Request;
import quina.http.Response;
import quina.http.response.ResponseUtil;
import quina.http.response.SyncResponse;

public interface ComponentSync extends Component {
	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	@Override
	default ComponentType getType() {
		return ComponentType.Sync;
	}

	/**
	 * コンポーネント実行処理.
	 * @param method HTTPメソッドが設定されます.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 */
	@Override
	default void call(Method method, Request req, Response<?> res) {
		Object ret = call(req, (SyncResponse)res);
		if(ret instanceof byte[]) {
			ResponseUtil.send((SyncResponse)res, (byte[])ret);
		} else if(ret instanceof String) {
			ResponseUtil.send((SyncResponse)res, (String)ret);
		} else {
			ResponseUtil.sendJSON((SyncResponse)res, ret);
		}
	}

	/**
	 * GETメソッド用実行.
	 * @param req HttpRequestが設定されます.
	 * @param res SyncResponseが設定されます.
	 * @return Object 返却するRESTfulオブジェクトを設定します.
	 */
	public Object call(Request req, SyncResponse res);

}
