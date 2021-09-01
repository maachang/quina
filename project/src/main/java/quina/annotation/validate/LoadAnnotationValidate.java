package quina.annotation.validate;

import quina.component.Component;
import quina.exception.QuinaException;
import quina.validate.VType;
import quina.validate.Validation;

/**
 * ValidateのAnnotationを読み込んで、Validationオブジェクトを作成.
 */
public class LoadAnnotationValidate {
	private LoadAnnotationValidate() {}
	
	/**
	 * Annotationで定義されてるValidationを読み込む.
	 * @param c 対象のComponentを設定します.
	 * @return Validation Validation定義が返却されます.
	 *                    null の場合定義されていません.
	 */
	public static final Validation load(Component c) {
		if(c == null) {
			throw new QuinaException("The specified component is Null.");
		}
		// 対象コンポーネントからValidateアノテーション定義を取得.
		ValidateArray array = c.getClass().getAnnotation(
			ValidateArray.class);
		// 存在しない場合.
		if(array == null) {
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
		String condisions = validate.conditions();
		// default が設定されてる場合.
		if(validate.defVal() != null && !validate.defVal().isEmpty()) {
			String defVal = validate.defVal();
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
		validation.add(validate.name(), validate.type(), condisions);
	}
}
