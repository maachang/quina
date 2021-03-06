package quina.component;

import java.io.File;

import quina.QuinaException;
import quina.http.Method;
import quina.http.Request;
import quina.http.Response;
import quina.http.response.ResponseUtil;
import quina.http.response.SyncResponse;

/**
 * 同期用コンポーネント.
 */
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
		final Object ret = call(req, (SyncResponse)res);
		// 返却内容が空の場合.
		if(ret == null) {
			// 空の返却.
			ResponseUtil.send((SyncResponse)res);
		// 返却条件がバイナリの場合.
		} else if(ret instanceof byte[]) {
			// バイナリ送信.
			ResponseUtil.send((SyncResponse)res, (byte[])ret);
		// 返却条件が文字列の場合.
		} else if(ret instanceof String) {
			// 文字列送信.
			ResponseUtil.send((SyncResponse)res, (String)ret);
		// 返却条件がファイルオブジェクトの場合.
		} else if(ret instanceof File) {
			// ファイル送信.
			String name;
			try {
				name = ((File)ret).getCanonicalPath();
			} catch(Exception e) {
				throw new QuinaException(e);
			}
			ResponseUtil.sendFile((SyncResponse)res, name);
		// 返却条件が上記以外の場合.
		} else {
			// JSON返却.
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
