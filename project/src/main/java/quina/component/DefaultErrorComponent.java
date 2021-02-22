package quina.component;

import quina.http.Request;
import quina.http.Response;

/**
 * 標準エラーコンポーネント.
 */
public class DefaultErrorComponent implements ErrorComponent {
	private static final DefaultErrorComponent INST = new DefaultErrorComponent();

	/**
	 * 標準エラーコンポーネントを取得.
	 * @return ErrorComponent 標準エラーコンポーネントが返却されます.
	 */
	public static final ErrorComponent getInstance() {
		return INST;
	}

	/**
	 * コンストラクタ.
	 */
	private DefaultErrorComponent() {
	}

	@Override
	public void call(int state, Request req, Response res, Throwable e) {
		// BodyなしのHttpHeaderでのエラーメッセージを送信.
		res.setStatus(state, e.getMessage())
			.send();
	}
}
