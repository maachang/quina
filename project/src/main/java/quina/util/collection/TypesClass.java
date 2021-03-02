package quina.util.collection;

import quina.util.Alphabet;

/**
 * タイプ変換定義.
 */
public enum TypesClass {
	Boolean(1, "boolean"),
	Byte(2, "byte"),
	Short(3, "short"),
	Integer(4, "integer"),
	Long(5, "long"),
	Float(6, "float"),
	Double(7, "double"),
	String(8, "string"),
	Date(9, "date");

	private int type;
	private String name;

	/**
	 * コンストラクタ.
	 * @param type
	 * @param name
	 */
	private TypesClass(int type, String name) {
		this.type = type;
		this.name = name;
	}

	/**
	 * タイプを取得.
	 * @return
	 */
	public int getType() {
		return type;
	}

	/**
	 * 名前を取得.
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
	 * 文字列から適切なTypesClassを取得.
	 * @param type
	 * @return
	 */
	public static final TypesClass get(String type) {
		if(type == null || (type = type.trim()).isEmpty()) {
			return String;
		}
		type = Alphabet.toLowerCase(type);
		if(type.equals("int") || type.equals("integer")) {
			return Integer;
		} else if(type.equals("string")) {
			return String;
		} else if(type.equals("boolean") || type.equals("bool")) {
			return Boolean;
		} else if(type.equals("long")) {
			return Long;
		} else if(type.equals("double")) {
			return Double;
		} else if(type.equals("date")) {
			return Date;
		} else if(type.equals("float")) {
			return Float;
		} else if(type.equals("byte")) {
			return Byte;
		} else if(type.equals("short")) {
			return Short;
		}
		return String;
	}

	/**
	 * 数値から適切なTypesClassを取得.
	 * @param type
	 * @return
	 */
	public static final TypesClass get(int type) {
		switch(type) {
		case 1: return Boolean;
		case 2: return Byte;
		case 3: return Short;
		case 4: return Integer;
		case 5: return Long;
		case 6: return Float;
		case 7: return Double;
		case 8: return String;
		case 9: return Date;
		}
		return String;
	}
}
