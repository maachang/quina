package quina.http;

import quina.util.Alphabet;

/**
 * Http-Method定義.
 */
public enum Method {
	GET("GET", 0x00000001, false),
	POST("POST", 0x00000002, true),
	DELETE("DELETE", 0x00000004, true),
	PUT("PUT", 0x00000008, true),
	PATCH("PATCH", 0x00000010, true),
	OPTIONS("OPTIONS", 0x00000100, false),
	TRACE("TRACE", 0x00000200, false),
	HEAD("HEAD", 0x00000400, false);

	private String name;
	private int type;
	private boolean body;

	/**
	 * コンストラクタ.
	 * @param name メソッド名を設定します.
	 * @param type メソッドタイプを設定します.
	 * @param body body付与が可能な場合はtrueが設定されます.
	 */
	private Method(String name, int type, boolean body) {
		this.name = name;
		this.type = type;
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
	 * メソッドタイプを取得.
	 * @return
	 */
	public int getType() {
		return type;
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
