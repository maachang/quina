package quina.util.collection;

import quina.util.BooleanUtil;
import quina.util.DateUtil;
import quina.util.NumberUtil;

/**
 * 要素に対する型変換対応.
 */
public interface TypesElement<V> {
	/**
	 * 取得処理.
	 *
	 * @return Object 対象情報が返却されます.
	 */
	public V get();

	/**
	 * boolean情報を取得.
	 *
	 * @return Boolean 情報が返却されます.
	 */
	default Boolean getBool() {
		return getBoolean();
	}

	/**
	 * boolean情報を取得.
	 *
	 * @return Boolean 情報が返却されます.
	 */
	default Boolean getBoolean() {
		Object ret = get();
		if(ret == null) {
			return null;
		}
		return BooleanUtil.parseBoolean(ret);
	}

	/**
	 * Byte情報を取得.
	 *
	 * @return Byte 情報が返却されます.
	 */
	default Byte getByte() {
		Object ret = get();
		if(ret == null) {
			return null;
		}
		return NumberUtil.parseByte(ret);
	}

	/**
	 * short情報を取得.
	 *
	 * @return Short 情報が返却されます.
	 */
	default Short getShort() {
		Object ret = get();
		if(ret == null) {
			return null;
		}
		return NumberUtil.parseShort(ret);
	}

	/**
	 * int情報を取得.
	 *
	 * @return Integer 情報が返却されます.
	 */
	default Integer getInt() {
		return getInteger();
	}

	/**
	 * int情報を取得.
	 *
	 * @return Integer 情報が返却されます.
	 */
	default Integer getInteger() {
		Object ret = get();
		if(ret == null) {
			return null;
		}
		return NumberUtil.parseInt(ret);
	}

	/**
	 * long情報を取得.
	 *
	 * @return Long 情報が返却されます.
	 */
	default Long getLong() {
		Object ret = get();
		if(ret == null) {
			return null;
		}
		return NumberUtil.parseLong(ret);
	}

	/**
	 * float情報を取得.
	 *
	 * @return Float 情報が返却されます.
	 */
	default Float getFloat() {
		Object ret = get();
		if(ret == null) {
			return null;
		}
		return NumberUtil.parseFloat(ret);
	}

	/**
	 * double情報を取得.
	 *
	 * @return Double 情報が返却されます.
	 */
	default Double getDouble() {
		Object ret = get();
		if(ret == null) {
			return null;
		}
		return NumberUtil.parseDouble(ret);
	}

	/**
	 * String情報を取得.
	 *
	 * @return String 情報が返却されます.
	 */
	default String getString() {
		Object ret = get();
		if(ret == null) {
			return null;
		}
		return String.valueOf(ret);
	}

	/**
	 * Date情報を取得.
	 *
	 * @return Date 情報が返却されます.
	 */
	default java.util.Date getDate() {
		Object ret = get();
		if(ret == null) {
			return null;
		}
		return DateUtil.parseDate(ret);
	}

}
