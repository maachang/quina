package quina.component.error;

import quina.component.ComponentConstants;
import quina.component.ComponentType;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.AnyResponse;
import quina.http.server.response.AnyResponseImpl;

/**
 * エラーコンポーネント.
 * 
 * ラムダ実装で、直接Routerにセットする場合は
 * こちらを利用します.
 */
@FunctionalInterface
public interface ErrorComponent {
	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	default ComponentType getType() {
		return ComponentType.Error;
	}

	/**
	 * 対応HTTPメソッド定義を取得.
	 * @return int このコンポーネントが対応するHTTPメソッド定義が返却されます.
	 */
	default int getMethod() {
		return ComponentConstants.HTTP_METHOD_ALL;
	}

	/**
	 * HttpError処理を実行.
	 * @param state HTTPステータスを設定します.
	 * @param req HttpRequestを設定します.
	 * @param res HttpResponseを設定します.
	 */
	default void call(int state, Request req, Response<?> res) {
		call(state, req, res, null);
	}

	/**
	 * HttpError処理を実行.
	 * @param state HTTPステータスを設定します.
	 * @param req HttpRequestを設定します.
	 * @param res HttpResponseを設定します.
	 * @param e 例外を設定します.
	 */
	default void call(
		int state, Request req, Response<?> res, Throwable e) {
		// 真っ更のResponseが必要なので新規作成.
		res = new AnyResponseImpl(((AbstractResponse<?>)res).getElement(),
			((AbstractResponse<?>)res).getSrcComponentType());
		final boolean json = ((AbstractResponse<?>)res)
			.getSrcComponentType().isRESTful();
		// json返却の場合.
		if(json) {
			// JSON返却条件を設定.
			res.setContentType("application/json");
		} else {
			// HTML返却条件を設定.
			res.setContentType("text/html");
		}
		// 実行処理.
		call(state, json, req, (AnyResponse)res, e);
	}

	/**
	 * HttpError処理を実行.
	 * @param state HTTPステータスを設定します.
	 * @param json エラーが発生した呼び出しコンポーネントが
	 *             [RESFful]の場合は[true]が設定されます.
	 * @param req HttpRequestを設定します.
	 * @param res Responseを設定します.
	 */
	default void call(
		int state, boolean restful, Request req, Response<?> res) {
		// 真っ更のResponseが必要なので新規作成.
		res = new AnyResponseImpl(((AbstractResponse<?>)res).getElement(),
			((AbstractResponse<?>)res).getSrcComponentType());
		call(state, restful, req, (AnyResponse)res, null);
	}

	/**
	 * HttpError処理を実行.
	 * @param state HTTPステータスを設定します.
	 * @param json エラーが発生した呼び出しコンポーネントが
	 *             [RESFful]の場合は[true]が設定されます.
	 * @param req HttpRequestを設定します.
	 * @param res AnyResponseを設定します.
	 * @param e 例外を設定します.
	 */
	public void call(int state, boolean restful, Request req,
		AnyResponse res, Throwable e);
}
