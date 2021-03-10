package quina.validate;

import quina.util.Alphabet;

/**
 * ValidateCheckType.
 */
enum VCheckType {
	None("none", 0),
	Not("not", 0),
	Null("null", 0),
	Date("date", 0),
	Time("time", 0),
	Zip("zip", 0),
	Tel("tel", 0),
	Ipv4("ipv4", 0),
	Url("url", 0),
	Email("email", 0),
	Min("min", 1),
	Max("max", 1),
	Range("range", 2),
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
		} else if(Alphabet.eq("not", value)) {
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
