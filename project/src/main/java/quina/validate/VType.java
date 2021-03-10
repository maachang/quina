package quina.validate;

import java.util.List;
import java.util.Map;

import quina.QuinaException;
import quina.json.Json;
import quina.util.BooleanUtil;
import quina.util.DateUtil;
import quina.util.NumberUtil;
import quina.util.StringUtil;

/**
 * Validateタイプ.
 */
public enum VType {
	String(1, "string"),
	Boolean(2, "boolean"),
	Number(10, "number"),
	Integer(11, "integer"),
	Long(12, "long"),
	Float(13, "float"),
	Date(20, "date"),
	Map(30, "map"),
	List(31, "list");

	private int type;
	private String name;

	private VType(int type, String name) {
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
	 * 文字列からVTypeに変換.
	 * @param type 文字列を設定します.
	 * @return VType VTypeが返却されます.
	 */
	public static final VType getStringByVType(String type) {
		if ("string".equals(type)) {
			return String;
		} else if ("number".equals(type)) {
			return Number;
		} else if ("int".equals(type) || "integer".equals(type)) {
			return Integer;
		} else if ("long".equals(type)) {
			return Long;
		} else if ("float".equals(type) || "double".equals(type)) {
			return Float;
		} else if ("date".equals(type)) {
			return Date;
		} else if ("bool".equals(type) || "boolean".equals(type)) {
			return Boolean;
		} else if ("map".equals(type)) {
			return Map;
		} else if ("array".equals(type) || "list".equals(type)) {
			return List;
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
		} catch (ValidateException ve) {
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
