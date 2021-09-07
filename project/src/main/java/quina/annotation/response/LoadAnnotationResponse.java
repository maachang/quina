package quina.annotation.response;

import quina.component.Component;
import quina.exception.QuinaException;

/**
 * ResponseのAnnotationを取得して、Response初期設定
 * を取得します.
 */
public class LoadAnnotationResponse {
	private LoadAnnotationResponse() {}
	
	/**
	 * Annotationに定義されてるResponse初期設定を取得.
	 * @param c コンポーネントを設定します.
	 * @return ResponseInitialSetting Response初期設定が返却されます.
	 *                nullの場合設定されていません.
	 */
	public static final ResponseInitialSetting loadResponse(Component c) {
		if(c == null) {
			throw new QuinaException("The specified component is Null.");
		}
		return loadResponse(c.getClass());
	}
	
	/**
	 * Annotationに定義されてるResponse初期設定を取得.
	 * @param c コンポーネントを設定します.
	 * @return ResponseInitialSetting Response初期設定が返却されます.
	 *                nullの場合設定されていません.
	 */
	public static final ResponseInitialSetting loadResponse(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified component is Null.");
		}
		Status status = c.getAnnotation(Status.class);
		ContentType contentType = c.getAnnotation(ContentType.class);
		Header[] headers = null;
		HeaderArray headerArray = c.getAnnotation(HeaderArray.class);
		if(headerArray != null) {
			headers = headerArray.value();
			if(headers == null || headers.length == 0) {
				headers = null;
			}
		}
		ResponseSwitch responseSwitch = c.getAnnotation(ResponseSwitch.class);
		if(status == null && contentType == null && headers == null &&
			responseSwitch == null) {
			return null;
		}
		return new ResponseInitialSetting(
			status, contentType, headers, responseSwitch);
	}
}
