package quina.component;

import quina.validate.Validation;

/**
 * Validationサポートのコンポーネント.
 */
public interface ValidationComponent<T> {
	/**
	 * Validationを生成.
	 * @param args パラメータの検証条件を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	public T createValidation(Object... args);

	/**
	 * Validationを取得.
	 * @return Validation 提議されたValidationが返却されます.
	 */
	public Validation getValidation();
}
