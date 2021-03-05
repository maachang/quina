package quina.component;

import quina.QuinaException;
import quina.http.Method;
import quina.http.Request;
import quina.http.Response;
import quina.http.response.AbstractResponse;
import quina.http.response.ResponseUtil;
import quina.util.FileUtil;

/**
 * ファイルコンポーネント.
 */
public class FileComponent implements Component {
	// ターゲットディレクトリ.
	protected String targetDir = null;

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
	 * @param dir ターゲットディレクトリを設定します.
	 */
	public FileComponent(String dir) {
		init(dir);
	}

	/**
	 * 初期処理.
	 * @param dir ターゲットディレクトリを設定します.
	 */
	protected void init(String dir) {
		if(dir == null || dir.isEmpty()) {
			throw new QuinaException("Target directory is not set.");
		}
		try {
			dir = FileUtil.getFullPath(dir);
			if(!dir.endsWith("/")) {
				dir += "/";
			}
			targetDir = dir;
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
				// ドットをカウントで取得.
				dotCount ++;
			} else if(c == '/' && dotCount >= 2) {
				// [../]が含まれてる場合はエラー.
				throw new QuinaException(
					"An invalid character string is set in the specified URL.");
			} else {
				// それ以外の場合はドットカウントをクリア.
				dotCount = 0;
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
	public ComponentType getType() {
		return ComponentType.FILE;
	}

	@Override
	public void call(Method method, Request req, Response<?> res) {
		final String fileName = urlAndComponentUrlByMargeLocalPath(
			req.getComponentUrl(), req.getComponentUrlSlashCount(),
			req.getUrl(), targetDir);
		if(FileUtil.isFile(fileName)) {
			throw new QuinaException(404);
		}
		ResponseUtil.sendFile((AbstractResponse<?>)res, fileName);
	}
}
