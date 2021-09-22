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
	Boolean(TypesConstants.TYPENO_BOOLEAN,
		"boolean", false, false, true, false, false),
	Byte(TypesConstants.TYPENO_BYTE,
		"byte", true, false, false, false, false),
	Short(TypesConstants.TYPENO_SHORT,
		"short", true, false, false, false, false),
	Integer(TypesConstants.TYPENO_INTEGER,
		"integer", true, false, false, false, false),
	Long(TypesConstants.TYPENO_LONG,
		"long", true, false, false, false, false),
	Float(TypesConstants.TYPENO_FLOAT,
		"float", true, true, false, false, false),
	Double(TypesConstants.TYPENO_DOUBLE,
		"double", true, true ,false, false, false),
	String(TypesConstants.TYPENO_STRING,
		"string", false, false, false, true, false),
	Date(TypesConstants.TYPENO_DATE,
		"date", false, false, false, false, true);
	
	private int typeNo;
	private String name;
	private boolean numberFlag;
	private boolean floatFlag;
	private boolean booleanFlag;
	private boolean stringFlag;
	private boolean dateFlag;
	
	/**
	 * コンストラクタ.
	 * @param typeNo
	 * @param name
	 * @param numberFlag
	 * @param floatFlag
	 * @param booleanFlag
	 * @param stringFlag
	 * @param dateFlag
	 */
	private TypesClass(int typeNo, String name, boolean numberFlag, boolean floatFlag,
		boolean booleanFlag, boolean stringFlag, boolean dateFlag) {
		this.typeNo = typeNo;
		this.name = name;
		this.numberFlag = numberFlag;
		this.floatFlag = floatFlag;
		this.booleanFlag = booleanFlag;
		this.stringFlag = stringFlag;
		this.dateFlag = dateFlag;
	}

	/**
	 * タイプNoを取得.
	 * @return
	 */
	public int getTypeNo() {
		return typeNo;
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
		// nullの場合、そのまま返却.
		if(v == null) {
			return null;
		// 対象が文字列の場合.
		} else if(v instanceof String) {
			final String s = (String)v;
			// 文字列が空の場合.
			if(s.isEmpty()) {
				// このenumが文字列以外の場合.
				if(!isString()) {
					// null 返却.
					return null;
				}
				// 文字列の場合、空文字を返却.
				return "";
			// このenumのタイプが数字系の場合.
			// 対象文字列が容量変換の形式の場合.
			} else if(isNumber() &&
				NumberUtil.isCapacityString(s)) {
				// 容量変換を行う.
				v = NumberUtil.parseCapacityByLong(s);
			}
		}
		// 通常変換の場合.
		switch(typeNo) {
		case TypesConstants.TYPENO_BOOLEAN:
			return BooleanUtil.parseBoolean(v);
		case TypesConstants.TYPENO_BYTE:
			return NumberUtil.parseByte(v);
		case TypesConstants.TYPENO_SHORT:
			return NumberUtil.parseShort(v);
		case TypesConstants.TYPENO_INTEGER:
			return NumberUtil.parseInt(v);
		case TypesConstants.TYPENO_LONG:
			return NumberUtil.parseLong(v);
		case TypesConstants.TYPENO_FLOAT:
			return NumberUtil.parseFloat(v);
		case TypesConstants.TYPENO_DOUBLE:
			return NumberUtil.parseDouble(v);
		case TypesConstants.TYPENO_STRING:
			return StringUtil.parseString(v);
		case TypesConstants.TYPENO_DATE:
			return DateUtil.parseDate(v);
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
		if(clazz == null ||
			(clazz = clazz.trim()).isEmpty()) {
			// 検出出来ない場合.
			return null;
		}
		clazz = Alphabet.toLowerCase(clazz);
		if(clazz.equals("int") ||
			clazz.equals("integer")) {
			return Integer;
		} else if(clazz.equals("string")) {
			return String;
		} else if(clazz.equals("boolean") ||
			clazz.equals("bool")) {
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
		case TypesConstants.TYPENO_BOOLEAN: return Boolean;
		case TypesConstants.TYPENO_BYTE: return Byte;
		case TypesConstants.TYPENO_SHORT: return Short;
		case TypesConstants.TYPENO_INTEGER: return Integer;
		case TypesConstants.TYPENO_LONG: return Long;
		case TypesConstants.TYPENO_FLOAT: return Float;
		case TypesConstants.TYPENO_DOUBLE: return Double;
		case TypesConstants.TYPENO_STRING: return String;
		case TypesConstants.TYPENO_DATE: return Date;
		}
		// 検出出来ない場合.
		return null;
	}
}
