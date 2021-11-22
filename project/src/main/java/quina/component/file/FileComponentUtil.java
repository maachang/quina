package quina.component.file;

import quina.exception.QuinaException;
import quina.http.HttpException;

/**
 * FileComponentユーティリティ.
 */
public final class FileComponentUtil {
	private FileComponentUtil() {}
	
	/**
	 * 指定targetCountに対するスラッシュ数の位置を取得.
	 * @param s スラッシュのカウントを取る対象の文字列を設定します.
	 * @param targetCount 位置を知りたいスラッシュのカウント位置を設定します.
	 * @return int 文字位置情報が返却されます.
	 *             -1の場合、見つかりませんでした.
	 */
	public static final int positionSlash(
		final String s, final int targetCount) {
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

	/**
	 * 不正なURLかチェック.
	 * @param target ターゲットを設定します.
	 */
	public static final void checkIllegalUrl(String target) {
		char c;
		int dotCount = 0;
		final int len = target.length();
		for(int i = 0; i < len; i ++) {
			if((c = target.charAt(i)) == '*' || c == '?' ||
				c == '\"' || c == '<' || c == '>' || c == '|' ||
				c == ';' || c == ':' || c == ',' || c == '\\') {
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
	public static final String urlAndComponentUrlByMargeLocalPath(
		final String componentUrl, final int countSlash, final String url,
		final String targetDir) {
		if(componentUrl == null) {
			throw new HttpException("Component definition URL is not set.");
		} else if(!componentUrl.endsWith("/*")) {
			throw new HttpException(
				"The end of the component definition URL is not an asterisk: " +
				componentUrl);
		}
		int pos = FileComponentUtil.positionSlash(url, countSlash);
		if(pos == -1) {
			// []ファイルが指定されていない場合.
			throw new HttpException(404);
		}
		final String target = url.substring(pos).trim();
		FileComponentUtil.checkIllegalUrl(target);
		if(target.isEmpty()) {
			// [/]ファイルが指定されていない場合
			throw new HttpException(404);
		}
		return targetDir + target;
	}

}
