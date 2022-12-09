package quina.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Reflection関連のチェック処理.
 */
public class CheckReflection {
	private CheckReflection() {}
	
	/**
	 * ObjectとClassのinstanceof的チェック.
	 * @param target 比較対象のオブジェクトを設定します.
	 * @param src 比較元のクラスを設定します.
	 * @return boolean trueの場合targetはsrcを継承しています.
	 */
	public static final boolean isInstanceof(
		Object target, Class<?> src) {
		if(src == null || target == null) {
			return false;
		}
		if(target instanceof Class) {
			return src.isAssignableFrom((Class<?>)target);
		} else {
			return src.isInstance(target);
		}
	}
	
	/**
	 * ２つのメソッド定義が一致しているかチェック.
	 * @param a 比較元のMethodを設定します.
	 * @param b 比較先のMethodを設定します.
	 * @return boolean true の場合一致しています.
	 */
	public static final boolean equalsMethod(Method a, Method b) {
		// メソッド名とパラメータの一致チェック.
		if(!a.getName().equals(b.getName())) {
			return false;
		}
		final Class<?>[] aParams = a.getParameterTypes();
		final Class<?>[] bParams = b.getParameterTypes();
		final int aLen = aParams == null ? 0 : aParams.length;
		final int bLen = bParams == null ? 0 : bParams.length;
		if(aLen != bLen) {
			return false;
		}
		for(int i = 0; i < aLen; i ++) {
			if(!aParams[i].equals(bParams[i])) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 指定フィールドの型と完全一致するかチェック.
	 * @param f 対象のFieldを設定します.
	 * @param c 対象のオブジェクトを設定します.
	 * @return boolean trueの場合一致します.
	 */
	public static final boolean equalsFieldType(
		Field f, Object o) {
		if(o == null) {
			return false;
		}
		return equalsFieldType(f, o.getClass());
	}
	
	/**
	 * 指定フィールドの型と完全一致するかチェック.
	 * @param f 対象のFieldを設定します.
	 * @param c 対象のクラスを設定します.
	 * @return boolean trueの場合一致します.
	 */
	public static final boolean equalsFieldType(
		Field f, Class<?> c) {
		if(f == null) {
			return false;
		}
		Class<?> fc = f.getType();
		return fc ==c;
	}
	
	/**
	 * 指定フィールドと同じかチェック.
	 * @param a 比較元のFieldを設定します.
	 * @param b 比較先のFieldを設定します.
	 * @return boolean trueの場合一致します.
	 */
	public static final boolean equalsField(
		Field a, Field b) {
		if(a == null || b == null) {
			return false;
		}
		return a.getName().equals(b.getName()) &&
			a.getType().equals(b.getType());
	}

	/**
	 * 指定フィールドの型とinstanceOf的に比較てきるかチェック.
	 * @param target 比較対象のFieldを設定します.
	 * @param src 比較元のクラスを設定します.
	 * @return boolean trueの場合targetはsrcを継承しています.
	 */
	public static final boolean instanceofByField(
		Field target, Class<?> src) {
		if(target == null) {
			return false;
		}
		return isInstanceof(target.getType(), src);
	}
}
