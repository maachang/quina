package quina.component;

import java.util.Map;

import quina.http.Method;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.HttpServerRequest;

/**
 * ComponentManagerに登録済みのコンポーネント.
 */
public class RegisterComponent implements Component {
	// 登録時のURL.
	private String url;
	// URLパラメータ.
	private Object[] urlParam;
	// URLパラメータが有効かチェックするフラグ.
	private boolean useUrlParam;
	// コンポーネント.
	private Component component;
	// ラスト情報がアスタリスクか判別フラグ.
	private boolean lastAsterrisk;

	/**
	 * コンストラクタ.
	 * @param url 登録対象のURLを設定します.
	 * @param component 対象のコンポーネントを設定します.
	 */
	protected RegisterComponent(String url, Object[] urlParam, Component component) {
		this.url = url;
		this.urlParam = urlParam;
		this.useUrlParam = useUrlParam(urlParam);
		this.component = component;
		this.lastAsterrisk = url.endsWith("/*");
	}

	// 有効なURLパラメータが存在するかチェック.
	private static final boolean useUrlParam(Object[] urlParam) {
		if(urlParam == null || urlParam.length == 0) {
			return false;
		}
		int len = urlParam.length;
		for(int i = 0; i < len; i += 2) {
			if(urlParam[i + 1] != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 登録対象のURLを取得.
	 * @return String urlが返却されます.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * URLパラメータを取得.
	 * @return Object[] URLパラメータが返却されます.
	 */
	public Object[] getUrlParam() {
		return urlParam;
	}

	/**
	 * 対象コンポーネントを取得.
	 * @return Component 対象のコンポーネントが返却されます.
	 */
	public Component getComponent() {
		return component;
	}

	/**
	 * URLに対するパラメータ情報が存在するかチェック.
	 * @return boolean trueの場合、URLパラメータは存在します.
	 */
	public boolean isUrlParam() {
		return useUrlParam;
	}

	/**
	 * URLに対するパラメータ情報数を取得.
	 * @return urlParam情報数が返却されます.
	 */
	public int getParamSize() {
		return urlParam != null ? urlParam.length >> 1 : 0;
	}

	/**
	 * URLに対するパラメータ項番を取得.
	 * urlParamは[項番, パラメータ名,...] な情報です.
	 * たとえばURLが [/a/{id}/c/{name}/＊/hoge.json]
	 * とした場合、[1, "id", 3, "name", 4, null] が
	 * urlParamとなります.
	 * @param no 取得位置を設定します.
	 * @return int urlParamの項番が返却されます.
	 */
	public int getParamNo(int no) {
		return (Integer)urlParam[no << 1];
	}

	/**
	 * URLに対するパラメータ名を取得.
	 * @param no 取得位置を設定します.
	 * @return String パラメータ名が返却されます.
	 *                [null]の場合は、アスタリスクです.
	 */
	public String getParamName(int no) {
		return (String)urlParam[(no << 1) + 1];
	}

	/**
	 * URLパラメータを取得.
	 * @param out URLパラメータを格納するMapオブジェクトを設定します.
	 * @param url urlを設定します.
	 */
	public void getUrlParam(Map<String, Object> out, String url) {
		getUrlParam(out, ComponentManager.getUrls(url));
	}

	/**
	 * URLパラメータを取得.
	 * @param out URLパラメータを格納するMapオブジェクトを設定します.
	 * @param urls [/]でパースされたURLを設定します.
	 */
	public void getUrlParam(Map<String, Object> out, String[] urls) {
		// パラメータ化するURL情報を取得.
		String key;
		final int len = getParamSize();
		for(int i = 0; i < len; i ++) {
			// キー名がnullの場合はアスタリスク.
			if((key = getParamName(i)) == null) {
				continue;
			}
			// URLをパラメータとしてセット.
			out.put(key, urls[getParamNo(i)]);
		}
	}

	/**
	 * URLの最後がアスタリスクで終わってるか取得.
	 * @return boolean trueの場合、アスタリスクで終わってます.
	 */
	public boolean isLastAsterrisk() {
		return lastAsterrisk;
	}

	// 文字列出力の階段的スペース値.
	private static final int KAIDAN_SPACE = 1;

	/**
	 * 文字列変換.
	 * @param out 文字列出力先のStringBuilderを設定します.
	 * @param spacePos 改行後のスペース入力値を設定します.
	 * @return StringBuilder
	 */
	public StringBuilder toString(StringBuilder out, int spacePos) {
		spacePos += KAIDAN_SPACE;
		ComponentManager.toSpace(out, spacePos)
			.append("*RegisterComponent\n");
		ComponentManager.toSpace(out, spacePos)
		.append("type: ").append(getType()).append("\n");
		ComponentManager.toSpace(out, spacePos)
		.append("url: ").append(getUrl()).append("\n");
		ComponentManager.toSpace(out, spacePos)
			.append("urlParam: ");
		int len = getParamSize();
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				out.append(", ");
			}
			out.append("pos: ").append(getParamNo(i))
				.append(" key: ").append(getParamName(i));
		}
		out.append("\n");
		ComponentManager.toSpace(out, spacePos)
			.append("component: ").append(component != null).append("\n");
		ComponentManager.toSpace(out, spacePos)
			.append("lastAsterrisk: ").append(lastAsterrisk).append("\n");
		return out;
	}

	@Override
	public ComponentType getType() {
		return component.getType();
	}
	@Override
	public void call(Method method, Request req, Response res) {
		// HttpServerRequestの場合は、コンポーネントURLを設定.
		if(req instanceof HttpServerRequest) {
			((HttpServerRequest)req).setComponentUrl(url);
		}
		component.call(method, req, res);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		return toString(buf, 0).toString();
	}
}
