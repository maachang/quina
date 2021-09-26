package quina.component;

import quina.http.HttpCustomAnalysisParams;
import quina.http.HttpElement;
import quina.http.MimeTypes;
import quina.http.server.HttpServerUtil;
import quina.util.AtomicObject;

/**
 * コンポーネント実行用オブジェクト.
 */
public class ExecuteComponent {
	private ExecuteComponent() {}
	
	// シングルトン.
	private static final ExecuteComponent SNGL =
		new ExecuteComponent();
	
	/**
	 * オブジェクトを取得.
	 * @return ExecuteComponent オブジェクトが返却されます.
	 */
	public static final ExecuteComponent getInstance() {
		return SNGL;
	}
	
	// カスタムなPostBody解析.
	private AtomicObject<HttpCustomAnalysisParams> custom =
		new AtomicObject<HttpCustomAnalysisParams>();
	
	/**
	 * HTTPパラメータ解析をカスタマイズ解析するオブジェクトを設定.
	 * @return custom カスタムオブジェクトを設定します.
	 */
	public void setHttpCustomAnalysisParams(
		HttpCustomAnalysisParams custom) {
		this.custom.set(custom);
	}
	
	/**
	 * HTTPパラメータ解析をカスタマイズ解析するオブジェクトを取得.
	 * @return HttpCustomAnalysisParams カスタムオブジェクトが返却されます.
	 */
	public HttpCustomAnalysisParams getHttpCustomAnalysisParams() {
		return custom.get();
	}

	/**
	 * URLを指定してコンポーネントを実行.
	 * @param em 対象のHttp要素を設定します.
	 */
	public final void execute(HttpElement em) {
		execute(null, em);
	}
	
	/**
	 * URLを指定してコンポーネントを実行.
	 * @param url 対象のURLを設定します.
	 *            null の場合は em.getRequest().getSrcUrl() で
	 *            処理されます
	 * @param em 対象のHttp要素を設定します.
	 */
	public final void execute(String url, HttpElement em) {
		HttpServerUtil.execComponent(
			url, em, MimeTypes.getInstance(), custom.get());
	}

}
