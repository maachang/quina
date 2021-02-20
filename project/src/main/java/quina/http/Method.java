package quina.http;

import quina.util.Alphabet;

/**
 * Http-Method定義.
 */
public enum Method {
	GET("GET", false),
	POST("POST", true),
	DELETE("DELETE", true),
	PUT("PUT", true),
	PATCH("PATCH", true),
	OPTIONS("OPTIONS", false),
	TRACE("TRACE", false),
	HEAD("HEAD", false);

	private String name;
	private boolean body;

	/**
	 * コンストラクタ.
	 * @param name
	 */
	private Method(String name, boolean body) {
		this.name = name;
		this.body = body;
	}

	/**
	 * メソッド名を取得.
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Body受信が可能かチェック.
	 * @return
	 */
	public boolean isBody() {
		return body;
	}

	/**
	 * 文字列をMethodに変換.
	 * @param method
	 * @return
	 */
	public static final Method get(String method) {
		if(Alphabet.eq("get", method)) {
			return GET;
		} else if(Alphabet.eq("post", method)) {
			return POST;
		} else if(Alphabet.eq("delete", method)) {
			return DELETE;
		} else if(Alphabet.eq("put", method)) {
			return PUT;
		} else if(Alphabet.eq("patch", method)) {
			return PATCH;
		} else if(Alphabet.eq("options", method)) {
			return OPTIONS;
		} else if(Alphabet.eq("trace", method)) {
			return TRACE;
		} else if(Alphabet.eq("head", method)) {
			return HEAD;
		}
		// 対応していないメソッドの場合はエラー.
		throw new HttpException(405, "Unsupported Http-Method: "
				+ method.toUpperCase());
	}
}
