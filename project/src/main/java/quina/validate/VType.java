package quina.validate;

import java.util.List;
import java.util.Map;

import quina.exception.QuinaException;
import quina.json.Json;
import quina.util.Alphabet;
import quina.util.BooleanUtil;
import quina.util.DateUtil;
import quina.util.NumberUtil;
import quina.util.StringUtil;

/**
 * Validateタイプ.
 */
public enum VType {
	/**
	 * 文字列.
	 */
	String(1, "string"),
	/**
	 * Bool.
	 */
	Boolean(2, "boolean"),
	/**
	 * 数字.
	 */
	Number(10, "number"),
	/**
	 * 32bit整数.
	 */
	Integer(11, "integer"),
	/**
	 * 64bit整数.
	 */
	Long(12, "long"),
	/**
	 * 浮動小数点.
	 */
	Float(13, "float"),
	/**
	 * 64bit浮動小数点.
	 */
	Double(14, "double"),
	/**
	 * java.util.Date.
	 */
	Date(20, "date"),
	/**
	 * java.util.Map.
	 */
	Map(30, "map"),
	/**
	 * java.util.List.
	 */
	List(31, "list");

	
	// タイプID.
	private int type;
	// タイプ名.
	private String name;

	private VType(int type, String name) {
		this.type = type;
		this.name = name;
	}

	/**
	 * タイプのIDを取得.
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
	 * 文字列からVTypeに変換.
	 * @param type 文字列を設定します.
	 * @return VType VTypeが返却されます.
	 */
	public static final VType getStringByVType(String type) {
		if (Alphabet.eq(type, "long")) {
			return Long;
		} else if (Alphabet.eq(type, "float")) {
			return Float;
		} else if (Alphabet.eq(type, "double")) {
			return Double;
		} else if (Alphabet.eq(type, "map")) {
			return Map;
		} else if (Alphabet.eqArray(type, "str", "string") != -1) {
			return String;
		} else if (Alphabet.eqArray(type, "num", "number") != -1) {
			return Number;
		} else if (Alphabet.eqArray(type, "int", "integer") != -1) {
			return Integer;
		} else if (Alphabet.eqArray(type, "array", "list") != -1) {
			return List;
		} else if (Alphabet.eqArray(type, "bool", "boolean") != -1) {
			return Boolean;
		} else if (Alphabet.eqArray(type, "date", "datetime", "timestamp") != -1) {
			return Date;
		} else {
			return String;
		}
	}

	/**
	 * パラメータ変換.
	 * @param type VTypeを設定します.
	 * @param value 変換対象のvalueを設定します.
	 * @return Object 変換結果が返却されます.
	 */
	public static final Object convert(VType type, Object value) {
		return convert(type, null, value);
	}

	/**
	 * パラメータ変換.
	 * @param type VTypeを設定します.
	 * @param column 対象のカラム名を設定します.
	 * @param value 変換対象のvalueを設定します.
	 * @return Object 変換結果が返却されます.
	 */
	public static final Object convert(VType type, String column, Object value) {
		try {
			if (value == null) {
				value = null;
			}
			switch (type) {
			case String:
				try {
					value = StringUtil.parseString(value);
				} catch (Exception e) {
					value = null;
				}
				break;
			case Number:
				if (NumberUtil.isNumeric(value)) {
					if (NumberUtil.isFloat(value)) {
						value = NumberUtil.parseDouble(value);
					} else {
						Integer v1 = NumberUtil.parseInt(value);
						Long v2 = NumberUtil.parseLong(value);
						if ((long) v1 == v2) {
							value = v1;
						} else {
							value = v2;
						}
					}
				} else {
					value = null;
				}
				break;
			case Integer:
				if (NumberUtil.isNumeric(value)) {
					value = NumberUtil.parseInt(value);
				} else {
					value = null;
				}
				break;
			case Long:
				if (NumberUtil.isNumeric(value)) {
					value = NumberUtil.parseLong(value);
				} else {
					value = null;
				}
				break;
			case Float:
				if (NumberUtil.isNumeric(value)) {
					Float v1 = NumberUtil.parseFloat(value);
					Double v2 = NumberUtil.parseDouble(value);
					if((double)v1 == v2) {
						return v1;
					}
					return v2;
				} else {
					value = null;
				}
				break;
			case Double:
				if (NumberUtil.isNumeric(value)) {
					value = NumberUtil.parseDouble(value);
				} else {
					value = null;
				}
				break;
			case Date:
				if(!(value instanceof java.util.Date)) {
					try {
						value = DateUtil.parseDate(value);
					} catch (Exception e) {
						value = null;
					}
				}
				break;
			case Boolean:
				try {
					value = BooleanUtil.parseBoolean(value);
				} catch (Exception e) {
					value = null;
				}
				break;
			case Map:
				if (value instanceof Map) {
					return value;
				}
				try {
					value = Json.decode(StringUtil.parseString(value));
				} catch (Exception e) {
					value = null;
				}
				break;
			case List:
				if (value instanceof List) {
					return value;
				}
				value = null;
				break;
			default:
				try {
					value = StringUtil.parseString(value);
				} catch (Exception e) {
					value = null;
				}
				break;
			}
		} catch (ValidationException ve) {
			throw ve;
		} catch (Exception e) {
			if(column != null) {
				throw new QuinaException(
					"The conversion condition " + type +
					" failed for the target column '" +
					column + "':" + value, e);
			}
			throw new QuinaException(
				"Conversion condition " + type +
				" failed:" + value, e);
		}
		return value;
	}

}
