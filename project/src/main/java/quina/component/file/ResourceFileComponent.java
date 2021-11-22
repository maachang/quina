package quina.component.file;

import java.io.InputStream;

import quina.annotation.route.AnnotationRoute;
import quina.component.Component;
import quina.component.ComponentConstants;
import quina.component.ComponentType;
import quina.exception.QuinaException;
import quina.http.HttpException;
import quina.http.MimeTypes;
import quina.http.Request;
import quina.http.Response;
import quina.http.server.response.AbstractResponse;
import quina.http.server.response.ResponseUtil;
import quina.util.StringUtil;

/**
 * Thread.currentThread().getClassLoader().getResourceAsStream(xxxx)
 * の条件で、Jar内のリソース情報を取得して返却処理します.
 */
public class ResourceFileComponent implements Component {
	// ターゲットリソースパッケージ.
	protected String targetResourcePackage = null;
	
	/**
	 * 指定jarやclass内のリソースファイルを返信するコンポーネント.
	 *
	 * 使い方として、たとえばリソースパッケージ[quina.jdbc.console.resource]を
	 * ターゲットとして定義して、コンポーネント定義のURLを[/quina/jdbc/console/*]と
	 * したとします。
	 *
	 * そして、URL：/quina/jdbc/console/login.html が設定された場合、
	 * このコンポーネントはリソースファイル[quina.jdbc.console.resource.login.html]の
	 * ファイルを返信します.
	 *
	 * ちなみにコンポーネント定義のURLの最後が[/*]で終わらないものは
	 * エラーになります。
	 */
	
	/**
	 * コンストラクタ.
	 */
	public ResourceFileComponent() {
		init(AnnotationRoute.loadResourcePackage(this));
	}
	
	/**
	 * コンストラクタ.
	 * @param resourcePackage リソースパッケージを設定します.
	 */
	public ResourceFileComponent(String resourcePackage) {
		init(resourcePackage);
	}
	
	// 初期化処理.
	private final void init(String resourcePackage) {
		if(resourcePackage == null ||
			(resourcePackage = resourcePackage.trim()).isEmpty()) {
			throw new QuinaException(
				"Target resource package is not set.");
		}
		// package名の区切[.]を[/]に変換.
		resourcePackage = StringUtil.changeString(
				resourcePackage.trim(), ".", "/");
		// 終端に[/]がある場合除去.
		if(!resourcePackage.endsWith("/")) {
			resourcePackage = resourcePackage + "/";
		}
		this.targetResourcePackage = resourcePackage;
	}
	
	
	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	@Override
	public ComponentType getType() {
		return ComponentType.File;
	}

	/**
	 * 対応HTTPメソッド定義を取得.
	 * @return int このコンポーネントが対応するHTTPメソッド定義が返却されます.
	 */
	@Override
	public int getMethod() {
		return ComponentConstants.HTTP_METHOD_ALL;
	}

	@Override
	public void call(Request req, Response<?> res) {
		// 要求URLパスとリソースパッケージをマージ.
		String path = FileComponentUtil.urlAndComponentUrlByMargeLocalPath(
			req.getComponentUrl(), req.getComponentUrlSlashCount(),
			req.getUrl(), targetResourcePackage);
		// 拡張子に対するmimeTypeをResponseヘッダにセット.
		String mimeType = MimeTypes.getInstance()
			.getFileNameToMimeType(req.getUrl());
		if(mimeType != null) {
			res.setContentType(mimeType);
		}
		// 拡張子がgzのファイルが存在しない場合.
		if(!isResourceFile(path + ".gz")) {
			// 対象のファイルが存在しない場合.
			if(!isResourceFile(path)) {
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
		InputStream in = null;
		try {
			in = getResourceFile(path);
			ResponseUtil.sendInputStream(
				(AbstractResponse<?>)res, in,
				(long)in.available());
		} catch(Exception e) {
			if(in != null) {
				try {
					in.close();
				} catch(Exception ee) {}
			}
			throw new HttpException(e);
		}
	}
	
	// リソースファイルが存在するかチェック.
	private static final boolean isResourceFile(
		String resourceName) {
		return Thread.currentThread()
			.getContextClassLoader()
			.getResource(resourceName) != null;
	}
	
	// リソースファイルを取得.
	private static final InputStream getResourceFile(
		String resourceName) {
		return Thread
			.currentThread()
			.getContextClassLoader()
			.getResourceAsStream(resourceName);
	}
}
