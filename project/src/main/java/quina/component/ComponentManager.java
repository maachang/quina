package quina.component;

import quina.annotation.component.ResponseInitialSetting;
import quina.component.error.ErrorComponent;
import quina.component.file.EtagManager;
import quina.component.file.EtagManagerInfo;
import quina.component.file.FileAttributeComponent;
import quina.exception.QuinaException;
import quina.http.HttpException;
import quina.http.Method;
import quina.http.Request;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.AnyResponse;
import quina.http.server.response.ResponseUtil;
import quina.util.collection.IndexKeyValueList;
import quina.util.collection.ObjectList;
import quina.validate.Validation;

/**
 * コンポーネントを管理するオブジェクト.
 */
public class ComponentManager {

	// デフォルトのエラー処理コンポーネント.
	private static final class DefaultErrorComponent
		implements ErrorComponent {
		private static final DefaultErrorComponent INST =
			new DefaultErrorComponent();

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
		public void call(int state, boolean restful, Request req,
			AnyResponse res, Throwable e) {
			// BodyなしのHttpHeaderでのエラーメッセージを送信.
			if(e != null) {
				res.setStatus(state, e.getMessage());
			} else {
				res.setStatus(state);
			}
			// 空データを送信.
			ResponseUtil.send((AbstractResponse<?>)res);
		}
	}
	
	/**
	 * 対象エラーコンポーネントに対する有効ステータス範囲要素.
	 */
	protected static final class RangeStatusElement {
		private int startStatus;
		private int endStatus;
		private ErrorComponent ErrorComponent;
		
		/**
		 * コンストラクタ.
		 * @param startStatus
		 * @param endStatus
		 * @param ErrorComponent
		 */
		public RangeStatusElement(
			int startStatus, int endStatus,
			ErrorComponent ErrorComponent) {
			if(startStatus > endStatus) {
				int n = startStatus;
				startStatus = endStatus;
				endStatus = n;
			}
			this.startStatus = startStatus;
			this.endStatus = endStatus;
			this.ErrorComponent = ErrorComponent;
		}
		
		/**
		 * 指定ステータスがマッチするかチェック.
		 * @param state
		 * @return
		 */
		public boolean match(int state) {
			return (startStatus <= state &&
				endStatus >= state);
		}
		
		/**
		 * 開始ステータス.
		 * @return
		 */
		public int getStartStatus() {
			return startStatus;
		}
		
		/**
		 * 終了ステータス.
		 * @return
		 */
		public int getEndStatus() {
			return endStatus;
		}
		
		/**
		 * エラーコンポーネント.
		 * @return
		 */
		public ErrorComponent getErrorComponent() {
			return ErrorComponent;
		}
		
		@Override
		public String toString() {
			return new StringBuilder()
				.append(startStatus).append(" - ")
				.append(endStatus).toString();
		}
	}
	
	/**
	 * エラーコンポーネントマネージャ.
	 */
	protected static final class ErrorComponentManager {
		// 指定ステータスでエラーコンポーネントを管理.
		private IndexKeyValueList<Integer, ErrorComponent> singleManager =
			new IndexKeyValueList<Integer, ErrorComponent>();
		
		// 範囲指定のステータスでエラーコンポーネントを管理.
		private ObjectList<RangeStatusElement> rangeManager =
			new ObjectList<RangeStatusElement>();
		
		// 登録ステータス以外のステータスに対応するエラーコンポーネント.
		private ErrorComponent anyErrorComponent;
		
		/**
		 * コンストラクタ.
		 */
		public ErrorComponentManager() {}
		
		/**
		 * 指定ステータスでエラーコンポーネント登録.
		 * @param state 
		 * @param cmp
		 */
		public void putSingle(int state, ErrorComponent cmp) {
			if(cmp == null) {
				throw new QuinaException("No error component is specified.");
			}
			singleManager.put(state, cmp);
		}
		
		/**
		 * 範囲指定のステータスでエラーコンポーネント登録.
		 * @param startState
		 * @param endState
		 * @param cmp
		 */
		public void putRange(int startState, int endState, ErrorComponent cmp) {
			if(cmp == null) {
				throw new QuinaException("No error component is specified.");
			}
			rangeManager.add(new RangeStatusElement(startState, endState, cmp));
		}
		
		/**
		 * 登録ステータス以外のステータスに対応するエラーコンポーネント登録.
		 * @param cmp
		 */
		public void any(ErrorComponent cmp) {
			anyErrorComponent = cmp;
		}
		
		/**
		 * 指定ステータスにマッチしたエラーコンポーネントを取得.
		 * @param state
		 * @return
		 */
		public ErrorComponent get(int state) {
			// 単一ステータスで検索.
			ErrorComponent ret = singleManager.get(state);
			if(ret != null) {
				return ret;
			}
			
			// 範囲ステータスで検索.
			RangeStatusElement rse;
			final int len = rangeManager.size();
			for(int i = 0; i < len; i ++) {
				rse = rangeManager.get(i);
				if(rse.match(state)) {
					return rse.getErrorComponent();
				}
			}
			
			// その他ステータス内容を返却.
			return anyErrorComponent == null ?
				DefaultErrorComponent.getInstance() :
				anyErrorComponent;
		}
		
		/**
		 * 文字列を出力.
		 * @param space
		 * @param buf
		 * @return
		 */
		public StringBuilder toString(int space, StringBuilder buf) {
			int len = singleManager.size();
			buf.append("\n");
			toSpace(buf, space).append("single: ").append(len).append("\n");
			toSpace(buf, space + 2);
			for(int i = 0; i < len; i ++) {
				if(i != 0) {
					buf.append(", ");
				}
				buf.append(singleManager.keyAt(i));
			}
			buf.append("\n");
			
			len = rangeManager.size();
			toSpace(buf, space).append("range: ").append(len).append("\n");
			toSpace(buf, space + 2);
			for(int i = 0; i < len; i ++) {
				if(i != 0) {
					buf.append(", ");
				}
				buf.append(rangeManager.get(i));
			}
			buf.append("\n");
			
			toSpace(buf, space).append("any: ")
				.append(anyErrorComponent == null ? "default" : "original")
				.append("\n");
			return buf;
		}
	}

	// スペースを指定長分セット.
	protected static final StringBuilder toSpace(StringBuilder out, int len) {
		for(int i = 0; i < len; i ++) {
			out.append(" ");
		}
		return out;
	}

	// HTTPメソッド別のコンポーネント管理.
	protected static final class MethodsComponent {
		// 全メソッド対応.
		private RegisterComponent all;
		// GETメソッド.
		private RegisterComponent get;
		// POSTメソッド.
		private RegisterComponent post;
		// DELETEメソッド.
		private RegisterComponent delete;
		// PUTメソッド.
		private RegisterComponent put;
		// PATCHメソッド.
		private RegisterComponent patch;

		/**
		 * コンストラクタ.
		 */
		public MethodsComponent() {}

		/**
		 * コンポーネントを設定.
		 * @param component 対象のコンポーネントを設定します.
		 */
		public void setComponent(RegisterComponent component) {
			int type = component.getMethod();
			if(type == ComponentConstants.HTTP_METHOD_ALL) {
				all = component;
			} else if(type == ComponentConstants.HTTP_METHOD_GET) {
				get = component;
			} else if(type == ComponentConstants.HTTP_METHOD_POST) {
				post = component;
			} else if(type == ComponentConstants.HTTP_METHOD_DELETE) {
				delete = component;
			} else if(type == ComponentConstants.HTTP_METHOD_PUT) {
				put = component;
			} else if(type == ComponentConstants.HTTP_METHOD_PATCH) {
				patch = component;
			} else {
				throw new QuinaException(
					"Failed to get Http method information: " +
					component.getMethod());
			}
		}

		/**
		 * HTTPメソッドに対するコンポーネントを取得します.
		 * @param method HTTPメソッドを設定します.
		 * @return RegisterComponent コンポーネントが返却されます.
		 */
		public RegisterComponent getComponent(Method method) {
			int type = method.getType();
			if(type == ComponentConstants.HTTP_METHOD_ALL) {
				return all;
			} else if(type == ComponentConstants.HTTP_METHOD_GET) {
				return get();
			} else if(type == ComponentConstants.HTTP_METHOD_POST) {
				return post();
			} else if(type == ComponentConstants.HTTP_METHOD_DELETE) {
				return delete();
			} else if(type == ComponentConstants.HTTP_METHOD_PUT) {
				return put();
			} else if(type == ComponentConstants.HTTP_METHOD_PATCH) {
				return patch();
			}
			throw new HttpException(405,
				"The specified method: " + method.getName() +
				" cannot be used for this URL.");
		}

		/**
		 * 登録されたコンポーネントが存在するかチェックします.
		 * @return boolean trueの場合、対象コンポーネントは存在します.
		 */
		public boolean isComponent() {
			return all != null || get != null || post != null
				|| delete != null || put != null || patch != null;
		}

		/**
		 * HTTPメソッドに対する登録されたコンポーネントが存在するかチェックします.
		 * @param method HTTPメソッドを設定します.
		 * @return boolean trueの場合、対象コンポーネントは存在します.
		 */
		public boolean isComponent(Method method) {
			int type = method.getType();
			if(type == ComponentConstants.HTTP_METHOD_ALL) {
				return all != null;
			} else if(type == ComponentConstants.HTTP_METHOD_GET) {
				return get != null;
			} else if(type == ComponentConstants.HTTP_METHOD_POST) {
				return post != null;
			} else if(type == ComponentConstants.HTTP_METHOD_DELETE) {
				return delete != null;
			} else if(type == ComponentConstants.HTTP_METHOD_PUT) {
				return put != null;
			} else if(type == ComponentConstants.HTTP_METHOD_PATCH) {
				return patch != null;
			}
			return false;
		}

		/**
		 * GETメソッドコンポーネントを取得.
		 * @return RegisterComponent コンポーネントが返却されます.
		 */
		public RegisterComponent get() {
			if(get != null) {
				return get;
			} else if(all != null) {
				return all;
			}
			throw new HttpException(405,
				"The specified method: GET cannot be used for this URL.");
		}

		/**
		 * POSTメソッドコンポーネントを取得.
		 * @return RegisterComponent コンポーネントが返却されます.
		 */
		public RegisterComponent post() {
			if(post != null) {
				return post;
			} else if(all != null) {
				return all;
			}
			throw new HttpException(405,
				"The specified method: POST cannot be used for this URL.");
		}

		/**
		 * DELETEメソッドコンポーネントを取得.
		 * @return RegisterComponent コンポーネントが返却されます.
		 */
		public RegisterComponent delete() {
			if(delete != null) {
				return delete;
			} else if(all != null) {
				return all;
			}
			throw new HttpException(405,
				"The specified method: DELETE cannot be used for this URL.");
		}

		/**
		 * PUTメソッドコンポーネントを取得.
		 * @return RegisterComponent コンポーネントが返却されます.
		 */
		public RegisterComponent put() {
			if(put != null) {
				return put;
			} else if(all != null) {
				return all;
			}
			throw new HttpException(405,
				"The specified method: PUT cannot be used for this URL.");
		}

		/**
		 * PATCHメソッドコンポーネントを取得.
		 * @return RegisterComponent コンポーネントが返却されます.
		 */
		public RegisterComponent patch() {
			if(patch != null) {
				return patch;
			} else if(all != null) {
				return all;
			}
			throw new HttpException(405,
				"The specified method: PATCH cannot be used for this URL.");
		}

		/**
		 * 文字列変換.
		 * @param out 文字列出力先のStringBuilderを設定します.
		 * @param spacePos 改行後のスペース入力値を設定します.
		 * @return StringBuilder
		 */
		public StringBuilder toString(StringBuilder out, int spacePos) {
			spacePos += 1;
			ComponentManager.toSpace(out, spacePos).append("all: ");
			if(all == null) {
				out.append("null\n");
			} else {
				out.append("\n");
				all.toString(out, spacePos);
			}
			ComponentManager.toSpace(out, spacePos).append("get: ");
			if(get == null) {
				out.append("null\n");
			} else {
				out.append("\n");
				get.toString(out, spacePos);
			}
			ComponentManager.toSpace(out, spacePos).append("post: ");
			if(post == null) {
				out.append("null\n");
			} else {
				out.append("\n");
				post.toString(out, spacePos);
			}
			ComponentManager.toSpace(out, spacePos).append("delete: ");
			if(delete == null) {
				out.append("null\n");
			} else {
				out.append("\n");
				delete.toString(out, spacePos);
			}
			ComponentManager.toSpace(out, spacePos).append("put: ");
			if(put == null) {
				out.append("null\n");
			} else {
				out.append("\n");
				put.toString(out, spacePos);
			}
			ComponentManager.toSpace(out, spacePos).append("patch: ");
			if(patch == null) {
				out.append("null\n");
			} else {
				out.append("\n");
				patch.toString(out, spacePos);
			}
			return out;
		}
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
		private IndexKeyValueList<String, AnyElement> staticPaths;
		// * や ${...} などのパス条件.
		private AnyElement anyPath;
		// このパスの実行コンポーネント.
		private MethodsComponent methodsComponent;

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
				staticPaths = new IndexKeyValueList<String, AnyElement>();
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
			if(methodsComponent == null) {
				methodsComponent = new MethodsComponent();
			}
			methodsComponent.setComponent(ac);
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
		 * コンポーネントが存在するかチェック.
		 * @return boolean trueの場合、コンポーネントは存在します.
		 */
		public boolean isComponent(Method method) {
			return methodsComponent == null ?
				false : methodsComponent.isComponent(method);
		}

		/**
		 * 終端パスとする実行コンポーネントを取得.
		 * @param method 対象のHTTPメソッドを設定します.
		 * @return RegisterComponent コンポーネントが返却されます.
		 */
		public RegisterComponent getAnyComponent(Method method) {
			return methodsComponent == null ?
				null : methodsComponent.getComponent(method);
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			return toString(buf, 0).toString();
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
				.append("useAnyComponent: [")
				.append(methodsComponent == null ? "Do not have" : "Have got")
				.append("]\n");
			if(methodsComponent != null) {
				methodsComponent.toString(out, spacePos);
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
	// /aaa/bbb/:id/:password/xxx.json
	protected static final int URL_PARAM_TYPE_4 = 4;

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
		} else if(dir.startsWith(":")) {
			return URL_PARAM_TYPE_4;
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
	 * <例> /aaa/bbb/:id/:password/xxx.json
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
			// /aaa/bbb/:id/:password/xxx.json
			case URL_PARAM_TYPE_4:
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
					// /aaa/bbb/:id/:password/xxx.json
					case URL_PARAM_TYPE_4:
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
		len = lst.size();
		String[] ret = new String[len];
		for(int i = 0; i < len; i ++) {
			ret[i] = lst.get(i);
		}
		return ret;
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
	 * any(${id} or *)なAnyComponentを検索.
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
				return searchAnyComponentByAny(out, urls, len, out[0], p);
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
	 * @param method 対象のHTTPメソッドを設定します.
	 * @return AnyElement アスタリスクでURL登録されたAnyComponentが存在する
	 *                    AnyElementが返却されます.
	 */
	private static final AnyElement getAnyElementByLastAsterrisk(AnyElement em, Method method) {
		while(em != null) {
			if(em.isComponent(method) &&
				em.getAnyComponent(method).isLastAsterrisk()) {
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
	 * @param validation Validationを設定します.
	 * @param responseInitialSetting 対象のResponse初期設定を設定します.
	 * @param component 実行コンポーネントを設定します.
	 */
	private static final void putAnyElement(AnyElement topAnyElement, String url, String[] urls,
		int pos, Object[] urlParam, Validation validation, ResponseInitialSetting responseInitialSetting,
		Component component) {
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
		RegisterComponent cmp = new RegisterComponent(
			url, urlParam, validation, responseInitialSetting, component);
		now.setAnyComponent(cmp);
	}

	// 固定パスコンポーネント管理.
	private final IndexKeyValueList<String, MethodsComponent> staticComponent =
		new IndexKeyValueList<String, MethodsComponent>();

	// rootの冗長パスコンポーネント管理.
	private AnyElement rootAnyElement = new AnyElement(0, null);

	// 指定URLの条件が存在しない場合の実行コンポーネント.
	private RegisterComponent notFoundUrlComponent = null;

	// エラー発生時に呼び出すコンポーネントマネージャ.
	private ErrorComponentManager ErrorComponentManager =
		new ErrorComponentManager();

	// Etagマネージャ.
	private EtagManager etagManager = null;

	/**
	 * コンストラクタ.
	 */
	public ComponentManager() {
		etagManager = new EtagManager();
	}

	/**
	 * クリア.
	 */
	public void clear() {
		staticComponent.clear();
		rootAnyElement = new AnyElement(0, null);
		notFoundUrlComponent = null;
		ErrorComponentManager = new ErrorComponentManager();
		etagManager = null;
	}

	/**
	 * 指定URLの条件が存在しない場合の実行コンポーネントをセット.
	 * @param validation 対象のValidationを設定します.
	 * @param responseInitialSetting 対象のResponse初期設定を設定します.
	 * @param component 対象のコンポーネントを設定します.
	 */
	public void put(Validation validation, ResponseInitialSetting responseInitialSetting,
		Component component) {
		if(component instanceof RegisterComponent) {
			component = ((RegisterComponent)component).getComponent();
		}
		// notFoundUrlに登録.
		notFoundUrlComponent = new RegisterComponent(
			"/*", null, validation, responseInitialSetting, component);
	}

	/**
	 * 指定URLに対してコンポーネントを登録.
	 * @param url 対象のURLを設定します.
	 * @param validation 対象のValidationを設定します.
	 * @param responseInitialSetting 対象のResponse初期設定を設定します.
	 * @param component 対象のコンポーネントを設定します.
	 * @return boolean [true]の場合はstaticなURLとして
	 *                 [false]の場合はanyなURLとして登録されました.
	 */
	public boolean put(String url, Validation validation,
		ResponseInitialSetting responseInitialSetting, Component component) {
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
			// staticコンポーネント管理に追加.
			component = fileComponentByAppendEtagComponent(component);
			MethodsComponent mc = staticComponent.get(url);
			if(mc == null) {
				staticComponent.put(url, (mc = new MethodsComponent()));
			}
			// staticコンポーネント登録.
			mc.setComponent(new RegisterComponent(
				url, null, validation, responseInitialSetting, component));
			return true;
		}
		// urlにurlParamsの条件に対してnullセット.
		urls = getUrlsByAppendUrlParams(urls, urlParam);
		// 対象コンポーネントがFileコンポーネントの場合はEtag管理をセット.
		component = fileComponentByAppendEtagComponent(component);
		// anyコンポーネント管理に追加.
		putAnyElement(this.rootAnyElement, url, urls, 0, urlParam, validation,
			responseInitialSetting, component);
		return false;
	}

	// コンポーネントがファイルコンポーネントの場合は
	// Etag管理オブジェクトをセット.
	private final Component fileComponentByAppendEtagComponent(
		Component component) {
		// ファイル属性コンポーネントに対してEtagManagerを設定.
		if(component instanceof FileAttributeComponent) {
			((FileAttributeComponent)component).setEtagManager(etagManager);
		}
		return component;
	}

	/**
	 * エラー処理用のコンポーネントをセット.
	 * 
	 * ここでは登録方法は少し特殊で、以下のような条件となります.
	 *  startState: 400, endState: 499
	 *   Httpステータスが 400 ～ 499 までの範囲を対象.
	 *  startState: 404, endState: 0
	 *   Httpステータスが 404 を対象.
	 *  startState: 0, endState: 0
	 *   登録内容が当てはまらない場合の全ステータスが対象.
	 * 
	 * @param startState 開始Httpステータスを設定します.
	 * @param endState 終了Httpステータスを設定します.
	 * @param component 対象のコンポーネントを設定します.
	 */
	public void putError(int startState, int endState, ErrorComponent component) {
		if(startState <= 0) {
			// 全ステータス対応エラーとして登録.
			ErrorComponentManager.any(component);
		} else if(endState <= 0) {
			// 指定ステータスのエラーとして登録.
			ErrorComponentManager.putSingle(startState, component);
		} else {
			// 範囲指定のエラーとして登録.
			ErrorComponentManager.putRange(startState, endState, component);
		}
	}
	
	/**
	 * エラー時の登録コンポーネントを取得.
	 * @param state 対象のHttpステータスを設定します.
	 * @return ErrorComponent エラー時のコンポーネントが返却されます.
	 */
	public ErrorComponent getError(int state) {
		return ErrorComponentManager.get(state);
	}

	/**
	 * 指定URLに対するコンポーネントを取得.
	 * @param url urlを設定します.
	 * @param method 対象のHTTPメソッドを設定します.
	 * @return Component 実行コンポーネントが返却されます.
	 */
	public RegisterComponent get(String url, Method method) {
		return get(url, getUrls(url), method);
	}

	/**
	 * 指定URLに対するコンポーネントを取得.
	 * @param url urlを設定します.
	 * @param urls [/]で配列化されたURLを設定します.
	 * @param method 対象のHTTPメソッドを設定します.
	 * @return Component 実行コンポーネントが返却されます.
	 */
	public RegisterComponent get(String url, String[] urls, Method method) {
		// ダイレクトにURL指定でコンポーネント取得.
		if(staticComponent.containsKey(url)) {
			// 存在する場合は返却.
			return staticComponent.get(url).getComponent(method);
		}
		final AnyElement[] out = new AnyElement[1];
		// any要素を取得.
		final boolean res = getAnyElement(out, this.rootAnyElement, urls);
		// 存在しない場合.
		RegisterComponent rcmp = null;
		if(!res || out[0] == null || (rcmp = out[0].getAnyComponent(method)) == null) {
			// URLの終端がアスタリスクのAnyElementを取得する.
			out[0] = getAnyElementByLastAsterrisk(out[0], method);
			// 存在しない場合.
			if(out[0] == null || (rcmp = out[0].getAnyComponent(method)) == null) {
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
					append("@static: ").append(" key: \"").
					append(now.staticPaths.keyAt(i)).append("\"\n");
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
	 * Etag管理定義情報を取得.
	 * @return EtagManagerInfo Etag管理定義情報が返却されます.
	 */
	public EtagManagerInfo getEtagManagerInfo() {
		return etagManager.getInfo();
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
		// エラー発生時のコンポーネントマネージャ.
		buf.append("*ErrorComponentManager: ");
		ErrorComponentManager.toString(2, buf)
			.append("\n");
		return buf.toString();
	}
}
