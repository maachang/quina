package quina.component.file;

import quina.annotation.route.AnnotationRoute;
import quina.exception.QuinaException;
import quina.http.HttpException;
import quina.http.MimeTypes;
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
			throw new QuinaException(
				"@FilePath annotation is not set.");
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
			throw new QuinaException(
				"Target directory is not set.");
		} else if(!FileUtil.isDir(dir)) {
			throw new QuinaException(
				"The specified directory \"" +
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
	public void call(Request req, Response<?> res) {
		// 要求URLパスとローカルディレクトリ名をマージ.
		String path = FileComponentUtil.urlAndComponentUrlByMargeLocalPath(
			req.getComponentUrl(), req.getComponentUrlSlashCount(),
			req.getUrl(), targetDir);
		// 拡張子に対するmimeTypeをResponseヘッダにセット.
		String mimeType = MimeTypes.getInstance()
			.getFileNameToMimeType(req.getUrl());
		if(mimeType != null) {
			res.setContentType(mimeType);
		}
		// 拡張子がgzのファイルが存在しない場合.
		if(!FileUtil.isFile(path + ".gz")) {
			// 対象のファイルが存在しない場合.
			if(!FileUtil.isFile(path)) {
				// 404エラーを返却.
				throw new HttpException(404);
			}
			// GzipモードはOff.
			res.setGzip(false);
		// 拡張子がgzのファイルが存在する場合.
		} else {
			// GzipモードはOn.
			res.setGzip(true);
			// gzipモードを正しく設定できた場合.
			if(res.isGzip()) {
				// gz拡張子のファイルを対象とする.
				path = path + ".gz";
			}
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
