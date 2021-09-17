package quina.component;

import quina.annotation.route.AnnotationRoute;
import quina.exception.QuinaException;
import quina.http.Method;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.ResponseUtil;
import quina.util.Env;
import quina.util.FileUtil;

/**
 * ファイルコンポーネント.
 */
public class FileComponent implements FileAttributeComponent {
	// ターゲットディレクトリ.
	protected String targetDir = null;

	// EtagManager.
	protected EtagManager etagManager = null;
	
	// キャッシュを有効にするか設定.
	protected Boolean cacheMode = null;

	/**
	 * ローカルファイルを返信するコンポーネント.
	 *
	 * 使い方として、たとえばディレクトリ[./target/]をターゲットディレクトリ
	 * として定義して、コンポーネント定義のURLを[/public/*]としたとします。
	 *
	 * そして、URL：/public/abc/hoge.jpg が設定された場合、
	 * このコンポーネントは[./target/abc/hoge.jpg]のファイルを返信します.
	 *
	 * ちなみにコンポーネント定義のURLの最後が[/*]で終わらないものは
	 * エラーになります。
	 */

	/**
	 * コンストラクタ.
	 */
	public FileComponent() {
		this((Boolean)null);
	}
	
	/**
	 * コンストラクタ.
	 * @param cacheMode [true]に設定する事でEtagによるキャッシュが有効になります.
	 *                  nullを設定した場合はデフォルトのキャッシュモードになります.
	 */
	public FileComponent(Boolean cacheMode) {
		// Annotationからファイルパスを取得.
		String dir = AnnotationRoute.loadFilePath(this);
		if(dir == null) {
			// 設定されてない場合はエラー.
			throw new QuinaException("@FilePath annotation is not set.");
		}
		init(cacheMode, dir);
	}
	
	/**
	 * コンストラクタ.
	 * @param dir ターゲットディレクトリを設定します.
	 */
	public FileComponent(String dir) {
		init(null, dir);
	}
	
	/**
	 * コンストラクタ.
	 * @param cacheMode [true]に設定する事でEtagによるキャッシュが有効になります.
	 *                  nullを設定した場合はデフォルトのキャッシュモードになります.
	 * @param dir ターゲットディレクトリを設定します.
	 */
	public FileComponent(Boolean cacheMode, String dir) {
		init(cacheMode, dir);
	}
	
	/**
	 * 初期処理.
	 * @param cacheMode [true]に設定する事でEtagによるキャッシュが有効になります.
	 *                  nullを設定した場合はデフォルトのキャッシュモードになります.
	 * @param dir ターゲットディレクトリを設定します.
	 */
	protected void init(Boolean cacheMode, String dir) {
		if(dir == null || dir.isEmpty()) {
			throw new QuinaException("Target directory is not set.");
		} else if(!FileUtil.isDir(dir)) {
			throw new QuinaException("The specified directory \"" +
				dir + "\" does not exist. ");
		}
		try {
			dir = FileUtil.getFullPath(Env.path(dir));
			if(!dir.endsWith("/")) {
				dir += "/";
			}
			this.targetDir = dir;
			this.cacheMode = cacheMode;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}

	/**
	 * 指定targetCountに対するスラッシュ数の位置を取得.
	 * @param s スラッシュのカウントを取る対象の文字列を設定します.
	 * @param targetCount 位置を知りたいスラッシュのカウント位置を設定します.
	 * @return int 文字位置情報が返却されます.
	 *             -1の場合、見つかりませんでした.
	 */
	protected static final int positionSlash(final String s, final int targetCount) {
		int cnt = 0;
		final int len = s.length();
		for(int i = 0; i < len; i ++) {
			if(s.charAt(i) == '/') {
				cnt ++;
				if(targetCount == cnt) {
					return i + 1;
				}
			}
		}
		return -1;
	}

	// 不正なURLかチェック.
	protected static final void checkIllegalUrl(String target) {
		char c;
		int dotCount = 0;
		final int len = target.length();
		for(int i = 0; i < len; i ++) {
			if((c = target.charAt(i)) == '*' || c == '?' || c == '\"' ||
				c == '<' || c == '>' || c == '|' || c == ';' || c == ':' ||
				c == ',' || c == '\\') {
				// 禁止文字が含まれる場合はエラー.
				throw new QuinaException(
					"An invalid character string is set in the specified URL.");
			} else if(c == '.') {
				// ../ or /../ の可能性の場合.
				if(dotCount >= 0) {
					dotCount ++;
				}
			} else if(c == '/') {
				if(dotCount >= 1) {
					// ./ or /./ or ../ or /../ のような
					// 相対パスを表すものはエラー返却.
					throw new QuinaException(
						"An invalid character string is set in the specified URL.");
				} else {
					// [/] を検知した場合は、dotCountを０にセット.
					dotCount = 0;
				}
			} else if(dotCount >= 1) {
				// .xxx or /.xxx or ..xyz or /..xyz のような
				// 先頭に.が名前の隠しファイルの可能性がある場合.
				throw new QuinaException(
					"An invalid character string is set in the specified URL.");
			} else {
				// dotカウントを / が来るまで無効にする.
				dotCount = -1;
			}
		}
	}

	/**
	 * URLとコンポーネントURLの差分を取って、ローカルパスを取得.
	 * @param componentUrl コンポーネントURLを設定します.
	 * @param countSlash コンポーネントURLのスラッシュ数を設定します.
	 * @param url 今回アクセスされたURLを設定します.
	 * @param targetDir ターゲットディレクトリを設定します.
	 * @return String ローカルパスが返却されます.
	 */
	protected static final String urlAndComponentUrlByMargeLocalPath(
		final String componentUrl, final int countSlash, final String url,
		final String targetDir) {
		if(componentUrl == null) {
			throw new QuinaException("Component definition URL is not set.");
		} else if(!componentUrl.endsWith("/*")) {
			throw new QuinaException(
				"The end of the component definition URL is not an asterisk: " + componentUrl);
		}
		int pos = positionSlash(url, countSlash);
		if(pos == -1) {
			throw new QuinaException(
				"The conditions of the component definition URL and this URL do not match: "
				+ "componentUrl: " + componentUrl + " url: " + url);
		}
		final String target = url.substring(pos);
		checkIllegalUrl(target);
		return targetDir + target;
	}
	
	@Override
	public Boolean getCacheMode() {
		return cacheMode;
	}

	@Override
	public void setEtagManager(EtagManager etagManager) {
		this.etagManager = etagManager;
	}

	@Override
	public EtagManager getEtagManager() {
		return this.etagManager;
	}

	@Override
	public void call(Method method, Request req, Response<?> res) {
		// 要求URLパスとローカルディレクトリ名とマージ.
		String path = urlAndComponentUrlByMargeLocalPath(
			req.getComponentUrl(), req.getComponentUrlSlashCount(),
			req.getUrl(), targetDir);
		// 拡張子がgzのファイルが存在しない場合.
		if(!FileUtil.isFile(path + ".gz")) {
			// 対象のファイルが存在しない場合.
			if(!FileUtil.isFile(path)) {
				// 404エラーを返却.
				throw new QuinaException(404);
			}
			// GzipモードはOff.
			res.setGzip(false);
		// 拡張子がgzのファイルが存在する場合.
		} else {
			// gz拡張子のファイルを対象とする.
			path = path + ".gz";
			// GzipモードはOn.
			res.setGzip(true);
		}
		// キャッシュモードが設定されてる場合.
		if(cacheMode != null) {
			// レスポンスのキャッシュモードに設定.
			res.setCacheMode(cacheMode);
		}
		// キャッシュがONの場合、Etag定義の確認.
		if(etagManager.setResponse(path, req, res)) {
			// 接続元と一致の場合はキャッシュ処理として0byteBody返却.
			ResponseUtil.send((AbstractResponse<?>)res);
		// キャッシュがOffかEtagが存在しない場合.
		} else {
			// 普通に送信.
			ResponseUtil.sendFile((AbstractResponse<?>)res, path);
		}
	}
}
