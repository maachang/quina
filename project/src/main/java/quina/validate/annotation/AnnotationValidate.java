package quina.validate.annotation;

import quina.component.Component;
import quina.exception.QuinaException;
import quina.validate.VType;
import quina.validate.Validation;
import quina.validate.annotation.Validate.ValidateArray;

/**
 * ValidateのAnnotationを読み込んで、Validationオブジェクトを作成.
 */
public class AnnotationValidate {
	private AnnotationValidate() {}
	
	/**
	 * Annotationで定義されてるValidationを読み込む.
	 * @param c 対象のComponentを設定します.
	 * @return Validation Validation定義が返却されます.
	 *                    null の場合定義されていません.
	 */
	public static final Validation loadValidation(Component c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		return loadValidation(c.getClass());
	}
	
	/**
	 * Annotationで定義されてるValidationを読み込む.
	 * @param c 対象のComponentクラスを設定します.
	 * @return Validation Validation定義が返却されます.
	 *                    null の場合定義されていません.
	 */
	public static final Validation loadValidation(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		// 対象コンポーネントからValidateアノテーション定義を取得.
		ValidateArray array = c.getAnnotation(
			ValidateArray.class);
		// 存在しない場合.
		if(array == null) {
			// 単体で取得.
			Validate v = c.getAnnotation(Validate.class);
			if(v != null) {
				// Validationを生成して返却.
				Validation ret = new Validation();
				addValidate(ret, v);
				return ret;
			}
			return null;
		}
		// 複数のValidateアノテーション定義を取得.
		Validate[] list = array.value();
		// 存在しない場合.
		if(list == null || list.length == 0) {
			return null;
		}
		// Validationを生成して返却.
		Validation ret = new Validation();
		int len = list.length;
		for(int i = 0; i < len; i ++) {
			addValidate(ret, list[i]);
		}
		return ret;
	}
	
	// １つのValidateを追加.
	private static final void addValidate(
		Validation validation, Validate validate) {
		// name は必須.
		if(validate.name() == null || validate.name().isEmpty()) {
			throw new QuinaException("The required \"name\" is not set.");
		}
		// Validate処理条件の処理.
		String condisions = validate.conditions();
		// default が設定されてる場合.
		if(validate.noset() != null && !validate.noset().isEmpty()) {
			String defVal = validate.noset();
			if(validate.type() == VType.String) {
				defVal = "default '" + defVal + "'";
			} else {
				defVal = "default " + defVal;
			}
			if(condisions != null && !condisions.isEmpty()) {
				condisions += " " + defVal;
			} else {
				condisions = defVal;
			}
		}
		// message が設定されている場合.
		if(validate.message() != null && !validate.message().isEmpty()) {
			String message = validate.message();
			if(condisions != null && !condisions.isEmpty()) {
				condisions += " message '" + message + "'";
			} else {
				condisions = "message '" + message + "'";
			}
		}
		// 1つのValidate条件を追加.
		validation.add(validate.name(), validate.type(), condisions);
	}
}
