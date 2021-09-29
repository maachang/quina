package quina.component.sync;

import quina.component.Component;
import quina.component.ComponentConstants;
import quina.component.ComponentType;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.ResponseUtil;
import quina.http.server.response.SyncResponse;
import quina.http.server.response.SyncResponseImpl;

/**
 * [同期]コンポーネント.
 */
@FunctionalInterface
public interface SyncComponent extends Component {
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
		return ComponentType.Sync;
	}

	/**
	 * 対応HTTPメソッド定義を取得.
	 * @return int このコンポーネントが対応するHTTPメソッド定義が返却されます.
	 */
	@Override
	default int getMethod() {
		return ComponentConstants.HTTP_METHOD_ALL;
	}

	/**
	 * コンポーネント実行処理.
	 * @param req HttpRequestが設定されます.
	 * @param res HttpResponseが設定されます.
	 */
	default void call(Request req, Response<?> res) {
		// ResponseがSyncResponseでない場合は変換.
		if(!(res instanceof SyncResponse)) {
			res = new SyncResponseImpl(res);
		}
		// 実行処理.
		final Object ret = call(req, (SyncResponse)res);
		// 同期返却.
		ResponseUtil.sendSync((AbstractResponse<?>)res, ret);
	}

	/**
	 * コール実行.
	 * @param req HttpRequestが設定されます.
	 * @param res SyncResponseが設定されます.
	 * @return Object 返却するオブジェクトを設定します.
	 */
	public Object call(Request req, SyncResponse res);

}
