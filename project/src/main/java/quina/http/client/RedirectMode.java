package quina.http.client;

import quina.util.Alphabet;

/**
 * HttpClientのリダイレクトモード.
 */
public enum RedirectMode {
	/**
	 * リダイレクトを許可.
	 */
	follow(false, "follow"),
	/**
	 * リダイレクトエラー.
	 */
	error(true, "error");

	private boolean errorMode;
	private String name;

	private RedirectMode(boolean errorMode, String name) {
		this.errorMode = errorMode;
		this.name = name;
	}

	/**
	 * リダイレクトが発生した場合、例外返却するかチェック.
	 * @return
	 */
	public boolean isErrorMode() {
		return errorMode;
	}

	/**
	 * リダイレクトモード名を取得.
	 * @return
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * リダイレクトモードを取得.
	 * @param name 文字列を設定します.
	 * @return
	 */
	public static final RedirectMode get(String name) {
		if(Alphabet.eq("follow", name)) {
			return RedirectMode.follow;
		} else if(Alphabet.eqArray(name, "err", "error") != -1) {
			return RedirectMode.error;
		}
		return RedirectMode.follow;
	}
}
