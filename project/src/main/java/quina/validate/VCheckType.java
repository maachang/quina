package quina.validate;

import quina.util.Alphabet;

/**
 * ValidateCheckType.
 */
enum VCheckType {
	/**
	 * 定義なし.
	 */
	None("none", 0),
	/**
	 * 比較の反転.
	 */
	Not("not", 0),
	/**
	 * NULL(文字列の場合は空)の場合エラー.
	 */
	Null("null", 0),
	/**
	 * 日付フォーマットでない場合エラー.
	 */
	Date("date", 0),
	/**
	 * 時間フォーマットでない場合エラー.
	 */
	Time("time", 0),
	/**
	 * 郵便番号でない場合エラー.
	 */
	Zip("zip", 0),
	/**
	 * 電話番号でない場合エラー.
	 */
	Tel("tel", 0),
	/**
	 * IPアドレスでない場合エラー.
	 */
	Ipv4("ipv4", 0),
	/**
	 * URLでない場合エラー.
	 */
	Url("url", 0),
	/**
	 * EMAILでない場合エラー.
	 */
	Email("email", 0),
	/**
	 * 指定正規表現と不一致でエラー.
	 */
	EXP("exp", 1),
	/**
	 * 比較元 < パラメータ でエラー.
	 */
	LT("lt", 1),
	/**
	 * 比較元 <= パラメータ でエラー.
	 */
	LE("le", 1),
	/**
	 * 比較元 > パラメータ でエラー.
	 */
	GT("gt", 1),
	/**
	 * 比較元 >= パラメータ でエラー.
	 */
	GE("ge", 1),
	/**
	 * 比較元 >= パラメータ でエラー.
	 */
	Min("min", 1),
	/**
	 * 比較元 <= パラメータ でエラー.
	 */
	Max("max", 1),
	/**
	 * 比較元 >= パラメータ AND 比較元 <= パラメータ でエラー.
	 */
	Range("range", 2),
	/**
	 * 条件が設定されてない場合にデフォルト値を設定.
	 */
	Default("default", 1);

	// 名前.
	private String name;
	// パラメータ数.
	private int argsLength;

	// コンストラクタ.
	private VCheckType(String name, int argsLength) {
		this.name = name;
		this.argsLength = argsLength;
	}

	/**
	 * 名前を取得.
	 * @return String 名前が返却されます.
	 */
	public String getName() {
		return name;
	}

	/**
	 * パラメータ数を取得.
	 * @return int パラメータ数が返却されます.
	 */
	public int getArgsLength() {
		return argsLength;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * validate文字列を解析してVCheckType群を取得.
	 * @param value 対象の文字列を設定します.
	 * @return VCheckType ValidateCheckTypeが返却されます.
	 */
	public static final VCheckType getStringByVCheckType(String value) {
		if(Alphabet.eq("none", value)) {
			return None;
		} else if(Alphabet.eq("not", value) || "!".equals(value)) {
			return Not;
		} else if(Alphabet.eq("null", value)) {
			return Null;
		} else if(Alphabet.eq("date", value)) {
			return Date;
		} else if(Alphabet.eq("time", value)) {
			return Time;
		} else if(Alphabet.eq("zip", value)) {
			return Zip;
		} else if(Alphabet.eq("tel", value) || Alphabet.eq("telephone", value)) {
			return Tel;
		} else if(Alphabet.eq("ip", value) || Alphabet.eq("ipv4", value)) {
			return Ipv4;
		} else if(Alphabet.eq("url", value)) {
			return Url;
		} else if(Alphabet.eq("email", value)) {
			return Email;
		} else if(Alphabet.eq("exp", value)) {
			return EXP;
		} else if(Alphabet.eq("lt", value) || Alphabet.eq("<", value)) {
			return LT;
		} else if(Alphabet.eq("le", value) || Alphabet.eq("<=", value)) {
			return LE;
		} else if(Alphabet.eq("gt", value) || Alphabet.eq(">", value)) {
			return GT;
		} else if(Alphabet.eq("ge", value) || Alphabet.eq(">=", value)) {
			return GE;
		} else if(Alphabet.eq("min", value)) {
			return Min;
		} else if(Alphabet.eq("max", value)) {
			return Max;
		} else if(Alphabet.eq("range", value)) {
			return Range;
		} else if(Alphabet.eq("def", value) || Alphabet.eq("default", value)) {
			return Default;
		}
		return null;
	}

}
