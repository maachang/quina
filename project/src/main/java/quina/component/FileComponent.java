package quina.component;

import quina.QuinaException;
import quina.http.Method;
import quina.http.Request;
import quina.http.Response;
import quina.util.FileUtil;

/**
 * ファイルコンポーネント.
 */
public class FileComponent implements Component {
	// ターゲットディレクトリ.
	private String targetDir = null;

	/**
	 * ローカルファイルを返信するコンポーネント.
	 *
	 * 使い方として、たとえばディレクトリ[./target/]以下を
	 * ターゲットディレクトリとして定義して、コンポーネント定義のURLを
	 * [/public/*]の場合に、このコンポーネント呼び出しを定義します。
	 *
	 * ちなみにコンポーネントのURLの最後が[/*]で終わらないものは
	 * エラーになります.
	 *
	 * そして、URL：/public/abc/hoge.jpg が設定された場合、
	 * このコンポーネントは[./target/abc/hoge.jpg]のファイルを返信します.
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

	// スラッシュの数をカウント.
	protected static final int countSlash(final String s) {
		int ret = 0;
		final int len = s.length();
		for(int i = 0; i < len; i ++) {
			if(s.charAt(i) == '/') {
				ret ++;
			}
		}
		return ret;
	}

	// 指定targetCountに対するスラッシュ数の位置を取得.
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

	// URLとコンポーネントURLの差分を取って、ローカルパスを取得.
	protected static final String urlAndComponentUrlByMargeLocalPath(
		final String componentUrl, final String url, final String targetDir) {
		if(componentUrl == null) {
			throw new QuinaException("Component definition URL is not set.");
		} else if(!componentUrl.endsWith("/*")) {
			throw new QuinaException(
				"The end of the component definition URL is not an asterisk.");
		}
		int no = countSlash(componentUrl);
		int pos = positionSlash(url, no);
		if(pos == -1) {
			throw new QuinaException(
				"The conditions of the component definition URL and this URL do not match: "
				+ "componentUrl: " + componentUrl + " url: " + url);
		}
		return targetDir + url.substring(pos);
	}

	@Override
	public ComponentType getType() {
		return ComponentType.FILE;
	}

	@Override
	public void call(Method method, Request req, Response res) {
		final String fileName = urlAndComponentUrlByMargeLocalPath(
			req.getComponentUrl(), req.getUrl(), targetDir);
		if(FileUtil.isFile(fileName)) {
			throw new QuinaException(404);
		}
		res.sendFile(fileName);
	}
}
