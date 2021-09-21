package quina.util.collection;

import quina.util.Alphabet;
import quina.util.BooleanUtil;
import quina.util.DateUtil;
import quina.util.NumberUtil;
import quina.util.StringUtil;

/**
 * タイプ変換定義.
 */
public enum TypesClass {
	Boolean(1, "boolean", false, false, true, false, false),
	Byte(2, "byte", true, false, false, false, false),
	Short(3, "short", true, false, false, false, false),
	Integer(4, "integer", true, false, false, false, false),
	Long(5, "long", true, false, false, false, false),
	Float(6, "float", true, true, false, false, false),
	Double(7, "double", true, true ,false, false, false),
	String(8, "string", false, false, false, true, false),
	Date(9, "date", false, false, false, false, true);
	
	private int type;
	private String name;
	private boolean numberFlag;
	private boolean floatFlag;
	private boolean booleanFlag;
	private boolean stringFlag;
	private boolean dateFlag;
	
	/**
	 * コンストラクタ.
	 * @param type
	 * @param name
	 */
	private TypesClass(int type, String name, boolean numberFlag, boolean floatFlag,
		boolean booleanFlag, boolean stringFlag, boolean dateFlag) {
		this.type = type;
		this.name = name;
		this.numberFlag = numberFlag;
		this.floatFlag = floatFlag;
		this.booleanFlag = booleanFlag;
		this.stringFlag = stringFlag;
		this.dateFlag = dateFlag;
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
	
	/**
	 * この情報がNumberオブジェクトの場合.
	 * @return boolean trueの場合Numberオブジェクトです.
	 */
	public boolean isNumber() {
		return numberFlag;
	}
	
	/**
	 * この情報がFloat(浮動小数点)オブジェクトの場合.
	 * @return boolean trueの場合Floatオブジェクトです.
	 */
	public boolean isFloat() {
		return floatFlag;
	}
	
	/**
	 * この情報がBooleanオブジェクトの場合.
	 * @return boolean trueの場合Booleanオブジェクトです.
	 */
	public boolean isBoolean() {
		return booleanFlag;
	}

	/**
	 * この情報がStringオブジェクトの場合.
	 * @return boolean trueの場合Stringオブジェクトです.
	 */
	public boolean isString() {
		return stringFlag;
	}
	
	/**
	 * この情報がDateオブジェクトの場合.
	 * @return boolean trueの場合Dateオブジェクトです.
	 */
	public boolean isDate() {
		return dateFlag;
	}


	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * 指定Valueを設定して、このTypeClassに合わせて変換する.
	 * @param value 変換対象の要素を設定します.
	 * @return Object 変換結果が返却されます.
	 */
	public Object getValue(Object v) {
		// 対象が文字列で容量定義された内容の場合.
		if(v instanceof String && !(((String)v).isEmpty()) &&
			NumberUtil.isCapacityString((String)v)) {
			// 容量変換を行う.
			v = NumberUtil.parseCapacityByLong((String)v);
		}
		// 通常変換の場合.
		switch(type) {
		case 1: return BooleanUtil.parseBoolean(v);
		case 2: return NumberUtil.parseByte(v);
		case 3: return NumberUtil.parseShort(v);
		case 4: return NumberUtil.parseInt(v);
		case 5: return NumberUtil.parseLong(v);
		case 6: return NumberUtil.parseFloat(v);
		case 7: return NumberUtil.parseDouble(v);
		case 8: return StringUtil.parseString(v);
		case 9: return DateUtil.parseDate(v);
		default: return StringUtil.parseString(v);
		}
	}

	/**
	 * オブジェクトから適切なTypesClassを取得.
	 * @param o 対象のオブジェクトを設定します.
	 * @return TypesClsss 対象の型が返却されます.
	 *         nullの場合検出出来ませんでした.
	 */
	public static final TypesClass get(Object o) {
		String clazz;
		if(o instanceof TypesClass) {
			return (TypesClass)o;
		} else if(o instanceof Number) {
			return TypesClass.getByTypeNo(((Number)o).intValue());
		} else if(o instanceof String) {
			clazz = (String)o;
		} else {
			clazz = o.toString();
		}
		if(clazz == null || (clazz = clazz.trim()).isEmpty()) {
			// 検出出来ない場合.
			return null;
		}
		clazz = Alphabet.toLowerCase(clazz);
		if(clazz.equals("int") || clazz.equals("integer")) {
			return Integer;
		} else if(clazz.equals("string")) {
			return String;
		} else if(clazz.equals("boolean") || clazz.equals("bool")) {
			return Boolean;
		} else if(clazz.equals("long")) {
			return Long;
		} else if(clazz.equals("double")) {
			return Double;
		} else if(clazz.equals("date")) {
			return Date;
		} else if(clazz.equals("float")) {
			return Float;
		} else if(clazz.equals("byte")) {
			return Byte;
		} else if(clazz.equals("short")) {
			return Short;
		}
		// 検出出来ない場合.
		return null;
	}

	/**
	 * 数値から適切なTypesClassを取得.
	 * @param no 番号を設定します.
	 * @return TypesClass TypesClassが返却されます.
	 *         nullの場合検出出来ませんでした.
	 */
	public static final TypesClass getByTypeNo(int no) {
		switch(no) {
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
		// 検出出来ない場合.
		return null;
	}
}
