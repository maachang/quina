package quina.component;

import quina.QuinaException;
import quina.util.collection.IndexMap;
import quina.util.collection.ObjectList;

/**
 * コンポーネントを管理するオブジェクト.
 */
public class ComponentManager {

	// スペースを指定長分セット.
	protected static final StringBuilder toSpace(StringBuilder out, int len) {
		for(int i = 0; i < len; i ++) {
			out.append(" ");
		}
		return out;
	}

	// コンポーネントの管理は２種類存在する.
	// １つは、固定パスに対するコンポーネント管理.
	//  <例> /a/b/c/d.json
	// もう１つは冗長パスに対するコンポーネント管理.
	//  <例> (1) /a/${id}/c/d.json
	//       (2) /a/*/c/d.json
	//       (3) /a/*
	// (1)の場合は /a/100/c/d.json でアクセスされた場合
	// パラメータとしてid=100がセットされます.
	//
	// (2)の場合は /a/b/c/d.json も /a/c/c/d.json も
	// 同じコンポーネントが呼び出されます.
	//
	// (3)の場合は /a/b/c/d.json も /a/x.json も
	// 同じコンポーネントが呼び出されます.

	/**
	 * 冗長パスに対する１要素.
	 */
	protected static final class AnyElement {
		// この要素の深度.
		private int position;
		// 前のAnyElement.
		private AnyElement parent;
		// 固定文字のパス条件.
		private IndexMap<String, AnyElement> staticPaths;
		// * や ${...} などのパス条件.
		private AnyElement anyPath;
		// 終端パスとする実行コンポーネント.
		private RegisterComponent anyComponent;

		/**
		 * コンストラクタ.
		 * @param position このAnyElementの深度を設定します.
		 * @param parent 親AnyElementを設定します.
		 */
		protected AnyElement(int position, AnyElement parent) {
			this.position = position;
			this.parent = parent;
		}

		/**
		 * このAnyElementの深度を取得します.
		 * @return int このオブジェクトの深度が返却されます.
		 */
		public int getPosition() {
			return position;
		}

		/**
		 * 親AnyElementを取得.
		 * @return AnyElement 親AnyElementが返却されます.
		 */
		public AnyElement getParent() {
			return parent;
		}

		/**
		 * 固定文字のパス要素をプット.
		 * @param path
		 * @param em
		 * @return
		 */
		public AnyElement putStaticPath(String path, AnyElement em) {
			if(staticPaths == null) {
				staticPaths = new IndexMap<String, AnyElement>();
			}
			staticPaths.put(path, em);
			return this;
		}

		/**
		 * アスタリスクやパラメータパス等の要素を追加.
		 * @param em
		 * @return
		 */
		public AnyElement putAnyPath(AnyElement em) {
			anyPath = em;
			return this;
		}

		/**
		 * 終端パスとする実行コンポーネントを設定.
		 * @param ac
		 * @return
		 */
		public AnyElement setAnyComponent(RegisterComponent ac) {
			anyComponent = ac;
			return this;
		}

		/**
		 * 固定文字パスを指定して要素を取得.
		 * @param path
		 * @return
		 */
		public AnyElement getStaticPath(String path) {
			if(staticPaths != null) {
				return staticPaths.get(path);
			}
			return null;
		}

		/**
		 * アスタリスクやパラメータパス等の要素を取得
		 * @return
		 */
		public AnyElement getAnyPath() {
			return anyPath;
		}

		/**
		 * 終端パスとする実行コンポーネントを取得.
		 * @return
		 */
		public RegisterComponent getAnyComponent() {
			return anyComponent;
		}

		/**
		 * 文字列変換.
		 * @param out 文字列出力先のStringBuilderを設定します.
		 * @param spacePos 改行後のスペース入力値を設定します.
		 * @return StringBuilder
		 */
		public StringBuilder toString(StringBuilder out, int spacePos) {
			ComponentManager.toSpace(out, spacePos)
				.append("*AnyElement position: ").append(position).append("\n");
			ComponentManager.toSpace(out, spacePos)
				.append("staticPaths: ").append(staticPaths == null ? 0 : staticPaths.size()).append("\n");
			ComponentManager.toSpace(out, spacePos)
				.append("anyPath: ").append(anyPath != null).append("\n");
			ComponentManager.toSpace(out, spacePos)
				.append("anyComponent: ").append(anyComponent != null).append("\n");
			if(anyComponent != null) {
				anyComponent.toString(out, spacePos);
			}
			return out;
		}
	}

	// パラメータやアスタリスクでは無い普通の固定ディレクトリ名.
	protected static final int NON_URL_PARAMS = -1;
	// /aaa/bbb/*/$password/xxx.json
	protected static final int URL_ASTERRISK = 0;
	// /aaa/bbb/{id}/{password}/xxx.json
	protected static final int URL_PARAM_TYPE_1 = 1;
	// /aaa/bbb/${id}/${password}/xxx.json
	protected static final int URL_PARAM_TYPE_2 = 2;
	// /aaa/bbb/$id/$password/xxx.json
	protected static final int URL_PARAM_TYPE_3 = 3;

	/**
	 * 対象ディレクトリ名のパラメータタイプを取得.
	 * @param dir
	 * @return int
	 */
	protected static final int getParamsType(String dir) {
		// /aaa/bbb/{id}/{password}/xxx.json
		if(dir.startsWith("{") && dir.endsWith("}")) {
			return URL_PARAM_TYPE_1;
		// /aaa/bbb/${id}/${password}/xxx.json
		} else if(dir.startsWith("${") && dir.endsWith("}")) {
			return URL_PARAM_TYPE_2;
		// /aaa/bbb/$id/$password/xxx.json
		} else if(dir.startsWith("$")) {
			return URL_PARAM_TYPE_3;
		// /aaa/bbb/*/$password/xxx.json
		} else if("*".equals(dir)) {
			return URL_ASTERRISK;
		}
		return NON_URL_PARAMS;
	}

	/**
	 * URLに対する"/"で区切った内容にパラメータ要素を解析.
	 * <例> /aaa/bbb/{id}/{password}/xxx.json
	 * <例> /aaa/bbb/${id}/${password}/xxx.json
	 * <例> /aaa/bbb/$id/$password/xxx.json
	 *
	 *      上記の場合[id]と[password]の部分がパラメータとして利用される.
	 * @param urls "/"で区切られた内容が配列化された情報を設定します.
	 * @return Object {項番, パラメータ....}で返却されます.
	 *         先程の<例>では [2, "id", 3, "password"] が返却されます.
	 */
	protected static final Object[] analysisUrlParams(String[] urls) {
		String k;
		int len = urls.length;
		ObjectList<Object> lst = new ObjectList<Object>(len);
		for(int i = 0; i < len; i ++) {
			switch(getParamsType(k = urls[i])) {
			// /aaa/bbb/{id}/{password}/xxx.json
			case URL_PARAM_TYPE_1:
				// urlパス位置.
				lst.add(i);
				// キー名.
				lst.add(k.substring(1, k.length()-1).trim());
				break;
			// /aaa/bbb/${id}/${password}/xxx.json
			case URL_PARAM_TYPE_2:
				// urlパス位置.
				lst.add(i);
				// キー名.
				lst.add(k.substring(2, k.length()-1).trim());
				break;
			// /aaa/bbb/$id/$password/xxx.json
			case URL_PARAM_TYPE_3:
				// urlパス位置.
				lst.add(i);
				// キー名.
				lst.add(k.substring(1, k.length()).trim());
				break;
			// /aaa/bbb/*/*/xxx.json.
			case URL_ASTERRISK:
				// urlパス位置.
				lst.add(i);
				// キー名.
				lst.add(null);
				break;
			}
		}
		return lst.toArray();
	}

	/**
	 * URLをスラッシュで区切って文字配列で返却します.
	 * @param url 対象のURLが返却されます.
	 * @return
	 */
	public static final String[] getUrls(String url) {
		return getUrls(null, url);
	}

	/**
	 * URLをスラッシュで区切って文字配列で返却します.
	 * @param out Object[0]に {項番, パラメータ....}で返却されます.
	 * @param url 対象のURLが返却されます.
	 * @return
	 */
	protected static final String[] getUrls(final Object[] out, final String url) {
		int pos = 0;
		int len = url.length();
		int end = len - 1;
		int p = url.indexOf("?");
		if(p != -1) {
			len = p;
			end = len - 1;
		}
		ObjectList<String> lst = new ObjectList<String>();
		p = url.startsWith("/") ? 1 : 0;
		if(out == null) {
			for(int i = p; i < len; i ++) {
				if(url.charAt(i) == '/' || i == end) {
					if(i == end) {
						if(url.charAt(i) == '/') {
							lst.add(url.substring(p, i));
							lst.add("");
						} else {
							lst.add(url.substring(p));
						}
					} else {
						lst.add(url.substring(p, i));
						p = i + 1;
					}
				}
			}
		} else {
			String k;
			final ObjectList<Object> plst = new ObjectList<Object>();
			for(int i = p; i < len; i ++) {
				if(url.charAt(i) == '/' || i == end) {
					if(i == end) {
						if(url.charAt(i) == '/') {
							lst.add(k = url.substring(p, i));
							lst.add("");
						} else {
							lst.add(k = url.substring(p));
						}
					} else {
						lst.add(k = url.substring(p, i));
					}
					p = i + 1;
					pos ++;
					switch(getParamsType(k)) {
					// /aaa/bbb/{id}/{password}/xxx.json
					case URL_PARAM_TYPE_1:
						// urlパス位置.
						plst.add(pos - 1);
						// キー名.
						plst.add(k.substring(1, k.length()-1).trim());
						break;
					// /aaa/bbb/${id}/${password}/xxx.json
					case URL_PARAM_TYPE_2:
						// urlパス位置.
						plst.add(pos - 1);
						// キー名.
						plst.add(k.substring(2, k.length()-1).trim());
						break;
					// /aaa/bbb/$id/$password/xxx.json
					case URL_PARAM_TYPE_3:
						// urlパス位置.
						plst.add(pos - 1);
						// キー名.
						plst.add(k.substring(1, k.length()).trim());
						break;
					// /aaa/bbb/*/*/xxx.json.
					case URL_ASTERRISK:
						// urlパス位置.
						plst.add(pos - 1);
						// キー名.
						plst.add(null);
						break;
					}
				}
			}
			out[0] = plst.size() == 0 ? null : plst.toArray();
		}
		return lst.toArray(String.class);
	}

	/**
	 * URLの[/]区切りの配列に対してurlParamとマッチする条件をnullセットする.
	 * @param urls [/]区切り配列のURLを設定します.
	 * @param urlParam urlParamを設定します.
	 * @return String[] 処理された内容が返却されます.
	 */
	private static final String[] getUrlsByAppendUrlParams(String[] urls, Object[] urlParam) {
		final int len = urls.length;
		String[] ret = urls;
		// anyParamが存在する場合.
		if(urlParam != null && urlParam.length > 0) {
			// anyParamの条件のurlパスに[null]セット.
			ret = new String[len];
			System.arraycopy(urls, 0, ret, 0, len);
			final int alen = urlParam.length;
			for(int i = 0; i < alen; i += 2) {
				ret[(Integer)urlParam[i]] = null;
			}
		}
		return ret;
	}

	/**
	 * staticなAnyComponentを検索.
	 * @param out 見つかったAnyElementがセットされる.
	 * @param urls スラッシュで区切られたURL文字列が設定される.
	 * @param len urlの文字列配列の長さが設定されます.
	 * @param now 検索開始対象のAnyElementが設定される.
	 * @param pos 開始ポジションがセットされます.
	 * @return int 次の検索位置が返却されます.
	 */
	private static final int searchAnyComponentByStatic(final AnyElement[] out,
		final String[] urls, final int len, final AnyElement now, final int pos) {
		AnyElement e, em;
		em = now;
		for(int i = pos; i < len; i ++) {
			// staticな検索で次のAnyElelemtが見つからない場合.
			if(urls[i] == null || (e = em.getStaticPath(urls[i])) == null) {
				// 現在の位置条件を返却.
				out[0] = em;
				return i;
			}
			// 次の条件をセット.
			em = e;
		}
		// 今回のAnyElementを返却.
		out[0] = em;
		// 終端ポジションをセット.
		return len;
	}

	/**
	 * anyなAnyComponentを検索.
	 * @param out 見つかったAnyElementがセットされる.
	 * @param urls スラッシュで区切られたURL文字列が設定される.
	 * @param now 検索開始対象のAnyElementが設定される.
	 * @param pos 開始ポジションがセットされます.
	 * @return int -1の場合、見つかりませんでした.
	 */
	private static final int searchAnyComponentByAny(final AnyElement[] out,
		final String[] urls, final int len, final AnyElement now, final int pos) {
		// 存在する複数のAnyで検索.
		if(now.getAnyPath() != null) {
			// urlの検索開始ポジションを取得.
			int p = pos + 1;
			// staticから検索する.
			p = searchAnyComponentByStatic(out, urls, len, now.getAnyPath(), p);
			// 終端を検知出来ない場合.
			if(p != len) {
				// staticで最後に見つかった内容の続きをany検索.
				return searchAnyComponentByAny(out, urls, len, out[0], p + 1);
			}
			// 終端が見つかった場合.
			return p;
		}
		// 見つからなかった場合.
		return -1;
	}

	/**
	 * URLに対するAnyElementを取得.
	 * @param out 最後に見つかったAnyElementがout[0]に格納されます.
	 * @param topAnyElement topのAnyElementを設定します.
	 * @param urlParam 冗長パラメータ[no, key, ....]が設定されます.
	 * @param urls [/]スラッシュで区切られたURLを配列化した内容が設定されます.
	 * @return boolean 正しい検索結果が見つかった場合は[true]が返却されます.
	 */
	private static final boolean getAnyElement(AnyElement[] out, AnyElement topAnyElement, String[] urls) {
		final int len = urls.length;
		// 最初staticで最初検索.
		int p = searchAnyComponentByStatic(out, urls, len, topAnyElement, 0);
		// 次にAnyで検索する.
		p = searchAnyComponentByAny(out, urls, len, out[0], p);
		// 終端の場合はtrue.
		return p == len;
	}

	/**
	 * 最後のURLがアスタリスクでURLが登録されているコンポーネントを
	 * 遡って検索します.
	 * @param em getAnyElementメソッドで検索出来なかった時に取得された
	 *           AnyElementを設定します.
	 * @return AnyElement アスタリスクでURL登録されたAnyComponentが存在する
	 *                    AnyElementが返却されます.
	 */
	private static final AnyElement getAnyElementByLastAsterrisk(AnyElement em) {
		RegisterComponent anyComponent;
		while(em != null) {
			if((anyComponent = em.getAnyComponent()) != null &&
				anyComponent.isLastAsterrisk()) {
				return em;
			}
			em = em.getParent();
		}
		return null;
	}

	/**
	 * 新しいAnyElementを追加.
	 * @param topAnyElement topのAnyElementを設定します.
	 * @param url 登録するURLを設定します.
	 * @param urls [/]スラッシュで区切られたURLを配列化した内容が設定されます.
	 * @param pos urlsの開始ポジションを設定します.
	 * @param urlParam urlParamを設定します.
	 * @param component 実行コンポーネントを設定します.
	 */
	private static final void putAnyElement(AnyElement topAnyElement, String url, String[] urls,
		int pos, Object[] urlParam, Component component) {
		String path;
		AnyElement em;
		AnyElement now = topAnyElement;
		final int len = urls.length;
		for(int i = pos; i < len; i ++) {
			// 個別のpath名を取得.
			path = urls[i];
			// urlパラメータか、アスタリスクの場合.
			if(path == null) {
				// 同じ条件が登録されているかチェック.
				em = now.getAnyPath();
				// 同じanyPath条件が存在しない場合.
				if(em == null) {
					em = new AnyElement(i + 1, now);
					now.putAnyPath(em);
				}
			// 有効なdir or file名の場合.
			} else {
				// 同じ条件が登録されているかチェック.
				em = now.getStaticPath(path);
				// 同じpathの条件が存在しない場合.
				if(em == null) {
					em = new AnyElement(i + 1, now);
					now.putStaticPath(path, em);
				}
			}
			// 次の条件をセット.
			now = em;
		}
		// 最後の条件に対してAnyComponentをセット.
		RegisterComponent cmp = new RegisterComponent(url, urlParam, component);
		now.setAnyComponent(cmp);
	}

	// 固定パスコンポーネント管理.
	private final IndexMap<String, RegisterComponent> staticComponent =
		new IndexMap<String, RegisterComponent>();

	// rootの冗長パスコンポーネント管理.
	private AnyElement rootAnyElement = new AnyElement(0, null);

	// 指定URLの条件が存在しない場合の実行コンポーネント.
	private RegisterComponent notFoundUrlComponent = null;

	// エラー発生時に呼び出すコンポーネント.
	private ErrorComponent errorComponent = null;

	/**
	 * コンストラクタ.
	 */
	public ComponentManager() {
	}

	/**
	 * クリア.
	 */
	public void clear() {
		staticComponent.clear();
		rootAnyElement = new AnyElement(0, null);
		notFoundUrlComponent = null;
		errorComponent = null;
	}

	/**
	 * 指定URLの条件が存在しない場合の実行コンポーネントをセット.
	 * @param component 対象のコンポーネントを設定します.
	 */
	public void put(Component component) {
		if(component instanceof RegisterComponent) {
			component = ((RegisterComponent)component).getComponent();
		}
		notFoundUrlComponent = new RegisterComponent("/*", null, component);
	}

	/**
	 * 指定URLに対してコンポーネントを登録.
	 * @param url 対象のURLを設定します.
	 * @param component 対象のコンポーネントを設定します.
	 * @return boolean [true]の場合はstaticなURLとして
	 *                 [false]の場合はanyなURLとして登録されました.
	 */
	public boolean put(String url, Component component) {
		if(url == null || url.length() == 0 || component == null) {
			if(url == null || url.length() == 0) {
				throw new QuinaException("url is not set.");
			} else if(component == null) {
				throw new QuinaException("The component is not set: " + url);
			}
		} else if(component instanceof RegisterComponent) {
			component = ((RegisterComponent)component).getComponent();
			if(component == null) {
				throw new QuinaException("The component is not set: " + url);
			}
		}
		// anyUrl関連の取得.
		Object[] outUrlParam = new Object[1];
		String[] urls = getUrls(outUrlParam, url);
		Object[] urlParam = (Object[])outUrlParam[0];
		outUrlParam = null;
		// anyUrlの条件が無い場合はstaticで設定する.
		if(urlParam == null || urlParam.length == 0) {
			// 対象URLにコンポーネントが登録されている場合.
			if(staticComponent.containsKey(url)) {
				throw new QuinaException(
					"It is a URL that has already been registered: " + url);
			}
			// staticコンポーネント管理に追加.
			staticComponent.put(url, new RegisterComponent(url, null, component));
			return true;
		}
		// urlにurlParamsの条件に対してnullセット.
		urls = getUrlsByAppendUrlParams(urls, urlParam);
		AnyElement[] out = new AnyElement[1];
		AnyElement now = this.rootAnyElement;
		// 既に登録されている場合はエラー.
		boolean res = getAnyElement(out, now, urls);
		if(res && out[0] != null && out[0].getAnyComponent() != null) {
			throw new QuinaException(
				"It is a URL that has already been registered: " + url);
		}
		out[0] = null;
		// anyコンポーネント管理に追加.
		putAnyElement(now, url, urls, 0, urlParam, component);
		return false;
	}

	/**
	 * エラー処理用のコンポーネントをセット.
	 * @param component 対象のコンポーネントを設定します.
	 */
	public void putError(ErrorComponent component) {
		errorComponent = component;
	}

	/**
	 * 指定URLに対するコンポーネントを取得.
	 * @param url urlを設定します.
	 * @return Component 実行コンポーネントが返却されます.
	 */
	public RegisterComponent get(String url) {
		return get(url, getUrls(url));
	}

	/**
	 * 指定URLに対するコンポーネントを取得.
	 * @param url urlを設定します.
	 * @param urls [/]で配列化されたURLを設定します.
	 * @return Component 実行コンポーネントが返却されます.
	 */
	public RegisterComponent get(String url, String[] urls) {
		// ダイレクトにURL指定でコンポーネント取得.
		if(staticComponent.containsKey(url)) {
			// 存在する場合は返却.
			return staticComponent.get(url);
		}
		AnyElement[] out = new AnyElement[1];
		AnyElement now = this.rootAnyElement;
		RegisterComponent rcmp = null;
		boolean res = getAnyElement(out, now, urls);
		// 存在しない場合.
		if(!(res && out[0] != null && (rcmp = out[0].getAnyComponent()) != null)) {
			// URLの終端がアスタリスクのAnyElementを取得する.
			out[0] = getAnyElementByLastAsterrisk(out[0]);
			// 存在しない場合.
			if(!(out[0] != null && (rcmp = out[0].getAnyComponent()) != null)) {
				// 存在しない場合に利用想定されるコンポーネント返却.
				return notFoundUrlComponent;
			}
		}
		// コンポーネント返却.
		return rcmp;
	}

	// AnyElementを文字列出力.
	private static final void toAnyElementByString(StringBuilder buf, AnyElement now) {
		int i, len, no;
		AnyElement em;
		// このAnyElementを文字列出力.
		now.toString(buf, now.getPosition() << 1);
		// AnyElementのstatic条件を文字列出力.
		if(now.staticPaths != null) {
			len = now.staticPaths.size();
			for(i = 0; i < len; i ++) {
				em = now.staticPaths.valueAt(i);
				no = em.getPosition() << 1;
				toSpace(buf, no).
					append("@static: ").append(" key: ").
					append(now.staticPaths.keyAt(i)).append("\n");
				// 次の条件を出力.
				toAnyElementByString(buf, em);
			}
		}
		// AnyElementのany条件を文字列出力.
		if((em = now.getAnyPath()) != null) {
			no = em.getPosition() << 1;
			toSpace(buf, no).
				append("@any: \n");
			// 次の条件を出力.
			toAnyElementByString(buf, em);
		}
	}

	/**
	 * エラー時の登録コンポーネントを取得.
	 * @return ErrorComponent エラー時のコンポーネントが返却されます.
	 */
	public ErrorComponent getError() {
		// エラーコンポーネントが存在しない場合は
		// 標準エラーコンポーネントを利用.
		return errorComponent == null ?
			DefaultErrorComponent.getInstance() : errorComponent;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		// staticなコンポーネント管理を出力.
		int len = staticComponent.size();
		buf.append("*ComponentManager\n").append("#staticComponents: ").append(len).append("\n");
		for(int i = 0; i < len; i ++) {
			toSpace(buf, 2).append("url: ").append(staticComponent.keyAt(i)).append("\n");
		}
		buf.append("#anyComponents\n");
		// AnyElementを出力.
		toAnyElementByString(buf, rootAnyElement);
		buf.append("\n");
		// 指定URLが存在しない場合のコンポーネント実行.
		buf.append("*notFoundUrlComponent: ")
			.append(notFoundUrlComponent != null)
			.append("\n");
		// エラー発生時のコンポーネント実行.
		buf.append("*errorComponent: ")
		.append(errorComponent != null)
		.append("\n");
		return buf.toString();
	}
}
