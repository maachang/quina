package quina.component;

import quina.http.Params;
import quina.http.Request;
import quina.validate.Validation;

/**
 * AbstractValidationコンポーネント.
 */
abstract class AbstractValidationComponent<T> implements ValidationComponent<T> {
	// validation.
	private Validation validation = null;

	/**
	 * Validationを生成.
	 * @param args パラメータの検証条件を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	@SuppressWarnings("unchecked")
	public T createValidation(Object... args) {
		validation = new Validation(args);
		return (T)this;
	}

	/**
	 * Validationを取得.
	 * @return Validation 提議されたValidationが返却されます.
	 */
	public Validation getValidation() {
		return validation;
	}

	/**
	 * Validation実行.
	 * @param req HTTPリクエストを設定します.
	 * @param params HTTPパラメータを設定します.
	 * @return Params 新しいパラメータが返却されます.
	 */
	protected Params execute(Request req) {
		return execute(req, req.getParams());
	}

	/**
	 * Validation実行.
	 * @param req HTTPリクエストを設定します.
	 * @param params HTTPパラメータを設定します.
	 * @return Params 新しいパラメータが返却されます.
	 */
	protected Params execute(Request req, Params params) {
		if(validation == null) {
			return params;
		}
		return validation.execute(req, params);
	}

}
