package quina.util.collection;

import quina.util.BooleanUtil;
import quina.util.DateUtil;
import quina.util.NumberUtil;

/**
 * リスト要素型変換対応.
 */
public interface TypesValue<V> {

	/**
	 * 取得処理.
	 *
	 * @parma n 対象の条件を設定します.
	 * @return Object 対象情報が返却されます.
	 */
	public V get(int n);

	/**
	 * boolean情報を取得.
	 *
	 * @parma n 対象の条件を設定します.
	 * @return Boolean 情報が返却されます.
	 */
	default Boolean getBool(int n) {
		return getBoolean(n);
	}

	/**
	 * boolean情報を取得.
	 *
	 * @parma n 対象の条件を設定します.
	 * @return Boolean 情報が返却されます.
	 */
	default Boolean getBoolean(int n) {
		Object ret = get(n);
		if(ret == null) {
			return null;
		}
		return BooleanUtil.parseBoolean(ret);
	}

	/**
	 * Byte情報を取得.
	 *
	 * @parma n 対象の条件を設定します.
	 * @return Byte 情報が返却されます.
	 */
	default Byte getByte(int n) {
		Object ret = get(n);
		if(ret == null) {
			return null;
		}
		return NumberUtil.parseByte(ret);
	}

	/**
	 * short情報を取得.
	 *
	 * @parma n 対象の条件を設定します.
	 * @return Short 情報が返却されます.
	 */
	default Short getShort(int n) {
		Object ret = get(n);
		if(ret == null) {
			return null;
		}
		return NumberUtil.parseShort(ret);
	}

	/**
	 * int情報を取得.
	 *
	 * @parma n 対象の条件を設定します.
	 * @return Integer 情報が返却されます.
	 */
	default Integer getInt(int n) {
		return getInteger(n);
	}

	/**
	 * int情報を取得.
	 *
	 * @parma n 対象の条件を設定します.
	 * @return Integer 情報が返却されます.
	 */
	default Integer getInteger(int n) {
		Object ret = get(n);
		if(ret == null) {
			return null;
		}
		return NumberUtil.parseInt(ret);
	}

	/**
	 * long情報を取得.
	 *
	 * @parma n 対象の条件を設定します.
	 * @return Long 情報が返却されます.
	 */
	default Long getLong(int n) {
		Object ret = get(n);
		if(ret == null) {
			return null;
		}
		return NumberUtil.parseLong(ret);
	}

	/**
	 * float情報を取得.
	 *
	 * @parma n 対象の条件を設定します.
	 * @return Float 情報が返却されます.
	 */
	default Float getFloat(int n) {
		Object ret = get(n);
		if(ret == null) {
			return null;
		}
		return NumberUtil.parseFloat(ret);
	}

	/**
	 * double情報を取得.
	 *
	 * @parma n 対象の条件を設定します.
	 * @return Double 情報が返却されます.
	 */
	default Double getDouble(int n) {
		Object ret = get(n);
		if(ret == null) {
			return null;
		}
		return NumberUtil.parseDouble(ret);
	}

	/**
	 * String情報を取得.
	 *
	 * @parma n 対象の条件を設定します.
	 * @return String 情報が返却されます.
	 */
	default String getString(int n) {
		Object ret = get(n);
		if(ret == null) {
			return null;
		}
		return String.valueOf(ret);
	}

	/**
	 * Date情報を取得.
	 *
	 * @parma n 対象の条件を設定します.
	 * @return Date 情報が返却されます.
	 */
	default java.util.Date getDate(int n) {
		Object ret = get(n);
		if(ret == null) {
			return null;
		}
		return DateUtil.parseDate(ret);
	}
}
