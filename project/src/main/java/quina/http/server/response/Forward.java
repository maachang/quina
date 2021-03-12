package quina.http.server.response;

import quina.QuinaException;

/**
 * 別のコンポーネントに対してフォワードします.
 */
public class Forward extends QuinaException {
	// フォワード先パス.
	private String path;

	/**
	 * コンストラクタ.
	 * @param path フォワード先のコンポーネントパスを設定します.
	 */
	public Forward(String path) {
		super(200);
		this.path = path;
	}

	/**
	 * フォワード先のコンポーネントパスを取得.
	 * @return String フォワード先のコンポーネントパスが返却されます.
	 */
	public String getPath() {
		return path;
	}

}
